package com.CystrySu.infinitydisk.item;

import com.CystrySu.infinitydisk.menu.InfinityDiskMenu;
import com.CystrySu.infinitydisk.storage.InfiniteDiskCellInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 无限磁盘物品
 * 可以放入 AE2 的 ME 驱动器中，提供无限存储容量
 */
public class ItemInfiniteDisk extends Item {
    
    private static final String TAG_STORAGE = "infinity_storage";
    private static final String TAG_UUID = "disk_uuid";
    
    /**
     * AE2 字节计算规则：
     * - 每个物品类型占用 8 字节（用于存储类型信息）
     * - 每个物品占用 1 字节
     */
    public static final int BYTES_PER_TYPE = 8;
    public static final int BYTES_PER_ITEM = 1;

    public ItemInfiniteDisk(Properties properties) {
        super(properties);
    }
    
    // ==================== 交互功能 ====================
    
    /**
     * 右键使用磁盘，打开信息界面
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // 在服务端打开 GUI
            openDiskGui(serverPlayer, stack, hand);
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    
    /**
     * 打开磁盘 GUI
     */
    private void openDiskGui(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        // 获取磁盘数据
        InfiniteDiskCellInventory inventory = new InfiniteDiskCellInventory(stack, null);
        long totalItems = inventory.getTotalItemCount();
        long distinctTypes = inventory.getDistinctTypeCount();
        UUID diskId = getOrCreateUUID(stack);
        
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.ae_infinity_disk.title");
            }
            
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new InfinityDiskMenu(containerId, playerInventory, stack, hand);
            }
        }, (FriendlyByteBuf buf) -> {
            buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
            buf.writeLong(totalItems);
            buf.writeLong(distinctTypes);
            buf.writeBoolean(diskId != null);
            if (diskId != null) {
                buf.writeUUID(diskId);
            }
        });
    }
    
    /**
     * 获取或创建磁盘 UUID
     */
    public static UUID getOrCreateUUID(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        
        if (tag.hasUUID(TAG_UUID)) {
            return tag.getUUID(TAG_UUID);
        } else {
            UUID uuid = UUID.randomUUID();
            tag.putUUID(TAG_UUID, uuid);
            return uuid;
        }
    }
    
    /**
     * 获取磁盘 UUID（如果存在）
     */
    @Nullable
    public static UUID getUUID(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID(TAG_UUID)) {
            return tag.getUUID(TAG_UUID);
        }
        return null;
    }
    
    /**
     * 计算已使用字节数（按 AE2 规则）
     * @param totalItems 总物品数量
     * @param distinctTypes 物品种类数
     * @return 已使用字节数
     */
    public static long calculateUsedBytes(long totalItems, long distinctTypes) {
        // 每个类型 8 字节 + 每个物品 1 字节
        return (distinctTypes * BYTES_PER_TYPE) + (totalItems / BYTES_PER_ITEM);
    }

    /**
     * 添加物品提示信息
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_STORAGE)) {
            // 创建临时存储以读取数据
            InfiniteDiskCellInventory inventory = new InfiniteDiskCellInventory(stack, null);
            
            long totalItems = inventory.getTotalItemCount();
            long distinctTypes = inventory.getDistinctTypeCount();
            long usedBytes = calculateUsedBytes(totalItems, distinctTypes);
            
            // 显示字节使用量
            tooltip.add(Component.translatable("tooltip.ae_infinity_disk.used_bytes", 
                    formatBytes(usedBytes))
                    .withStyle(ChatFormatting.GRAY));
            
            // 显示物品种类数
            tooltip.add(Component.translatable("tooltip.ae_infinity_disk.stored_types", 
                    formatNumber(distinctTypes))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.ae_infinity_disk.empty")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        
        // 显示 UUID（如果启用高级提示）
        if (flag.isAdvanced()) {
            UUID uuid = getUUID(stack);
            if (uuid != null) {
                tooltip.add(Component.translatable("tooltip.ae_infinity_disk.uuid", 
                        uuid.toString().substring(0, 8) + "...")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        
        // 添加无限容量说明
        tooltip.add(Component.translatable("tooltip.ae_infinity_disk.infinite_capacity")
                .withStyle(ChatFormatting.GOLD));
    }
    
    /**
     * 格式化字节显示（按 AE2 风格）
     */
    private String formatBytes(long bytes) {
        if (bytes >= 1_099_511_627_776L) { // >= 1 TB
            return String.format("%.2f TB", bytes / 1_099_511_627_776.0);
        } else if (bytes >= 1_073_741_824L) { // >= 1 GB
            return String.format("%.2f GB", bytes / 1_073_741_824.0);
        } else if (bytes >= 1_048_576L) { // >= 1 MB
            return String.format("%.2f MB", bytes / 1_048_576.0);
        } else if (bytes >= 1024L) { // >= 1 KB
            return String.format("%.2f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }

    /**
     * 格式化数字显示
     */
    private String formatNumber(long number) {
        if (number >= 1_000_000_000_000L) {
            return String.format("%.2fT", number / 1_000_000_000_000.0);
        } else if (number >= 1_000_000_000L) {
            return String.format("%.2fB", number / 1_000_000_000.0);
        } else if (number >= 1_000_000L) {
            return String.format("%.2fM", number / 1_000_000.0);
        } else if (number >= 1_000L) {
            return String.format("%.2fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }
    
    /**
     * 物品首次进入物品栏时自动创建 UUID
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        
        // 确保磁盘有 UUID
        if (!level.isClientSide()) {
            getOrCreateUUID(stack);
        }
    }
    
    /**
     * 物品制作时自动创建 UUID
     */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, net.minecraft.world.entity.player.Player player) {
        super.onCraftedBy(stack, level, player);
        
        if (!level.isClientSide()) {
            getOrCreateUUID(stack);
        }
    }

    /**
     * 使物品在物品栏中显示为有附魔效果（表示特殊物品）
     */
    @Override
    public boolean isFoil(ItemStack stack) {
        // 如果磁盘内有数据，显示附魔光效
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_STORAGE);
    }

    /**
     * 防止物品堆叠
     */
    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }
}
