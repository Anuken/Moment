package mindustry.world.meta;

import arc.*;

import java.util.*;

/**
 * Defines a unit of measurement for block stats.
 */
public enum StatUnit{
    blocks,
    blocksSquared,
    minMaxBlocksSquared,
    powerSecond,
    liquidSecond,
    itemsSecond,
    liquidUnits,
    powerUnits,
    degrees,
    seconds,
    minutes,
    perSecond,
    perMinute,
    perShot,
    timesSpeed(false),
    percent(false),
    shieldHealth,
    none,
    items;

    public final boolean space;

    StatUnit(boolean space){
        this.space = space;
    }

    StatUnit(){
        this(true);
    }

    public String localized(){
        if(this == none) return "";
        return Core.bundle.get("unit." + name().toLowerCase(Locale.ROOT));
    }
}
