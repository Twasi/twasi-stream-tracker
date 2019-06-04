package net.twasiplugin.dependency.streamtracker.database;

import net.twasi.core.database.lib.Repository;
import net.twasi.core.database.models.User;

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

    public List<ViewTimeEntity> getAllByUser(User user) {
        return store.createQuery(ViewTimeEntity.class).field("user").equal(user).asList();
    }

}
