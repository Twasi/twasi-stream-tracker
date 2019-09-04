package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.database.models.TwitchAccount;
import net.twasi.core.services.providers.InstanceManagerService;
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
        return plugins.stream().map(pl -> {
            TwitchAccount acc = pl.getTwasiInterface().getStreamer().getUser().getTwitchAccount();
            StreamTracker tracker = pl.getTracker();
            return new LiveBotUserDTO(
                    acc.getTwitchId(),
                    acc.getUserName(),
                    acc.getDisplayName(),
                    tracker.getLastViewerCount(),
                    tracker.getLastFollowerCount(),
                    tracker.getLastGameId(),
                    tracker.getLastTitle()
            );
        }).collect(Collectors.toList());
    }

    private String twitchId;
    private String userName;
    private String displayName;
    private int viewerCount;
    private int followerCount;
    private String currentGameId;
    private String currentTitle;

    public LiveBotUserDTO(String twitchId, String userName, String displayName, int viewerCount, int followerCount, String currentGameId, String currentTitle) {
        this.twitchId = twitchId;
        this.userName = userName;
        this.displayName = displayName;
        this.viewerCount = viewerCount;
        this.followerCount = followerCount;
        this.currentGameId = currentGameId;
        this.currentTitle = currentTitle;
    }

    public String getTwitchId() {
        return twitchId;
    }

    public String getUserName() {
        return userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getViewerCount() {
        return viewerCount;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public String getCurrentGameId() {
        return currentGameId;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }
}
