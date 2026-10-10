package com.github.kuroronyancat.tacz_maid_balance_fix.mixin;

import com.github.kuroronyancat.tacz_maid_balance_fix.ModConfigHolder;
import com.github.tartaricacid.touhoulittlemaid.compat.gun.tacz.TacInnerCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.InaccuracyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.Optional;

@Mixin(value = TacInnerCompat.class, remap = false)
public abstract class GunShootTargetTaskMixin {
    /*
     * Keep the existing mixin class name so an existing mixins.json entry for
     * "GunShootTargetTaskMixin" does not need to be renamed. The target is now
     * TacInnerCompat, where the real performGunAttack method lives.
     */
    @Unique private static final String maidTaczBalanceFix$remainingKey =
            "maid_tacz_balance_fix_burst_remaining";
    @Unique private static final String maidTaczBalanceFix$gunKey =
            "maid_tacz_balance_fix_burst_gun";
    @Unique private static final String maidTaczBalanceFix$targetKey =
            "maid_tacz_balance_fix_burst_target";
    @Unique private static final String maidTaczBalanceFix$reloadPostDelayKey =
            "maid_tacz_balance_fix_reload_post_delay";

    @Inject(
            method = "performGunAttack(Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void maidTaczBalanceFix$performGunAttack(
            EntityMaid shooter,
            LivingEntity target,
            ItemStack gunItem,
            CallbackInfoReturnable<Integer> cir
    ) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            maidTaczBalanceFix$clearBurstState(shooter);
            cir.setReturnValue(100);
            return;
        }

        ResourceLocation gunId = iGun.getGunId(gunItem);
        Optional<CommonGunIndex> optional = TimelessAPI.getCommonGunIndex(gunId);
        if (optional.isEmpty()) {
            maidTaczBalanceFix$clearBurstState(shooter);
            cir.setReturnValue(100);
            return;
        }

        CommonGunIndex gunIndex = optional.get();
        GunData gunData = gunIndex.getGunData();
        var persistentData = shooter.getPersistentData();

        // A burst must not carry over to a different weapon or target.
        String targetId = target.getUUID().toString();
        if (!persistentData.getString(maidTaczBalanceFix$gunKey).equals(gunId.toString())
                || !persistentData.getString(maidTaczBalanceFix$targetKey).equals(targetId)) {
            persistentData.putInt(maidTaczBalanceFix$remainingKey, 0);
            persistentData.putInt(maidTaczBalanceFix$reloadPostDelayKey, 0);
            persistentData.putString(maidTaczBalanceFix$gunKey, gunId.toString());
            persistentData.putString(maidTaczBalanceFix$targetKey, targetId);
        }

        /*
         * After an empty-magazine reload, wait about one additional second
         * before attempting the next shot. The reload cooldown returned below
         * accounts for the reload itself; this counter is consumed only when
         * performGunAttack is called again after that cooldown.
         */
        int reloadPostDelay = persistentData.getInt(maidTaczBalanceFix$reloadPostDelayKey);
        if (reloadPostDelay > 0) {
            persistentData.putInt(maidTaczBalanceFix$reloadPostDelayKey, reloadPostDelay - 1);
            cir.setReturnValue(1);
            return;
        }

        double x = target.getX() - shooter.getX();
        double y = target.getEyeY() - shooter.getEyeY();
        double z = target.getZ() - shooter.getZ();

        float yaw = (float) -Math.toDegrees(Math.atan2(x, z));
        float pitch = (float) -Math.toDegrees(Math.atan2(y, Math.sqrt(x * x + z * z)));

        // Add aim error based on the weapon's standing inaccuracy.
        Float baseInaccuracyValue = gunData.getInaccuracy().get(InaccuracyType.STAND);
        float baseInaccuracy = baseInaccuracyValue != null ? Math.max(0.0F, baseInaccuracyValue) : 0.0F;
        float spread = 0.0F;
        if (ModConfigHolder.COMMON.enableInaccuracy.get()) {
            float baseMultiplier = ModConfigHolder.COMMON.baseInaccuracyMultiplier.get().floatValue();
            spread = baseInaccuracy * baseMultiplier;
        }

        if (spread > 0.0F) {
            pitch += (shooter.getRandom().nextFloat() - 0.5F) * spread * 2.0F;
            yaw += (shooter.getRandom().nextFloat() - 0.5F) * spread * 2.0F;
        }

        final float shotPitch = pitch;
        final float shotYaw = yaw;

        IGunOperator gunOperator = IGunOperator.fromLivingEntity(shooter);
        ShootResult result = gunOperator.shoot(() -> shotPitch, () -> shotYaw);

        /*
         * Account for SUCCESS before aim-state transitions. The original method
         * calls shoot() before changing aim, so a shot can succeed on the same
         * invocation that starts/stops aiming. Such a shot must still count.
         */
        int successfulShotCooldown = -1;
        if (result == ShootResult.SUCCESS) {
            FireMode fireMode = iGun.getFireMode(gunItem);
            if (fireMode == FireMode.SEMI || fireMode == FireMode.BURST) {
                persistentData.putInt(maidTaczBalanceFix$remainingKey, 0);
                successfulShotCooldown = 10 + shooter.getRandom().nextInt(5);
            } else if (!ModConfigHolder.COMMON.enableBurst.get()) {
                // Burst modification disabled: retain the original full-auto cadence.
                // Clear any saved burst so re-enabling the feature cannot resume stale state.
                persistentData.putInt(maidTaczBalanceFix$remainingKey, 0);
                successfulShotCooldown = ModConfigHolder.COMMON.fullAutoShotDelayTicks.get();
            } else {
                int remaining = persistentData.getInt(maidTaczBalanceFix$remainingKey);
                if (remaining > 0) {
                    remaining--;
                } else {
                    int minBurst = ModConfigHolder.COMMON.minBurstCount.get();
                    int maxBurst = ModConfigHolder.COMMON.maxBurstCount.get();
                    if (minBurst > maxBurst) {
                        int swap = minBurst;
                        minBurst = maxBurst;
                        maxBurst = swap;
                    }
                    int burstCount = minBurst;
                    if (maxBurst > minBurst) {
                        burstCount += shooter.getRandom().nextInt(maxBurst - minBurst + 1);
                    }
                    remaining = burstCount - 1;
                }

                persistentData.putInt(maidTaczBalanceFix$remainingKey, remaining);
                successfulShotCooldown = remaining > 0
                        ? ModConfigHolder.COMMON.fullAutoShotDelayTicks.get()
                        : maidTaczBalanceFix$getRandomBurstIntervalTicks(shooter);
            }
        }

        // Preserve the original TaCZ/Touhou Little Maid draw/aim/bolt/reload flow.
        String sniper = GunTabType.SNIPER.name().toLowerCase(Locale.ENGLISH);
        if (gunIndex.getType().equals(sniper) && !gunOperator.getSynIsAiming()) {
            gunOperator.aim(true);
            int aimCooldown = Math.round(gunData.getAimTime() * 20) + 2;
            cir.setReturnValue(result == ShootResult.SUCCESS
                    ? Math.max(aimCooldown, successfulShotCooldown)
                    : aimCooldown);
            return;
        }

        if (!gunIndex.getType().equals(sniper)) {
            float distance = shooter.distanceTo(target);
            float radius = shooter.getRestrictRadius();

            if (distance <= radius && gunOperator.getSynIsAiming()) {
                gunOperator.aim(false);
                int aimCooldown = Math.round(gunData.getAimTime() * 20) + 2;
                cir.setReturnValue(result == ShootResult.SUCCESS
                        ? Math.max(aimCooldown, successfulShotCooldown)
                        : aimCooldown);
                return;
            }
            if (distance > radius && !gunOperator.getSynIsAiming()) {
                gunOperator.aim(true);
                int aimCooldown = Math.round(gunData.getAimTime() * 20) + 2;
                cir.setReturnValue(result == ShootResult.SUCCESS
                        ? Math.max(aimCooldown, successfulShotCooldown)
                        : aimCooldown);
                return;
            }
        }

        if (result == ShootResult.SUCCESS) {
            cir.setReturnValue(successfulShotCooldown);
            return;
        }

        if (result == ShootResult.NOT_DRAW) {
            gunOperator.draw(shooter::getMainHandItem);
            cir.setReturnValue(Math.round(gunData.getDrawTime() * 20) + 2);
            return;
        }

        if (result == ShootResult.NEED_BOLT) {
            gunOperator.bolt();
            cir.setReturnValue(Math.round(gunData.getBoltActionTime() * 20) + 2);
            return;
        }

        if (result == ShootResult.NO_AMMO) {
            // Reset burst progress and add a one-second delay after the reload cooldown.
            persistentData.putInt(maidTaczBalanceFix$remainingKey, 0);
            int postReloadDelayTicks = Math.max(
                    0,
                    (int) Math.round(ModConfigHolder.COMMON.reloadPostCooldownSeconds.get() * 20.0)
            );
            persistentData.putInt(maidTaczBalanceFix$reloadPostDelayKey, postReloadDelayTicks);
            var availableInv = shooter.getAvailableInv(true);
            ItemsUtil.findStackSlot(availableInv, stack -> {
                IAmmo ammo = IAmmo.getIAmmoOrNull(stack);
                return ammo != null && ammo.isAmmoOfGun(gunItem, stack);
            });
            gunOperator.reload();
            cir.setReturnValue(Math.round(gunData.getReloadData().getCooldown().getEmptyTime() * 20) + 2);
            return;
        }

        if (result == ShootResult.ID_NOT_EXIST || result == ShootResult.NOT_GUN) {
            maidTaczBalanceFix$clearBurstState(shooter);
            cir.setReturnValue(100);
            return;
        }

        if (result == ShootResult.COOL_DOWN) {
            cir.setReturnValue(ModConfigHolder.COMMON.fullAutoShotDelayTicks.get());
            return;
        }

        // Any other non-success result should not consume a burst shot.
        cir.setReturnValue(20);
    }

    @Unique
    private static int maidTaczBalanceFix$getRandomBurstIntervalTicks(EntityMaid shooter) {
        double minSeconds = ModConfigHolder.COMMON.minBurstIntervalSeconds.get();
        double maxSeconds = ModConfigHolder.COMMON.maxBurstIntervalSeconds.get();

        if (minSeconds > maxSeconds) {
            double swap = minSeconds;
            minSeconds = maxSeconds;
            maxSeconds = swap;
        }

        double seconds = minSeconds;
        if (maxSeconds > minSeconds) {
            seconds += shooter.getRandom().nextDouble() * (maxSeconds - minSeconds);
        }
        return Math.max(0, (int) Math.round(seconds * 20.0));
    }

    @Unique
    private static void maidTaczBalanceFix$clearBurstState(EntityMaid shooter) {
        var data = shooter.getPersistentData();
        data.putInt(maidTaczBalanceFix$remainingKey, 0);
        data.putInt(maidTaczBalanceFix$reloadPostDelayKey, 0);
        data.remove(maidTaczBalanceFix$gunKey);
        data.remove(maidTaczBalanceFix$targetKey);
    }
}
