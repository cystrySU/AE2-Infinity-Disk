package com.CystrySu.infinitydisk.command;

import com.CystrySu.infinitydisk.Config;
import com.CystrySu.infinitydisk.Infinitydisk;
import com.CystrySu.infinitydisk.item.ItemInfiniteDisk;
import com.CystrySu.infinitydisk.storage.InfiniteDiskCellInventory;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 无限磁盘调试命令
 * 提供测试和验证功能
 */
public class InfinityDiskCommands {
    
    /**
     * 注册所有命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("infinitydisk")
                .requires(source -> source.hasPermission(2)) // 需要 OP 权限
                .then(Commands.literal("info")
                    .executes(InfinityDiskCommands::showDiskInfo))
                .then(Commands.literal("config")
                    .executes(InfinityDiskCommands::showConfig))
                .then(Commands.literal("verify")
                    .executes(InfinityDiskCommands::verifyPersistence))
                .then(Commands.literal("give")
                    .executes(InfinityDiskCommands::giveDisk))
        );
    }
    
    /**
     * 显示手持磁盘的信息
     */
    private static int showDiskInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        
        if (!(mainHand.getItem() instanceof ItemInfiniteDisk)) {
            source.sendFailure(Component.literal("You must hold an Infinity Disk"));
            return 0;
        }
        
        // 获取磁盘数据
        InfiniteDiskCellInventory inventory = new InfiniteDiskCellInventory(mainHand, null);
        UUID diskId = ItemInfiniteDisk.getUUID(mainHand);
        
        source.sendSuccess(Component.literal("=== Infinity Disk Info ===").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(Component.literal("UUID: " + (diskId != null ? diskId.toString() : "None")).withStyle(ChatFormatting.GRAY), false);
        source.sendSuccess(Component.literal("Total Items: " + inventory.getTotalItemCount()).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(Component.literal("Distinct Types: " + inventory.getDistinctTypeCount()).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(Component.literal("Used Bytes: " + inventory.getUsedBytes()).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(Component.literal("Idle Drain: " + String.format("%.2f AE/t", inventory.getIdleDrain())).withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(Component.literal("Cell State: " + inventory.getStatus().name()).withStyle(ChatFormatting.GREEN), false);
        source.sendSuccess(Component.literal("Has NBT Storage: " + (mainHand.getTag() != null && mainHand.getTag().contains("infinity_storage"))).withStyle(ChatFormatting.YELLOW), false);
        
        return 1;
    }
    
    /**
     * 显示当前配置
     */
    private static int showConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(Component.literal("=== Infinity Disk Config ===").withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(Component.literal("Soft Limit: " + (Config.softLimit <= 0 ? "Unlimited" : Config.softLimit)).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(Component.literal("Type Soft Limit: " + (Config.typeSoftLimit <= 0 ? "Unlimited" : Config.typeSoftLimit)).withStyle(ChatFormatting.WHITE), false);
        source.sendSuccess(Component.literal("Energy Enabled: " + Config.enableEnergyConsumption).withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(Component.literal("Energy Multiplier: " + Config.energyMultiplier).withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(Component.literal("Base Idle Drain: " + Config.baseIdleDrain + " AE/t").withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(Component.literal("Idle Drain per 1K Items: " + Config.idleDrainPerThousandItems + " AE/t").withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(Component.literal("Idle Drain per 100 Types: " + Config.idleDrainPerHundredTypes + " AE/t").withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(Component.literal("Debug Logging: " + Config.enableDebugLogging).withStyle(ChatFormatting.YELLOW), false);
        
        return 1;
    }
    
    /**
     * 验证 NBT 持久化
     */
    private static int verifyPersistence(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        
        if (!(mainHand.getItem() instanceof ItemInfiniteDisk)) {
            source.sendFailure(Component.literal("You must hold an Infinity Disk"));
            return 0;
        }
        
        source.sendSuccess(Component.literal("=== NBT Persistence Verification ===").withStyle(ChatFormatting.GOLD), false);
        
        // 检查 NBT 标签
        if (mainHand.getTag() == null) {
            source.sendSuccess(Component.literal("✗ No NBT tag present").withStyle(ChatFormatting.RED), false);
            return 0;
        }
        
        source.sendSuccess(Component.literal("✓ NBT tag present").withStyle(ChatFormatting.GREEN), false);
        
        // 检查 UUID
        if (mainHand.getTag().hasUUID("disk_uuid")) {
            UUID uuid = mainHand.getTag().getUUID("disk_uuid");
            source.sendSuccess(Component.literal("✓ UUID persisted: " + uuid.toString().substring(0, 8) + "...").withStyle(ChatFormatting.GREEN), false);
        } else {
            source.sendSuccess(Component.literal("✗ UUID not persisted").withStyle(ChatFormatting.RED), false);
        }
        
        // 检查存储数据
        if (mainHand.getTag().contains("infinity_storage")) {
            source.sendSuccess(Component.literal("✓ Storage data present").withStyle(ChatFormatting.GREEN), false);
            
            // 尝试加载并验证
            try {
                InfiniteDiskCellInventory inventory = new InfiniteDiskCellInventory(mainHand, null);
                source.sendSuccess(Component.literal("✓ Storage data readable").withStyle(ChatFormatting.GREEN), false);
                source.sendSuccess(Component.literal("  - Items: " + inventory.getTotalItemCount()).withStyle(ChatFormatting.GRAY), false);
                source.sendSuccess(Component.literal("  - Types: " + inventory.getDistinctTypeCount()).withStyle(ChatFormatting.GRAY), false);
            } catch (Exception e) {
                source.sendSuccess(Component.literal("✗ Storage data corrupt: " + e.getMessage()).withStyle(ChatFormatting.RED), false);
            }
        } else {
            source.sendSuccess(Component.literal("○ No storage data (disk is empty)").withStyle(ChatFormatting.YELLOW), false);
        }
        
        source.sendSuccess(Component.literal("=== Verification Complete ===").withStyle(ChatFormatting.GOLD), false);
        
        return 1;
    }
    
    /**
     * 给予玩家一个新的无限磁盘
     */
    private static int giveDisk(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        ItemStack disk = new ItemStack(Infinitydisk.INFINITY_DISK.get());
        ItemInfiniteDisk.getOrCreateUUID(disk);
        
        if (player.getInventory().add(disk)) {
            source.sendSuccess(Component.literal("Given 1 Infinity Disk").withStyle(ChatFormatting.GREEN), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("Could not give disk - inventory full?"));
            return 0;
        }
    }
}
