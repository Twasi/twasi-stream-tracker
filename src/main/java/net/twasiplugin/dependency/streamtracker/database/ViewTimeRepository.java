package net.twasiplugin.dependency.streamtracker.database;

import net.twasi.core.database.lib.Repository;
import net.twasi.core.database.models.User;
import org.mongodb.morphia.query.FindOptions;

import java.util.List;

public class ViewTimeRepository extends Repository<ViewTimeEntity> {

    public ViewTimeEntity getViewTimeEntityOrCreate(User user, String twitchId) {
        ViewTimeEntity entity = store.createQuery(ViewTimeEntity.class).field("user").equal(user).field("twitchId").equal(twitchId).get();
        if (entity == null) {
            entity = new ViewTimeEntity(user, twitchId);
            add(entity);
        }
        return entity;
    }

    public List<ViewTimeEntity> getByUser(User user, int limit) {
        return store.createQuery(ViewTimeEntity.class).field("user").equal(user).asList(new FindOptions().limit(limit));
    }

}
