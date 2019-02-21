package net.twasiplugin.dependency;

import net.twasi.core.database.models.User;
import net.twasi.core.events.TwasiEvent;
import net.twasi.core.events.TwasiEventHandler;
import net.twasi.core.services.IService;
import net.twasiplugin.dependency.events.StreamStartEvent;
import net.twasiplugin.dependency.events.StreamTrackEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StreamTrackerService implements IService {
    private HashMap<String, RegisteredStreamEventHandlers> registeredHandlers = new HashMap<>();

    void emitEvent(User user, TwasiEvent event) {
        RegisteredStreamEventHandlers handlers = registeredHandlers.get(user.getId().toString());
        if (handlers == null) return;
        if (event instanceof StreamTrackEvent) {
            StreamTrackEvent streamTrackEvent = (StreamTrackEvent) event;
            for (TwasiStreamTrackEventHandler handler : handlers.getTrackEventHandlers())
                if (streamTrackEvent.getCurrentTrackEntity() != null || handler.getEventsWhenOffline)
                    new Thread(() -> handler.on(streamTrackEvent)).start();
        }
        if (event instanceof StreamStartEvent) {
            StreamStartEvent streamTrackEvent = (StreamStartEvent) event;
            for (TwasiEventHandler<StreamStartEvent> handler : handlers.getStartEventHandlers())
                new Thread(() -> handler.on((StreamStartEvent) event)).start();
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

    private class RegisteredStreamEventHandlers {
        private List<TwasiEventHandler<StreamStartEvent>> startEventHandlers = new ArrayList<>();
        private List<TwasiStreamTrackEventHandler> trackEventHandlers = new ArrayList<>();

        public RegisteredStreamEventHandlers registerStartEventHandler(TwasiEventHandler<StreamStartEvent> handler) {
            this.startEventHandlers.add(handler);
            return this;
        }

        public RegisteredStreamEventHandlers registerTrackEventHandler(TwasiStreamTrackEventHandler handler) {
            this.trackEventHandlers.add(handler);
            return this;
        }

        public List<TwasiEventHandler<StreamStartEvent>> getStartEventHandlers() {
            return startEventHandlers;
        }

        public List<TwasiStreamTrackEventHandler> getTrackEventHandlers() {
            return trackEventHandlers;
        }
    }

    public abstract class TwasiStreamTrackEventHandler extends TwasiEventHandler<StreamTrackEvent> {
        public boolean getEventsWhenOffline = false; // Can be set to true to get events when offline (entities will be null)
    }
}


/*
        private class RegisteredStreamEventHandlers {
            private List<TwasiEventHandler<? extends TwasiEvent>> list = new ArrayList<>();

            public void put(TwasiEventHandler<? extends TwasiEvent> handler) {
                list.add(handler);
            }

            public <T extends TwasiEvent> List<TwasiEventHandler<T>> getEventHandlersOfType(Class<T> clazz) {
                List<TwasiEventHandler<T>> found = new ArrayList<>();
                for (TwasiEventHandler<? extends TwasiEvent> handler : list)
                    if (handler.getClass().getTypeName().equalsIgnoreCase(clazz.getTypeName()))
                        found.add((TwasiEventHandler<T>) handler);
                return found;
            }

            public List<TwasiEventHandler> getEventHandlers() {
                return list;
            }
        }
    */