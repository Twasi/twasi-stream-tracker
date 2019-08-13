package net.twasiplugin.dependency.streamtracker.database;

import com.mongodb.AggregationOptions;
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
                .project(Projection.projection("userMessages"))
                .unwind("userMessages")
                .group(Group.grouping("numberMessages", Accumulator.accumulator("$sum", "userMessages.messages")))
                .aggregate(Document.class, AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.CURSOR).build());

        if (!aggregate.hasNext()) {
            return 0;
        }

        Document first = aggregate.next();
        return first.getInteger("numberMessages");
    }

    /**
     * Returns the distinct amount of chatter
     * @param user the user to search chatter for
     * @return the number of unique chatter
     */
    public int getChatterAmount(User user) {
        Iterator<Document> aggregate = store.createAggregation(StreamTrackEntity.class)
                .lookup("tracked-streams", "stream", "_id", "stream")
                .match(store.createQuery(Document.class).disableValidation().field("stream.user").equal(user.getId()).field("userMessages").exists())
                .project(Projection.projection("userMessages"))
                .unwind("userMessages")
                .group("userMessages.twitchId")
                .group(Group.grouping("numberChatter", Accumulator.accumulator("$sum", 1)))
                .aggregate(Document.class, AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.CURSOR).build());

        if (!aggregate.hasNext()) {
            return 0;
        }

        Document first = aggregate.next();
        return first.getInteger("numberChatter");
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
    }
}
