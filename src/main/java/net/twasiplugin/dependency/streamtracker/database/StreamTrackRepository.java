package net.twasiplugin.dependency.streamtracker.database;

import com.mongodb.BasicDBObject;
import net.twasi.core.database.lib.Repository;
import net.twasi.core.database.models.User;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.StreamTracker;
import org.bson.Document;
import org.mongodb.morphia.aggregation.Accumulator;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.aggregation.Projection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StreamTrackRepository extends Repository<StreamTrackEntity> {

    public List<StreamTrackEntity> getStreamEntitiesByStream(StreamEntity stream) {
        try {
            return store.createQuery(StreamTrackEntity.class).field("stream").equal(stream.getId()).asList();
        } catch (Exception e) {
            TwasiLogger.log.debug(e);
            return new ArrayList<>();
        }
    }

    public List<StreamTrackEntity> getStreamEntitiesByStream(StreamEntity stream, boolean b) {
        return getStreamEntitiesByStream(stream);
    }

    public static List<StreamTrackEntity> autoSumUp(List<StreamTrackEntity> list) {
        try {
            list = new ArrayList<>(list);
            List<StreamTrackEntity> entities = new ArrayList<>();
            int counter = 0;
            while (list.size() > 400) {
                StreamTrackEntity tmp = null;
                for (StreamTrackEntity entity : list) {
                    if (counter++ % 2 == 0) {
                        tmp = entity;
                        continue;
                    }
                    int viewer = (tmp.getViewerCount() + entity.getViewerCount()) / 2;
                    List<StreamTracker.UserMessagesAndCommands> userMessages = new ArrayList<>(tmp.getUserMessages());
                    entity.getUserMessages().forEach(e -> {
                        StreamTracker.UserMessagesAndCommands msgs = userMessages.stream().filter(e1 -> e1.twitchId.equals(e.twitchId)).findFirst().orElse(null);
                        if (msgs != null) {
                            msgs.messages += e.messages;
                            msgs.commands += e.commands;
                        } else {
                            userMessages.add(e);
                        }
                    });
                    entities.add(new StreamTrackEntity(tmp.getStream(), tmp.getGameId(), tmp.getTitle(), viewer, tmp.getTimestamp(), userMessages));
                }
                list = entities;
                entities = new ArrayList<>();
                counter = 0;
            }
            return list;
        } catch (Throwable t) {
            TwasiLogger.log.debug(t);
        }
        return new ArrayList<>();
    }

    public int getTotalMessages(User user) {
        Iterator<Document> aggregate = store.createAggregation(StreamTrackEntity.class)
                .lookup("tracked-streams", "stream", "_id", "stream")
                .match(store.createQuery(Document.class).disableValidation().field("stream.user").equal(user.getId()).field("userMessages").exists())
                .project(Projection.expression("numberMessages", new BasicDBObject("$size", "$userMessages")))
                .group(Group.grouping("numberMessages", Accumulator.accumulator("$sum", "numberMessages")))
                .aggregate(Document.class);

        if (!aggregate.hasNext()) {
            return 0;
        }

        Document first = aggregate.next();
        return first.getInteger("numberMessages");
    }

    /* public List<String> getChatterTwitchIds(User user) {
        List<String> twitchIds = new ArrayList<>();
        StreamRepository repo = DataService.get().get(StreamRepository.class);
        repo.getAllByUser(user).forEach(stream ->
                getStreamEntitiesByStream(stream).forEach(entity -> {
                    if (entity.getUserMessages() != null)
                        entity.getUserMessages().forEach(msgs ->
                                twitchIds.add(msgs.twitchId));
                }));
        return twitchIds.stream().distinct().collect(Collectors.toList());
    } */

    /**
     *
     * db.getCollection('stream-track-entities').aggregate(
     *     [
     *         {
     *             $lookup: {
     *                 from: "tracked-streams",
     *                 localField: "stream",
     *                 foreignField: "_id",
     *                 as: "stream"
     *             }
     *         },
     *         {
     *             $match: {
     *                 "stream.user": ObjectId("5ba3eb2573f13a64200f55d8"),
     *                 "userMessages": { $exists: true }
     *             }
     *         },
     *         {
     *             $project: {
     *                 userMessages: 1
     *             }
     *         },
     *         {
     *             $unwind: {
     *                 path: "$userMessages"
     *             }
     *         },
     *         {
     *             $group: {
     *                 _id: null,
     *                 count: { $sum: "$userMessages.messages" }
     *             }
     *         }
     *     ]
     * )
     *
     * @param user
     * @return
     */
    public int getChatterAmount(User user) {
        Iterator<Document> aggregate = store.createAggregation(StreamTrackEntity.class)
                .lookup("tracked-streams", "stream", "_id", "stream")
                .match(store.createQuery(Document.class).disableValidation().field("stream.user").equal(user.getId()).field("userMessages").exists())
                .aggregate(Document.class);
        return 0;
    }

    public int getTotalMessagesByUserAndTwitchId(User user, String twitchId) {
        AtomicInteger amount = new AtomicInteger();
        StreamRepository repo = DataService.get().get(StreamRepository.class);
        repo.getAllByUser(user).forEach(stream ->
                getStreamEntitiesByStream(stream).forEach(entity -> {
                    if (entity.getUserMessages() != null)
                        entity.getUserMessages().stream().filter(e -> e.twitchId.equals(twitchId)).forEach(e -> amount.addAndGet(e.messages));
                }));
        return amount.get();
    }/*
        try {
            Iterator<Document> aggregate = store.createAggregation(StreamTrackEntity.class)
                    .lookup("StreamEntity", "stream._id", "_id", " ")
                    .match(store.createQuery(Document.class).disableValidation().field("streamJoined.user._id").equal(user.getId()))
                    .unwind("userMessages");
                    /* .match(store.createQuery(Document.class).disableValidation().field("twitchId").equal(twitchId))
                    .group("twitchId", grouping("count", new Accumulator("$sum", 1)))
                    .project(Projection.projection("count"))
                    .aggregate(Document.class);

            Document first = aggregate.next();

            return first.getInteger("count");*/

}
