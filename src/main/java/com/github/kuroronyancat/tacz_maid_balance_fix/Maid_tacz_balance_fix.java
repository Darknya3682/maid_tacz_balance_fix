package com.github.kuroronyancat.tacz_maid_balance_fix;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Maid_tacz_balance_fix.MODID)
public class Maid_tacz_balance_fix {
    public static final String MODID = "maid_tacz_balance_fix";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Maid_tacz_balance_fix() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 共通セットアップイベントの登録
        modEventBus.addListener(this::commonSetup);

        // Forgeイベントバスの登録（ゲーム内イベント等をMixin以外でフックする場合に使用）
        MinecraftForge.EVENT_BUS.register(this);

        // 設定ファイル（ModConfig）の登録
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigHolder.COMMON_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Maid TaCZ Balance Fix initialized.");
    }
}
