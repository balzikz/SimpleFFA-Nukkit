package com.yourname.ffa.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.arena.Arena;
import java.util.stream.Collectors;

public class FFACommand implements cn.nukkit.command.CommandExecutor {
    
    private final FFAPlugin plugin;

    public FFACommand(FFAPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("ffa")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cКоманда только для игроков.");
            return true;
        }
        Player player = (Player) sender;
        
        plugin.getArenaManager().loadArenas();

        if (args.length == 0) {
            var arenas = plugin.getArenaManager().getAllArenas();
            if (arenas.isEmpty()) {
                player.sendMessage("§cНа сервере нет настроенных и рабочих FFA арен.");
                return true;
            }
            String arenaList = arenas.stream().map(arena -> arena.id).collect(Collectors.joining("§7, §a"));
            player.sendMessage("§eДоступные арены: §a" + arenaList);
            player.sendMessage("§eИспользование: §b/ffa <id_арены>");
            return true;
        }

        String arenaId = args[0].toLowerCase();
        if (plugin.getArenaManager().getArenaById(arenaId) == null) {
            player.sendMessage("§cАрена с ID '" + arenaId + "' не найдена!");
            return true;
        }
        plugin.getArenaManager().joinArena(player, arenaId);
        return true;
    }
}
