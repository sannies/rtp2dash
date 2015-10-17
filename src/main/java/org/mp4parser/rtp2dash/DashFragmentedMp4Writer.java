package org.mp4parser.rtp2dash;

import mpeg.dash.schema.mpd._2011.RepresentationType;
import mpeg.dash.schema.mpd._2011.SegmentTemplateType;
import mpeg.dash.schema.mpd._2011.SegmentTimelineType;
import org.mp4parser.BasicContainer;
import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.TrackFragmentBaseMediaDecodeTimeBox;
import org.mp4parser.boxes.iso14496.part12.TrackRunBox;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.extensions.TrackIdTrackExtension;
import org.mp4parser.tools.Path;

import java.io.*;
import java.math.BigInteger;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;

/**

 */
public class DashFragmentedMp4Writer extends MultiTrackFragmentedMp4Writer {
    private static final Logger LOG = Logger.getLogger(DashFragmentedMp4Writer.class.getName());
    private File representationBaseDir;
    private long adaptationSetId;
    private String representationId;
    private StreamingTrack source;


    public DashFragmentedMp4Writer(StreamingTrack source, File baseDir) throws IOException {
        super(Collections.<StreamingTrack>singletonList(source), null);
        RepresentationIdTrackExtension representationIdTrackExtension = source.getTrackExtension(RepresentationIdTrackExtension.class);
        assert representationIdTrackExtension != null;
        TrackIdTrackExtension trackIdTrackExtension = source.getTrackExtension(TrackIdTrackExtension.class);
        assert trackIdTrackExtension != null;

        this.source = source;
        this.adaptationSetId = trackIdTrackExtension.getTrackId();
        this.representationId = representationIdTrackExtension.getRepresentationId();
        representationBaseDir = new File(baseDir, representationId);
        representationBaseDir.mkdir();
    }

    public StreamingTrack getSource() {
        return source;
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        isClosed = true;
    }

    boolean isClosed = false;

    public boolean isClosed() {
        return isClosed;
    }

    protected void writeHeader(Box... boxes) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(representationBaseDir, "init.mp4"));
        WritableByteChannel wbc = fos.getChannel();
        write(wbc, boxes);
        fos.close();
        wbc.close();
    }


    @Override
    protected void writeFragment(Box... boxes) throws IOException {
        TrackFragmentBaseMediaDecodeTimeBox tfdt = null;

        for (Box box : boxes) {
            if ("moof".equals(box.getType())) {
                tfdt = Path.getPath(box, "traf[0]/tfdt[0]");
                break;
            }
        }


        assert tfdt != null;
        File f = new File(representationBaseDir, "media-" + tfdt.getBaseMediaDecodeTime() + ".mp4");
        FileOutputStream fos = new FileOutputStream(f);
        WritableByteChannel wbc = fos.getChannel();
        write(wbc, boxes);
        fos.close();
        wbc.close();

    }

    long getTime(File f) {
        String n = f.getName().substring(6);
        n = n.substring(0, n.indexOf("."));
        return Long.parseLong(n);
    }


    RepresentationType getRepresentation() throws IOException {
        if (source == null) {
            return null;
        }
        RepresentationType representationType = new RepresentationType();
        representationType.setId(representationId);
        representationType.setCodecs(DashHelper.getRfc6381Codec(source.getSampleDescriptionBox().getSampleEntry()));
        representationType.setStartWithSAP(1L);
        if ("vide".equals(this.source.getHandler())) {
            representationType.setMimeType("video/mp4");
            representationType.setWidth((long) ((VisualSampleEntry) source.getSampleDescriptionBox().getSampleEntry()).getWidth());
            representationType.setHeight((long) ((VisualSampleEntry) source.getSampleDescriptionBox().getSampleEntry()).getHeight());
        } else if ("soun".equals(this.source.getHandler())) {
            representationType.setMimeType("audio/mp4");
        } else {
            LOG.severe("I can't build Representation for track with handler " + this.source.getHandler());
            return null;
        }
        SegmentTemplateType segmentTemplateType = new SegmentTemplateType();
        segmentTemplateType.setTimescale(source.getTimescale());
        representationType.setSegmentTemplate(segmentTemplateType);
        SegmentTimelineType segmentTimelineType = new SegmentTimelineType();
        segmentTemplateType.setSegmentTimeline(segmentTimelineType);
        segmentTemplateType.setInitializationAttribute(representationId + "/init.mp4");
        segmentTemplateType.setMedia(representationId + "/media-$Time$.mp4");

        File[] files = representationBaseDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.contains("media-");
            }
        });

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return (int) (getTime(o1) - getTime(o2));
            }
        });
        long fileSize = 0;
        long representationDuration = 0;
        for (File file : files) {
            fileSize += file.length();
            long d = 0;
            FileInputStream fis = new FileInputStream(file);
            IsoFile isoFile = new IsoFile(fis.getChannel());
            TrackRunBox trun = Path.getPath(isoFile, "moof[0]/traf[0]/trun[0]");
            for (TrackRunBox.Entry entry : trun.getEntries()) {
                d += entry.getSampleDuration();
            }
            fis.close();

            SegmentTimelineType.S sOld = segmentTimelineType.getS().size() > 0 ? segmentTimelineType.getS().get(segmentTimelineType.getS().size() - 1) : null;
            if (sOld != null && sOld.getD().equals(BigInteger.valueOf(d))) {
                BigInteger r = sOld.getR();
                if (r == null || r.equals(BigInteger.ZERO)) {
                    sOld.setR(BigInteger.ONE);
                } else {
                    sOld.setR(r.add(BigInteger.ONE));
                }
            } else {
                SegmentTimelineType.S s = new SegmentTimelineType.S();
                s.setD(BigInteger.valueOf(d));
                s.setT(BigInteger.valueOf(getTime(file)));
                segmentTimelineType.getS().add(s);
            }
            representationDuration += d;
        }
        representationType.setBandwidth(representationDuration != 0 ? ((fileSize * 8 * this.source.getTimescale()) / representationDuration) : 0);


        return representationType;
    }

    public long getAdaptationSetId() {
        return adaptationSetId;
    }
}
