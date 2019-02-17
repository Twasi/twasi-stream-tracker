package net.twasiplugin.dependency.database;

import net.twasi.core.database.models.BaseEntity;
import net.twasi.core.database.models.User;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;
import java.util.List;

@Entity(value = "tracked-streams", noClassnameStored = true)
public class StreamEntity extends BaseEntity {

    @Reference
    private User user;

    private String streamId;
    private String language;
    private Date startedAt;
    private String streamType;
    private List<String> communityIds;
    private List<String> tagIds;


    public StreamEntity(User user, String streamId, String language, Date startedAt, String streamType, List<String> communityIds, List<String> tagIds) {
        this.user = user;
        this.streamId = streamId;
        this.language = language;
        this.startedAt = startedAt;
        this.streamType = streamType;
        this.communityIds = communityIds;
        this.tagIds = tagIds;
    }

    public StreamEntity() {
    }

    public User getUser() {
        return user;
    }

    public String getStreamId() {
        return streamId;
    }

    public String getLanguage() {
        return language;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public String getStreamType() {
        return streamType;
    }

    public List<String> getCommunityIds() {
        return communityIds;
    }

    public List<String> getTagIds() {
        return tagIds;
    }
}
