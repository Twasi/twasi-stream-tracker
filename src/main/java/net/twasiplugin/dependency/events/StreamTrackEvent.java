package net.twasiplugin.dependency.events;

import net.twasi.core.database.models.User;
import net.twasi.core.events.TwasiEvent;
import net.twasiplugin.dependency.database.StreamTrackEntity;

public class StreamTrackEvent extends TwasiEvent {

    private User user;
    private StreamTrackEntity currentTrackEntity;

    public User getUser() {
        return user;
    }

    public StreamTrackEntity getCurrentTrackEntity() {
        return currentTrackEntity;
    }

    public StreamTrackEvent(User user, StreamTrackEntity currentTrackEntity) {
        this.user = user;
        this.currentTrackEntity = currentTrackEntity;
    }
}
