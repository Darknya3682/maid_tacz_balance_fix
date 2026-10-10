package com.github.kuroronyancat.tacz_maid_balance_fix;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Locale;

public class ModConfigHolder {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue enableBurst;
        public final ForgeConfigSpec.BooleanValue enableInaccuracy;
        public final CategorySettings pistol;
        public final CategorySettings smg;
        public final CategorySettings shotgun;
        public final CategorySettings rifle;
        public final CategorySettings sniper;
        public final CategorySettings mg;
        public final CategorySettings rpg;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            enableBurst = builder
                    .comment("Enable or disable the maid full-auto burst-fire modification.")
                    .define("enableBurst", true);
            enableInaccuracy = builder
                    .comment("Enable or disable additional maid aiming inaccuracy.")
                    .define("enableInaccuracy", true);
            builder.pop();

            //                                          section           minC maxC minInt maxInt  inacc  delay reload
            pistol  = new CategorySettings(builder, "Pistol Settings",    3,   6, 0.5,  1.0,  0.5,  1, 0.3);
            smg     = new CategorySettings(builder, "SMG Settings",       8,  12, 0.5,  0.75, 0.75, 1, 0.3);
            shotgun = new CategorySettings(builder, "Shotgun Settings",   3,   6, 1.0,  2.0,  0.25, 1, 0.5);
            rifle   = new CategorySettings(builder, "Rifle Settings",     4,   8, 0.75, 1.5,  0.5,  1, 0.8);
            sniper  = new CategorySettings(builder, "Sniper Settings",    1,   1, 1.5,  2.5,  0.1,  1, 2.0);
            mg      = new CategorySettings(builder, "MG Settings",       32,  64, 1.25, 2.5,  1.0,  1, 1.5);
            rpg     = new CategorySettings(builder, "RPG Settings",       1,   6, 1.0,  2.0,  0.25, 1, 0.75);
        }
    }

    public static class CategorySettings {
        public final ForgeConfigSpec.IntValue minBurstCount;
        public final ForgeConfigSpec.IntValue maxBurstCount;
        public final ForgeConfigSpec.DoubleValue minBurstIntervalSeconds;
        public final ForgeConfigSpec.DoubleValue maxBurstIntervalSeconds;
        public final ForgeConfigSpec.DoubleValue inaccuracyMultiplier;
        public final ForgeConfigSpec.IntValue fullAutoShotDelayTicks;
        public final ForgeConfigSpec.DoubleValue reloadPostCooldownSeconds;

        public CategorySettings(ForgeConfigSpec.Builder builder, String section,
                                int defMinBurstCount, int defMaxBurstCount,
                                double defMinBurstInterval, double defMaxBurstInterval,
                                double defInaccuracyMultiplier, int defFullAutoShotDelayTicks,
                                double defReloadPostCooldown) {
            builder.push(section);
            minBurstCount = builder.comment("Minimum successful shots in one full-auto burst.")
                    .defineInRange("minBurstCount", defMinBurstCount, 1, 100);
            maxBurstCount = builder.comment("Maximum successful shots in one full-auto burst.")
                    .defineInRange("maxBurstCount", defMaxBurstCount, 1, 100);
            minBurstIntervalSeconds = builder.comment("Minimum pause between full-auto bursts, in seconds.")
                    .defineInRange("minBurstIntervalSeconds", defMinBurstInterval, 0.0, 60.0);
            maxBurstIntervalSeconds = builder.comment("Maximum pause between full-auto bursts, in seconds.")
                    .defineInRange("maxBurstIntervalSeconds", defMaxBurstInterval, 0.0, 60.0);
            inaccuracyMultiplier = builder.comment("Multiplier for TaCZ standing inaccuracy for this gun category.")
                    .defineInRange("inaccuracyMultiplier", defInaccuracyMultiplier, 0.1, 2.0);
            fullAutoShotDelayTicks = builder.comment("Delay between successful full-auto shots, in ticks. 20 ticks = 1 second.")
                    .defineInRange("fullAutoShotDelayTicks", defFullAutoShotDelayTicks, 1, 20);
            reloadPostCooldownSeconds = builder.comment("Additional delay after the reload cooldown completes, in seconds.")
                    .defineInRange("reloadPostCooldownSeconds", defReloadPostCooldown, 0.0, 60.0);
            builder.pop();
        }
    }

    public static CategorySettings getSettingsForType(String type) {
        if (type == null) return COMMON.rifle;
        switch (type.toLowerCase(Locale.ROOT)) {
            case "pistol": return COMMON.pistol;
            case "smg": return COMMON.smg;
            case "shotgun": return COMMON.shotgun;
            case "rifle": return COMMON.rifle;
            case "sniper": return COMMON.sniper;
            case "mg": return COMMON.mg;
            case "rpg": return COMMON.rpg;
            default: return COMMON.rifle;
        }
    }
}
