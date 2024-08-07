package me.notenoughskill.marketplace.commands;

import me.notenoughskill.marketplace.DB.Database;
import me.notenoughskill.marketplace.Marketplace;
import me.notenoughskill.marketplace.utils.ItemSerialization;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MarketplaceCommand implements CommandExecutor, Listener {
    private final Marketplace plugin;

    public MarketplaceCommand(Marketplace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        Inventory marketplaceInventory = Bukkit.createInventory(null, 54, "Marketplace");

        try (Connection connection = Database.getConnection()) {
            if (connection == null) {
                player.sendMessage(ChatColor.RED + "Failed to connect to the database.");
                return true;
            }

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM marketplace_items");
            ResultSet resultSet = statement.executeQuery();
            int slot = 0;

            while (resultSet.next()) {
                String serializedItem = resultSet.getString("item");
                double price = resultSet.getDouble("price");

                Bukkit.getLogger().info("Serialized Item: " + serializedItem);

                ItemStack item = ItemSerialization.deserializeItemStack(serializedItem);

                if (item != null) {
                    Bukkit.getLogger().info("Deserialized Item: " + item.toString());
                } else {
                    Bukkit.getLogger().warning("Failed to deserialize item.");
                }

                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.getLore();
                        if (lore != null) {
                            lore.add(ChatColor.GREEN + "Price: " + ChatColor.GOLD + price + " coins");
                            meta.setLore(lore);
                        } else {
                            meta.setLore(List.of(ChatColor.GREEN + "Price: " + ChatColor.GOLD + price + " coins"));
                        }

                        item.setItemMeta(meta);
                    }
                    marketplaceInventory.setItem(slot++, item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        player.openInventory(marketplaceInventory);
        return true;
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Marketplace")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                openConfirmationGUI(player, clickedItem);
            }
        } else if (event.getView().getTitle().equals("Confirm Purchase")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                if (clickedItem.getType() == Material.GREEN_WOOL) {
                    ItemStack item = Marketplace.getPlayerData().get(player.getUniqueId());

                    if (item != null) {
                        double price = getPriceFromItem(item);
                        Economy economy = Marketplace.getEconomy();

                        if (economy.getBalance(player) >= price) {
                            economy.withdrawPlayer(player, price);

                            removeItemFromDatabase(item);

                            player.getInventory().addItem(item);
                            player.sendMessage(ChatColor.GREEN + "Purchase successful!");
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have enough money!");
                        }

                        player.closeInventory();
                    }
                } else if (clickedItem.getType() == Material.RED_WOOL) {
                    player.closeInventory();
                }
            }
        }
    }


    private double getPriceFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.getLore() != null) {
            for (String lore : meta.getLore()) {
                if (lore.startsWith(ChatColor.GREEN + "Price: ")) {
                    String priceString = ChatColor.stripColor(lore.split(": ")[1]);
                    try {
                        String numericPart = priceString.replaceAll("[^\\d.]", "");
                        return Double.parseDouble(numericPart);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return 0;
    }

    private void removeItemFromDatabase(ItemStack item) {
        try (Connection connection = Database.getConnection()) {
            if (connection != null) {
                String serializedItem = ItemSerialization.serializeItemStack(item);

                // Debug: Log serialized item
                Bukkit.getLogger().info("Removing item: " + serializedItem);

                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM marketplace_items WHERE item = ?"
                );
                statement.setString(1, serializedItem);

                int rowsAffected = statement.executeUpdate();

                // Debug: Check if item was removed
                if (rowsAffected > 0) {
                    Bukkit.getLogger().info("Item removed successfully.");
                } else {
                    Bukkit.getLogger().info("No item found with the serialized data.");
                }
            } else {
                Bukkit.getLogger().warning("Database connection is null.");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("SQL error while removing item: " + e.getMessage());
        }
    }


    private void openConfirmationGUI(Player player, ItemStack item) {
        Inventory confirmationInventory = Bukkit.createInventory(null, 27, "Confirm Purchase");

        ItemStack acceptItem = new ItemStack(Material.GREEN_WOOL);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        if (acceptMeta != null) {
            acceptMeta.setDisplayName(ChatColor.GREEN + "Accept");
            acceptItem.setItemMeta(acceptMeta);
        }

        ItemStack declineItem = new ItemStack(Material.RED_WOOL);
        ItemMeta declineMeta = declineItem.getItemMeta();
        if (declineMeta != null) {
            declineMeta.setDisplayName(ChatColor.RED + "Decline");
            declineItem.setItemMeta(declineMeta);
        }

        confirmationInventory.setItem(11, acceptItem);
        confirmationInventory.setItem(15, declineItem);

        player.openInventory(confirmationInventory);

        plugin.getPlayerData().put(player.getUniqueId(), item);
    }
}