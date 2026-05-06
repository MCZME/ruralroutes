package github.mczme.ruralroutes.core.rumor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 单个便签的布局数据
 * 用于传闻板 GUI 的杂乱分布渲染
 */
public record StickyNoteLayout(
        int index,          // 便签索引
        float offsetX,      // X 偏移（0.0-1.0，相对于 GUI 宽度）
        float offsetY,      // Y 偏移（0.0-1.0，相对于 GUI 高度）
        float rotation,     // 旋转角度（度）
        int layer           // 层级：0=底层, 1=顶层
) {
    public static final Codec<StickyNoteLayout> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("index").forGetter(StickyNoteLayout::index),
                    Codec.FLOAT.fieldOf("offset_x").forGetter(StickyNoteLayout::offsetX),
                    Codec.FLOAT.fieldOf("offset_y").forGetter(StickyNoteLayout::offsetY),
                    Codec.FLOAT.fieldOf("rotation").forGetter(StickyNoteLayout::rotation),
                    Codec.INT.fieldOf("layer").forGetter(StickyNoteLayout::layer)
            ).apply(instance, StickyNoteLayout::new)
    );

    /**
     * 获取便签在 GUI 中的实际 X 坐标
     * @param guiLeft GUI 左边缘
     * @param guiWidth GUI 宽度
     * @param noteWidth 便签宽度
     * @return 实际 X 坐标
     */
    public int getAbsoluteX(int guiLeft, int guiWidth, int noteWidth) {
        int availableWidth = guiWidth - noteWidth;
        return guiLeft + (int) (offsetX * availableWidth);
    }

    /**
     * 获取便签在 GUI 中的实际 Y 坐标
     * @param guiTop GUI 上边缘
     * @param guiHeight GUI 高度
     * @param noteHeight 便签高度
     * @return 实际 Y 坐标
     */
    public int getAbsoluteY(int guiTop, int guiHeight, int noteHeight) {
        int availableHeight = guiHeight - noteHeight;
        return guiTop + (int) (offsetY * availableHeight);
    }
}
