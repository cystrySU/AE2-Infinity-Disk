package com.CystrySu.infinitydisk.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.CystrySu.infinitydisk.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/**
 * 无限磁盘存储实现
 * 实现 AE2 的 MEStorage 接口，提供无限容量和无限种类的存储
 * 已集成配置系统，支持软上限和类型限制
 */
public class InfiniteDiskStorage implements MEStorage {
    
    private final InfiniteDiskManager manager;
    
    public InfiniteDiskStorage(InfiniteDiskManager manager) {
        this.manager = manager;
    }

    /**
     * 插入物品/流体到存储
     * @param what 要插入的 AEKey
     * @param amount 数量
     * @param mode SIMULATE 或 MODULATE
     * @param source 操作来源
     * @return 实际插入的数量
     */
    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what == null || amount <= 0) {
            return 0;
        }
        
        // 检查是否是新类型，以及是否允许添加新类型
        boolean isNewType = manager.getAmount(what) == 0;
        if (isNewType && !manager.canAddNewType()) {
            // 类型限制已达到
            return 0;
        }
        
        // 计算在软上限下可以插入的数量
        long allowedAmount = manager.getAllowedInsertAmount(amount);
        if (allowedAmount <= 0) {
            return 0;
        }
        
        // 模拟模式只返回可插入数量，不实际执行
        if (mode == Actionable.SIMULATE) {
            return allowedAmount;
        }
        
        // 实际执行插入（使用 insertUnchecked 因为我们已经检查过限制）
        return manager.insertUnchecked(what, allowedAmount);
    }

    /**
     * 从存储中提取物品/流体
     * @param what 要提取的 AEKey
     * @param amount 数量
     * @param mode SIMULATE 或 MODULATE
     * @param source 操作来源
     * @return 实际提取的数量
     */
    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what == null || amount <= 0) {
            return 0;
        }
        
        long available = manager.getAmount(what);
        long toExtract = Math.min(available, amount);
        
        if (toExtract <= 0) {
            return 0;
        }
        
        // 模拟模式只返回可提取数量，不实际执行
        if (mode == Actionable.SIMULATE) {
            return toExtract;
        }
        
        // 实际执行提取
        return manager.extract(what, toExtract);
    }

    /**
     * 获取存储中的所有物品列表
     * @param out 输出计数器
     */
    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var entry : manager.getStorage()) {
            out.add(entry.getKey(), entry.getLongValue());
        }
    }

    /**
     * 获取存储描述
     */
    @Override
    public Component getDescription() {
        return Component.translatable("item.ae_infinity_disk.infinity_disk");
    }

    // ==================== 辅助方法 ====================

    /**
     * 从 NBT 加载数据
     */
    public void load(CompoundTag tag) {
        manager.load(tag);
    }

    /**
     * 保存数据到 NBT
     */
    public CompoundTag save() {
        return manager.save();
    }

    /**
     * 获取存储的总物品数量
     */
    public long getTotalItemCount() {
        return manager.getTotalCount();
    }

    /**
     * 获取存储的不同物品种类数
     */
    public long getDistinctTypeCount() {
        return manager.getDistinctTypeCount();
    }

    /**
     * 获取底层管理器
     */
    public InfiniteDiskManager getManager() {
        return manager;
    }
    
    /**
     * 计算当前存储的空闲能耗
     * @return 空闲能耗 (AE/t)
     */
    public double calculateIdleDrain() {
        return manager.calculateIdleDrain();
    }
    
    /**
     * 获取存储使用百分比
     * @return 使用百分比，无限制时返回 -1
     */
    public double getUsagePercentage() {
        return manager.getUsagePercentage();
    }
    
    /**
     * 获取类型使用百分比
     * @return 使用百分比，无限制时返回 -1
     */
    public double getTypeUsagePercentage() {
        return manager.getTypeUsagePercentage();
    }
    
    /**
     * 检查是否配置了软上限
     */
    public boolean hasSoftLimit() {
        return Config.isSoftLimitEnabled();
    }
    
    /**
     * 检查是否配置了类型软上限
     */
    public boolean hasTypeSoftLimit() {
        return Config.isTypeSoftLimitEnabled();
    }
}
