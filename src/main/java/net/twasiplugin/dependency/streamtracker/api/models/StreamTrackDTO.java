package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.logger.TwasiLogger;
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

    public long getTimestamp() {
        return entity.getTimestamp().getTime();
    }

    public String getGame() {
        return game;
    }

    public int getChatMessages() {
        try {
            AtomicInteger total = new AtomicInteger();
            this.entity.getUserMessages().forEach(e -> total.addAndGet(e.messages));
            return total.get();
        } catch (Exception e) {
            TwasiLogger.log.debug(e);
            return 0;
        }
    }

    public int getChatCommands() {
        try {
            AtomicInteger total = new AtomicInteger();
            this.entity.getUserMessages().forEach(e -> total.addAndGet(e.commands));
            return total.get();
        } catch (Exception e) {
            TwasiLogger.log.debug(e);
            return 0;
        }
    }

    public StreamTrackEntity getEntity() {
        return entity;
    }
}
