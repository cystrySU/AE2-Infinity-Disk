package com.CystrySu.infinitydisk.networking;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端磁盘状态缓存
 * 存储从服务器同步的磁盘状态信息，供 GUI 显示使用
 */
public class ClientDiskStateCache {
    
    // 磁盘状态缓存 (磁盘 UUID -> 状态)
    private static final Map<UUID, DiskState> CACHE = new ConcurrentHashMap<>();
    
    /**
     * 磁盘状态数据结构
     */
    public static class DiskState {
        public final long totalItems;
        public final long distinctTypes;
        public final long lastUpdateTime;
        
        public DiskState(long totalItems, long distinctTypes) {
            this.totalItems = totalItems;
            this.distinctTypes = distinctTypes;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 更新磁盘状态缓存
     * @param diskId 磁盘 UUID
     * @param totalItems 总物品数量
     * @param distinctTypes 不同种类数量
     */
    public static void updateDiskState(UUID diskId, long totalItems, long distinctTypes) {
        CACHE.put(diskId, new DiskState(totalItems, distinctTypes));
    }
    
    /**
     * 获取磁盘状态
     * @param diskId 磁盘 UUID
     * @return 磁盘状态，如果不存在则返回 null
     */
    public static DiskState getDiskState(UUID diskId) {
        return CACHE.get(diskId);
    }
    
    /**
     * 获取磁盘的总物品数量
     * @param diskId 磁盘 UUID
     * @return 总物品数量，如果不存在则返回 0
     */
    public static long getTotalItems(UUID diskId) {
        DiskState state = CACHE.get(diskId);
        return state != null ? state.totalItems : 0;
    }
    
    /**
     * 获取磁盘的不同种类数量
     * @param diskId 磁盘 UUID
     * @return 不同种类数量，如果不存在则返回 0
     */
    public static long getDistinctTypes(UUID diskId) {
        DiskState state = CACHE.get(diskId);
        return state != null ? state.distinctTypes : 0;
    }
    
    /**
     * 检查缓存是否包含指定磁盘的状态
     * @param diskId 磁盘 UUID
     * @return 是否存在
     */
    public static boolean hasDiskState(UUID diskId) {
        return CACHE.containsKey(diskId);
    }
    
    /**
     * 移除磁盘状态缓存
     * @param diskId 磁盘 UUID
     */
    public static void removeDiskState(UUID diskId) {
        CACHE.remove(diskId);
    }
    
    /**
     * 清空所有缓存
     * 通常在断开服务器连接时调用
     */
    public static void clearAll() {
        CACHE.clear();
    }
    
    /**
     * 检查缓存是否过期（超过指定毫秒数）
     * @param diskId 磁盘 UUID
     * @param maxAgeMs 最大缓存时间（毫秒）
     * @return 是否过期
     */
    public static boolean isExpired(UUID diskId, long maxAgeMs) {
        DiskState state = CACHE.get(diskId);
        if (state == null) {
            return true;
        }
        return System.currentTimeMillis() - state.lastUpdateTime > maxAgeMs;
    }
}
