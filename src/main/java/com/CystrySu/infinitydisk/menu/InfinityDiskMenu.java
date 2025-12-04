package com.CystrySu.infinitydisk.menu;

import com.CystrySu.infinitydisk.Infinitydisk;
import com.CystrySu.infinitydisk.item.ItemInfiniteDisk;
import com.CystrySu.infinitydisk.storage.InfiniteDiskCellInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

/**
 * 无限磁盘信息菜单
 * 用于显示磁盘的存储统计信息
 */
public class InfinityDiskMenu extends AbstractContainerMenu {
    
    // ==================== 菜单注册 ====================
    
    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Infinitydisk.MODID);
    
    public static final RegistryObject<MenuType<InfinityDiskMenu>> INFINITY_DISK_MENU = 
            MENUS.register("infinity_disk_menu", 
                    () -> IForgeMenuType.create(InfinityDiskMenu::new));
    
    // ==================== 字段 ====================
    
    /** 持有的磁盘物品 */
    private final ItemStack diskStack;
    
    /** 玩家引用 */
    private final Player player;
    
    /** 使用的手 */
    private final InteractionHand hand;
    
    /**
     * 容器数据 - 用于同步到客户端
     * data[0-3]: totalItems (long, split into 4 shorts)
     * data[4-7]: distinctTypes (long, split into 4 shorts)
     */
    private final ContainerData data;
    
    // 缓存的统计数据
    private long totalItems = 0;
    private long distinctTypes = 0;
    private UUID diskId = null;
    
    // ==================== 构造函数 ====================
    
    /**
     * 服务端构造函数
     */
    public InfinityDiskMenu(int containerId, Inventory playerInventory, ItemStack diskStack, InteractionHand hand) {
        super(INFINITY_DISK_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.diskStack = diskStack;
        this.hand = hand;
        
        // 创建数据容器（8个short = 2个long）
        this.data = new SimpleContainerData(8);
        this.addDataSlots(data);
        
        // 初始化数据
        refreshData();
    }
    
    /**
     * 客户端构造函数（从网络缓冲区）
     */
    public InfinityDiskMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(INFINITY_DISK_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.hand = extraData.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        this.diskStack = playerInventory.player.getItemInHand(this.hand);
        
        // 从网络数据读取
        this.totalItems = extraData.readLong();
        this.distinctTypes = extraData.readLong();
        if (extraData.readBoolean()) {
            this.diskId = extraData.readUUID();
        }
        
        this.data = new SimpleContainerData(8);
        this.addDataSlots(data);
    }
    
    // ==================== 数据同步 ====================
    
    /**
     * 刷新磁盘数据
     */
    public void refreshData() {
        if (!player.level.isClientSide() && !diskStack.isEmpty()) {
            InfiniteDiskCellInventory inventory = new InfiniteDiskCellInventory(diskStack, null);
            this.totalItems = inventory.getTotalItemCount();
            this.distinctTypes = inventory.getDistinctTypeCount();
            this.diskId = ItemInfiniteDisk.getUUID(diskStack);
            
            // 将 long 拆分为 short 存储
            encodeLongToData(totalItems, 0);
            encodeLongToData(distinctTypes, 4);
        }
    }
    
    /**
     * 将 long 编码到 ContainerData（使用4个short）
     */
    private void encodeLongToData(long value, int startIndex) {
        data.set(startIndex, (int) (value & 0xFFFF));
        data.set(startIndex + 1, (int) ((value >> 16) & 0xFFFF));
        data.set(startIndex + 2, (int) ((value >> 32) & 0xFFFF));
        data.set(startIndex + 3, (int) ((value >> 48) & 0xFFFF));
    }
    
    /**
     * 从 ContainerData 解码 long
     */
    private long decodeLongFromData(int startIndex) {
        return ((long) data.get(startIndex) & 0xFFFF)
                | (((long) data.get(startIndex + 1) & 0xFFFF) << 16)
                | (((long) data.get(startIndex + 2) & 0xFFFF) << 32)
                | (((long) data.get(startIndex + 3) & 0xFFFF) << 48);
    }
    
    // ==================== Getters ====================
    
    /**
     * 获取总物品数量
     */
    public long getTotalItems() {
        if (player.level.isClientSide()) {
            return decodeLongFromData(0);
        }
        return totalItems;
    }
    
    /**
     * 获取不同种类数量
     */
    public long getDistinctTypes() {
        if (player.level.isClientSide()) {
            return decodeLongFromData(4);
        }
        return distinctTypes;
    }
    
    /**
     * 获取已使用字节数
     */
    public long getUsedBytes() {
        return ItemInfiniteDisk.calculateUsedBytes(getTotalItems(), getDistinctTypes());
    }
    
    /**
     * 获取磁盘 UUID
     */
    public UUID getDiskId() {
        return diskId;
    }
    
    /**
     * 获取磁盘物品
     */
    public ItemStack getDiskStack() {
        return diskStack;
    }
    
    // ==================== AbstractContainerMenu ====================
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 没有物品槽，不需要快速移动
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        // 检查玩家是否还持有磁盘
        ItemStack currentStack = player.getItemInHand(hand);
        return !currentStack.isEmpty() && currentStack.getItem() instanceof ItemInfiniteDisk;
    }
    
    @Override
    public void broadcastChanges() {
        // 每次同步时刷新数据
        refreshData();
        super.broadcastChanges();
    }
}
