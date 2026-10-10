package com.github.kuroronyancat.tacz_maid_balance_fix;

import net.minecraftforge.common.ForgeConfigSpec;

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
        public final ForgeConfigSpec.DoubleValue baseInaccuracyMultiplier;
        public final ForgeConfigSpec.IntValue minBurstCount;
        public final ForgeConfigSpec.IntValue maxBurstCount;
        public final ForgeConfigSpec.DoubleValue minBurstIntervalSeconds;
        public final ForgeConfigSpec.DoubleValue maxBurstIntervalSeconds;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("Balance Settings");

            enableBurst = builder
                    .comment(
                            "Enable or disable the maid full-auto burst-fire modification.",
                            "When disabled, full-auto weapons use the original continuous-fire cadence."
                    )
                    .define("enableBurst", true);

            enableInaccuracy = builder
                    .comment(
                            "Enable or disable the additional maid aiming inaccuracy added by this mod.",
                            "When disabled, the mod does not add extra random aim error; TaCZ's own spread remains unchanged."
                    )
                    .define("enableInaccuracy", true);

            baseInaccuracyMultiplier = builder
                    .comment(
                            "Global base multiplier for additional maid aiming inaccuracy.",
                            "Final added aim error = TaCZ standing inaccuracy * this multiplier.",
                            "Allowed range: 0.1 to 2.0."
                    )
                    .defineInRange("baseInaccuracyMultiplier", 0.5, 0.1, 2.0);

            minBurstCount = builder
                    .comment("Minimum successful shots in one full-auto burst.")
                    .defineInRange("minBurstCount", 4, 1, 100);

            maxBurstCount = builder
                    .comment("Maximum successful shots in one full-auto burst.")
                    .defineInRange("maxBurstCount", 8, 1, 100);

            minBurstIntervalSeconds = builder
                    .comment("Minimum pause between full-auto bursts, in seconds.")
                    .defineInRange("minBurstIntervalSeconds", 0.5, 0.0, 60.0);

            maxBurstIntervalSeconds = builder
                    .comment("Maximum pause between full-auto bursts, in seconds.")
                    .defineInRange("maxBurstIntervalSeconds", 2.0, 0.0, 60.0);

            builder.pop();
        }
    }
}
