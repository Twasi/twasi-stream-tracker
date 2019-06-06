package net.twasiplugin.dependency.streamtracker.api.models;

import net.twasiplugin.dependency.streamtracker.database.ViewTimeEntity;

public class ViewTimeDTO {

    private String twitchId;
    private String displayName;
    private int minutes;

    public ViewTimeDTO(ViewTimeEntity viewTimeEntity) {
        this.twitchId = viewTimeEntity.getTwitchId();
        this.displayName = viewTimeEntity.getDisplayName();
        this.minutes = viewTimeEntity.getMinutes();
    }

    public String getTwitchId() {
        return twitchId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinutes() {
        return minutes;
    }
}
