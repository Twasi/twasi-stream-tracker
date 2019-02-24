package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasiplugin.dependency.streamtracker.database.StreamTrackEntity;

public class StreamTrackDTO {

    private StreamTrackEntity entity;

    public StreamTrackDTO(StreamTrackEntity entity) {
        this.entity = entity;
    }

    public String getGameId() {
        return entity.getGameId();
    }

    public String getTitle() {
        return entity.getTitle();
    }

    public int getViewerCount() {
        return entity.getViewerCount();
    }

    public String getTimestamp() {
        return entity.getTimestamp().toString();
    }
}
