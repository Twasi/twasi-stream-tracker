package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasiplugin.dependency.streamtracker.database.StreamTrackEntity;

import java.util.concurrent.atomic.AtomicInteger;

public class StreamTrackDTO {

    private StreamTrackEntity entity;
    private String game = "";

    public StreamTrackDTO(StreamTrackEntity entity) {
        this.entity = entity;
    }

    public StreamTrackDTO(StreamTrackEntity entity, String game) {
        this.entity = entity;
        this.game = game;
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

    public String getGame() {
        return game;
    }

    public int getChatMessages() {
        AtomicInteger total = new AtomicInteger();
        this.entity.getUserMessages().forEach(e -> total.addAndGet(e.messages));
        return total.get();
    }

    public int getChatCommands() {
        AtomicInteger total = new AtomicInteger();
        this.entity.getUserMessages().forEach(e -> total.addAndGet(e.commands));
        return total.get();
    }

    public StreamTrackEntity getEntity() {
        return entity;
    }
}
