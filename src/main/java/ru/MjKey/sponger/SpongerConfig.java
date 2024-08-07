package ru.MjKey.sponger;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "sponger")
public class SpongerConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean enableInOverworld = true;

    @ConfigEntry.Gui.Tooltip
    public boolean enableInNether = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int spongeRadius = 3;
}