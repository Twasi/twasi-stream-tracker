package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.database.models.User;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasi.twitchapi.helix.games.response.GameDTO;
import net.twasi.twitchapi.options.TwitchRequestOptions;
import net.twasiplugin.dependency.streamtracker.database.StreamRepository;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.twasi.twitchapi.TwitchAPI.helix;

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
            List<StreamTrackEntity> entities = new ArrayList<>();
            repo.getAllByUser(user).forEach(stream -> entities.addAll(trackRepo.getStreamEntitiesByStream(stream)));

            Map<String, String> gameNames = new HashMap<>();
            List<GameDTO> games = helix().games().getGames(
                    entities.stream().map(StreamTrackEntity::getGameId).distinct().toArray(String[]::new), null,
                    new TwitchRequestOptions().withAuth(user.getTwitchAccount().toAuthContext())
            );

            games.forEach(game -> gameNames.put(game.getId(), game.getName()));
            return entities.stream().map(entity -> {
                if (!gameNames.containsKey(entity.getGameId())) return new StreamTrackDTO(entity);
                return new StreamTrackDTO(entity, gameNames.get(entity.getGameId()));
            }).collect(Collectors.toList());
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

}
