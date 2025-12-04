package com.CystrySu.infinitydisk.networking;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 磁盘状态同步数据包
 * 用于从服务器向客户端同步磁盘的存储统计信息
 */
public class PacketSyncDiskState {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // 磁盘标识符
    private final UUID diskId;
    // 总物品数量
    private final long totalItems;
    // 不同物品种类数
    private final long distinctTypes;

    /**
     * 创建磁盘状态同步包
     * @param diskId 磁盘 UUID
     * @param totalItems 总物品数量
     * @param distinctTypes 不同种类数量
     */
    public PacketSyncDiskState(UUID diskId, long totalItems, long distinctTypes) {
        this.diskId = diskId;
        this.totalItems = totalItems;
        this.distinctTypes = distinctTypes;
    }
    
    /**
     * 简化构造器（使用随机 UUID）
     * @param totalItems 总物品数量
     * @param distinctTypes 不同种类数量
     */
    public PacketSyncDiskState(long totalItems, long distinctTypes) {
        this(UUID.randomUUID(), totalItems, distinctTypes);
    }

    /**
     * 从网络缓冲区解码数据包
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    public static PacketSyncDiskState decode(FriendlyByteBuf buf) {
        UUID diskId = buf.readUUID();
        long total = buf.readLong();
        long types = buf.readLong();
        return new PacketSyncDiskState(diskId, total, types);
    }

    /**
     * 将数据包编码到网络缓冲区
     * @param buf 网络缓冲区
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(diskId);
        buf.writeLong(totalItems);
        buf.writeLong(distinctTypes);
    }

    /**
     * 处理接收到的数据包（客户端）
     * @param ctx 网络事件上下文
     */
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 更新客户端缓存
            ClientDiskStateCache.updateDiskState(diskId, totalItems, distinctTypes);
            LOGGER.debug("Received disk state sync: diskId={}, totalItems={}, distinctTypes={}", 
                    diskId, totalItems, distinctTypes);
        });
        ctx.get().setPacketHandled(true);
    }

    // ==================== Getters ====================
    
    public UUID getDiskId() {
        return diskId;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public long getDistinctTypes() {
        return distinctTypes;
    }
}

