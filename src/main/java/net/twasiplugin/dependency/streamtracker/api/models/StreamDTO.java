package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.database.StreamEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.twasiplugin.dependency.streamtracker.StreamTrackerPlugin.getGameIdsAndNames;
import static net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository.autoSumUp;

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
        List<StreamTrackEntity> streamEntitiesByStream = repo.getStreamEntitiesByStream(entity);
        streamEntitiesByStream = autoSumUp(streamEntitiesByStream);

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
    }

}
