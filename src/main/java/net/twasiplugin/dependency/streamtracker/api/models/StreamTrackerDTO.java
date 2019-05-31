package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.database.models.User;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.database.StreamRepository;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.twasiplugin.dependency.streamtracker.StreamTrackerPlugin.getGameIdsAndNames;
import static net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository.autoSumUp;

public class StreamTrackerDTO {

    private User user;
    private StreamRepository repo = ServiceRegistry.get(DataService.class).get(StreamRepository.class);
    private StreamTrackRepository trackRepo = ServiceRegistry.get(DataService.class).get(StreamTrackRepository.class);

    public StreamTrackerDTO(User user) {
        this.user = user;
    }

    public StreamDTO getLastStream() {
        return new StreamDTO(repo.getLatestStreamEntityOfUser(user));
    }

    public List<StreamDTO> getStreamsByRange(String start, String end) {
        return null;
    }

    public StreamDTO getLastStreamWithOffset(int offset) {
        return null;
    }

    public List<StreamTrackDTO> getAllStreamData() {
        try {
            final List<StreamTrackEntity> tmpEntities = new ArrayList<>();
            repo.getAllByUser(user).forEach(stream -> tmpEntities.addAll(trackRepo.getStreamEntitiesByStream(stream)));

            List<StreamTrackEntity> entities = autoSumUp(tmpEntities);

            Map<String, String> gameNames = getGameIdsAndNames(entities.stream().map(StreamTrackEntity::getGameId).collect(Collectors.toList()), user);

            return entities.stream().map(entity -> {
                if (!gameNames.containsKey(entity.getGameId())) return new StreamTrackDTO(entity);
                return new StreamTrackDTO(entity, gameNames.get(entity.getGameId()));
            }).collect(Collectors.toList());
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public StreamDTO getStreamById(String id) {
        return null;
    }

}
