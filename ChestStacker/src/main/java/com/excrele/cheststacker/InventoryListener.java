package com.excrele.cheststacker;  // Locked to com.excrele—our dev namespace. Keeps code organized like sorted chests!

import org.bukkit.Material;  // For item/block types—like DIRT or CHEST. No ghosts here!
import org.bukkit.block.Block;  // Basic block stuff—grabbing the chest under your feet.
import org.bukkit.block.BlockState;  // Snapshot of a block's state (inv contents, etc.).
import org.bukkit.entity.Player;  // The player clicking—our hero!
import org.bukkit.event.EventHandler;  // "Hey server, run this on events!" sticker.
import org.bukkit.event.Listener;  // Makes us an event catcher—watches inventory drama.
import org.bukkit.event.inventory.InventoryClickEvent;  // Triggers on clicks/drags—our main stage!
import org.bukkit.event.inventory.InventoryType;  // Types like PLAYER or CHEST—helps filter.
import org.bukkit.inventory.Inventory;  // The open chest/barrel—full of loot!
import org.bukkit.inventory.ItemStack;  // Single item stack—like 64 dirt.

/**
 * Yo, young builder! This is the "watcher" that spots clicks in chests and merges stacks up to 1000.
 * Hold dirt, click a pile? It smooshes 'em! But your backpack? Stays 64-max—classic MC.
 * Modular: One file for events, easy to add "merge on hopper" later. No bloat!
 * Author: Excrele (com.excrele) – Stack huge, code simple. Let's build!
 */
public class InventoryListener implements Listener {  // Listener = "I'm ready for action!"

    // Max stack pulled from main plugin—one tweak rules all. Like a command block for sizes.
    private final int maxStackSize = ChestStackerPlugin.MAX_STACK_SIZE;

    // Plugin link: For future configs/perms. Teamwork without tangled wires.
    private final ChestStackerPlugin plugin;

    // Setup method: Called when main plugin activates us. "Lights, camera, clicks!"
    public InventoryListener(ChestStackerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Event superstar! Fires on every inv click (left, shift, drag— all of 'em).
     * Filters: Right spot? Same item? Merge city!
     */
    @EventHandler  // Bukkit tag: "Call me on clicks!" Clean, no old junk.
    public void onInventoryClick(InventoryClickEvent event) {
        // Step 1: Snag player & inv. Safety first—no crashes on weird server hiccups.
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        if (player == null || inventory == null) {
            return;  // Early out—like breaking a redstone line.
        }

        // Step 2: Perm gate—modular! Only powered users get mega-stacks.
        if (!player.hasPermission("cheststacker.use")) {
            return;  // No pass? Vanilla 64 it is. Fair game!
        }

        // Step 3: Player's pockets? Skip—keep hotbar/backpack normal.
        if (isPlayerInventory(inventory)) {
            return;  // Classic MC vibes only here!
        }

        // Step 4: Supported container? Chests/barrels—yes! Random anvil? Nope.
        if (!isSupportedContainer(inventory)) {
            return;  // Wrong inv—no party.
        }

        // Step 5: Items check. Cursor = held stuff; clicked = target slot.
        ItemStack cursorItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getSlot();

        // Step 6: Skip empties or mismatches (air? Tools? Bye!).
        if (cursorItem == null || cursorItem.getType().isAir() ||
                clickedItem == null || clickedItem.getType().isAir() ||
                !cursorItem.isSimilar(clickedItem)) {  // Same type? Dirt + dirt = yes!
            return;
        }

        // Step 7: Hijack the click—vanilla's 64-limit? Canceled!
        event.setCancelled(true);

        // Step 8: Merge math! Room left? Add min(held, room).
        int spaceLeft = maxStackSize - clickedItem.getAmount();
        int toAdd = Math.min(cursorItem.getAmount(), spaceLeft);

        if (toAdd > 0) {
            // Do the swap: Grow slot, shrink held.
            clickedItem.setAmount(clickedItem.getAmount() + toAdd);
            cursorItem.setAmount(cursorItem.getAmount() - toAdd);

            // Refresh inv—server updates the view. Like saving your world edit.
            inventory.setItem(slot, clickedItem);

            // Cursor cleanup: Gone? Clear. Leftover? Refresh.
            if (cursorItem.getAmount() <= 0) {
                event.setCursor(null);
            } else {
                event.setCursor(cursorItem);
            }

            // Victory chat: "Stacked X/1000 dirt!" Fun feedback.
            player.sendMessage("§aMega-merge! §e" + clickedItem.getAmount() + "/" + maxStackSize +
                    " §a" + clickedItem.getType().name().toLowerCase().replace("_", " ") + "§r loaded!");
        }
    }

    /**
     * Quick check: Player's own inv? (Hotbar, crafting grid—vanilla zone.)
     * Type peek—fast as a hopper!
     */
    private boolean isPlayerInventory(Inventory inventory) {
        return inventory.getType() == InventoryType.PLAYER ||  // Backpack/hotbar central.
                inventory.getHolder() instanceof Player;  // Player-run grids too.
    }

    /**
     * VIP list: Chests, barrels, shulkers? Green light!
     * Modular: Add `|| type == Material.YOUR_NEW_BLOCK` easy.
     */
    private boolean isSupportedContainer(Inventory inventory) {
        // Ender special: No block, but type screams "yes!"
        if (inventory.getType() == InventoryType.ENDER_CHEST) {
            return true;  // Your personal mega-vault—stack ender-style!
        }

        // Block hunt: Get the thing you're opening.
        Block block = getBlockFromInventory(inventory);
        if (block == null) {
            return false;
        }

        Material type = block.getType();  // What's this block? CHEST? BARREL?

        // Whitelist party: Regular, trapped, barrels, shulkers. No ghosts!
        return type == Material.CHEST || type == Material.TRAPPED_CHEST ||
                type == Material.BARREL || isShulkerBox(type);
    }

    /**
     * Block grabber: From inv holder to real-world block. Double chests? Grabs one—inv sees both.
     */
    private Block getBlockFromInventory(Inventory inventory) {
        if (!(inventory.getHolder() instanceof BlockState)) {
            return null;  // Not block-based? (Like ender.) No prob.
        }
        return ((BlockState) inventory.getHolder()).getBlock();
    }

    /**
     * Shulker scanner: Any color? "_SHULKER_BOX" in name? Portable mega-stacks unlocked!
     */
    private boolean isShulkerBox(Material material) {
        return material.toString().endsWith("_SHULKER_BOX");  // White, black—fits all!
    }
}