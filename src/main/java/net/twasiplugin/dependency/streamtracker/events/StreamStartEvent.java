package net.twasiplugin.dependency.streamtracker.events;

import net.twasi.core.database.models.User;
import net.twasi.core.events.TwasiEvent;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackEntity;

public class StreamStartEvent extends TwasiEvent {

    private User user;
    private StreamTrackEntity firstTrackEntity;

    public User getUser() {
        return user;
    }

    public StreamTrackEntity getFirstTrackEntity() {
        return firstTrackEntity;
    }

    public StreamStartEvent(User user, StreamTrackEntity firstTrackEntity) {
        this.user = user;
        this.firstTrackEntity = firstTrackEntity;
    }
}
