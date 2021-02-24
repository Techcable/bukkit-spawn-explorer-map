package net.techcable.bukkit.spawn_explorer_map;

import java.util.*;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExplorerMap extends JavaPlugin {

    private static final List<String> structureNames() {
        List<String> result = new ArrayList<>();
        for (StructureType st : StructureType.getStructureTypes().values()) {
            result.add(st.getName().replace('_', '-'));
        }
        return result;
    }
    private static final StructureType parseStructure(String name) {
        StructureType result = StructureType.getStructureTypes().get(name);
        if (result != null) return result;
        // Backup fuzzy match
        name = name.replace('-', '_');
        for (String key : StructureType.getStructureTypes().keySet()) {
            String originalKey = key;
            int idx = key.lastIndexOf('.');
            if (idx >= 0) {
                key = key.substring(idx, key.length());
            }
            if (name.equalsIgnoreCase(key)) {
                return parseStructure(originalKey); // Parse from *exact* name
            }
        }
        return null;
    }

    @Override
    public void onEnable() {
        String enabledStructuresTxt;
        {
            StringBuilder builder = new StringBuilder();
            List<String> list = structureNames();
            for (int i = 0; i < list.size(); i++) {
                if (i != 0) builder.append(", ");
                builder.append(list.get(i));
            }
            enabledStructuresTxt = builder.toString();
        }
        this.getLogger().info("Enabled structures: " + enabledStructuresTxt);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equals("spawnExplorerMap")) {
            List<String> result;
            switch (args.length) {
                case 0:
                    result = ExplorerMap.structureNames();
                    break;
                case 1:
                    // complete players
                    result = new ArrayList<>();
                    for (Player p : getServer().getOnlinePlayers()) {
                        result.add(p.getName());
                    }
                case 2: // how would we auto-complete integers?
                default:
                    return null; // too many args
            }
            if (result.isEmpty()) return result;
            String lastArg = args[args.length - 1];
            {
                // Iterate backwards, removing failed matches
                // This is how we filtered lists in the dark ages
                for (int index = result.size() - 1; index >= 0; index--) {
                    String potentialMatch = result.get(index);
                    // Logically this is potentialMatch.startsWith(lastArg)
                    // However, we also want to ignore case so we use 'regionMatches'
                    boolean shouldRemove = potentialMatch.regionMatches(
                        /* ignore case */ true,
                        /* this offset */ 0,
                        /* target */ lastArg,
                        /* target offset */ 0,
                        potentialMatch.length()
                    );
                    if (shouldRemove) {
                        // Avoids O(n^2) because this is final element
                        result.remove(index);
                    }
                }
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("spawnExplorerMap")) {
            if (args.length == 0) {
                sender.sendMessage("Insufficient arguments!");
                return false; // display usage
            }
            int desiredAmount = 1;
            final Player target;
            final StructureType desiredType = parseStructure(args[0]);
            if (desiredType == null) {
                sender.sendMessage("Unknown structure type: " + args[0]);
                return false;
            }
            switch (args.length) {
                case 0:
                    throw new AssertionError();
                case 1:
                    if (sender instanceof Player) {
                        target = (Player) sender;
                        if (!sender.hasPermission("spawn_explorer_map.spawn")) {
                            sender.sendMessage("Insufficeint permissions");
                            return true; // Handled
                        }
                    } else {
                        sender.sendMessage("Can't give explorer map to a non-player");
                        return false; // False
                    }
                    break;
                case 3:
                    try {
                        desiredAmount = Integer.parseInt(args[2]);
                        // Forbid negatives too
                        if (desiredAmount < 1) throw new NumberFormatException();
                    } catch (NumberFormatException ignored) {
                        sender.sendMessage("Invalid number of maps to give: " + args[2]);
                        return false; // Show help
                    }
                    // fallthrough
                case 2:
                    String targetName = args[1];
                    if (!sender.hasPermission("spawn_explorer_map.spawn.others")) {
                        sender.sendMessage("Insufficient permissions to spawn for others");
                        return true; // Handled
                    }
                    target = Bukkit.getPlayer(targetName);
                    if (target == null) {
                        sender.sendMessage("Unable to find player: " + args[1]);
                    }
                    break;
                default:
                    sender.sendMessage("Incorrect number of arguments");
                    return false; // Display help
            }
            assert desiredAmount >= 1;
            assert target != null;
            assert desiredType != null;
            PlayerInventory inventory = target.getInventory();
            sender.sendMessage("Giving " + desiredAmount + " explorer maps to " + target.getName());
            for (int i = 0; i < desiredAmount; i++) {
                ItemStack stack = getServer().createExplorerMap(target.getWorld(), target.getLocation(), desiredType);
                Map<Integer, ItemStack> failedToPlace = inventory.addItem(stack);
                if (!failedToPlace.isEmpty()) {
                    sender.sendMessage("Insufficient room to place #" + (i + 1) + " in inventory of " + target.getName());
                    return true;
                }
            }
            return true; // handled :)
        }
        return false;
    }
}
