package net.mrbt0907.weather2.api.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.mrbt0907.weather2.util.ConfigList;

@Cancelable
public class EventRegisterStages extends Event {
    public final ConfigList tornadoStageList;
    public final ConfigList hurricaneStageList;


    public EventRegisterStages(ConfigList tornadoStageList, ConfigList hurricaneStageList) {
        this.tornadoStageList = tornadoStageList;
        this.hurricaneStageList = hurricaneStageList;
    }
}