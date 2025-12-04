package com.CystrySu.infinitydisk.storage;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import com.CystrySu.infinitydisk.item.ItemInfiniteDisk;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 无限磁盘 Cell Handler
 * 向 AE2 注册我们的无限磁盘，使其能够被 ME 驱动器识别
 */
public class InfiniteDiskCellHandler implements ICellHandler {
    
    public static final InfiniteDiskCellHandler INSTANCE = new InfiniteDiskCellHandler();
    
    private InfiniteDiskCellHandler() {
        // 私有构造函数，使用单例
    }

    /**
     * 检查此 ItemStack 是否是我们的无限磁盘
     */
    @Override
    public boolean isCell(ItemStack is) {
        return !is.isEmpty() && is.getItem() instanceof ItemInfiniteDisk;
    }

    /**
     * 获取磁盘的存储单元
     * @param is 磁盘物品
     * @param host 保存提供者（用于在数据变化时标记脏数据）
     * @return StorageCell 实现，如果不是有效磁盘则返回 null
     */
    @Nullable
    @Override
    public StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
        if (!isCell(is)) {
            return null;
        }
        
        return new InfiniteDiskCellInventory(is, host);
    }
}
