package net.twasiplugin.dependency.streamtracker;

import net.twasi.core.database.models.User;
import net.twasi.core.events.CommandExecutedEvent;
import net.twasi.core.events.TwasiEventHandler;
import net.twasi.core.messages.MessageDispatcher;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.events.TwasiEnableEvent;
import net.twasi.core.plugin.api.events.TwasiMessageEvent;
import net.twasiplugin.dependency.streamtracker.variables.MessagesVariable;
import net.twasiplugin.dependency.streamtracker.variables.ViewTimeVariable;

import static net.twasiplugin.dependency.streamtracker.StreamTrackerPlugin.registeredTrackers;

public class StreamTrackerUserPlugin extends TwasiUserPlugin {

    private StreamTracker tracker;

    @Override
    public void onEnable(TwasiEnableEvent e) {
        User user = getTwasiInterface().getStreamer().getUser();
        String tId = user.getTwitchAccount().getTwitchId();
        if (registeredTrackers.containsKey(tId)) registeredTrackers.get(tId).stopTracking();
        tracker = new StreamTracker(getTwasiInterface());
        registeredTrackers.put(tId, tracker);
        tracker.start();

        MessageDispatcher.registerCommandExecutedEventHandler(getTwasiInterface().getStreamer().getUser(), new TwasiEventHandler<CommandExecutedEvent>() {
            @Override
            public void on(CommandExecutedEvent commandExecutedEvent) {
                tracker.addCommand(commandExecutedEvent.getCommand().getSender());
            }
        });

        registerVariable(ViewTimeVariable.class);
        registerVariable(MessagesVariable.class);
    }

    @Override
    public void onMessage(TwasiMessageEvent e) {
        tracker.addMessage(e.getMessage().getSender());
    }

    public StreamTracker getTracker() {
        return this.tracker;
    }
}
