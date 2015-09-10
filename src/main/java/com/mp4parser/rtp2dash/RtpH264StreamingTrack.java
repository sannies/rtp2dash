package com.mp4parser.rtp2dash;

import com.mp4parser.muxer.tracks.h264.parsing.read.BitstreamReader;
import com.mp4parser.streaming.rawformats.h264.H264NalConsumingTrack;
import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.Mp4Arrays;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class RtpH264StreamingTrack extends H264NalConsumingTrack implements ReceivingStreamingTrack {
    private static final Logger LOG = Logger.getLogger(RtpH264StreamingTrack.class.getName());
    boolean isReceiving = false;
    private int initialTimeout = 10000;
    private int timeout = 5000;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    private int port;
    private int payloadType;


    long rtpTimestamp;



    @Override
    public boolean sourceDepleted() {
        return !isReceiving;
    }

    public void close() throws IOException {
        isReceiving = false;
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public RtpH264StreamingTrack(String sprop, int port, int payloadType) throws IOException {
        super();
        this.port = port;
        this.payloadType = payloadType;

        String[] spspps = sprop.split(",");
        byte[] sps = Base64.getDecoder().decode(spspps[0]);
        consumeNal(sps);
        byte[] pps = Base64.getDecoder().decode(spspps[1]);
        consumeNal(pps);
    }

    public Void call() throws IOException {
        isReceiving = true;
        try {
            DatagramSocket socket = new DatagramSocket(port);
            socket.setSoTimeout(initialTimeout);
            byte[] fuNalBuf = new byte[0];
            byte[] buf = new byte[16384];
            LOG.info("Start Receiving H264 RTP Packets on port " + port);
            while (isReceiving && !Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    LOG.info("Socket Timeout closed RtpH264StreamingTrack");
                    isReceiving = false;
                    continue;
                }
                socket.setSoTimeout(timeout);
                BitstreamReader bsr = new BitstreamReader(new ByteArrayInputStream(packet.getData()));
                int v = (int) bsr.readNBit(2);
                boolean p = bsr.readBool();
                boolean x = bsr.readBool();
                int cc = (int) bsr.readNBit(4);
                boolean m = bsr.readBool();
                int pt = (int) bsr.readNBit(7);
                if (pt != payloadType) {
                    LOG.warning("Received package of payload type " + pt + " eventhough it should be of type " + payloadType + ". Ignoring.");
                    // typically it's the RTSP sender report
                    continue; //  this is not a packet for me.
                }
                int sequenceNumber = (int) bsr.readNBit(16);
                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 4, packet.getData().length - 4);
                bb.limit(packet.getLength());
                rtpTimestamp = IsoTypeReader.readUInt32(bb);
                long ssrc = IsoTypeReader.readUInt32(bb);
                long[] csrc = new long[cc];
                for (int i = 0; i < csrc.length; i++) {
                    csrc[i] = IsoTypeReader.readUInt32(bb);
                }
                ByteBuffer payload = bb.slice();
                int nalUnitType = payload.get(0) & 0x1f;
                if (nalUnitType >= 1 && nalUnitType <= 23) {
                    if (nalUnitType != 1 && nalUnitType != 5 && nalUnitType != 6) {
                        System.err.println(nalUnitType);
                    }
                    byte[] nalSlice = new byte[payload.remaining()];
                    payload.get(nalSlice);
                    consumeNal(nalSlice);
                } else if (nalUnitType == 24) {
                    payload.position(1);
                    while (payload.remaining()>1) {
                        int length = IsoTypeReader.readUInt16(payload);
                        ByteBuffer nalBuf = payload.slice();
                        nalBuf.limit(length);
                        payload.position(payload.position() + length);
                    }
                    throw new RuntimeException("No Support for STAP A " + toString());

                } else if (nalUnitType == 25) {
                    throw new RuntimeException("No Support for STAP B " + toString());
                } else if (nalUnitType == 26) {
                    throw new RuntimeException("No Support for MTAP 16 " + toString());
                } else if (nalUnitType == 27) {
                    throw new RuntimeException("No Support for MTAP 24 " + toString());
                } else if (nalUnitType == 28) {
                    int fuIndicator = payload.get(0);
                    int fuHeader = IsoTypeReader.byte2int(payload.get(1));
                    boolean s = (fuHeader & 128) > 0;
                    boolean e = (fuHeader & 64) > 0;


                    //System.out.print("FU-A start: " + s + " end: " + e);
                    payload.position(2);
                    if (s) {
                        fuNalBuf = new byte[]{(byte) ((fuIndicator & 96) + (fuHeader & 31))};
                    }
                    if (fuNalBuf != null) {
                        byte[] nalSlice = new byte[payload.remaining()];
                        payload.get(nalSlice);
                        fuNalBuf = Mp4Arrays.copyOfAndAppend(fuNalBuf, nalSlice);
                    }
                    if (e && fuNalBuf != null) {
                        consumeNal(fuNalBuf);
                        fuNalBuf = null;
                    }


                } else if (nalUnitType == 29) {
                    System.out.print("FU B ");
                    throw new RuntimeException("No Support for FU-B");
                } else {
                    throw new RuntimeException("No Support for nalUnitType " + nalUnitType);
                }

                String log = "Receiver{" +
                        "v=" + v +
                        ", len=" + packet.getLength() +
                        ", p=" + p +
                        ", x=" + x +
                        ", cc=" + cc +
                        ", m=" + m +
                        ", pt=" + pt +
                        ", sequenceNumber=" + sequenceNumber +
                        ", ts=" + rtpTimestamp +
                        ", ssrc=" + ssrc;
                for (int i = 0; i < csrc.length; i++) {
                    log += ", csrc[" + i + "]=" + csrc[i];

                }
                log += '}';

                //System.out.println(log);


            }
            LOG.info("Done receiving RTP Packets");
            drainDecPictureBuffer(true);
            LOG.info("Picture Buffer drained");
            return null;
        } finally {
            countDownLatch.countDown();
            if (isReceiving && !Thread.currentThread().isInterrupted()) {
                LOG.warning("Stopping RTP Receiver due to exception. " + toString());
            }
            isReceiving = false;
        }
    }


    @Override
    public String toString() {
        return "RtpH264StreamingTrack{" +
                "port=" + port +
                '}';
    }


    public boolean isReceiving() {
        return isReceiving;
    }
}
