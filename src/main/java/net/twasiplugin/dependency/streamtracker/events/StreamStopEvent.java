package net.twasiplugin.dependency.streamtracker.events;

import net.twasi.core.database.models.User;
import net.twasi.core.events.TwasiEvent;

public class StreamStopEvent extends TwasiEvent {

    private User user;

    public User getUser() {
        return user;
    }

    public StreamStopEvent(User user) {
        this.user = user;
    }
}
