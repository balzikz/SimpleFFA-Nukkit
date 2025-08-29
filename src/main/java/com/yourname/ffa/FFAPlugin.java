package com.yourname.ffa;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import com.yourname.ffa.arena.Arena;
import com.yourname.ffa.arena.ArenaManager;
import com.yourname.ffa.commands.FFACommand;
import com.yourname.ffa.listeners.EventListener;

import java.util.*;

public class FFAPlugin extends PluginBase {

    private static FFAPlugin instance;
    private ArenaManager arenaManager;
    private final Map<UUID, Arena> playerArenas = new HashMap<>();
    private final Set<Location> playerPlacedBlocks = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        this.arenaManager = new ArenaManager(this);

        this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

        ((cn.nukkit.command.PluginCommand) 
        this.getServer().getPluginCommand("ffa")).setExecutor(new FFACommand(this));
        
        getLogger().info("§aSimpleFFA (Феррари) запущен! Арены будут загружены по запросу.");
    }

    public static FFAPlugin getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public void setPlayerArena(Player p, Arena a) { if(a==null) playerArenas.remove(p.getUniqueId()); else playerArenas.put(p.getUniqueId(), a); }
    public Arena getPlayerArena(Player p) { return playerArenas.get(p.getUniqueId()); }
    public boolean isPlayerInArena(Player p) { return playerArenas.containsKey(p.getUniqueId()); }
    public Set<Location> getPlayerPlacedBlocks() { return playerPlacedBlocks; }
}
