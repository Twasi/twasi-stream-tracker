package net.twasiplugin.dependency.database;

import net.twasi.core.database.lib.Repository;

import java.util.ArrayList;
import java.util.List;

public class StreamTrackRepository extends Repository<StreamTrackEntity> {

    public List<StreamTrackEntity> getStreamEntitiesByStream(StreamEntity stream) {
        try {
            return store.createQuery(StreamTrackEntity.class).field("stream").equal(stream).asList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

}
