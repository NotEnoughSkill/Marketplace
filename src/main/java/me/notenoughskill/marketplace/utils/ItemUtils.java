package me.notenoughskill.marketplace.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {
    public static ItemStack addLore(ItemStack item, String... lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        List<String> loreList = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if (loreList == null) loreList = new ArrayList<>();

        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);

        return item;
    }
}
