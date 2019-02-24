package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.database.StreamEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;

import java.util.List;
import java.util.stream.Collectors;

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
        return repo.getStreamEntitiesByStream(entity)
                .stream()
                .map(StreamTrackDTO::new)
                .collect(Collectors.toList());
    }

}
