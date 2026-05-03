package com.animeorigins.tasks;

import com.animeorigins.AnimeOriginsPlugin;
import com.animeorigins.Origin;
import com.animeorigins.managers.CooldownManager;
import com.animeorigins.managers.OriginManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class CooldownDisplayTask extends BukkitRunnable {

    private final AnimeOriginsPlugin plugin;
    private final CooldownManager cm;
    private final OriginManager om;

    public CooldownDisplayTask(AnimeOriginsPlugin plugin) {
        this.plugin = plugin;
        this.cm = plugin.getCooldownManager();
        this.om = plugin.getOriginManager();
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Origin origin = om.getOrigin(player);
            if (origin == null) {
                // Show prompt to pick origin
                player.sendActionBar(Component.text("Use /origin to choose your origin!", NamedTextColor.YELLOW));
                continue;
            }

            Map<String, Long> cooldowns = cm.getPlayerCooldowns(player.getUniqueId());

            if (cooldowns.isEmpty()) {
                // Show ready status + keybinds
                player.sendActionBar(buildReadyBar(origin));
                // XP bar: fully charged (set to 1.0 = full)
                player.setExp(1.0f);
                player.setLevel(0);
            } else {
                // Find the primary cooldown (first / longest)
                String primaryAbility = null;
                long primaryMs = 0;
                long maxMs = 0;

                for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
                    if (entry.getValue() > primaryMs) {
                        primaryMs = entry.getValue();
                        primaryAbility = entry.getKey();
                    }
                    if (entry.getValue() > maxMs) maxMs = entry.getValue();
                }

                // XP bar shows primary cooldown progress
                if (primaryAbility != null) {
                    int totalCd = cm.getConfiguredCooldown(primaryAbility);
                    if (totalCd <= 0) totalCd = 60;
                    long remainingMs = primaryMs;
                    float progress = 1.0f - (float) remainingMs / (totalCd * 1000L);
                    progress = Math.max(0f, Math.min(1f, progress));
                    player.setExp(progress);
                    player.setLevel((int) Math.ceil(remainingMs / 1000.0));
                }

                // Action bar: show all active cooldowns
                player.sendActionBar(buildCooldownBar(origin, cooldowns));
            }
        }
    }

    private Component buildReadyBar(Origin origin) {
        String name = stripColor(origin.getDisplayName());
        String keybinds = getKeybinds(origin);
        return Component.text("【" + name + "】 ", NamedTextColor.AQUA, TextDecoration.BOLD)
                .append(Component.text("✦ READY ", NamedTextColor.GREEN))
                .append(Component.text(keybinds, NamedTextColor.GRAY));
    }

    private Component buildCooldownBar(Origin origin, Map<String, Long> cooldowns) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(stripColor(origin.getDisplayName())).append("】 ");

        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            String abilityName = formatAbilityName(entry.getKey());
            int seconds = (int) Math.ceil(entry.getValue() / 1000.0);
            sb.append("⏳ ").append(abilityName).append(": §c").append(seconds).append("s §7| ");
        }

        // Remove trailing " | "
        String text = sb.toString();
        if (text.endsWith("§7| ")) text = text.substring(0, text.length() - 4);

        return Component.text(text, NamedTextColor.YELLOW);
    }

    private String formatAbilityName(String key) {
        // e.g. "gojo-hollow-purple" -> "Hollow Purple"
        String[] parts = key.split("-");
        if (parts.length <= 1) return key;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)))
                        .append(parts[i].substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String getKeybinds(Origin origin) {
        return switch (origin) {
            case GOJO -> "| [Sneak+RClick] Hollow Purple";
            case GOKU -> "| [Sneak+RClick] Ki Blast";
            case SAITAMA -> "| [Sneak+RClick] Serious Punch";
            case EREN -> "| [Sneak+RClick] Titan Shift";
            case WUKONG -> "| [Sneak+RClick] Nimbus Cloud";
            case L -> "| [Sneak+RClick] Detective Scan";
            case LIGHT -> "| [Sneak+RClick] Kira's Judgement";
            case NARUTO -> "| [Sneak+RClick] Shadow Clones";
            case ASH -> "| [Sneak+RClick] Wolf Recall";
            case LEBRON -> "| [Sneak+LClick] Posterize";
            default -> "";
        };
    }

    private String stripColor(String s) {
        return s.replaceAll("§[0-9a-fklmnor]", "");
    }
}
