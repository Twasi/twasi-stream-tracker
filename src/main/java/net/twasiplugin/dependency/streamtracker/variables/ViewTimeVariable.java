package net.twasiplugin.dependency.streamtracker.variables;

import net.twasi.core.database.models.Units;
import net.twasi.core.interfaces.api.TwasiInterface;
import net.twasi.core.models.Message.TwasiMessage;
import net.twasi.core.plugin.api.TwasiUserPlugin;
import net.twasi.core.plugin.api.TwasiVariable;
import net.twasi.core.services.providers.DataService;
import net.twasiplugin.dependency.streamtracker.database.ViewTimeRepository;

import java.util.Collections;
import java.util.List;

public class ViewTimeVariable extends TwasiVariable {

    public ViewTimeVariable(TwasiUserPlugin owner) {
        super(owner);
    }

    @Override
    public List<String> getNames() {
        return Collections.singletonList("viewtime");
    }

    @Override
    public String process(String name, TwasiInterface inf, String[] params, TwasiMessage message) {
        int minutes = DataService.get().get(ViewTimeRepository.class).getViewTimeEntityOrCreate(inf.getStreamer().getUser(), message.getSender().getTwitchId()).getMinutes();
        Units units = inf.getStreamer().getUser().getConfig().getLanguage().getUnits();
        String unit = minutes == 1 ? units.MINUTE : units.MINUTES;
        if (minutes >= 60) {
            minutes /= 60;
            unit = minutes == 1 ? units.HOUR : units.HOURS;
        }
        return minutes + " " + unit;
    }

}
