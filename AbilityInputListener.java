package com.animeorigins.tasks;

import com.animeorigins.AnimeOriginsPlugin;
import com.animeorigins.Origin;
import com.animeorigins.listeners.EquipmentListener;
import com.animeorigins.listeners.PlayerStateListener;
import com.animeorigins.managers.OriginManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PassiveEffectsTask extends BukkitRunnable {

    private final AnimeOriginsPlugin plugin;
    private final OriginManager om;

    // For Goku triple exhaustion
    private final Map<UUID, Float> lastExhaustion = new HashMap<>();

    // For Ash wolf buff tracking
    private final Map<UUID, Long> lastWolfCheck = new HashMap<>();

    public PassiveEffectsTask(AnimeOriginsPlugin plugin) {
        this.plugin = plugin;
        this.om = plugin.getOriginManager();
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Origin origin = om.getOrigin(player);
            if (origin == null) continue;

            switch (origin) {
                case GOJO -> tickGojo(player);
                case YUJI -> tickYuji(player);
                case SAITAMA -> tickSaitama(player);
                case GOKU -> tickGoku(player);
                case LUFFY -> tickLuffy(player);
                case EREN -> tickEren(player);
                case L -> tickL(player);
                case ASH -> tickAsh(player);
                case NARUTO -> tickNaruto(player);
            }

            // Equipment stripping (run every second)
            plugin.getServer().getPluginManager().callEvent(
                    new org.bukkit.event.player.PlayerItemHeldEvent(player,
                            player.getInventory().getHeldItemSlot(),
                            player.getInventory().getHeldItemSlot()));

            // Get the equipment listener reference
            stripEquipment(player, origin);
        }
    }

    private void stripEquipment(Player player, Origin origin) {
        // We replicate the logic inline to avoid circular references
        switch (origin) {
            case SAITAMA -> {
                var inv = player.getInventory();
                if (inv.getHelmet() != null) {
                    player.sendMessage("§cSaitama cannot wear helmets!");
                    player.getWorld().dropItemNaturally(player.getLocation(), inv.getHelmet());
                    inv.setHelmet(null);
                }
                // Diamond/Netherite gear
                var armor = inv.getArmorContents();
                boolean changed = false;
                for (int i = 0; i < armor.length; i++) {
                    if (armor[i] != null && (armor[i].getType().name().startsWith("DIAMOND_") || armor[i].getType().name().startsWith("NETHERITE_"))) {
                        player.getWorld().dropItemNaturally(player.getLocation(), armor[i]);
                        armor[i] = new org.bukkit.inventory.ItemStack(Material.AIR);
                        changed = true;
                    }
                }
                if (changed) {
                    inv.setArmorContents(armor);
                    player.sendMessage("§cSaitama is too humble for Diamond/Netherite gear!");
                }
            }
            case GOJO -> {
                var inv = player.getInventory();
                if (inv.getItemInOffHand().getType() == Material.SHIELD) {
                    player.sendMessage("§cGojo refuses to carry a shield!");
                    player.getWorld().dropItemNaturally(player.getLocation(), inv.getItemInOffHand());
                    inv.setItemInOffHand(new org.bukkit.inventory.ItemStack(Material.AIR));
                }
            }
            case LIGHT -> {
                var inv = player.getInventory();
                if (inv.getHelmet() != null) {
                    player.sendMessage("§cLight's God Complex: No helmets!");
                    player.getWorld().dropItemNaturally(player.getLocation(), inv.getHelmet());
                    inv.setHelmet(null);
                }
            }
        }
    }

    // ════════════════ GOJO ════════════════
    private void tickGojo(Player player) {
        // Sensory Overload
        boolean hasHelmet = player.getInventory().getHelmet() != null;
        long time = player.getWorld().getTime();
        boolean isDay = time < 12300 || time > 23850;

        if (!hasHelmet && isDay) {
            if (!player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, false, true, true));
                player.sendMessage("§c§lSensory Overload! Wear your blindfold (helmet)!");
            }
        } else {
            if (!(!hasHelmet && isDay)) {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.NAUSEA);
            }
        }

        // Hunger I passive
        ensureEffect(player, PotionEffectType.HUNGER, 0);
    }

    // ════════════════ YUJI ════════════════
    private void tickYuji(Player player) {
        // Ensure passive buffs stay active
        ensureEffect(player, PotionEffectType.RESISTANCE, 0);
        ensureEffect(player, PotionEffectType.JUMP_BOOST, 0);
        ensureEffect(player, PotionEffectType.SPEED, 1);

        // Sukuna's Toll: Wither below 3 hearts
        if (player.getHealth() <= 6.0) {
            if (!player.hasPotionEffect(PotionEffectType.WITHER)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 0, false, true, true));
                player.sendMessage("§4Sukuna stirs within you! §cWither effect!");
            }
        }
    }

    // ════════════════ SAITAMA ════════════════
    // Track last combat time for Saitama boredom
    private final Map<UUID, Long> saitamaLastCombat = new HashMap<>();

    public void updateSaitamaCombat(UUID uuid) {
        saitamaLastCombat.put(uuid, System.currentTimeMillis());
    }

    private void tickSaitama(Player player) {
        long last = saitamaLastCombat.getOrDefault(player.getUniqueId(), System.currentTimeMillis());
        boolean bored = System.currentTimeMillis() - last > 3 * 60 * 1000L;
        if (bored) {
            ensureEffect(player, PotionEffectType.WEAKNESS, 1);
            ensureEffect(player, PotionEffectType.SLOWNESS, 0);
        } else {
            player.removePotionEffect(PotionEffectType.WEAKNESS);
            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }

    // ════════════════ GOKU ════════════════
    private void tickGoku(Player player) {
        // Triple exhaustion: we add extra exhaustion each tick
        float extra = 0.1f; // additional exhaustion per second (normal is ~0.005/tick sprint)
        player.setExhaustion(Math.min(4.0f, player.getExhaustion() + extra));
    }

    // ════════════════ LUFFY ════════════════
    private void tickLuffy(Player player) {
        // Devil Fruit: 5x faster oxygen drain in water
        if (player.isUnderWater()) {
            // Drain oxygen faster (we can't directly set, but we can add damage)
            // We simulate by dealing tiny damage if almost out of breath
            int air = player.getRemainingAir();
            if (air > 0) {
                player.setRemainingAir(Math.max(0, air - (int)(player.getMaximumAir() * 0.04)));
            }
        }

        // Ensure Resistance for reduced knockback
        ensureEffect(player, PotionEffectType.RESISTANCE, 0);
    }

    // ════════════════ EREN ════════════════
    private void tickEren(Player player) {
        if (!plugin.getTitanManager().isInTitanForm(player)) {
            ensureEffect(player, PotionEffectType.SPEED, 0);
        }
    }

    // ════════════════ L ════════════════
    private void tickL(Player player) {
        // Cap health at 6 hearts
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null && attr.getBaseValue() != 12.0) {
            attr.setBaseValue(12.0);
        }
        if (player.getHealth() > 12.0) {
            player.setHealth(12.0);
        }
    }

    // ════════════════ NARUTO ════════════════
    private void tickNaruto(Player player) {
        ensureEffect(player, PotionEffectType.JUMP_BOOST, 1);
        ensureEffect(player, PotionEffectType.SPEED, 0);
    }

    // ════════════════ ASH ════════════════
    private void tickAsh(Player player) {
        // Cap health at 7 hearts
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null && attr.getBaseValue() != 14.0) {
            attr.setBaseValue(14.0);
        }
        if (player.getHealth() > 14.0) player.setHealth(14.0);

        // Buff tamed wolves
        long now = System.currentTimeMillis();
        if (now - lastWolfCheck.getOrDefault(player.getUniqueId(), 0L) > 10_000) {
            lastWolfCheck.put(player.getUniqueId(), now);
            for (var entity : player.getWorld().getEntities()) {
                if (entity instanceof Wolf wolf && wolf.isTamed() &&
                        wolf.getOwner() != null && wolf.getOwner().getUniqueId().equals(player.getUniqueId())) {
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1, false, false));
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 15 * 20, 1, false, false));
                    wolf.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 15 * 20, 0, false, false));
                }
            }
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────────────
    private void ensureEffect(Player player, PotionEffectType type, int amplifier) {
        PotionEffect current = player.getPotionEffect(type);
        if (current == null || current.getAmplifier() < amplifier || current.getDuration() < 60) {
            player.addPotionEffect(new PotionEffect(type, 80, amplifier, false, false, true));
        }
    }
}
