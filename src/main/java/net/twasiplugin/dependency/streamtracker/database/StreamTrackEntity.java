package net.twasiplugin.dependency.streamtracker.database;

import net.twasi.core.database.models.BaseEntity;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.Calendar;
import java.util.Date;

@Entity(value = "stream-track-entities", noClassnameStored = true)
public class StreamTrackEntity extends BaseEntity {

    @Reference
    private StreamEntity stream;

    private String gameId;
    private String title;
    private int viewerCount;
    private Date timestamp;

    public StreamTrackEntity(StreamEntity stream, String gameId, String title, int viewerCount) {
        this.stream = stream;
        this.gameId = gameId;
        this.title = title;
        this.viewerCount = viewerCount;
        this.timestamp = Calendar.getInstance().getTime();
    }

    public StreamTrackEntity() {
    }

    public StreamEntity getStream() {
        return stream;
    }

    public String getGameId() {
        return gameId;
    }

    public String getTitle() {
        return title;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
