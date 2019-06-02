package net.twasiplugin.dependency.streamtracker.database;

import net.twasi.core.database.lib.Repository;
import net.twasi.core.database.models.User;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.StreamTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StreamTrackRepository extends Repository<StreamTrackEntity> {

    public List<StreamTrackEntity> getStreamEntitiesByStream(StreamEntity stream) {
        try {
            List<StreamTrackEntity> streamEntities = store.createQuery(StreamTrackEntity.class).field("stream").equal(stream).asList();
            return streamEntities;
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
        AtomicInteger amount = new AtomicInteger();
        StreamRepository repo = DataService.get().get(StreamRepository.class);
        repo.getAllByUser(user).forEach(stream ->
                getStreamEntitiesByStream(stream).forEach(entity -> {
                    if (entity.getUserMessages() != null)
                        entity.getUserMessages().forEach(msgs ->
                                amount.addAndGet(msgs.messages));
                }));
        return amount.get();
    }

    public List<String> getChatterTwitchIds(User user) {
        List<String> twitchIds = new ArrayList<>();
        StreamRepository repo = DataService.get().get(StreamRepository.class);
        repo.getAllByUser(user).forEach(stream ->
                getStreamEntitiesByStream(stream).forEach(entity -> {
                    if (entity.getUserMessages() != null)
                        entity.getUserMessages().forEach(msgs ->
                                twitchIds.add(msgs.twitchId));
                }));
        return twitchIds.stream().distinct().collect(Collectors.toList());
    }

    public int getChatterAmount(User user) {
        return getChatterTwitchIds(user).size();
    }

}
