package net.twasiplugin.dependency.streamtracker.database;

import net.twasi.core.database.models.BaseEntity;
import net.twasi.core.database.models.User;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "stream-tracker-view-time", noClassnameStored = true)
public class ViewTimeEntity extends BaseEntity {

    @Reference
    private User user;

    private String twitchId;
    private int minutes;
    private String displayName;

    public ViewTimeEntity(User user, String twitchId) {
        this.user = user;
        this.twitchId = twitchId;
        minutes = 0;
    }

    public ViewTimeEntity() {
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void increment() {
        this.minutes++;
    }

    public User getUser() {
        return user;
    }

    public String getTwitchId() {
        return twitchId;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
