package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.services.providers.InstanceManagerService;
import net.twasi.twitchapi.kraken.channels.response.ChannelDTO;
import net.twasiplugin.dependency.streamtracker.StreamTracker;
import net.twasiplugin.dependency.streamtracker.StreamTrackerUserPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LiveBotUserDTO {

    public static List<LiveBotUserDTO> getAll() {
        List<StreamTrackerUserPlugin> plugins = new ArrayList<>();
        InstanceManagerService.get()
                .getInterfaces()
                .forEach(tif ->
                        tif.getPlugins().stream().filter(pl -> pl.getClass().equals(StreamTrackerUserPlugin.class))
                                .map(pl -> (StreamTrackerUserPlugin) pl)
                                .filter(pl -> pl.getTracker().isOnline())
                                .forEach(plugins::add)
                );
        return plugins.stream()
                .filter(pl -> pl.getTracker().getChannelData() != null)
                .map(pl -> {
                    StreamTracker tracker = pl.getTracker();
                    return new LiveBotUserDTO(
                            tracker.getChannelData(),
                            tracker.getLastViewerCount()
                    );
                }).collect(Collectors.toList());
    }

    private ChannelDTO channelData;
    private int viewerCount;

    public LiveBotUserDTO(ChannelDTO channelData, int viewerCount) {
        this.channelData = channelData;
        this.viewerCount = viewerCount;
    }

    public ChannelDTO getChannelData() {
        return channelData;
    }

    public int getViewerCount() {
        return viewerCount;
    }
}
