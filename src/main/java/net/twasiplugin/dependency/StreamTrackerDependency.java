package net.twasiplugin.dependency;

import net.twasi.core.database.models.User;
import net.twasi.core.events.NewInstanceEvent;
import net.twasi.core.events.TwasiEventHandler;
import net.twasi.core.plugin.TwasiDependency;
import net.twasi.core.services.ServiceRegistry;
import net.twasi.core.services.providers.InstanceManagerService;

import java.util.HashMap;

public class StreamTrackerDependency extends TwasiDependency {

    private HashMap<String, StreamTracker> registeredTrackers;
    static StreamTrackerService service = new StreamTrackerService();

    @Override
    public void onActivate() {
        ServiceRegistry.register(service);

        registeredTrackers = new HashMap<>();
        InstanceManagerService instanceManager = ServiceRegistry.get(InstanceManagerService.class);
        instanceManager.registerNewInstanceHandler(new TwasiEventHandler<NewInstanceEvent>() {
            @Override
            public void on(NewInstanceEvent newInstanceEvent) {
                User user = newInstanceEvent.getTwasiInterface().getStreamer().getUser();
                String tId = user.getTwitchAccount().getTwitchId();
                if (registeredTrackers.containsKey(tId)) registeredTrackers.get(tId).stopTracking();
                StreamTracker tracker = new StreamTracker(newInstanceEvent.getTwasiInterface());
                registeredTrackers.put(tId, tracker);
                tracker.start();
            }
        });
    }
}
