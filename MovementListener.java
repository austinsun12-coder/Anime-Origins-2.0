package com.animeorigins;

import com.animeorigins.gui.OriginGUI;
import com.animeorigins.listeners.*;
import com.animeorigins.managers.CooldownManager;
import com.animeorigins.managers.OriginManager;
import com.animeorigins.managers.TitanManager;
import com.animeorigins.tasks.PassiveEffectsTask;
import com.animeorigins.tasks.CooldownDisplayTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AnimeOriginsPlugin extends JavaPlugin {

    private static AnimeOriginsPlugin instance;
    private OriginManager originManager;
    private CooldownManager cooldownManager;
    private TitanManager titanManager;
    private OriginGUI originGUI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        originManager = new OriginManager(this);
        cooldownManager = new CooldownManager(this);
        titanManager = new TitanManager(this);
        originGUI = new OriginGUI(this);

        // Register all listeners
        getServer().getPluginManager().registerEvents(new AbilityInputListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerStateListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new EquipmentListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);

        // Start tasks
        new PassiveEffectsTask(this).runTaskTimer(this, 20L, 20L);
        new CooldownDisplayTask(this).runTaskTimer(this, 5L, 5L);

        getLogger().info("AnimeOrigins enabled! 12 origins loaded.");
    }

    @Override
    public void onDisable() {
        // Restore all players to normal state
        if (titanManager != null) {
            getServer().getOnlinePlayers().forEach(p -> titanManager.exitTitanForm(p, false));
        }
        if (originManager != null) {
            originManager.saveAll();
        }
        getLogger().info("AnimeOrigins disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "origin" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can use this command.");
                    return true;
                }
                if (originManager.getOrigin(player) != null && !getConfig().getBoolean("settings.allow-reselection")) {
                    player.sendMessage("§cYou already have an origin! Ask an admin to reset it.");
                    return true;
                }
                originGUI.openSelectionGUI(player);
                return true;
            }
            case "setorigin" -> {
                if (!sender.hasPermission("animeorigins.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /setorigin <player> <origin>");
                    return true;
                }
                Player target = getServer().getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found.");
                    return true;
                }
                String origin = args[1].toLowerCase();
                if (!OriginManager.VALID_ORIGINS.contains(origin)) {
                    sender.sendMessage("§cInvalid origin. Valid: " + String.join(", ", OriginManager.VALID_ORIGINS));
                    return true;
                }
                originManager.setOrigin(target, origin);
                sender.sendMessage("§aSet " + target.getName() + "'s origin to " + origin);
                target.sendMessage("§aYour origin has been set to: §e" + origin);
                return true;
            }
            case "resetorigin" -> {
                if (args.length > 0 && sender.hasPermission("animeorigins.admin")) {
                    Player target = getServer().getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage("§cPlayer not found.");
                        return true;
                    }
                    originManager.removeOrigin(target);
                    target.sendMessage("§eYour origin has been reset. Use /origin to choose again.");
                    sender.sendMessage("§aReset " + target.getName() + "'s origin.");
                } else if (sender instanceof Player player) {
                    if (!getConfig().getBoolean("settings.allow-reselection")) {
                        player.sendMessage("§cYou cannot reset your own origin. Ask an admin.");
                        return true;
                    }
                    originManager.removeOrigin(player);
                    player.sendMessage("§eYour origin has been reset. Use /origin to choose again.");
                } else {
                    sender.sendMessage("§cUsage: /resetorigin <player>");
                }
                return true;
            }
        }
        return false;
    }

    public static AnimeOriginsPlugin getInstance() { return instance; }
    public OriginManager getOriginManager() { return originManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public TitanManager getTitanManager() { return titanManager; }
    public OriginGUI getOriginGUI() { return originGUI; }
}
