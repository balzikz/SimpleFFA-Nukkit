package com.yourname.ffa.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import com.yourname.ffa.FFAPlugin;
import com.yourname.ffa.arena.Arena;

public class FFACommand implements cn.nukkit.command.CommandExecutor {

    private static final int FFA_MENU_FORM_ID = 777;

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
                player.sendMessage("§cНа сервере нет доступных арен.");
                return true;
            }

            FormWindowSimple form = new FormWindowSimple("§l§6FFA", "§7Выберите арену:");

            for (Arena arena : arenas) {
                int players = plugin.getPlayersInArena(arena);
                String buttonText = arena.name + "\n§8Играют: §e" + players;
                form.addButton(new ElementButton(buttonText));
            }

            form.addButton(new ElementButton("§cЗакрыть"));
            player.showFormWindow(form, FFA_MENU_FORM_ID);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("leave")) {
            if (!plugin.isPlayerInArena(player)) {
                player.sendMessage("§cВы не находитесь на FFA арене.");
                return true;
            }

            plugin.getArenaManager().leaveArena(player, true);
            player.sendMessage("§aВы вышли с FFA арены.");
            return true;
        }

        if (plugin.getArenaManager().getArenaById(sub) == null) {
            player.sendMessage("§cАрена с ID '" + sub + "' не найдена!");
            return true;
        }

        plugin.getArenaManager().joinArena(player, sub);
        return true;
    }
}
