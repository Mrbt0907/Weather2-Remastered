package net.modconfig;

import java.util.Comparator;
import net.modconfig.ConfigEntryInfo;

public class ConfigComparatorName implements Comparator<ConfigEntryInfo> {

    @Override
    public int compare(ConfigEntryInfo arg0, ConfigEntryInfo arg1) {
        return arg0.name.compareToIgnoreCase(arg1.name);
    }

}