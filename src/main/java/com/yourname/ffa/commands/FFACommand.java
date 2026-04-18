package com.yourname.ffa.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.arena.Arena;

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
                player.sendMessage("§cНет доступных арен.");
                return true;
            }

            FormWindowSimple form = new FormWindowSimple("§l§cFFA", "§7Выберите арену:");

            for (Arena arena : arenas) {
                form.addButton(new ElementButton(arena.name));
            }

            form.addButton(new ElementButton("§cЗакрыть"));

            player.showFormWindow(form, 777);

            return true;
        }

        String arenaId = args[0].toLowerCase();

        if (plugin.getArenaManager().getArenaById(arenaId) == null) {
            player.sendMessage("§cАрена не найдена!");
            return true;
        }

        plugin.getArenaManager().joinArena(player, arenaId);

        return true;
    }
}
