package com.CystrySu.infinitydisk.networking;

import com.CystrySu.infinitydisk.Infinitydisk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.UUID;

/**
 * 网络处理器
 * 管理 Mod 的所有网络通信
 */
public class NetworkHandler {
    
    // 网络协议版本
    private static final String PROTOCOL_VERSION = "1";
    
    // 网络通道
    @SuppressWarnings({"removal", "deprecation"})
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Infinitydisk.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    
    // 消息 ID 计数器
    private static int messageId = 0;
    
    /**
     * 注册所有网络数据包
     * 应在 commonSetup 中调用
     */
    public static void register() {
        // 注册磁盘状态同步包（服务器 -> 客户端）
        CHANNEL.registerMessage(
                messageId++,
                PacketSyncDiskState.class,
                PacketSyncDiskState::encode,
                PacketSyncDiskState::decode,
                PacketSyncDiskState::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }
    
    /**
     * 向指定玩家发送磁盘状态同步包
     * @param player 目标玩家
     * @param diskId 磁盘 UUID
     * @param totalItems 总物品数量
     * @param distinctTypes 不同种类数量
     */
    public static void sendDiskStateToPlayer(ServerPlayer player, UUID diskId, long totalItems, long distinctTypes) {
        CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PacketSyncDiskState(diskId, totalItems, distinctTypes)
        );
    }
    
    /**
     * 向所有玩家广播磁盘状态同步包
     * @param diskId 磁盘 UUID
     * @param totalItems 总物品数量
     * @param distinctTypes 不同种类数量
     */
    public static void broadcastDiskState(UUID diskId, long totalItems, long distinctTypes) {
        CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new PacketSyncDiskState(diskId, totalItems, distinctTypes)
        );
    }
    
    /**
     * 向指定维度的所有玩家发送磁盘状态同步包
     * @param player 参考玩家（用于获取维度）
     * @param diskId 磁盘 UUID
     * @param totalItems 总物品数量
     * @param distinctTypes 不同种类数量
     */
    public static void sendDiskStateToDimension(ServerPlayer player, UUID diskId, long totalItems, long distinctTypes) {
        CHANNEL.send(
                PacketDistributor.DIMENSION.with(() -> player.level.dimension()),
                new PacketSyncDiskState(diskId, totalItems, distinctTypes)
        );
    }
}
