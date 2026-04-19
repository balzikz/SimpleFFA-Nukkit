package com.yourname.ffa.listeners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.inventory.InventoryPickupItemEvent;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.level.Location;
import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.arena.Arena;

import java.util.ArrayList;
import java.util.Locale;

public class EventListener implements Listener {

    private static final int FFA_MENU_FORM_ID = 777;

    private final FFAPlugin plugin;

    public EventListener(FFAPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onForm(PlayerFormRespondedEvent event) {
        if (event.getFormID() != FFA_MENU_FORM_ID) {
            return;
        }

        if (!(event.getResponse() instanceof FormResponseSimple response)) {
            return;
        }

        Player player = event.getPlayer();
        int id = response.getClickedButtonId();

        if (id < 0) {
            return;
        }

        var arenas = new ArrayList<>(plugin.getArenaManager().getAllArenas());

        if (id == arenas.size()) {
            return;
        }

        if (id >= arenas.size()) {
            return;
        }

        Arena arena = arenas.get(id);
        plugin.getArenaManager().joinArena(player, arena.id);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getPlayerArena(player);

        if (arena == null) {
            return;
        }

        String message = event.getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }

        String raw = message.startsWith("/") ? message.substring(1) : message;
        raw = raw.trim().toLowerCase(Locale.ROOT);

        if (raw.equals("hub") || raw.startsWith("hub ")) {
            return;
        }

        if (raw.equals("ffa leave")) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage("§cНа FFA арене это команда недопустна");
    }

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

            if (plugin.getPlayerPlacedBlocks().remove(blockLoc)) {
                return;
            }

            if (!arena.canBreakBlocks) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getPlayerArena(player);

        if (arena != null) {
            if (!arena.canPlaceBlocks) {
                event.setCancelled(true);
                return;
            }

            if (arena.placedBlocksDecaySeconds > 0) {
                Block block = event.getBlock();
                Location blockLoc = block.getLocation();

                plugin.getPlayerPlacedBlocks().add(blockLoc);

                plugin.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
                    if (plugin.getPlayerPlacedBlocks().remove(blockLoc)
                            && block.getLevel().getBlock(blockLoc).getId() == block.getId()) {
                        block.getLevel().setBlock(blockLoc, Block.get(Block.AIR));
                    }
                }, arena.placedBlocksDecaySeconds * 20);
            }
        }
    }

    @EventHandler
    public void onPickupItem(InventoryPickupItemEvent event) {
        if (!(event.getInventory().getHolder() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getInventory().getHolder();
        Arena arena = plugin.getPlayerArena(player);

        if (arena != null && !arena.canPickupItems) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getPlayerArena(player);

        if (arena != null && !arena.canDropItems) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.isPlayerInArena(event.getPlayer())) {
            plugin.getArenaManager().leaveArena(event.getPlayer(), false);
        }
    }
}
