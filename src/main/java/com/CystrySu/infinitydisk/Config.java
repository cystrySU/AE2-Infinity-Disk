package com.CystrySu.infinitydisk;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 无限磁盘 Mod 配置类
 * 提供软上限、能量倍率等配置选项
 */
@Mod.EventBusSubscriber(modid = Infinitydisk.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    
    // ============== 配置规格 ==============
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    
    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
                .configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }
    
    // ============== 缓存的配置值 ==============
    
    /** 软上限：总物品数量限制，0 表示无限制 */
    public static long softLimit = 0;
    
    /** 类型软上限：不同物品种类限制，0 表示无限制 */
    public static long typeSoftLimit = 0;
    
    /** 能量倍率：每个操作消耗的基础能量乘数 */
    public static double energyMultiplier = 1.0;
    
    /** 基础空闲能耗 (AE/t) */
    public static double baseIdleDrain = 1.0;
    
    /** 每 1000 个存储物品增加的额外能耗 */
    public static double idleDrainPerThousandItems = 0.1;
    
    /** 每 100 种物品类型增加的额外能耗 */
    public static double idleDrainPerHundredTypes = 0.5;
    
    /** 最大空闲能耗上限 (AE/t)，0 表示无上限 */
    public static double maxIdleDrain = 512000.0;
    
    /** 是否启用动态能量消耗（根据物品数量增加能耗） */
    public static boolean enableDynamicEnergyDrain = false;
    
    /** 是否在日志中输出调试信息 */
    public static boolean enableDebugLogging = false;
    
    /**
     * 通用配置类
     */
    public static class CommonConfig {
        
        public final ForgeConfigSpec.LongValue softLimit;
        public final ForgeConfigSpec.LongValue typeSoftLimit;
        public final ForgeConfigSpec.DoubleValue energyMultiplier;
        public final ForgeConfigSpec.DoubleValue baseIdleDrain;
        public final ForgeConfigSpec.DoubleValue idleDrainPerThousandItems;
        public final ForgeConfigSpec.DoubleValue idleDrainPerHundredTypes;
        public final ForgeConfigSpec.DoubleValue maxIdleDrain;
        public final ForgeConfigSpec.BooleanValue enableDynamicEnergyDrain;
        public final ForgeConfigSpec.BooleanValue enableDebugLogging;
        
        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Infinity Disk Configuration")
                   .push("general");
            
            softLimit = builder
                    .comment(
                            "Soft limit for total item count stored in a single disk.",
                            "Set to 0 for truly infinite storage (default).",
                            "When exceeded, new insertions will be rejected."
                    )
                    .defineInRange("softLimit", 0L, 0L, Long.MAX_VALUE);
            
            typeSoftLimit = builder
                    .comment(
                            "Soft limit for the number of different item types stored.",
                            "Set to 0 for unlimited types (default).",
                            "When exceeded, new item types cannot be added."
                    )
                    .defineInRange("typeSoftLimit", 0L, 0L, Long.MAX_VALUE);
            
            builder.pop();
            
            builder.comment("Energy Configuration")
                   .push("energy");
            
            enableDynamicEnergyDrain = builder
                    .comment(
                            "Whether the Infinity Disk energy consumption scales with stored items.",
                            "When FALSE (default): disk uses fixed baseIdleDrain energy.",
                            "When TRUE: energy consumption increases based on stored items and types,",
                            "capped at maxIdleDrain."
                    )
                    .define("enableDynamicEnergyDrain", false);
            
            energyMultiplier = builder
                    .comment(
                            "Multiplier for energy consumption on insert/extract operations.",
                            "Higher values = more energy cost per operation.",
                            "Set to 0 to disable operation energy cost."
                    )
                    .defineInRange("energyMultiplier", 1.0D, 0.0D, 1000.0D);
            
            baseIdleDrain = builder
                    .comment(
                            "Base idle energy drain in AE/tick when the disk is in a drive.",
                            "This is the fixed energy consumption when enableDynamicEnergyDrain is false,",
                            "or the minimum energy consumption when enableDynamicEnergyDrain is true."
                    )
                    .defineInRange("baseIdleDrain", 1.0D, 0.0D, 1000.0D);
            
            idleDrainPerThousandItems = builder
                    .comment(
                            "Additional idle drain (AE/t) per 1000 items stored.",
                            "Only applies when enableDynamicEnergyDrain is true."
                    )
                    .defineInRange("idleDrainPerThousandItems", 0.1D, 0.0D, 100.0D);
            
            idleDrainPerHundredTypes = builder
                    .comment(
                            "Additional idle drain (AE/t) per 100 different item types stored.",
                            "Only applies when enableDynamicEnergyDrain is true."
                    )
                    .defineInRange("idleDrainPerHundredTypes", 0.5D, 0.0D, 100.0D);
            
            maxIdleDrain = builder
                    .comment(
                            "Maximum idle energy drain cap in AE/tick (default: 512000 = 512k AE/t).",
                            "Only applies when enableDynamicEnergyDrain is true.",
                            "Set to 0 for no cap (not recommended)."
                    )
                    .defineInRange("maxIdleDrain", 512000.0D, 0.0D, Double.MAX_VALUE);
            
            builder.pop();
            
            builder.comment("Debug Configuration")
                   .push("debug");
            
            enableDebugLogging = builder
                    .comment("Enable debug logging for troubleshooting.")
                    .define("enableDebugLogging", false);
            
            builder.pop();
        }
    }
    
    /**
     * 配置加载事件处理
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            bakeConfig();
        }
    }
    
    /**
     * 将配置值缓存到静态字段中以便快速访问
     */
    public static void bakeConfig() {
        softLimit = COMMON.softLimit.get();
        typeSoftLimit = COMMON.typeSoftLimit.get();
        energyMultiplier = COMMON.energyMultiplier.get();
        baseIdleDrain = COMMON.baseIdleDrain.get();
        idleDrainPerThousandItems = COMMON.idleDrainPerThousandItems.get();
        idleDrainPerHundredTypes = COMMON.idleDrainPerHundredTypes.get();
        maxIdleDrain = COMMON.maxIdleDrain.get();
        enableDynamicEnergyDrain = COMMON.enableDynamicEnergyDrain.get();
        enableDebugLogging = COMMON.enableDebugLogging.get();
    }
    
    // ============== 便捷方法 ==============
    
    /**
     * 计算当前存储状态的空闲能耗
     * @param totalItems 总物品数
     * @param totalTypes 总类型数
     * @return 空闲能耗 (AE/t)
     */
    public static double calculateIdleDrain(long totalItems, long totalTypes) {
        // 如果未启用动态能耗，返回固定的基础能耗
        if (!enableDynamicEnergyDrain) {
            return baseIdleDrain;
        }
        
        // 计算动态能耗
        double drain = baseIdleDrain;
        drain += (totalItems / 1000.0) * idleDrainPerThousandItems;
        drain += (totalTypes / 100.0) * idleDrainPerHundredTypes;
        
        // 应用能耗上限（如果配置了）
        if (maxIdleDrain > 0 && drain > maxIdleDrain) {
            drain = maxIdleDrain;
        }
        
        return drain;
    }
    
    /**
     * 计算操作的能量消耗
     * @param baseEnergy 基础能量
     * @return 实际能量消耗
     */
    public static double calculateOperationEnergy(double baseEnergy) {
        return baseEnergy * energyMultiplier;
    }
    
    /**
     * 检查是否可以添加更多物品（基于软上限）
     * @param currentCount 当前物品数
     * @param toAdd 要添加的数量
     * @return 是否允许添加
     */
    public static boolean canAddItems(long currentCount, long toAdd) {
        if (softLimit <= 0) {
            return true; // 无限制
        }
        return currentCount + toAdd <= softLimit;
    }
    
    /**
     * 计算在软上限下实际可以添加的数量
     * @param currentCount 当前物品数
     * @param requested 请求添加的数量
     * @return 实际可添加的数量
     */
    public static long getAllowedInsertAmount(long currentCount, long requested) {
        if (softLimit <= 0) {
            return requested; // 无限制
        }
        long remaining = softLimit - currentCount;
        return Math.max(0, Math.min(requested, remaining));
    }
    
    /**
     * 检查是否可以添加新的物品类型
     * @param currentTypes 当前类型数
     * @return 是否允许添加新类型
     */
    public static boolean canAddNewType(long currentTypes) {
        if (typeSoftLimit <= 0) {
            return true; // 无限制
        }
        return currentTypes < typeSoftLimit;
    }
    
    /**
     * 检查软上限是否启用
     */
    public static boolean isSoftLimitEnabled() {
        return softLimit > 0;
    }
    
    /**
     * 检查类型软上限是否启用
     */
    public static boolean isTypeSoftLimitEnabled() {
        return typeSoftLimit > 0;
    }
}
