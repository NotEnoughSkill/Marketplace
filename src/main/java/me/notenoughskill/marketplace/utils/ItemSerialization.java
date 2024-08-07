package me.notenoughskill.marketplace.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class ItemSerialization {

    public static String serializeItemStack(ItemStack item) {
        if (item == null) return null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream bo = new BukkitObjectOutputStream(baos);
            bo.writeObject(item);
            bo.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack deserializeItemStack(String base64) {
        if (base64 == null || base64.isEmpty()) return null;

        try {
            byte[] data = Base64.getDecoder().decode(base64);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            BukkitObjectInputStream bo = new BukkitObjectInputStream(bais);
            return (ItemStack) bo.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
