package me.notenoughskill.marketplace.commands;

import me.notenoughskill.marketplace.DB.Database;
import me.notenoughskill.marketplace.Marketplace;
import me.notenoughskill.marketplace.utils.ItemSerialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

public class SellCommand implements CommandExecutor {
    private final Marketplace plugin;

    public SellCommand(Marketplace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /sell <price>");
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid price.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must hold an item in your hand to sell.");
            return true;
        }

        String serializedItem = ItemSerialization.serializeItemStack(item);

        try (Connection connection = Database.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("INSERT INTO marketplace_items (player_uuid, item, price) VALUES (?, ?, ?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, serializedItem);
            statement.setDouble(3, price);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Clear the item from the player's hand after listing
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(ChatColor.GREEN + "Item listed for sale at " + ChatColor.GOLD + price + ChatColor.GREEN + " coins.");
        return true;
    }
}

