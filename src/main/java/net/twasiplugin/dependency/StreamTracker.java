package net.twasiplugin.dependency;

import net.twasi.core.database.models.TwitchAccount;
import net.twasi.core.database.models.User;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.logger.TwasiLogger;
import net.twasi.twitchapi.helix.streams.response.StreamDTO;
import net.twasi.twitchapi.options.TwitchRequestOptions;
import net.twasiplugin.dependency.database.StreamEntity;
import net.twasiplugin.dependency.database.StreamRepository;
import net.twasiplugin.dependency.database.StreamTrackEntity;
import net.twasiplugin.dependency.database.StreamTrackRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.twasi.twitchapi.TwitchAPI.helix;

public class StreamTracker extends Thread {

    // Thread controller
    private boolean continueTracking = true;

    // User information
    private TwasiInterface twasiInterface;
    private User user;
    private TwitchAccount twitchAccount;

    // Repositories
    private StreamRepository streamRepo;
    private StreamTrackRepository streamTrackRepo;

    public StreamTracker(TwasiInterface twasiInterface) {
        this.setDaemon(true);
        this.twasiInterface = twasiInterface;
        this.user = twasiInterface.getStreamer().getUser();
        this.twitchAccount = user.getTwitchAccount();
    }

    @Override
    public void run() {
        while (continueTracking) {
            try {
                track();
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopTracking() {
        continueTracking = false;
    }

    private void track() {
        String result = "[Tracker] User " + twitchAccount.getDisplayName() + " ";
        List<StreamDTO> dtoList = helix().streams().getStreamsByUser(twitchAccount.getTwitchId(), 1, new TwitchRequestOptions().withAuth(twitchAccount.toAuthContext())).getData();
        if (dtoList.size() > 0) {
            processDTO(dtoList.get(0));
            result += "tracked.";
        } else {
            result += "is offline. Skipped tracking.";
        }
        TwasiLogger.log.debug(result);
    }

    private void processDTO(StreamDTO dto) {
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

    }

}
