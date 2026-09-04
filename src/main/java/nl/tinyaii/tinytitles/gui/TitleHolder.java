package nl.tinyaii.tinytitles.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * 称号选择 GUI 标识。
 */
public class TitleHolder implements InventoryHolder {

    public final UUID owner;

    public TitleHolder(UUID owner) {
        this.owner = owner;
    }

    @Override
    public Inventory getInventory() { return null; }
}
