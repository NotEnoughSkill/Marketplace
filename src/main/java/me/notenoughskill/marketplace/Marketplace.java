package me.notenoughskill.marketplace;

import me.notenoughskill.marketplace.DB.Database;
import me.notenoughskill.marketplace.commands.BlackMarketCommand;
import me.notenoughskill.marketplace.commands.MarketplaceCommand;
import me.notenoughskill.marketplace.commands.SellCommand;
import me.notenoughskill.marketplace.commands.TransactionsCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Marketplace extends JavaPlugin {
    private static Economy economy = null;

    private static final Map<UUID, ItemStack> playerData = new HashMap<>();

    @Override
    public void onEnable() {
        Database.initialize();

        if (!setupEconomy()) {
            getLogger().severe("Vault economy plugin not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new MarketplaceCommand(this), this);

        this.getCommand("sell").setExecutor(new SellCommand(this));
        this.getCommand("marketplace").setExecutor(new MarketplaceCommand(this));
        this.getCommand("blackmarket").setExecutor(new BlackMarketCommand(this));
        this.getCommand("transactions").setExecutor(new TransactionsCommand(this));
    }

    @Override
    public void onDisable() {
        try {
            if (Database.getConnection() != null && !Database.getConnection().isClosed()) {
                Database.getConnection().close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static Map<UUID, ItemStack> getPlayerData() {
        return playerData;
    }
}
