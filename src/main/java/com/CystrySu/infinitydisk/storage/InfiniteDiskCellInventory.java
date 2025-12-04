package com.CystrySu.infinitydisk.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import com.CystrySu.infinitydisk.Config;
import com.CystrySu.infinitydisk.item.ItemInfiniteDisk;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 无限磁盘 Cell Inventory
 * 实现 StorageCell 接口，处理 AE2 存储系统的所有存储操作
 * 已集成配置系统，支持能量消耗和软上限
 */
public class InfiniteDiskCellInventory implements StorageCell {
    
    private static final String TAG_STORAGE = "infinity_storage";
    
    private final ItemStack stack;
    @Nullable
    private final ISaveProvider saveProvider;
    private final InfiniteDiskStorage storage;
    private boolean isPersisted = true;
    private final UUID diskUUID;

    public InfiniteDiskCellInventory(ItemStack stack, @Nullable ISaveProvider saveProvider) {
        this.stack = stack;
        this.saveProvider = saveProvider;
        
        // 获取或创建磁盘 UUID
        this.diskUUID = ItemInfiniteDisk.getOrCreateUUID(stack);
        
        // 创建管理器和存储
        InfiniteDiskManager manager = new InfiniteDiskManager();
        this.storage = new InfiniteDiskStorage(manager);
        
        // 从 ItemStack 的 NBT 加载数据
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_STORAGE)) {
            storage.load(tag.getCompound(TAG_STORAGE));
        }
    }
    
    /**
     * 获取磁盘 UUID
     */
    public UUID getDiskUUID() {
        return diskUUID;
    }

    @Override
    public CellState getStatus() {
        long types = storage.getDistinctTypeCount();
        long items = storage.getTotalItemCount();
        
        if (types == 0 && items == 0) {
            return CellState.EMPTY;
        }
        
        // 检查是否接近或达到软上限
        long softLimit = Config.softLimit;
        long typeSoftLimit = Config.typeSoftLimit;
        
        // 如果配置了软上限，根据使用率显示状态
        if (softLimit > 0) {
            double usagePercent = (items * 100.0) / softLimit;
            if (usagePercent >= 100) {
                return CellState.FULL;
            } else if (usagePercent >= 90) {
                return CellState.TYPES_FULL;
            } else if (usagePercent >= 75) {
                return CellState.NOT_EMPTY;
            }
        }
        
        if (typeSoftLimit > 0) {
            double typeUsagePercent = (types * 100.0) / typeSoftLimit;
            if (typeUsagePercent >= 100) {
                return CellState.TYPES_FULL;
            }
        }
        
        // 无限磁盘默认状态：根据存储量显示
        if (types > 1000 || items > 1000000) {
            return CellState.TYPES_FULL; // 显示为几乎满的状态
        }
        
        return CellState.NOT_EMPTY;
    }

    /**
     * 获取空闲能耗 - 使用配置系统计算
     */
    @Override
    public double getIdleDrain() {
        if (!Config.enableEnergyConsumption) {
            return 0.0;
        }
        
        // 使用配置系统计算动态能耗
        return Config.calculateIdleDrain(
            storage.getTotalItemCount(), 
            storage.getDistinctTypeCount()
        );
    }

    /**
     * 插入物品
     */
    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        long inserted = storage.insert(what, amount, mode, source);
        
        if (inserted > 0 && mode == Actionable.MODULATE) {
            saveChanges();
        }
        
        return inserted;
    }

    /**
     * 提取物品
     */
    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        long extracted = storage.extract(what, amount, mode, source);
        
        if (extracted > 0 && mode == Actionable.MODULATE) {
            saveChanges();
        }
        
        return extracted;
    }

    /**
     * 获取可用物品列表
     */
    @Override
    public void getAvailableStacks(KeyCounter out) {
        storage.getAvailableStacks(out);
    }

    @Override
    public Component getDescription() {
        return Component.translatable("item.ae_infinity_disk.infinity_disk");
    }

    /**
     * 检查此 Cell 是否持久化到磁盘
     */
     public boolean isPersisted() {
         return isPersisted;
     }

     /**
      * 持久化数据
      */
     public void persist() {
         if (isPersisted) {
             return;
         }
         saveToNBT();
         isPersisted = true;
     }

    /**
     * 保存更改（使用脏标记优化，仅在数据实际变化时保存）
     */
    private void saveChanges() {
        // 检查管理器的脏标记，避免不必要的序列化
        if (storage.getManager().isDirty()) {
            isPersisted = false;
            saveToNBT();
            storage.getManager().clearDirty();
            
            if (saveProvider != null) {
                saveProvider.saveChanges();
            }
        }
    }

    /**
     * 保存数据到 ItemStack 的 NBT
     */
    private void saveToNBT() {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_STORAGE, storage.save());
    }

    /**
     * 强制保存（忽略脏标记）
     */
    public void forceSave() {
        saveToNBT();
        storage.getManager().clearDirty();
        isPersisted = true;
    }

    /**
     * 获取内部存储
     */
    public InfiniteDiskStorage getInternalStorage() {
        return storage;
    }

    /**
     * 获取总存储数量
     */
    public long getTotalItemCount() {
        return storage.getTotalItemCount();
    }

    /**
     * 获取存储的种类数
     */
    public long getDistinctTypeCount() {
        return storage.getDistinctTypeCount();
    }
    
    /**
     * 获取已使用字节数（按 AE2 规则）
     */
    public long getUsedBytes() {
        return ItemInfiniteDisk.calculateUsedBytes(getTotalItemCount(), getDistinctTypeCount());
    }
    
    /**
     * 获取存储使用百分比（如果配置了软上限）
     * @return 使用百分比 (0-100)，无限制时返回 -1
     */
    public double getUsagePercentage() {
        long limit = Config.softLimit;
        if (limit <= 0) {
            return -1;
        }
        return (getTotalItemCount() * 100.0) / limit;
    }
    
    /**
     * 获取类型使用百分比（如果配置了类型软上限）
     * @return 使用百分比 (0-100)，无限制时返回 -1
     */
    public double getTypeUsagePercentage() {
        long limit = Config.typeSoftLimit;
        if (limit <= 0) {
            return -1;
        }
        return (getDistinctTypeCount() * 100.0) / limit;
    }
}
