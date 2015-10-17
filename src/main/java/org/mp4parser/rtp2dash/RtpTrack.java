package org.mp4parser.rtp2dash;


import org.mp4parser.streaming.StreamingTrack;

public interface RtpTrack extends StreamingTrack {
    boolean isClosed();
}
