package com.yourname.ffa.arena;

import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import java.util.List;

public class Arena {
    public final String id, name, worldName;
    public final Location spawn;
    public final List<Item> kit;
    public final boolean canPlaceBlocks, canBreakBlocks, canDropItems, canPickupItems;
    public final int placedBlocksDecaySeconds;
    public Arena(String id, String name, String worldName, Location spawn, List<Item> kit, boolean p, boolean b, int d, boolean dr, boolean pu) {
        this.id = id; this.name = name; this.worldName = worldName; this.spawn = spawn; this.kit = kit;
        this.canPlaceBlocks = p; this.canBreakBlocks = b; this.placedBlocksDecaySeconds = d;
        this.canDropItems = dr; this.canPickupItems = pu;
    }
}