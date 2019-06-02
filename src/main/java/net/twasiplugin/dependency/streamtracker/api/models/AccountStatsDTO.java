package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.database.models.User;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.database.StreamRepository;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;

public class AccountStatsDTO {

    private final StreamTrackRepository trackRepo;
    private final StreamRepository streamRepo;
    private User user;

    public AccountStatsDTO(User user) {
        this.user = user;
        streamRepo = DataService.get().get(StreamRepository.class);
        trackRepo = DataService.get().get(StreamTrackRepository.class);
    }

    public int totalTrackedViewers() {
        try {
            return trackRepo.getChatterAmount(user);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int totalTrackedMessages() {
        try {
            return trackRepo.getTotalMessages(user);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int totalTrackedStreams() {
        try {
            return (int) streamRepo.getStreamAmountByUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
