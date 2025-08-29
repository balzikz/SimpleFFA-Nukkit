package com.yourname.ffa.arena;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.yourname.ffa.FFAPlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArenaManager {

    private final FFAPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();

    public ArenaManager(FFAPlugin plugin) { this.plugin = plugin; }

    public void loadArenas() {
        arenas.clear();
        ConfigSection arenasSection = plugin.getConfig().getSection("arenas");
        for (String id : arenasSection.getKeys(false)) {
            try {
                ConfigSection data = arenasSection.getSection(id);
                Level level = Server.getInstance().getLevelByName(data.getString("world"));
                if (level == null) {
                    plugin.getLogger().warning("Мир '" + data.getString("world") + "' для арены '" + id + "' не найден! Арена пропущена.");
                    continue;
                }
                String[] spawnCoords = data.getString("spawn").split(",");
                Location spawn = new Location(Double.parseDouble(spawnCoords[0]), Double.parseDouble(spawnCoords[1]), Double.parseDouble(spawnCoords[2]), Double.parseDouble(spawnCoords[3]), Double.parseDouble(spawnCoords[4]), level);
                ConfigSection rules = data.getSection("rules");
                Arena arena = new Arena(id, TextFormat.colorize(data.getString("name")), data.getString("world"), spawn, parseKit(data.getStringList("kit")), rules.getBoolean("place-blocks"), rules.getBoolean("break-blocks"), rules.getInt("placed-blocks-decay-seconds"), rules.getBoolean("drop-items"), rules.getBoolean("pickup-items"));
                arenas.put(id, arena);
            } catch (Exception e) {
                plugin.getLogger().error("Ошибка загрузки арены '" + id + "'. Проверь конфиг. Ошибка: " + e.getMessage());
            }
        }
    }

    private List<Item> parseKit(List<String> kitStrings) {
        List<Item> kitItems = new ArrayList<>();
        Pattern enchPattern = Pattern.compile("\\{ench:\\[\\{id:(\\d+),lvl:(\\d+)\\}\\]\\}");
        for (String line : kitStrings) {
            String[] parts = line.split(":");
            String slot = null;
            if (parts.length > 3 && Arrays.asList("helmet", "chestplate", "leggings", "boots").contains(parts[0])) {
                slot = parts[0];
                parts = Arrays.copyOfRange(parts, 1, parts.length);
            }
            Item item = Item.get(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            if (line.contains("{ench:")) {
                Matcher matcher = enchPattern.matcher(line);
                if (matcher.find()) {
                    Enchantment e = Enchantment.get(Integer.parseInt(matcher.group(1)));
                    if(e != null) { e.setLevel(Integer.parseInt(matcher.group(2))); item.addEnchantment(e); }
                }
            }
            if (slot != null) {
                CompoundTag nbt = item.getNamedTag() == null ? new CompoundTag() : item.getNamedTag();
                nbt.putString("ffaslot", slot);
                item.setNamedTag(nbt);
            }
            kitItems.add(item);
        }
        return kitItems;
    }

    public void joinArena(Player player, String arenaId) {
        Arena arena = arenas.get(arenaId);
        if (arena == null) { player.sendMessage("§cАрена не найдена!"); return; }
        if (plugin.isPlayerInArena(player)) { leaveArena(player, false); }
        plugin.setPlayerArena(player, arena);
        player.teleport(arena.spawn);
        preparePlayer(player, arena);
        player.sendMessage("§aВы вошли на арену: " + arena.name);
    }
    
    public void leaveArena(Player player, boolean teleportToHub) {
        plugin.setPlayerArena(player, null);
        player.getInventory().clearAll();
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setLevel(20);
        if(teleportToHub) {
            Level hub = Server.getInstance().getLevelByName(plugin.getConfig().getString("main-hub-world", "world"));
            player.teleport(hub != null ? hub.getSafeSpawn() : Server.getInstance().getDefaultLevel().getSafeSpawn());
        }
    }

    public void preparePlayer(Player player, Arena arena) {
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setLevel(20);
        player.removeAllEffects();
        PlayerInventory inv = player.getInventory();
        inv.clearAll();
        for (Item item : arena.kit) {
            if (item.hasCompoundTag() && item.getNamedTag().contains("ffaslot")) {
                switch (item.getNamedTag().getString("ffaslot")) {
                    case "helmet": inv.setHelmet(item); break;
                    case "chestplate": inv.setChestplate(item); break;
                    case "leggings": inv.setLeggings(item); break;
                    case "boots": inv.setBoots(item); break;
                }
            } else { inv.addItem(item.clone()); }
        }
    }

    public Arena getArenaById(String id) { return arenas.get(id); }
    public Collection<Arena> getAllArenas() { return arenas.values(); }
}