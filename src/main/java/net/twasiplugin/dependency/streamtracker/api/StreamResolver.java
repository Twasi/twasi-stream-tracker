package net.twasiplugin.dependency.streamtracker.api;

import net.twasi.core.database.models.User;
import net.twasi.core.graphql.TwasiCustomResolver;
import net.twasiplugin.dependency.streamtracker.api.models.StreamTrackerDTO;

public class StreamResolver extends TwasiCustomResolver {

    public StreamTrackerDTO getStreamtracker(String token) {
        User user = getUser(token);
        if (user == null) return null;
        return new StreamTrackerDTO(user);
    }

}
