package net.twasiplugin.dependency.streamtracker;

import net.twasi.core.database.models.TwitchAccount;
import net.twasi.core.database.models.User;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasi.twitchapi.helix.streams.response.StreamDTO;
import net.twasi.twitchapi.options.TwitchRequestOptions;
import net.twasiplugin.dependency.streamtracker.database.StreamEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamRepository;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackEntity;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;
import net.twasiplugin.dependency.streamtracker.events.StreamStartEvent;
import net.twasiplugin.dependency.streamtracker.events.StreamStopEvent;
import net.twasiplugin.dependency.streamtracker.events.StreamTrackEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.twasi.twitchapi.TwitchAPI.helix;
import static net.twasiplugin.dependency.streamtracker.StreamTrackerDependency.service;

public class StreamTracker extends Thread {

    // Thread controller
    private boolean continueTracking = true;

    // User information
    private TwasiInterface twasiInterface;
    private User user;
    private TwitchAccount twitchAccount;

    // State watcher
    private boolean online = false;

    // Repositories
    private static StreamRepository streamRepo = ServiceRegistry
            .get(DataService.class).get(StreamRepository.class);
    private static StreamTrackRepository streamTrackRepo = ServiceRegistry
            .get(DataService.class).get(StreamTrackRepository.class);

    public StreamTracker(TwasiInterface twasiInterface) {
        this.setDaemon(true);
        this.twasiInterface = twasiInterface;
        this.user = twasiInterface.getStreamer().getUser();
        this.twitchAccount = user.getTwitchAccount();
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
        if (stream == null) {
            TwasiLogger.log.debug("[Tracker] Tracking new stream of user " + twitchAccount.getDisplayName() + ".");
            stream = new StreamEntity(user, dto.getId(), dto.getLanguage(), dto.getStartedAt(), dto.getType(), dto.getCommunityIds(), dto.getTagIds());
            streamRepo.add(stream);
            streamRepo.commitAll();
        }
        StreamTrackEntity entity = new StreamTrackEntity(stream, dto.getGameId(), dto.getTitle(), dto.getViewerCount());
        streamTrackRepo.add(entity);
        streamTrackRepo.commitAll();
        return entity;
    }

    public boolean isTracking() {
        return continueTracking;
    }

    public boolean isOnline() {
        return online;
    }
}
