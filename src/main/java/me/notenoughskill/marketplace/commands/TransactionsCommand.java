package me.notenoughskill.marketplace.commands;

import me.notenoughskill.marketplace.DB.Database;
import me.notenoughskill.marketplace.Marketplace;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionsCommand implements CommandExecutor {
    private final Marketplace plugin;

    public TransactionsCommand(Marketplace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player)sender;
        player.sendMessage(ChatColor.GREEN + "Your Transactions History:");

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM transaction_history WHERE player_uuid = ?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String type = resultSet.getString("type");
                double amount = resultSet.getDouble("amount");
                String item = resultSet.getString("item");
                player.sendMessage(ChatColor.GOLD + type + ChatColor.WHITE + ": " + amount + " coins for " + item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}
