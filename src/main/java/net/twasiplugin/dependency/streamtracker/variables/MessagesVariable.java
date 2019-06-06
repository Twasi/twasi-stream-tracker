package net.twasiplugin.dependency.streamtracker.variables;

import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.models.Message.TwasiMessage;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.TwasiVariable;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.database.StreamTrackRepository;

import java.util.Collections;
import java.util.List;

public class MessagesVariable extends TwasiVariable {

    public MessagesVariable(TwasiUserPlugin owner) {
        super(owner);
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList("messages");
    }

    @Override
    public String process(String name, TwasiInterface inf, String[] params, TwasiMessage message) {
        return String.valueOf(DataService.get().get(StreamTrackRepository.class).getTotalMessagesByUserAndTwitchId(inf.getStreamer().getUser(), message.getSender().getTwitchId()));
    }
}
