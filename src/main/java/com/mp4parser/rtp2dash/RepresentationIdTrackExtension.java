package com.mp4parser.rtp2dash;

import com.mp4parser.streaming.TrackExtension;

/**
 * TrackExtension to store the representationId that is to be used in Manifest.
 */
public class RepresentationIdTrackExtension implements TrackExtension {
    private String representationId;

    public RepresentationIdTrackExtension(String representationId) {
        this.representationId = representationId;
    }

    public String getRepresentationId() {
        return representationId;
    }
}
