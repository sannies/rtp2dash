import org.mp4parser.rtp2dash.RtpH264StreamingTrack;
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
 * Created by sannies on 23.08.2015.
 */
public class TestH264 {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        LogManager.getLogManager().readConfiguration(TestH264.class.getResourceAsStream("/log.properties"));


        RtpH264StreamingTrack st = new RtpH264StreamingTrack("Z2QAFUs2QCAb5/ARAAADAAEAAAMAMI8WLZY=,aEquJyw=", 5000, 96);

        OutputStream os = new FileOutputStream("output.mp4");
        final FragmentedMp4Writer streamingMp4Writer = new FragmentedMp4Writer(Collections.<StreamingTrack>singletonList(st), Channels.newChannel(os));
        ExecutorService es = Executors.newCachedThreadPool();
        es.submit(st);

        System.in.read();
        es.shutdown();
        streamingMp4Writer.close();
        st.close();


    }
}

