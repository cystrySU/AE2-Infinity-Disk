package com.CystrySu.infinitydisk.client.gui;

import com.CystrySu.infinitydisk.Infinitydisk;
import com.CystrySu.infinitydisk.item.ItemInfiniteDisk;
import com.CystrySu.infinitydisk.menu.InfinityDiskMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 无限磁盘信息界面
 * 显示磁盘的存储统计信息
 */
public class InfinityDiskScreen extends AbstractContainerScreen<InfinityDiskMenu> {
    
    // 使用 Minecraft 默认容器背景纹理
    @SuppressWarnings({"removal", "deprecation"})
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    // GUI 尺寸
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    
    // 进度条位置和尺寸
    private static final int BAR_X = 8;
    private static final int BAR_Y = 60;
    private static final int BAR_WIDTH = 160;
    private static final int BAR_HEIGHT = 8;
    
    public InfinityDiskScreen(InfinityDiskMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }
    
    @Override
    protected void init() {
        super.init();
        // 隐藏物品栏标签
        this.inventoryLabelY = 10000;
        this.titleLabelY = 6;
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // 渲染背景暗化
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        // 渲染提示信息
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 绘制背景（如果纹理不存在，会显示为紫黑方格）
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
    
    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // 标题
        this.font.draw(poseStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, 0x404040);
        
        // 获取数据
        long totalItems = menu.getTotalItems();
        long distinctTypes = menu.getDistinctTypes();
        long usedBytes = menu.getUsedBytes();
        
        int textY = 20;
        int lineHeight = 12;
        int textColor = 0x404040;
        int valueColor = 0x0066CC;
        int infinityColor = 0xFFAA00;
        
        // ==================== 统计信息 ====================
        
        // 总物品数量
        this.font.draw(poseStack, 
                Component.translatable("gui.ae_infinity_disk.total_items"), 
                8, textY, textColor);
        this.font.draw(poseStack, 
                formatNumber(totalItems), 
                GUI_WIDTH - 8 - font.width(formatNumber(totalItems)), textY, valueColor);
        textY += lineHeight;
        
        // 物品种类数
        this.font.draw(poseStack, 
                Component.translatable("gui.ae_infinity_disk.distinct_types"), 
                8, textY, textColor);
        this.font.draw(poseStack, 
                formatNumber(distinctTypes), 
                GUI_WIDTH - 8 - font.width(formatNumber(distinctTypes)), textY, valueColor);
        textY += lineHeight;
        
        // 已使用字节
        this.font.draw(poseStack, 
                Component.translatable("gui.ae_infinity_disk.used_bytes"), 
                8, textY, textColor);
        this.font.draw(poseStack, 
                formatBytes(usedBytes), 
                GUI_WIDTH - 8 - font.width(formatBytes(usedBytes)), textY, valueColor);
        textY += lineHeight + 4;
        
        // ==================== 容量信息 ====================
        
        // 容量标签
        this.font.draw(poseStack, 
                Component.translatable("gui.ae_infinity_disk.capacity"), 
                8, textY, textColor);
        
        // 无限符号
        String infinitySymbol = "∞";
        this.font.draw(poseStack, 
                infinitySymbol, 
                GUI_WIDTH - 8 - font.width(infinitySymbol), textY, infinityColor);
        textY += lineHeight;
        
        // 种类上限
        this.font.draw(poseStack, 
                Component.translatable("gui.ae_infinity_disk.type_limit"), 
                8, textY, textColor);
        this.font.draw(poseStack, 
                infinitySymbol, 
                GUI_WIDTH - 8 - font.width(infinitySymbol), textY, infinityColor);
        textY += lineHeight + 8;
        
        // ==================== 可视化进度条 ====================
        
        // 进度条标签
        this.font.draw(poseStack, 
                Component.translatable("gui.ae_infinity_disk.usage"), 
                8, textY, textColor);
        textY += lineHeight;
        
        // 绘制进度条背景
        fill(poseStack, BAR_X, textY, BAR_X + BAR_WIDTH, textY + BAR_HEIGHT, 0xFF333333);
        
        // 绘制进度条边框
        hLine(poseStack, BAR_X - 1, BAR_X + BAR_WIDTH, textY - 1, 0xFF000000);
        hLine(poseStack, BAR_X - 1, BAR_X + BAR_WIDTH, textY + BAR_HEIGHT, 0xFF000000);
        vLine(poseStack, BAR_X - 1, textY - 1, textY + BAR_HEIGHT, 0xFF000000);
        vLine(poseStack, BAR_X + BAR_WIDTH, textY - 1, textY + BAR_HEIGHT, 0xFF000000);
        
        // 绘制已使用部分（使用渐变色）
        // 由于容量无限，我们用对数刻度显示
        int filledWidth = calculateBarWidth(totalItems);
        if (filledWidth > 0) {
            // 渐变色从绿色到黄色到红色
            int color = getColorForUsage(filledWidth);
            fill(poseStack, BAR_X, textY, BAR_X + filledWidth, textY + BAR_HEIGHT, color);
        }
        
        textY += BAR_HEIGHT + 8;
        
        // ==================== 底部信息 ====================
        
        // 显示无限容量提示
        Component infiniteHint = Component.translatable("gui.ae_infinity_disk.infinite_hint");
        int hintWidth = font.width(infiniteHint);
        this.font.draw(poseStack, infiniteHint, 
                (GUI_WIDTH - hintWidth) / 2f, textY, infinityColor);
    }
    
    /**
     * 计算进度条宽度（对数刻度）
     * 由于容量无限，使用对数来可视化
     */
    private int calculateBarWidth(long totalItems) {
        if (totalItems <= 0) return 0;
        
        // 使用对数刻度：log10(items) / log10(Long.MAX_VALUE) * BAR_WIDTH
        // 简化为：log10(items) / 19 * BAR_WIDTH（因为 log10(Long.MAX_VALUE) ≈ 19）
        double logValue = Math.log10(totalItems);
        double ratio = logValue / 19.0;
        int width = (int) (ratio * BAR_WIDTH);
        
        // 最小显示1像素，最大不超过BAR_WIDTH
        return Math.max(1, Math.min(width, BAR_WIDTH));
    }
    
    /**
     * 根据使用量获取颜色
     */
    private int getColorForUsage(int barWidth) {
        float ratio = (float) barWidth / BAR_WIDTH;
        
        if (ratio < 0.5f) {
            // 绿色到黄色
            int green = 255;
            int red = (int) (255 * ratio * 2);
            return 0xFF000000 | (red << 16) | (green << 8);
        } else {
            // 黄色到红色
            int red = 255;
            int green = (int) (255 * (1 - (ratio - 0.5f) * 2));
            return 0xFF000000 | (red << 16) | (green << 8);
        }
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
     * 格式化字节显示
     */
    private String formatBytes(long bytes) {
        if (bytes >= 1_099_511_627_776L) {
            return String.format("%.2f TB", bytes / 1_099_511_627_776.0);
        } else if (bytes >= 1_073_741_824L) {
            return String.format("%.2f GB", bytes / 1_073_741_824.0);
        } else if (bytes >= 1_048_576L) {
            return String.format("%.2f MB", bytes / 1_048_576.0);
        } else if (bytes >= 1024L) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }
}
