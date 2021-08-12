package com.willfp.reforges.reforges.util;

import com.willfp.eco.core.EcoPlugin;
import com.willfp.reforges.ReforgesPlugin;
import com.willfp.reforges.gui.ReforgeStatus;
import com.willfp.reforges.reforges.Reforge;
import com.willfp.reforges.reforges.Reforges;
import com.willfp.reforges.reforges.meta.ReforgeTarget;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class ReforgeUtils {
    /**
     * Instance of StatTrackers.
     */
    private static final EcoPlugin PLUGIN = ReforgesPlugin.getInstance();

    /**
     * The key for storing the currently displayed stat.
     */
    private static final NamespacedKey REFORGE_KEY = PLUGIN.getNamespacedKeyFactory().create("reforge");

    /**
     * Get a random reforge for a target.
     *
     * @param target The target.
     */
    public static Reforge getRandomReforge(@NotNull final ReforgeTarget target) {
        List<Reforge> applicable = new ArrayList<>();

        for (Reforge reforge : Reforges.values()) {
            if (reforge.getTarget().equals(target)) {
                applicable.add(reforge);
            }
        }

        Collections.shuffle(applicable);

        if (applicable.isEmpty()) {
            return null;
        }

        return applicable.get(0);
    }

    public static ReforgeStatus getStatus(@NotNull final List<ItemStack> captive) {
        ItemStack toReforge = captive.isEmpty() ? null : captive.get(0);
        ReforgeStatus status = null;

        ReforgeTarget target = null;

        if (toReforge == null) {
            status = ReforgeStatus.NO_ITEM;
        } else {
            target = ReforgeTarget.getForMaterial(toReforge.getType());
            if (target == null) {
                status = ReforgeStatus.INVALID_ITEM;
            }
        }

        if (target != null) {
            status = ReforgeStatus.ALLOW;
        }

        return status;
    }

    /**
     * Get reforge on an item.
     *
     * @param item The item to query.
     * @return The found reforge, or null if none active.
     */
    public static Reforge getReforge(@Nullable final ItemStack item) {
        if (item == null) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return null;
        }

        return getReforge(meta);
    }

    /**
     * Get reforge on an item.
     *
     * @param meta The item to query.
     * @return The found reforge, or null if none active.
     */
    public static Reforge getReforge(@Nullable final ItemMeta meta) {
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(REFORGE_KEY, PersistentDataType.STRING)) {
            return null;
        }

        String active = container.get(REFORGE_KEY, PersistentDataType.STRING);

        return Reforges.getByKey(active);
    }

    /**
     * Set reforge on an item.
     *
     * @param item    The item.
     * @param reforge The reforge.
     */
    public static void setReforge(@NotNull final ItemStack item,
                                  @NotNull final Reforge reforge) {

        if (item.getItemMeta() == null) {
            return;
        }

        Reforge previous = getReforge(item);

        if (previous != null) {
            previous.handleRemoval(item);
        }

        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(REFORGE_KEY, PersistentDataType.STRING, reforge.getKey());

        item.setItemMeta(meta);

        reforge.handleApplication(item);
    }
}
