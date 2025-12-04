package com.CystrySu.infinitydisk.client;

import com.CystrySu.infinitydisk.Infinitydisk;
import com.CystrySu.infinitydisk.client.gui.InfinityDiskScreen;
import com.CystrySu.infinitydisk.menu.InfinityDiskMenu;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端初始化
 * 注册 Screen 和其他客户端资源
 */
@Mod.EventBusSubscriber(modid = Infinitydisk.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册 Screen（必须在主线程上执行）
        event.enqueueWork(() -> {
            MenuScreens.register(InfinityDiskMenu.INFINITY_DISK_MENU.get(), InfinityDiskScreen::new);
        });
    }
}
