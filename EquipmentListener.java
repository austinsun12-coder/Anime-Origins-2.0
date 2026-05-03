package com.animeorigins.gui;

import com.animeorigins.AnimeOriginsPlugin;
import com.animeorigins.Origin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class OriginGUI {

    public static final int PREV_PAGE_SLOT = 45;
    public static final int NEXT_PAGE_SLOT = 53;
    public static final int BACK_SLOT = 49;

    private static final int PAGE_SIZE = 45; // slots 0-44 for origins
    private static final int ORIGINS_PER_ROW = 9;

    private final AnimeOriginsPlugin plugin;
    private final Set<Inventory> guiInventories = new HashSet<>();

    // Per-player tracking
    private final Map<UUID, Integer> playerPage = new HashMap<>();
    private final Map<UUID, Map<Integer, Origin>> slotOriginMap = new HashMap<>();
    private final Map<UUID, Origin> pendingSelection = new HashMap<>();
    private final Map<UUID, Boolean> onConfirmPage = new HashMap<>();

    public OriginGUI(AnimeOriginsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openSelectionGUI(Player player) {
        onConfirmPage.put(player.getUniqueId(), false);
        int page = playerPage.getOrDefault(player.getUniqueId(), 0);
        buildSelectionPage(player, page);
    }

    private void buildSelectionPage(Player player, int page) {
        Origin[] origins = Origin.values();
        int totalPages = (int) Math.ceil(origins.length / (double) 9); // 9 per page for nice layout

        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text("✦ Choose Your Origin — Page " + (page + 1) + "/" + totalPages, NamedTextColor.DARK_PURPLE));
        guiInventories.add(inv);

        Map<Integer, Origin> slotMap = new HashMap<>();

        // Layout: 3 rows of 3 origins each (centered)
        int[] displaySlots = {10, 12, 14, 19, 21, 23, 28, 30, 32, 4}; // up to 10 per page
        int startIdx = page * 9;

        for (int i = 0; i < 9 && (startIdx + i) < origins.length; i++) {
            Origin origin = origins[startIdx + i];
            int slot = displaySlots[i];
            inv.setItem(slot, buildOriginItem(origin, false));
            slotMap.put(slot, origin);
        }

        slotOriginMap.put(player.getUniqueId(), slotMap);

        // Navigation
        if (page > 0) {
            inv.setItem(PREV_PAGE_SLOT, buildNavItem(Material.ARROW, "§e← Previous Page"));
        }
        if (startIdx + 9 < origins.length) {
            inv.setItem(NEXT_PAGE_SLOT, buildNavItem(Material.ARROW, "§eNext Page →"));
        }

        // Border
        fillBorder(inv);

        player.openInventory(inv);
    }

    public void openConfirmPage(Player player, Origin origin) {
        pendingSelection.put(player.getUniqueId(), origin);
        onConfirmPage.put(player.getUniqueId(), true);

        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text("✦ Confirm: " + stripColor(origin.getDisplayName()), NamedTextColor.GOLD));
        guiInventories.add(inv);

        Map<Integer, Origin> slotMap = new HashMap<>();

        // Show origin item in center
        inv.setItem(22, buildOriginItem(origin, true));
        slotMap.put(22, origin);

        // Confirm button
        ItemStack confirm = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta cm = confirm.getItemMeta();
        cm.displayName(Component.text("§a§l✔ CONFIRM — Play as " + stripColor(origin.getDisplayName())));
        List<Component> cLore = new ArrayList<>();
        cLore.add(Component.text("§7Click to lock in your origin."));
        cLore.add(Component.text("§c§lThis cannot be undone!"));
        cm.lore(cLore);
        confirm.setItemMeta(cm);
        inv.setItem(38, confirm);
        slotMap.put(38, origin);

        // Cancel button
        ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
        ItemMeta xm = cancel.getItemMeta();
        xm.displayName(Component.text("§c§l✘ Go Back"));
        cancel.setItemMeta(xm);
        inv.setItem(42, cancel);

        fillBorder(inv);
        slotOriginMap.put(player.getUniqueId(), slotMap);

        // Back slot is 42 for confirm page
        player.openInventory(inv);
    }

    private ItemStack buildOriginItem(Origin origin, boolean enhanced) {
        ItemStack item = new ItemStack(origin.getIcon());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Display name
        meta.displayName(Component.text(stripColor(origin.getDisplayName()))
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(origin.getSeries()).decoration(TextDecoration.ITALIC, true).color(NamedTextColor.GRAY));
        lore.add(Component.empty());

        for (String ability : origin.getAbilities()) {
            // Strip Minecraft color codes for component serialization
            lore.add(LegacyComponentSerializer.legacySection().deserialize(ability)
                    .decoration(TextDecoration.ITALIC, false));
        }

        if (!enhanced) {
            lore.add(Component.empty());
            lore.add(Component.text("§e▶ Click to preview").decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        if (enhanced) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildNavItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name)
                    .decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBorder(Inventory inv) {
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        if (bm != null) {
            bm.displayName(Component.text(" "));
            border.setItemMeta(bm);
        }

        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i) == null) {
                // Fill bottom and top rows
                if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                    inv.setItem(i, border);
                }
            }
        }
    }

    public void nextPage(Player player) {
        int page = playerPage.getOrDefault(player.getUniqueId(), 0) + 1;
        playerPage.put(player.getUniqueId(), page);
        buildSelectionPage(player, page);
    }

    public void previousPage(Player player) {
        int page = Math.max(0, playerPage.getOrDefault(player.getUniqueId(), 0) - 1);
        playerPage.put(player.getUniqueId(), page);
        buildSelectionPage(player, page);
    }

    public boolean isGuiInventory(Inventory inv) {
        return guiInventories.contains(inv);
    }

    public Origin getOriginAtSlot(Player player, int slot) {
        Map<Integer, Origin> map = slotOriginMap.get(player.getUniqueId());
        if (map == null) return null;
        return map.get(slot);
    }

    public boolean isOnConfirmPage(Player player) {
        return onConfirmPage.getOrDefault(player.getUniqueId(), false);
    }

    private String stripColor(String s) {
        return s.replaceAll("§[0-9a-fklmnor]", "");
    }
}
