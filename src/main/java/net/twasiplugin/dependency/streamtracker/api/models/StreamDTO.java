package net.twasiplugin.dependency.streamtracker.api.models;

import com.google.common.collect.Lists;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.StreamTracker;
import net.twasiplugin.dependency.streamtracker.database.StreamEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;

import java.util.*;
import java.util.stream.Collectors;

import static net.twasiplugin.dependency.streamtracker.StreamTrackerPlugin.getGameIdsAndNames;

public class StreamDTO {

    private StreamEntity entity;
    StreamTrackRepository repo = ServiceRegistry.get(DataService.class).get(StreamTrackRepository.class);

    public StreamDTO(StreamEntity stream) {
        this.entity = stream;
    }

    public String getStreamId() {
        return entity.getStreamId();
    }

    public String getLanguage() {
        return entity.getLanguage();
    }

    public String getStartedAt() {
        return entity.getStartedAt().toString();
    }

    public String getStreamType() {
        return entity.getStreamType();
    }

    public List<String> getCommunityIds() {
        return entity.getCommunityIds();
    }

    public List<String> getTagIds() {
        return entity.getTagIds();
    }

    public List<StreamTrackDTO> getData() {
        try {
            List<StreamTrackEntity> streamEntitiesByStream = repo.getStreamEntitiesByStream(entity);
            TwasiLogger.log.debug(streamEntitiesByStream.size());
            //streamEntitiesByStream = autoSumUp(streamEntitiesByStream);

            List<StreamTrackDTO> collect = streamEntitiesByStream.stream()
                    .map(StreamTrackDTO::new)
                    .collect(Collectors.toList());

            Map<String, String> gameIdsAndNames = getGameIdsAndNames(collect.stream().map(StreamTrackDTO::getGameId).distinct().collect(Collectors.toList()), entity.getUser());

            List<StreamTrackDTO> finalList = new ArrayList<>();

            collect.forEach(dto -> {
                if (gameIdsAndNames.containsKey(dto.getGameId()))
                    finalList.add(new StreamTrackDTO(dto.getEntity(), gameIdsAndNames.get(dto.getGameId())));
                else
                    finalList.add(new StreamTrackDTO(dto.getEntity()));

            });
            return finalList;
        } catch (Exception e) {
            TwasiLogger.log.debug(e);
            return new ArrayList<>();
        }
    }

    public List<StreamTrackChattersDTO> getTopChatters() {
        List<StreamTrackEntity> streamEntitiesByStream = repo.getStreamEntitiesByStream(entity);
        Map<String, StreamTracker.UserMessagesAndCommands> msgs = new HashMap<>();
        streamEntitiesByStream.forEach(entity -> {
            if (entity.getUserMessages() != null)
                entity.getUserMessages().forEach(uMsgs -> {
                    if (msgs.containsKey(uMsgs.twitchId)) {
                        msgs.get(uMsgs.twitchId).messages += uMsgs.messages;
                    } else {
                        msgs.put(uMsgs.twitchId, uMsgs);
                    }
                });
        });
        LinkedHashMap<String, StreamTracker.UserMessagesAndCommands> sorted = msgs.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().messages)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return Lists.reverse(sorted.values().stream().map(StreamTrackChattersDTO::new).collect(Collectors.toList()));
    }

    public int getNewFollowers(){
        return this.entity.getNewFollowers();
    }

    public int getNewViews(){
        return this.entity.getNewViews();
    }

}
