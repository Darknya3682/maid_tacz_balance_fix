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
        public final ForgeConfigSpec.DoubleValue inaccuracyMultiplier;
        public final ForgeConfigSpec.IntValue minBurstCount;
        public final ForgeConfigSpec.IntValue maxBurstCount;
        public final ForgeConfigSpec.DoubleValue minBurstIntervalSeconds;
        public final ForgeConfigSpec.DoubleValue maxBurstIntervalSeconds;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("Balance Settings");

            inaccuracyMultiplier = builder
                    .comment(
                            "Multiplier for additional maid aim error, based on the gun's standing inaccuracy.",
                            "0.0 disables this extra aim error; maximum allowed value is 5.0."
                    )
                    .defineInRange("inaccuracyMultiplier", 1.0, 0.0, 5.0);

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
