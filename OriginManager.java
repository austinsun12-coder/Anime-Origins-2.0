package com.animeorigins;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum Origin {

    GOJO("Satoru Gojo", "§b⚡ §fSatoru Gojo", "§7Jujutsu Kaisen", Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            "§aInfinity: Projectiles bounce off you",
            "§a[Crouch+R] Hollow Purple: Devastating beam (3min CD)",
            "§cSensory Overload: Must wear helmet, blinded in daylight",
            "§cCursed Drain: Permanent Hunger I, Hollow Purple empties hunger",
            "§cArrogant Fighter: Cannot equip shields"),

    YUJI("Yuji Itadori", "§e⚡ §fYuji Itadori", "§7Jujutsu Kaisen", Material.ORANGE_STAINED_GLASS_PANE,
            "§aDivergent Fist: Punches deal iron sword damage + 25% Black Flash (4x dmg)",
            "§aTough Vessel: Permanent Resistance I, Jump Boost I, Speed II",
            "§cSukuna's Toll: Wither effect below 3 hearts",
            "§cCursed Tools Only: 50% less damage with swords/axes",
            "§cCursed Burnout: Black Flash drains hunger; recoil if hungry"),

    SAITAMA("Saitama", "§6★ §fSaitama", "§7One Punch Man", Material.YELLOW_STAINED_GLASS_PANE,
            "§a[Crouch+R] Serious Punch: Insta-kill mobs, 15 hearts true damage (1min CD)",
            "§aIndestructible: Immune to fall damage and fire/lava",
            "§cHero for Fun: Weakness+Slowness if no combat for 3 minutes",
            "§cDiscount Shopper: Cannot equip Diamond or Netherite gear",
            "§cBald Cape: Cannot equip helmets"),

    GOKU("Goku", "§c★ §fGoku", "§7Dragon Ball", Material.ORANGE_STAINED_GLASS_PANE,
            "§aSaiyan Blood: Gain Strength+Speed when hit (scales with low HP)",
            "§a[Crouch+R] Ki Blast: Shoot explosive fireballs",
            "§cInsatiable Appetite: Triple exhaustion rate",
            "§cTail Weakness: Slowness II when hit from behind",
            "§cFair Fighter: 0 damage while sneaking"),

    LUFFY("Monkey D. Luffy", "§c⚓ §fMonkey D. Luffy", "§7One Piece", Material.RED_STAINED_GLASS_PANE,
            "§aRubber Body: Immune to fall/blunt damage, reduced knockback",
            "§aGomu Gomu: +3 block attack and mining reach",
            "§cDevil Fruit Curse: Cannot swim up, 5x faster drowning, rain=Slowness",
            "§cMeat Lover: Only eat meat; crops give Poison+Nausea"),

    EREN("Eren Yeager", "§8⚔ §fEren Yeager", "§7Attack on Titan", Material.GRAY_STAINED_GLASS_PANE,
            "§a[Crouch+R] Colossal Shift: Become 5x giant with massive bonuses (5min CD)",
            "§aScout Agility: Permanent Speed I, reduced fall damage",
            "§cTitan Trigger: Costs 3 hearts to transform",
            "§cMassive Target: Huge hitbox in titan form",
            "§cExhaustion: Slowness+Weakness+Hunger for 15s after titan form"),

    WUKONG("Sun Wukong", "§d☁ §fSun Wukong", "§7Journey to the West", Material.PURPLE_STAINED_GLASS_PANE,
            "§a[Crouch+R] Nimbus Cloud: Double jump / air dash",
            "§aJingu Bang: Stick deals diamond sword damage with sweep",
            "§cGolden Headband: Attacking passive mobs deals 2 hearts to you",
            "§cMischievous Reputation: All villager trades are 2x expensive"),

    L("L Lawliet", "§f☕ §fL Lawliet", "§7Death Note", Material.WHITE_STAINED_GLASS_PANE,
            "§a[Crouch+R] World's Greatest Detective: Glowing outline 30 blocks (1min CD)",
            "§aSugar Rush: Sweet foods grant Haste II + Regen I for 20s",
            "§cFragile Body: Max health capped at 6 hearts",
            "§cInsomniac: Cannot sleep in beds"),

    LIGHT("Light Yagami", "§e☠ §fLight Yagami", "§7Death Note", Material.YELLOW_STAINED_GLASS_PANE,
            "§a[Crouch+R] Kira's Judgement: Inflict Wither III on target (1.5min CD)",
            "§aCharismatic: Villager trades heavily discounted",
            "§cMortal Frame: Max health capped at 8 hearts",
            "§cGod Complex: +50% damage from players/named mobs; no helmets"),

    NARUTO("Naruto Uzumaki", "§e🌀 §fNaruto Uzumaki", "§7Naruto", Material.ORANGE_STAINED_GLASS_PANE,
            "§a[Crouch+R] Shadow Clones: Spawn 3 wolf-zombies for 15s (45s CD)",
            "§aNinja Agility: Permanent Jump Boost II + Speed I, sprint on water",
            "§cChakra Exhaustion: Clones drain XP, or health if empty",
            "§cReckless: Double damage from magic attacks and explosions"),

    ASH("Ash Ketchum", "§a⚡ §fAsh Ketchum", "§7Pokémon", Material.GREEN_STAINED_GLASS_PANE,
            "§a[Crouch+R] Wolf Pack Recall: Teleport all tamed wolves to you (3min CD)",
            "§aThe Very Best: Tamed wolves get Speed II, Strength II, Resistance I",
            "§cPacifist Trainer: 60% less direct damage",
            "§cTen Year Old: Max health capped at 7 hearts"),

    LEBRON("LeBron James", "§6🏀 §fLeBron James", "§7Real Life", Material.GOLD_BLOCK,
            "§aThe King's Leap: Permanent Jump Boost III",
            "§a[Crouch+L] Posterize: Shockwave + Slowness IV on critical aerial hits",
            "§cTall Frame: 1.2x scale; can't fit standard doorways/tunnels",
            "§cFourth Quarter Cramps: Slowness III after 15s of sprinting");

    private final String id;
    private final String displayName;
    private final String series;
    private final Material icon;
    private final List<String> abilities;

    Origin(String id, String displayName, String series, Material icon, String... abilities) {
        this.id = id;
        this.displayName = displayName;
        this.series = series;
        this.icon = icon;
        this.abilities = Arrays.asList(abilities);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getSeries() { return series; }
    public Material getIcon() { return icon; }
    public List<String> getAbilities() { return abilities; }

    public static Origin fromString(String s) {
        if (s == null) return null;
        for (Origin o : values()) {
            if (o.name().equalsIgnoreCase(s) || o.getId().equalsIgnoreCase(s)) return o;
        }
        return null;
    }
}
