package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.database.models.User;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.database.StreamRepository;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;
import net.twasiplugin.dependency.streamtracker.database.ViewTimeRepository;

import java.util.List;
import java.util.stream.Collectors;

public class AccountStatsDTO {

    private final StreamTrackRepository trackRepo;
    private final StreamRepository streamRepo;
    private final ViewTimeRepository viewTimeRepo;
    private User user;

    public AccountStatsDTO(User user) {
        this.user = user;
        streamRepo = DataService.get().get(StreamRepository.class);
        trackRepo = DataService.get().get(StreamTrackRepository.class);
        viewTimeRepo = DataService.get().get(ViewTimeRepository.class);
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

    public List<ViewTimeDTO> getViewTime() {
        return viewTimeRepo.getAllByUser(user).stream().map(ViewTimeDTO::new).collect(Collectors.toList());
    }

    public int getMessagesOfUser(String twitchId){
        return trackRepo.getTotalMessagesByUserAndTwitchId(user, twitchId);
    }
}
