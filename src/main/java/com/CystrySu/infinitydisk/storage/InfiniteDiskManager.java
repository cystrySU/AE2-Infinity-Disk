package com.CystrySu.infinitydisk.storage;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.CystrySu.infinitydisk.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 无限磁盘数据管理器
 * 负责维护虚拟存储池、软上限配置以及 NBT 序列化
 * 现已集成全局配置系统
 */
public class InfiniteDiskManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InfiniteDiskManager.class);
    
    /** 存储所有物品/流体的计数器，使用 AE2 的 KeyCounter */
    private final KeyCounter storage = new KeyCounter();
    
    /** 
     * 实例级软上限覆盖，-1 表示使用全局配置
     * 允许每个磁盘有不同的限制（如果需要）
     */
    private long instanceSoftLimit = -1;
    
    /** 
     * 实例级类型软上限覆盖，-1 表示使用全局配置 
     */
    private long instanceTypeSoftLimit = -1;
    
    // ==================== 性能优化：缓存变量 ====================
    
    /** 缓存的总物品数量，避免每次遍历计算 */
    private long cachedTotalCount = 0;
    
    /** 缓存是否有效 */
    private boolean totalCountCacheValid = false;
    
    /** 脏标记：数据是否已修改但未保存 */
    private boolean dirty = false;
    
    /** NBT 标签名称常量 */
    private static final String TAG_SOFT_LIMIT = "softLimit";
    private static final String TAG_TYPE_SOFT_LIMIT = "typeSoftLimit";
    private static final String TAG_ITEMS = "items";
    private static final String TAG_KEY = "key";
    private static final String TAG_COUNT = "count";

    /**
     * 设置实例级软上限
     * @param softLimit 软上限值，-1 使用全局配置，0 表示无限制
     */
    public void setInstanceSoftLimit(long softLimit) {
        this.instanceSoftLimit = softLimit;
        if (Config.enableDebugLogging) {
            LOGGER.debug("Instance soft limit set to: {}", softLimit);
        }
    }

    /**
     * 获取有效的软上限（考虑实例覆盖和全局配置）
     */
    public long getEffectiveSoftLimit() {
        if (instanceSoftLimit >= 0) {
            return instanceSoftLimit;
        }
        return Config.softLimit;
    }

    /**
     * 设置实例级类型软上限
     * @param typeSoftLimit 类型软上限值，-1 使用全局配置，0 表示无限制
     */
    public void setInstanceTypeSoftLimit(long typeSoftLimit) {
        this.instanceTypeSoftLimit = typeSoftLimit;
        if (Config.enableDebugLogging) {
            LOGGER.debug("Instance type soft limit set to: {}", typeSoftLimit);
        }
    }

    /**
     * 获取有效的类型软上限
     */
    public long getEffectiveTypeSoftLimit() {
        if (instanceTypeSoftLimit >= 0) {
            return instanceTypeSoftLimit;
        }
        return Config.typeSoftLimit;
    }

    /**
     * 插入物品/流体（应用配置限制）
     * @param key AE2 的 AEKey（物品或流体）
     * @param amount 数量
     * @return 实际插入的数量
     */
    public long insert(AEKey key, long amount) {
        if (key == null || amount <= 0) {
            return 0;
        }
        
        // 检查是否是新类型
        boolean isNewType = storage.get(key) == 0;
        
        // 检查类型限制
        if (isNewType && !canAddNewType()) {
            if (Config.enableDebugLogging) {
                LOGGER.debug("Cannot insert {}: type limit reached ({}/{})", 
                    key, getDistinctTypeCount(), getEffectiveTypeSoftLimit());
            }
            return 0;
        }
        
        // 计算在软上限下可以插入的数量
        long allowedAmount = getAllowedInsertAmount(amount);
        
        if (allowedAmount <= 0) {
            if (Config.enableDebugLogging) {
                LOGGER.debug("Cannot insert {}: soft limit reached ({}/{})", 
                    key, getTotalCount(), getEffectiveSoftLimit());
            }
            return 0;
        }
        
        storage.add(key, allowedAmount);
        
        // 更新缓存
        if (totalCountCacheValid) {
            cachedTotalCount += allowedAmount;
        }
        dirty = true;
        
        if (Config.enableDebugLogging) {
            LOGGER.debug("Inserted {} x {} (requested: {})", key, allowedAmount, amount);
        }
        
        return allowedAmount;
    }
    
    /**
     * 插入物品/流体（不检查限制，用于强制插入）
     * @param key AE2 的 AEKey
     * @param amount 数量
     * @return 实际插入的数量
     */
    public long insertUnchecked(AEKey key, long amount) {
        if (key == null || amount <= 0) {
            return 0;
        }
        storage.add(key, amount);
        
        // 更新缓存
        if (totalCountCacheValid) {
            cachedTotalCount += amount;
        }
        dirty = true;
        
        return amount;
    }

    /**
     * 提取物品/流体
     * @param key AE2 的 AEKey
     * @param amount 请求提取的数量
     * @return 实际提取的数量
     */
    public long extract(AEKey key, long amount) {
        if (key == null || amount <= 0) {
            return 0;
        }
        
        long available = storage.get(key);
        long extracted = Math.min(available, amount);
        
        if (extracted > 0) {
            storage.remove(key, extracted);
            
            // 更新缓存
            if (totalCountCacheValid) {
                cachedTotalCount -= extracted;
            }
            dirty = true;
            
            if (Config.enableDebugLogging) {
                LOGGER.debug("Extracted {} x {} (requested: {})", key, extracted, amount);
            }
        }
        
        return extracted;
    }

    /**
     * 获取指定 Key 的存储数量
     */
    public long getAmount(AEKey key) {
        return storage.get(key);
    }

    /**
     * 检查是否超过软上限
     * @param pendingAddition 待添加的数量
     * @return 如果超过软上限返回 true
     */
    public boolean exceedsSoftLimit(long pendingAddition) {
        long limit = getEffectiveSoftLimit();
        return limit > 0 && getTotalCount() + pendingAddition > limit;
    }
    
    /**
     * 检查是否可以添加新的物品类型
     * @return 如果可以添加新类型返回 true
     */
    public boolean canAddNewType() {
        long limit = getEffectiveTypeSoftLimit();
        if (limit <= 0) {
            return true; // 无限制
        }
        return getDistinctTypeCount() < limit;
    }
    
    /**
     * 计算在软上限下允许插入的数量
     * @param requested 请求插入的数量
     * @return 实际允许插入的数量
     */
    public long getAllowedInsertAmount(long requested) {
        long limit = getEffectiveSoftLimit();
        if (limit <= 0) {
            return requested; // 无限制
        }
        long remaining = limit - getTotalCount();
        return Math.max(0, Math.min(requested, remaining));
    }

    /**
     * 获取存储的总物品数量（使用缓存优化）
     */
    public long getTotalCount() {
        if (!totalCountCacheValid) {
            // 缓存失效，重新计算
            cachedTotalCount = 0;
            for (var entry : storage) {
                cachedTotalCount += entry.getLongValue();
            }
            totalCountCacheValid = true;
        }
        return cachedTotalCount;
    }
    
    /**
     * 使缓存失效（当外部直接修改存储时调用）
     */
    public void invalidateCache() {
        totalCountCacheValid = false;
    }
    
    /**
     * 检查数据是否已修改
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * 清除脏标记（保存后调用）
     */
    public void clearDirty() {
        dirty = false;
    }

    /**
     * 获取存储的不同物品种类数
     */
    public long getDistinctTypeCount() {
        return storage.size();
    }

    /**
     * 获取底层存储的只读视图
     */
    public KeyCounter getStorage() {
        return storage;
    }

    /**
     * 清空所有存储
     */
    public void clear() {
        storage.clear();
        cachedTotalCount = 0;
        totalCountCacheValid = true;
        dirty = true;
        if (Config.enableDebugLogging) {
            LOGGER.debug("Storage cleared");
        }
    }
    
    /**
     * 计算当前存储状态的空闲能耗
     * @return 空闲能耗 (AE/t)
     */
    public double calculateIdleDrain() {
        return Config.calculateIdleDrain(getTotalCount(), getDistinctTypeCount());
    }
    
    /**
     * 获取存储使用百分比（如果有软上限）
     * @return 使用百分比 (0-100)，无限制时返回 -1
     */
    public double getUsagePercentage() {
        long limit = getEffectiveSoftLimit();
        if (limit <= 0) {
            return -1; // 无限制
        }
        return (getTotalCount() * 100.0) / limit;
    }
    
    /**
     * 获取类型使用百分比（如果有类型软上限）
     * @return 使用百分比 (0-100)，无限制时返回 -1
     */
    public double getTypeUsagePercentage() {
        long limit = getEffectiveTypeSoftLimit();
        if (limit <= 0) {
            return -1; // 无限制
        }
        return (getDistinctTypeCount() * 100.0) / limit;
    }

    /**
     * 保存到 NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        
        // 保存实例级覆盖（仅当设置了时）
        if (instanceSoftLimit >= 0) {
            tag.putLong(TAG_SOFT_LIMIT, instanceSoftLimit);
        }
        if (instanceTypeSoftLimit >= 0) {
            tag.putLong(TAG_TYPE_SOFT_LIMIT, instanceTypeSoftLimit);
        }
        
        ListTag itemList = new ListTag();
        for (var entry : storage) {
            AEKey key = entry.getKey();
            long count = entry.getLongValue();
            
            if (key != null && count > 0) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.put(TAG_KEY, key.toTagGeneric());
                itemTag.putLong(TAG_COUNT, count);
                itemList.add(itemTag);
            }
        }
        tag.put(TAG_ITEMS, itemList);
        
        if (Config.enableDebugLogging) {
            LOGGER.debug("Saved {} item types to NBT", itemList.size());
        }
        
        return tag;
    }

    /**
     * 从 NBT 加载
     */
    public void load(CompoundTag tag) {
        storage.clear();
        totalCountCacheValid = false; // 加载后需要重新计算缓存
        dirty = false; // 刚加载的数据是干净的
        
        if (tag == null) {
            return;
        }
        
        // 加载实例级覆盖
        if (tag.contains(TAG_SOFT_LIMIT)) {
            instanceSoftLimit = tag.getLong(TAG_SOFT_LIMIT);
        } else {
            instanceSoftLimit = -1; // 使用全局配置
        }
        
        if (tag.contains(TAG_TYPE_SOFT_LIMIT)) {
            instanceTypeSoftLimit = tag.getLong(TAG_TYPE_SOFT_LIMIT);
        } else {
            instanceTypeSoftLimit = -1; // 使用全局配置
        }
        
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            ListTag itemList = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < itemList.size(); i++) {
                CompoundTag itemTag = itemList.getCompound(i);
                
                if (itemTag.contains(TAG_KEY) && itemTag.contains(TAG_COUNT)) {
                    AEKey key = AEKey.fromTagGeneric(itemTag.getCompound(TAG_KEY));
                    long count = itemTag.getLong(TAG_COUNT);
                    
                    if (key != null && count > 0) {
                        storage.add(key, count);
                    }
                }
            }
        }
        
        if (Config.enableDebugLogging) {
            LOGGER.debug("Loaded {} item types from NBT", storage.size());
        }
    }
}
