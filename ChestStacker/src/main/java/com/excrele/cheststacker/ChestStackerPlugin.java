package com.excrele.cheststacker;  // com.excrele all the way—pro namespace!

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin brain: Starts us up! Registers listener for click magic.
 * Modular: Setup here, events elsewhere. Few files = quick tweaks.
 * Author: Excrele (com.excrele) – Chest stacking pro!
 */
public final class ChestStackerPlugin extends JavaPlugin {

    public static final int MAX_STACK_SIZE = 1000;  // One-stop shop for stack limits.

    @Override
    public void onEnable() {
        saveDefaultConfig();  // Config ready if we add one.

        // Plug in the listener—now it watches all clicks!
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);

        getLogger().info("Excrele's Chest Stacker online! Mega-stacks up to " + MAX_STACK_SIZE + ". Build wild!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Chest Stacker signing off. Happy stacking!");
    }
}