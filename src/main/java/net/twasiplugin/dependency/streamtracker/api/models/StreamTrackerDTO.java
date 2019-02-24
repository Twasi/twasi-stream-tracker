package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.database.models.User;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.database.StreamRepository;

import java.util.List;

public class StreamTrackerDTO {

    private User user;
    private StreamRepository repo = ServiceRegistry.get(DataService.class).get(StreamRepository.class);

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

}
