package net.twasiplugin.dependency.streamtracker;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.google.gson.Gson;
import net.twasi.core.database.models.User;
import net.twasi.core.database.repositories.UserRepository;
import net.twasi.core.events.NewInstanceEvent;
import net.twasi.core.events.TwasiEventHandler;
import net.twasi.core.plugin.TwasiDependency;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasi.core.services.providers.InstanceManagerService;
import net.twasi.twitchapi.helix.games.response.GameDTO;
import net.twasi.twitchapi.options.TwitchRequestOptions;
import net.twasiplugin.chatstatistics.database.ChatStatisticsEntity;
import net.twasiplugin.chatstatistics.database.ChatStatisticsRepository;
import net.twasiplugin.dependency.streamtracker.api.StreamResolver;
import net.twasiplugin.dependency.streamtracker.database.StreamEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamRepository;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

        Thread t1 = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            UserRepository repo = ServiceRegistry.get(DataService.class).get(UserRepository.class);
            StreamTrackRepository repo2 = ServiceRegistry.get(DataService.class).get(StreamTrackRepository.class);
            ChatStatisticsRepository repo3 = ServiceRegistry.get(DataService.class).get(ChatStatisticsRepository.class);
            User sd = repo.getByTwitchId("119965990");
            List<StreamEntity> entities = ServiceRegistry.get(DataService.class).get(StreamRepository.class).getAllByUser(sd);
            List<ChatStatisticsEntity> streamTrackEntities = new ArrayList<>();
            entities.stream().map(repo3::getByStream).collect(Collectors.toList()).forEach(streamTrackEntities::addAll);
            Map<String, Integer> emotes = new HashMap<>(), chatter = new HashMap<>();
            streamTrackEntities.forEach(entitiy -> {
                entitiy.getMessagesByUser().forEach((name, amount) -> {
                    if (chatter.containsKey(name)) chatter.put(name, chatter.get(name) + amount);
                    else chatter.put(name, amount);
                });
                entitiy.getUsedEmotes().forEach((emote, amount) -> {
                    if (emotes.containsKey(emote)) emotes.put(emote, emotes.get(emote) + amount);
                    else emotes.put(emote, amount);
                });
            });

            Map<String, Integer> sChatter = chatter.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(10)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (e1, e2) -> e1, LinkedHashMap::new)),

                    sEmotes = emotes.entrySet().stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .limit(10)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                    (e1, e2) -> e1, LinkedHashMap::new));

            System.out.println(new Gson().toJson(sChatter));
            System.out.println(new Gson().toJson(sEmotes));
        });
        t1.setDaemon(true);
        t1.start();
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
