package net.twasiplugin.dependency.streamtracker;

import net.twasi.core.database.models.User;
import net.twasi.core.events.TwasiEvent;
import net.twasi.core.events.TwasiEventHandler;
import net.twasi.core.services.IService;
import net.twasiplugin.dependency.streamtracker.events.StreamStartEvent;
import net.twasiplugin.dependency.streamtracker.events.StreamStopEvent;
import net.twasiplugin.dependency.streamtracker.events.StreamTrackEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StreamTrackerService implements IService {
    private HashMap<String, RegisteredStreamEventHandlers> registeredHandlers = new HashMap<>();

    void emitEvent(User user, TwasiEvent event) {
        RegisteredStreamEventHandlers handlers = registeredHandlers.get(user.getId().toString());
        if (handlers == null) return; // Return if no handler is registered
        if (event instanceof StreamTrackEvent) {
            StreamTrackEvent streamTrackEvent = (StreamTrackEvent) event; // Cast TwasiEvent to specific event
            for (TwasiStreamTrackEventHandler handler : handlers.getTrackEventHandlers()) // Loop through registered handlers
                if (streamTrackEvent.getCurrentTrackEntity() != null || handler.getEventsWhenOffline) // Online trigger handler when streamer is online or handler requests offline events
                    new Thread(() -> handler.on(streamTrackEvent)).start(); // Trigger handler in new Thread
        }
        if (event instanceof StreamStartEvent) {
            for (TwasiEventHandler<StreamStartEvent> handler : handlers.getStartEventHandlers()) // Loop through registered handlers
                new Thread(() -> handler.on((StreamStartEvent) event)).start(); // Trigger handler in new Thread
        }
        if (event instanceof StreamStopEvent) {
            for (TwasiEventHandler<StreamStopEvent> handler : handlers.getStopEventHandlers()) // Loop through registered handlers
                new Thread(() -> handler.on((StreamStopEvent) event)).start(); // Trigger handler in new Thread
        }
    }

    public void registerStreamStartEvent(User user, TwasiEventHandler<StreamStartEvent> handler) {
        RegisteredStreamEventHandlers handlers = registeredHandlers.get(user.getId().toString());
        if (handlers == null) handlers = new RegisteredStreamEventHandlers();
        registeredHandlers.put(user.getId().toString(), handlers.registerStartEventHandler(handler));
    }

    public void registerStreamTrackEvent(User user, TwasiStreamTrackEventHandler handler) {
        RegisteredStreamEventHandlers handlers = registeredHandlers.get(user.getId().toString());
        if (handlers == null) handlers = new RegisteredStreamEventHandlers();
        registeredHandlers.put(user.getId().toString(), handlers.registerTrackEventHandler(handler));
    }

    public void registerStreamStopEvent(User user, TwasiEventHandler<StreamStopEvent> handler) {
        RegisteredStreamEventHandlers handlers = registeredHandlers.get(user.getId().toString());
        if (handlers == null) handlers = new RegisteredStreamEventHandlers();
        registeredHandlers.put(user.getId().toString(), handlers.registerStopEventHander(handler));
    }

    private class RegisteredStreamEventHandlers {
        private List<TwasiEventHandler<StreamStartEvent>> startEventHandlers = new ArrayList<>();
        private List<TwasiStreamTrackEventHandler> trackEventHandlers = new ArrayList<>();
        private List<TwasiEventHandler<StreamStopEvent>> stopEventHandlers = new ArrayList<>();

        public RegisteredStreamEventHandlers registerStartEventHandler(TwasiEventHandler<StreamStartEvent> handler) {
            this.startEventHandlers.add(handler);
            return this;
        }

        public RegisteredStreamEventHandlers registerTrackEventHandler(TwasiStreamTrackEventHandler handler) {
            this.trackEventHandlers.add(handler);
            return this;
        }

        public RegisteredStreamEventHandlers registerStopEventHander(TwasiEventHandler<StreamStopEvent> handler) {
            this.stopEventHandlers.add(handler);
            return this;
        }

        List<TwasiEventHandler<StreamStartEvent>> getStartEventHandlers() {
            return startEventHandlers;
        }

        List<TwasiStreamTrackEventHandler> getTrackEventHandlers() {
            return trackEventHandlers;
        }

        List<TwasiEventHandler<StreamStopEvent>> getStopEventHandlers() {
            return stopEventHandlers;
        }
    }

    public static abstract class TwasiStreamTrackEventHandler extends TwasiEventHandler<StreamTrackEvent> {
        public boolean getEventsWhenOffline = false; // Can be set to true to get events when offline (entities will be null)
    }
}