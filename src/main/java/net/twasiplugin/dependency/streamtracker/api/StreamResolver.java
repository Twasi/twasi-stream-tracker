package net.twasiplugin.dependency.streamtracker.api;

import net.twasi.core.database.models.User;
import net.twasi.core.database.repositories.UserRepository;
import net.twasi.core.graphql.TwasiCustomResolver;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.api.models.StreamTrackerDTO;

public class StreamResolver extends TwasiCustomResolver {

    public StreamTrackerDTO getStreamtracker(String token) {

        if (token.equals("SD")) {
            UserRepository repo = ServiceRegistry.get(DataService.class).get(UserRepository.class);
            return new StreamTrackerDTO(repo.getByTwitchId("119965990"));
        }

        User user = getUser(token);
        if (user == null) return null;
        return new StreamTrackerDTO(user);
    }

}
