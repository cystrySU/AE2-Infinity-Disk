package com.CystrySu.infinitydisk;

import appeng.api.storage.StorageCells;
import com.CystrySu.infinitydisk.command.InfinityDiskCommands;
import com.CystrySu.infinitydisk.item.ItemInfiniteDisk;
import com.CystrySu.infinitydisk.menu.InfinityDiskMenu;
import com.CystrySu.infinitydisk.networking.NetworkHandler;
import com.CystrySu.infinitydisk.storage.InfiniteDiskCellHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * AE2 Infinity Disk Mod 主类
 * 提供无限容量和无限种类的 AE2 存储磁盘
 */
@Mod(Infinitydisk.MODID)
public class Infinitydisk
{
    /*
     * 架构概览（1.19.2 / AE2-only）
     * - storage.InfiniteDiskStorage：实现 AE2 MEStorage 接口，负责无限磁盘数据池与序列化。
     * - storage.InfiniteDiskManager：维护虚拟池、软上限以及审计日志。
     * - storage.InfiniteDiskCellHandler：向 AE2 注册磁盘处理器。
     * - storage.InfiniteDiskCellInventory：实现 StorageCell 接口。
     * - item.ItemInfiniteDisk：无限磁盘物品。
     * - networking 包：PacketSyncDiskState 等用于同步磁盘统计信息。
     * - client.gui / menu：InfinityDiskScreen 与 InfinityDiskMenu 提供磁盘管理界面。
     * - Config：集中提供能量倍率、配方开关、软上限等服务器配置。
     */

    // Mod ID
    public static final String MODID = "ae_infinity_disk";
    
    // Logger
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Deferred Registers
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // ==================== 物品注册 ====================
    
    /**
     * 无限磁盘物品
     * 可放入 ME 驱动器，提供无限存储容量和种类
     */
    public static final RegistryObject<Item> INFINITY_DISK = ITEMS.register("infinity_disk", 
            () -> new ItemInfiniteDisk(new Item.Properties()
                    .tab(CreativeModeTab.TAB_MISC)
                    .stacksTo(1)));

    // ==================== 构造函数 ====================
    
    public Infinitydisk(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // 注册 commonSetup
        modEventBus.addListener(this::commonSetup);

        // 注册物品
        ITEMS.register(modEventBus);
        
        // 注册菜单类型
        InfinityDiskMenu.MENUS.register(modEventBus);

        // 注册事件总线
        MinecraftForge.EVENT_BUS.register(this);

        // 注册配置（使用新的 COMMON 配置规格）
        context.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    // ==================== 初始化方法 ====================
    
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("AE Infinity Disk - Common Setup");
        
        // 注册网络数据包
        NetworkHandler.register();
        LOGGER.info("AE Infinity Disk - Registered Network Packets");
        
        // 在主线程上注册 Cell Handler（线程安全）
        event.enqueueWork(() -> {
            // 向 AE2 注册我们的 Cell Handler
            StorageCells.addCellHandler(InfiniteDiskCellHandler.INSTANCE);
            LOGGER.info("AE Infinity Disk - Registered Cell Handler");
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("AE Infinity Disk - Server Starting");
    }
    
    /**
     * 注册调试命令
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        InfinityDiskCommands.register(event.getDispatcher());
        LOGGER.info("AE Infinity Disk - Registered Commands");
    }

    // ==================== 客户端事件 ====================
    
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("AE Infinity Disk - Client Setup");
        }
    }
}
