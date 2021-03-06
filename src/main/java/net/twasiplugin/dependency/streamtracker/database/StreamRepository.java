package net.twasiplugin.dependency.streamtracker.database;

import net.twasi.core.database.lib.Repository;
import net.twasi.core.database.models.User;

import java.util.ArrayList;
import java.util.List;

public class StreamRepository extends Repository<StreamEntity> {

    /**
     * @param streamId String ID of the stream generated by Twitch
     * @return StreamEntity|null
     */
    public StreamEntity getStreamEntityByStreamId(String streamId) {
        try {
            return store.createQuery(StreamEntity.class).field("streamId").equal(streamId).get();
        } catch (Exception e) {
            return null;
        }
    }

    public StreamEntity getLatestStreamEntityOfUser(User user) {
        try {
            return store.createQuery(StreamEntity.class).field("user").equal(user.getId()).order("-startedAt").get();
        } catch (Exception e) {
            return null;
        }
    }

    public List<StreamEntity> getAllStreamsByUser(User user) {
        List<StreamEntity> streams = store.createQuery(StreamEntity.class)
                .field("user").equal(user.getId()).order("-startedAt").asList();
        if (streams.size() == 0) {
            return new ArrayList<>();
        }
        return streams;
    }

    public List<StreamEntity> getAllByUser(User user) {
        return store.createQuery(StreamEntity.class).field("user").equal(user.getId()).asList();
    }

    public long getStreamAmountByUser(User user) {
        return store.createQuery(StreamEntity.class).field("user").equal(user.getId()).count();
    }
}
