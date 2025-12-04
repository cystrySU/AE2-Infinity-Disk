# GUI 纹理说明

## 当前状态: 使用纯色背景 ✅

已优化为使用 Minecraft 标准灰色纯色背景，无需加载纹理文件：

```java
// MC 标准灰色背景 RGB(198, 198, 198)
private static final int BG_COLOR = 0xFFC6C6C6;
```

### 性能优势
- ✅ 无需纹理文件 I/O 加载
- ✅ 无需 GPU 纹理采样
- ✅ 减少显存占用
- ✅ 渲染更快速

### 视觉效果
- 使用 Minecraft 原版 GUI 标准灰色 `RGB(198, 198, 198)`
- 带有 3D 凹凸边框效果（高光 + 阴影）
- 与原版 GUI 风格一致

## 物品纹理

磁盘物品使用自定义纹理:
- 路径: `ae_infinity_disk:item/infinity_disk`
- 文件: `textures/item/infinity_disk.png`

## 如果想恢复使用纹理背景

修改 `InfinityDiskScreen.java`，添加纹理加载代码：

```java
private static final ResourceLocation TEXTURE =
    new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

@Override
protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderTexture(0, TEXTURE);
    this.blit(poseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
}
```
