package net.twasiplugin.dependency.streamtracker;

import net.twasi.core.database.models.TwitchAccount;
import net.twasi.core.database.models.User;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasi.twitchapi.helix.streams.response.StreamDTO;
import net.twasi.twitchapi.helix.users.response.UserDTO;
import net.twasi.twitchapi.kraken.channels.response.ChannelDTO;
import net.twasi.twitchapi.options.TwitchRequestOptions;
import net.twasiplugin.dependency.streamtracker.database.*;
import net.twasiplugin.dependency.streamtracker.events.StreamStartEvent;
import net.twasiplugin.dependency.streamtracker.events.StreamStopEvent;
import net.twasiplugin.dependency.streamtracker.events.StreamTrackEvent;
import org.mongodb.morphia.annotations.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.twasi.twitchapi.TwitchAPI.helix;
import static net.twasi.twitchapi.TwitchAPI.tmi;
import static net.twasi.twitchapi.TwitchAPI.kraken;
import static net.twasiplugin.dependency.streamtracker.StreamTrackerPlugin.service;

public class StreamTracker extends Thread {

    // Thread controller
    private boolean continueTracking = true;

    // User information
    private TwasiInterface twasiInterface;
    private User user;
    private TwitchAccount twitchAccount;

    // State watcher
    private boolean online = false;

    // Tracked data from UserPlugin class
    private List<UserMessagesAndCommands> userMessages; // TwitchID and amount of messages and commands

    // Repositories
    private static StreamRepository streamRepo = ServiceRegistry
            .get(DataService.class).get(StreamRepository.class);
    private static StreamTrackRepository streamTrackRepo = ServiceRegistry
            .get(DataService.class).get(StreamTrackRepository.class);
    private static ViewTimeRepository viewTimeRepo = ServiceRegistry
            .get(DataService.class).get(ViewTimeRepository.class);

    public StreamTracker(TwasiInterface twasiInterface) {
        this.setDaemon(true);
        this.twasiInterface = twasiInterface;
        this.user = twasiInterface.getStreamer().getUser();
        this.twitchAccount = user.getTwitchAccount();
        this.userMessages = new ArrayList<>();
    }

    // Track every minute
    @Override
    public void run() {
        while (continueTracking) {
            try {
                new Thread(this::track).start(); // Track in another thread to not get out of sync
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // If tracker should be stopped just let the thread loop run out
    public void stopTracking() {
        continueTracking = false;
    }

    // Look up for online status and emit events
    private void track() {
        String result = "[Tracker] User " + twitchAccount.getDisplayName() + " ";
        List<StreamDTO> dtoList = helix().streams().getStreamsByUser(twitchAccount.getTwitchId(), 1, new TwitchRequestOptions().withAuth(twitchAccount.toAuthContext())).getData();
        if (dtoList.size() > 0) {
            StreamTrackEntity entity = processDTO(dtoList.get(0));
            service.emitEvent(this.user, new StreamTrackEvent(this.user, entity));
            if (!online) service.emitEvent(this.user, new StreamStartEvent(this.user, entity));
            result += "tracked.";
            online = true;
        } else {
            service.emitEvent(this.user, new StreamTrackEvent(this.user, null));
            result += "is offline. Skipped tracking.";
            if (online) service.emitEvent(this.user, new StreamStopEvent(this.user));
            online = false;
        }
        TwasiLogger.log.debug(result);
    }

    // Create stream-association for entity and commit to database
    private StreamTrackEntity processDTO(StreamDTO dto) {
        StreamEntity stream = streamRepo.getStreamEntityByStreamId(dto.getId());
        UserDTO currentUser = helix().users().withAuth(twasiInterface.getStreamer().getUser().getTwitchAccount().toAuthContext()).getCurrentUser();
        ChannelDTO channelDTO = kraken().channels().withAuth(user.getTwitchAccount().toAuthContext()).updateChannel(null, null);
        if (stream == null) {
            TwasiLogger.log.debug("[Tracker] Tracking new stream of user " + twitchAccount.getDisplayName() + ".");
            stream = new StreamEntity(user, dto.getId(), dto.getLanguage(), dto.getStartedAt(), dto.getType(), dto.getCommunityIds(), dto.getTagIds(), channelDTO.getFollowers(), currentUser.getViewCount());
            streamRepo.add(stream);
        } else {
            stream.setFollowers(channelDTO.getFollowers());
            stream.setViews(currentUser.getViewCount());
        }
        streamRepo.commit(stream);
        streamRepo.commitAll();
        StreamTrackEntity entity = new StreamTrackEntity(stream, dto.getGameId(), dto.getTitle(), dto.getViewerCount(), userMessages);
        streamTrackRepo.add(entity);
        streamTrackRepo.commitAll();

        List<String> all = new ArrayList<>(tmi().chatters().getByName(user.getTwitchAccount().getUserName()).getChatters().getAll());
        Map<String, String> idsAndNames = new HashMap<>();

        while (all.size() > 0) {
            List<String> collect = all.stream().limit(100).collect(Collectors.toList());
            all.removeAll(collect);
            helix().users().getUsers(null, collect.toArray(new String[0]), new TwitchRequestOptions().withAuth(user.getTwitchAccount().toAuthContext())).forEach(e -> {
                TwasiLogger.log.debug("Viewer tracked: " + e.getId() + " (" + e.getDisplayName() + ")");
                idsAndNames.put(e.getId(), e.getDisplayName());
            });
        }

        idsAndNames.forEach((id, name) -> {
            ViewTimeEntity vtEntity = viewTimeRepo.getViewTimeEntityOrCreate(user, id);
            vtEntity.increment();
            vtEntity.setDisplayName(name);
            viewTimeRepo.commit(vtEntity);
        });

        userMessages = new ArrayList<>();
        TwasiLogger.log.debug("Saved trackentity for Stream #" + stream.getStreamId() + " (" + stream.getUser().getTwitchAccount().getDisplayName() + ") into database.");
        return entity;
    }

    public boolean isTracking() {
        return continueTracking;
    }

    public boolean isOnline() {
        return online;
    }

    private UserMessagesAndCommands getUserMessagesObject(TwitchAccount acc) {
        UserMessagesAndCommands usrMsgs = userMessages.stream().filter(e -> e.twitchId.equals(acc.getTwitchId())).findAny().orElse(null);
        if (usrMsgs == null) {
            usrMsgs = new UserMessagesAndCommands(acc);
            userMessages.add(usrMsgs);
        }
        return usrMsgs;
    }

    public void addMessage(TwitchAccount acc) {
        getUserMessagesObject(acc).messages++;
    }

    public void addCommand(TwitchAccount acc) {
        getUserMessagesObject(acc).commands++;
    }

    @Entity
    public static class UserMessagesAndCommands {
        public int messages = 0;
        public int commands = 0;
        public String twitchId;
        public String displayName;

        public UserMessagesAndCommands(TwitchAccount acc) {
            this.twitchId = acc.getTwitchId();
            this.displayName = acc.getDisplayName();
        }

        public UserMessagesAndCommands() {
        }
    }
}
