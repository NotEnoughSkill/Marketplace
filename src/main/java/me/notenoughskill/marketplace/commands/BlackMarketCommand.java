package me.notenoughskill.marketplace.commands;

import me.notenoughskill.marketplace.DB.Database;
import me.notenoughskill.marketplace.Marketplace;
import me.notenoughskill.marketplace.utils.ItemSerialization;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BlackMarketCommand implements CommandExecutor {
    private final Marketplace plugin;

    public BlackMarketCommand(Marketplace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player)sender;
        Inventory blackMarketInventory = Bukkit.createInventory(null, 54, "Black Market");

        List<ItemStack> items = new ArrayList<>();
        List<Double> prices = new ArrayList<>();
        List<UUID> sellers = new ArrayList<>();

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM marketplace_items");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String serializedItem = resultSet.getString("item");
                double price = resultSet.getDouble("price");
                UUID seller = UUID.fromString(resultSet.getString("player_uuid"));

                items.add(ItemSerialization.deserializeItemStack(serializedItem));
                prices.add(price);
                sellers.add(seller);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Random random = new Random();
        for (int i = 0; i < items.size() && i < 27; i++) {
            int index = random.nextInt(items.size());
            ItemStack item = items.get(index);
            double price = prices.get(index) * 0.5;
            UUID seller = sellers.get(index);

            try (Connection connection = Database.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM marketplace_items WHERE player_uuid = ? AND item = ? AND price = ?");
                statement.setString(1, seller.toString());
                statement.setString(2, ItemSerialization.serializeItemStack(item));
                statement.setDouble(3, prices.get(index));
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            items.remove(index);
            prices.remove(index);
            sellers.remove(index);
        }

        player.openInventory(blackMarketInventory);
        return true;
    }
}
