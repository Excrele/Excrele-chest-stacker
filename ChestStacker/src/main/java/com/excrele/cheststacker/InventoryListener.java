package com.excrele.cheststacker;  // Excrele squad HQ—our code home base!

import org.bukkit.Material;  // Item types—like DIRT for that farm life.
import org.bukkit.block.Block;  // Blocks in the world—chests you build!
import org.bukkit.block.BlockState;  // Block freeze-frame—holds inv secrets.
import org.bukkit.entity.Player;  // You, the player—clicking hero!
import org.bukkit.event.EventHandler;  // "Server, call me on clicks!" Like a bell ringer.
import org.bukkit.event.Listener;  // Event spy—catches all the action.
import org.bukkit.event.inventory.ClickType;  // Click flavors—left, right, shift.
import org.bukkit.event.inventory.InventoryClickEvent;  // Every tap in inv—our big stage!
import org.bukkit.event.inventory.InventoryType;  // Inv styles—CHEST vs PLAYER.
import org.bukkit.inventory.Inventory;  // The open box—slots full of goodies.
import org.bukkit.inventory.ItemStack;  // One stack—like 999 cobble.
import org.bukkit.inventory.meta.ItemMeta;  // Item's "ID card"—names, lores for tooltips!

/**
 * Hey, young dev! This is the click wizard—merges stacks huge in chests, now with hover tips ("Merged: 512!")
 * and splits (right-click for half, left for full—but pockets get max 64). Like a smart chest that whispers secrets.
 * Modular: Helpers for lore/split—one job per method, easy tweaks. Debug? Spy lines for testing.
 * Author: Excrele (com.excrele) – Tooltips pop, splits rock. Stack smart!
 */
public class InventoryListener implements Listener {  // Listener = "Ready for clicks!"

    // Max mega-size from main—one tweak for all. Dream big: 1000+?
    private final int maxStackSize = ChestStackerPlugin.MAX_STACK_SIZE;

    // Plugin pal: For logs (spy mode) or future configs.
    private final ChestStackerPlugin plugin;

    // Startup: Main plugin builds us—"Go watch those clicks, kid!"
    public InventoryListener(ChestStackerPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Click central! Every poke in inv (chest/player) hits here. We sort, merge, split—like a redstone sorter.
     */
    @EventHandler  // Fresh tag for 1.21— "Run on clicks!"
    public void onInventoryClick(InventoryClickEvent event) {
        // Step 1: Player & top inv (chest). Null? Weird—bail!
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();  // Chest you're opening.
        if (player == null || inventory == null) {
            return;  // Safety net—like a fence around lava.
        }

        // Debug spy: What's happening? (Comment these for quiet mode.)
        // plugin.getLogger().info("§e[Debug] Click! Inv: " + inventory.getName() + " | Type: " + inventory.getType() + " | Click: " + event.getClick() + " | Slot: " + event.getSlot());

        // Step 2: No perm? Vanilla only—no mega fun.
        if (!player.hasPermission("cheststacker.use")) {
            // plugin.getLogger().info("§e[Debug] No perm—vanilla city.");
            return;
        }

        // Step 3: Player pockets? Skip—64-max forever.
        if (isPlayerInventory(inventory)) {
            // plugin.getLogger().info("§e[Debug] Player inv—classic rules.");
            return;
        }

        // Step 4: Supported chest-like? Yes—merge/split time!
        if (!isSupportedContainer(inventory)) {
            // Block block = getBlockFromInventory(inventory);
            // plugin.getLogger().info("§e[Debug] Not supported: " + (block != null ? block.getType() : "No block"));
            return;
        }
        // plugin.getLogger().info("§e[Debug] Container OK! Action time...");

        // Step 5: Click type branch—shift for quick-merge, left/right for split/pull.
        ClickType click = event.getClick();
        if (click.isShiftClick()) {
            handleShiftClick(event, player, inventory);  // Quick-move to chest—merge smart.
        } else if (click == ClickType.LEFT || click == ClickType.RIGHT) {  // Pull/split from chest.
            handlePullOrSplit(event, player, inventory);
        } else {
            // Other clicks (middle, drop)? Vanilla for now—keeps it simple.
            // plugin.getLogger().info("§e[Debug] Other click—vanilla handles.");
        }
    }

    /**
     * Shift-click: Zip from pockets to chest—merge to existing or new stack (up to 1000).
     * Adds lore tooltip after!
     */
    private void handleShiftClick(InventoryClickEvent event, Player player, Inventory topInv) {
        // Only shifts from player side (slots 0-35 = inv/hotbar).
        if (event.getRawSlot() >= topInv.getSize() || event.getRawSlot() < 0) {  // Bottom half = player.
            return;  // Shift from chest to player? Vanilla (we cap below if needed).
        }

        ItemStack item = event.getCurrentItem();  // Your stack—e.g., 64 wood.
        if (item == null || item.getType().isAir()) {
            return;  // Empty? No action.
        }

        event.setCancelled(true);  // Vanilla pause—we drive!
        // plugin.getLogger().info("§e[Debug] Shift: " + item.getType() + " x" + item.getAmount());

        int added = 0;  // Score: How much we packed?

        // A: Merge to same-item slots with room.
        for (int slot = 0; slot < topInv.getSize(); slot++) {
            ItemStack slotItem = topInv.getItem(slot);
            if (slotItem != null && slotItem.isSimilar(item) && slotItem.getAmount() < maxStackSize) {
                int space = maxStackSize - slotItem.getAmount();
                int addHere = Math.min(space, item.getAmount() - added);
                slotItem.setAmount(slotItem.getAmount() + addHere);
                addLore(slotItem);  // Secret note: "Merged: X/1000" for hover!
                topInv.setItem(slot, slotItem);
                added += addHere;
                // plugin.getLogger().info("§e[Debug] Shift merge: +" + addHere + " to slot " + slot);
                if (added >= item.getAmount()) break;
            }
        }

        // B: Leftovers? New stack in empty slot (up to 1000).
        if (added < item.getAmount()) {
            for (int slot = 0; slot < topInv.getSize(); slot++) {
                if (topInv.getItem(slot) == null || topInv.getItem(slot).getType().isAir()) {
                    int remaining = item.getAmount() - added;
                    int placeMax = Math.min(remaining, maxStackSize);
                    ItemStack newStack = item.clone();
                    newStack.setAmount(placeMax);
                    addLore(newStack);  // Tooltip magic here too!
                    topInv.setItem(slot, newStack);
                    added += placeMax;
                    // plugin.getLogger().info("§e[Debug] New shift stack: " + placeMax + " in " + slot);
                    if (added >= item.getAmount()) break;
                }
            }
        }

        // C: Update player inv—remove what we took (leftover stays).
        ItemStack leftover = item.clone();
        leftover.setAmount(item.getAmount() - added);
        player.getInventory().setItem(event.getSlot(), leftover.getAmount() > 0 ? leftover : null);

        // Win message: "Packed 128 wood!"
        if (added > 0) {
            player.sendMessage("§aShift-packed! §e+" + added + " §a" + item.getType().name().toLowerCase().replace("_", " ") + " §ainto chest§r");
        }
    }

    /**
     * Left/right click: Pull whole/half from chest stack to cursor/player.
     * Caps taken at 64 (vanilla pockets), updates remaining with lore.
     */
    private void handlePullOrSplit(InventoryClickEvent event, Player player, Inventory topInv) {
        ItemStack clickedItem = event.getCurrentItem();  // Chest stack—e.g., 200 stone.
        int slot = event.getSlot();  // Chest slot number.
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;  // Empty? Vanilla.
        }

        event.setCancelled(true);  // We control the pull!
        // plugin.getLogger().info("§e[Debug] Pull/split: " + clickedItem.getType() + " x" + clickedItem.getAmount() + " | Click: " + event.getClick());

        int takeAmount;
        if (event.getClick() == ClickType.LEFT) {
            // Left: Grab whole—but cap at 64 for cursor (pockets rule).
            takeAmount = Math.min(64, clickedItem.getAmount());
        } else {  // Right: Split half—but cap at 64.
            takeAmount = Math.min(32, clickedItem.getAmount() / 2);  // Half, floored, capped.
        }

        if (takeAmount <= 0) {
            return;  // Nothing to grab? Done.
        }

        // Shrink chest stack.
        int remaining = clickedItem.getAmount() - takeAmount;
        clickedItem.setAmount(remaining);
        if (remaining > 0) {
            addLore(clickedItem);  // Update tooltip: "Merged: 168/1000" on leftover!
            topInv.setItem(slot, clickedItem);
        } else {
            topInv.setItem(slot, null);  // Empty? Clear slot.
        }

        // New stack for cursor—cloned, no lore (vanilla feel in hand).
        ItemStack taken = clickedItem.clone();
        taken.setAmount(takeAmount);
        // Clear any old lore—pockets stay plain.
        ItemMeta meta = taken.getItemMeta();
        if (meta != null) {
            meta.setLore(null);  // No secrets in your hand!
            taken.setItemMeta(meta);
        }
        event.setCursor(taken);

        // Chat flex: "Grabbed 64 stone (168 left)!"
        String itemName = clickedItem.getType().name().toLowerCase().replace("_", " ");
        player.sendMessage("§aSplit-pulled! §e" + takeAmount + " §a" + itemName +
                (remaining > 0 ? " (§e" + remaining + " left§a)" : "") + "§r");
        // plugin.getLogger().info("§e[Debug] Pulled " + takeAmount + " | Left: " + remaining);
    }

    /**
     * Helper: Add hover tooltip lore—"§7Merged: 256/1000" (gray text, cool!).
     * Like a sticky note on the item—shows when you mouse over in inv.
     */
    private void addLore(ItemStack item) {
        if (item.getAmount() <= 64) return;  // Vanilla size? No note needed.

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = item.getItemMeta() != null ? item.getItemMeta().clone() : item.getItemMeta();
            item.setItemMeta(meta);
        }
        if (meta != null) {
            meta.setLore(java.util.Arrays.asList("§7Merged: §f" + item.getAmount() + "/" + maxStackSize));
            item.setItemMeta(meta);  // Stick it on—hover sees it!
        }
        // plugin.getLogger().info("§e[Debug] Added lore to " + item.getType() + ": " + item.getAmount());
    }

    /**
     * Helper: Player's backpack/hotbar? Yes—skip mega stuff.
     */
    private boolean isPlayerInventory(Inventory inventory) {
        return inventory.getType() == InventoryType.PLAYER || inventory.getHolder() instanceof Player;
    }

    /**
     * Helper: Chest fam? Whitelist for merges/splits.
     */
    private boolean isSupportedContainer(Inventory inventory) {
        if (inventory.getType() == InventoryType.ENDER_CHEST) return true;  // Personal mega!

        Block block = getBlockFromInventory(inventory);
        if (block == null) return false;

        Material type = block.getType();
        return type == Material.CHEST || type == Material.TRAPPED_CHEST ||
                type == Material.BARREL || isShulkerBox(type);
    }

    /**
     * Helper: Inv to block—easy peek.
     */
    private Block getBlockFromInventory(Inventory inventory) {
        if (!(inventory.getHolder() instanceof BlockState)) return null;
        return ((BlockState) inventory.getHolder()).getBlock();
    }

    /**
     * Shulker check: Portable box? Yes if ends with _SHULKER_BOX.
     */
    private boolean isShulkerBox(Material material) {
        return material.toString().endsWith("_SHULKER_BOX");
    }
}