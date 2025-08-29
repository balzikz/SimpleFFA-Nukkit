package com.yourname.ffa.listeners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.Task;
import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.arena.Arena;

public class EventListener implements Listener {

    private final FFAPlugin plugin;
    public EventListener(FFAPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = plugin.getPlayerArena(player);
        if (arena != null) {
            event.setDrops(new cn.nukkit.item.Item[0]);
            event.setDeathMessage("");
            plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                if (player.isOnline()) {
                    player.teleport(arena.spawn);
                    plugin.getArenaManager().preparePlayer(player, arena);
                }
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getPlayerArena(player);
        if (arena != null) {
            Location blockLoc = event.getBlock().getLocation();
            if (plugin.getPlayerPlacedBlocks().remove(blockLoc)) return;
            if (!arena.canBreakBlocks) event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getPlayerArena(player);
        if (arena != null) {
            if (!arena.canPlaceBlocks) { event.setCancelled(true); return; }
            if (arena.placedBlocksDecaySeconds > 0) {
                Block block = event.getBlock();
                Location blockLoc = block.getLocation();
                plugin.getPlayerPlacedBlocks().add(blockLoc);
                plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                    if (plugin.getPlayerPlacedBlocks().remove(blockLoc) && block.getLevel().getBlock(blockLoc).getId() == block.getId()) {
                        block.getLevel().setBlock(blockLoc, Block.get(Block.AIR));
                    }
                }, arena.placedBlocksDecaySeconds * 20);
            }
        }
    }

    @EventHandler
    public void onPickupItem(InventoryPickupItemEvent event) {
        if (!(event.getInventory().getHolder() instanceof Player)) return;
        Player player = (Player) event.getInventory().getHolder();
        Arena arena = plugin.getPlayerArena(player);
        if (arena != null && !arena.canPickupItems) event.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getPlayerArena(player);
        if (arena != null && !arena.canDropItems) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.isPlayerInArena(event.getPlayer())) {
            plugin.getArenaManager().leaveArena(event.getPlayer(), false);
        }
    }
}