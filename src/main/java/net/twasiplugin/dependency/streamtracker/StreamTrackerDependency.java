package net.twasiplugin.dependency.streamtracker;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import net.twasi.core.database.models.User;
import net.twasi.core.events.NewInstanceEvent;
import net.twasi.core.events.TwasiEventHandler;
import net.twasi.core.plugin.TwasiDependency;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.InstanceManagerService;
import net.twasi.twitchapi.helix.games.response.GameDTO;
import net.twasi.twitchapi.options.TwitchRequestOptions;
import net.twasiplugin.dependency.streamtracker.api.StreamResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.twasi.twitchapi.TwitchAPI.helix;

public class StreamTrackerDependency extends TwasiDependency {

    static HashMap<String, StreamTracker> registeredTrackers;
    static StreamTrackerService service = new StreamTrackerService();

    @Override
    public void onActivate() {
        ServiceRegistry.register(service);

        registeredTrackers = new HashMap<>();
        InstanceManagerService instanceManager = ServiceRegistry.get(InstanceManagerService.class);
        instanceManager.registerNewInstanceHandler(new TwasiEventHandler<NewInstanceEvent>() {
            @Override
            public void on(NewInstanceEvent newInstanceEvent) {
                User user = newInstanceEvent.getTwasiInterface().getStreamer().getUser();
                String tId = user.getTwitchAccount().getTwitchId();
                if (registeredTrackers.containsKey(tId)) registeredTrackers.get(tId).stopTracking();
                StreamTracker tracker = new StreamTracker(newInstanceEvent.getTwasiInterface());
                registeredTrackers.put(tId, tracker);
                tracker.start();
            }
        });
    }

    @Override
    public GraphQLQueryResolver getGraphQLResolver() {
        return new StreamResolver();
    }

    public static Map<String, String> getGameIdsAndNames(List<String> ofGameIds, User user) {
        ofGameIds = ofGameIds.stream().distinct().collect(Collectors.toList());
        List<GameDTO> games = helix().games().getGames(ofGameIds.toArray(new String[ofGameIds.size()]), null, new TwitchRequestOptions().withAuth(user.getTwitchAccount().toAuthContext()));
        return games.stream().collect(Collectors.toMap(GameDTO::getId, GameDTO::getName));
    }
}
