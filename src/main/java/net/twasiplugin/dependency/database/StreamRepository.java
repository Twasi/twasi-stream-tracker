package net.twasiplugin.dependency.database;

import net.twasi.core.database.lib.Repository;
import net.twasi.core.database.models.User;

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

    public StreamEntity getLatestStreamEntityOfUser(User user){
        try {
            return store.createQuery(StreamEntity.class).field("user").equal(user).order("-startedAt").get();
        } catch (Exception e) {
            return null;
        }
    }

}
