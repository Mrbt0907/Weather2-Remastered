package net.mrbt0907.weather2.api.event;

import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.function.Predicate;

public class EventRegisterStormNames extends Event {
    protected final List<String> nameList;

    public EventRegisterStormNames(List<String> nameList) {
        this.nameList = nameList;
    }

    public int size() {
        return nameList.size();
    }

    public boolean contains(String name) {
        return nameList.contains(name);
    }

    public void removeName(String... names) {
        for (String name : names)
            nameList.remove(name);
    }

    public void removeIf(Predicate<? super String> predicate) {
        nameList.removeIf(predicate);
    }

    public void addName(String... names) {
        for (String name : names)
            if (nameList.contains(name))
                nameList.add(name);
    }
}