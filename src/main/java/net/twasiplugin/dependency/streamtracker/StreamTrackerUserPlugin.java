package net.twasiplugin.dependency.streamtracker;

import net.twasi.core.database.models.User;
import net.twasi.core.events.CommandExecutedEvent;
import net.twasi.core.events.TwasiEventHandler;
import net.twasi.core.messages.MessageDispatcher;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.events.TwasiEnableEvent;
import net.twasi.core.plugin.api.events.TwasiMessageEvent;

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
    }

    @Override
    public void onMessage(TwasiMessageEvent e) {
        tracker.addMessage(e.getMessage().getSender());
    }
}
