package com.excrele.cheststacker;  // Our pro namespace—keeps code in the Excrele zone!

import org.bukkit.plugin.java.JavaPlugin;  // The base for all plugins—like the bedrock of your world.

/**
 * Hey, teen coder! This is the plugin's "main menu"—runs when the server starts it up.
 * It hooks up the listener (the click-watcher) so stacks can mega-merge in chests.
 * Modular: Setup here, magic in another file. Few files = less mess to debug!
 * Author: Excrele (com.excrele) – Stack like a pro, crash like never!
 */
public final class ChestStackerPlugin extends JavaPlugin {  // Extends JavaPlugin = "I'm a real plugin!"

    // Global max stack—change this number once, it rules everywhere. Like a world edit for limits.
    public static final int MAX_STACK_SIZE = 1000;

    @Override
    public void onEnable() {
        // Old line: saveDefaultConfig();  // This tried to load a config.yml— but we got none! Crash city.
        // Fixed: Skip it for now. No config needed (yet). Future? Add settings like "max_stack: 500".
        // (We'll make config.yml later if we want reloadable tweaks.)

        // Step 1: Hook the listener! This watches for inventory clicks and does the merge dance.
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);

        // Step 2: Shout to console—"We're live!" Check here for success.
        getLogger().info("Excrele's Chest Stacker fired up! Mega-stacks up to " + MAX_STACK_SIZE + " in chests & more. Go build!");
    }

    @Override
    public void onDisable() {
        // Cleanup party: Server shutting down? Say bye.
        // (Nothing fancy yet—no open tasks to close.)
        getLogger().info("Chest Stacker powering down. Keep those stacks epic!");
    }
}