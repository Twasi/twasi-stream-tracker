package net.twasiplugin.dependency.streamtracker.database;

import net.twasi.core.database.lib.Repository;

import java.util.ArrayList;
import java.util.List;

public class StreamTrackRepository extends Repository<StreamTrackEntity> {

    public List<StreamTrackEntity> getStreamEntitiesByStream(StreamEntity stream, boolean autoSumUp) {
        try {
            List<StreamTrackEntity> streamEntities = store.createQuery(StreamTrackEntity.class).field("stream").equal(stream).asList();
            if (!autoSumUp) return streamEntities;
            return streamEntities; // TODO auto sum up
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<StreamTrackEntity> getStreamEntitiesByStream(StreamEntity stream) {
        return getStreamEntitiesByStream(stream, true);
    }

    public List<StreamTrackEntity> sumUp(ArrayList<StreamTrackEntity> list, int max) {
        return list; // TODO build logic for auto sum up
    }

}
