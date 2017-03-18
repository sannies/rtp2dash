
import org.mp4parser.rtp2dash.RtpAacStreamingTrack;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.logging.LogManager;

/**
 * Created by sannies on 03.09.2015.
 */
public class TestAac {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        LogManager.getLogManager().readConfiguration(TestAac.class.getResourceAsStream("/log.properties"));


        RtpAacStreamingTrack st = new RtpAacStreamingTrack( 5005, 97, 128, "profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3; config=1190", "MPEG4-GENERIC/48000/2");
        OutputStream os = new FileOutputStream("output.mp4");
        final FragmentedMp4Writer streamingMp4Writer =
                new FragmentedMp4Writer(Collections.<StreamingTrack>singletonList(st), Channels.newChannel(os));
        st.call();
        st.close();
        streamingMp4Writer.close();

    }
}
