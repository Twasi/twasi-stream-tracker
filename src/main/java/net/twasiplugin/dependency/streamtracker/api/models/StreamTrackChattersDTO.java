package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasi.core.database.models.TwitchAccount;
import net.twasiplugin.dependency.streamtracker.StreamTracker;

public class StreamTrackChattersDTO {

    private String twitchId;
    private String displayName;
    private int messages;
    private int commands;

    public StreamTrackChattersDTO(String twitchId, String displayName, int messages, int commands) {
        this.twitchId = twitchId;
        this.displayName = displayName;
        this.messages = messages;
        this.commands = commands;
    }

    public StreamTrackChattersDTO(TwitchAccount acc, int messages, int commands) {
        this.messages = messages;
        this.commands = commands;
        this.twitchId = acc.getTwitchId();
        this.displayName = acc.getDisplayName();
    }

    public StreamTrackChattersDTO(StreamTracker.UserMessagesAndCommands msgs) {
        this(msgs.twitchId, msgs.displayName, msgs.messages, msgs.commands);
    }

    public String getTwitchId() {
        return twitchId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMessages() {
        return messages;
    }

    public int getCommands() {
        return commands;
    }
}
