package org.mp4parser.rtp2dash;


import org.mp4parser.streaming.StreamingTrack;

import java.util.concurrent.Callable;

/**
 * Created by sannies on 10.09.2015.
 */
public interface ReceivingStreamingTrack extends StreamingTrack, Callable<Void> {
    boolean isReceiving();
}
