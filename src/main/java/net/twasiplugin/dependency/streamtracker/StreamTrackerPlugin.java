package net.twasiplugin.dependency.streamtracker;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import net.twasi.core.database.models.User;
import net.twasi.core.plugin.TwasiPlugin;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.twitchapi.helix.games.response.GameDTO;
import net.twasi.twitchapi.options.TwitchRequestOptions;
import net.twasiplugin.dependency.streamtracker.api.StreamResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.twasi.twitchapi.TwitchAPI.helix;

public class StreamTrackerPlugin extends TwasiPlugin<StreamTrackerConfiguration> {

    static HashMap<String, StreamTracker> registeredTrackers;
    static StreamTrackerService service = new StreamTrackerService();

    public static StreamTrackerConfiguration CONFIG;

    @Override
    public void onActivate() {
        CONFIG = getConfiguration();
        ServiceRegistry.register(service);
        registeredTrackers = new HashMap<>();
    }

    @Override
    public Class<? extends TwasiUserPlugin> getUserPluginClass() {
        return StreamTrackerUserPlugin.class;
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
