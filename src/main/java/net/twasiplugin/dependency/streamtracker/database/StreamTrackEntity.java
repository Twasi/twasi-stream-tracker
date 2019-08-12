package net.twasiplugin.dependency.streamtracker.database;

import net.twasi.core.database.models.BaseEntity;
import net.twasiplugin.dependency.streamtracker.StreamTracker;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity(value = "stream-track-entities", noClassnameStored = true)
public class StreamTrackEntity extends BaseEntity {

    private ObjectId stream;

    private String gameId;
    private String title;
    private int viewerCount;
    private List<StreamTracker.UserMessagesAndCommands> userMessages;
    private Date timestamp;

    public StreamTrackEntity() {
    }

    public StreamTrackEntity(ObjectId stream, String gameId, String title, int viewerCount, List<StreamTracker.UserMessagesAndCommands> userMessages) {
        this.stream = stream;
        this.gameId = gameId;
        this.title = title;
        this.viewerCount = viewerCount;
        this.userMessages = userMessages;
        this.timestamp = Calendar.getInstance().getTime();
    }

    public StreamTrackEntity(ObjectId stream, String gameId, String title, int viewerCount, Date timestamp, List<StreamTracker.UserMessagesAndCommands> userMessages) {
        this.stream = stream;
        this.gameId = gameId;
        this.title = title;
        this.viewerCount = viewerCount;
        this.timestamp = timestamp;
        this.userMessages = userMessages;
    }

    public ObjectId getStream() {
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

    public List<StreamTracker.UserMessagesAndCommands> getUserMessages() {
        return userMessages;
    }
}
