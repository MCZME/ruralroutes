package github.mczme.ruralroutes.core.rumor;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 便签布局生成器
 * 实现杂乱分布、奇偶分层、同层不重叠的布局算法
 */
public final class StickyNoteLayoutGenerator {

    // 便签尺寸常量
    public static final int NOTE_WIDTH = 80;
    public static final int NOTE_HEIGHT = 60;

    // 布局参数
    private static final float MIN_OFFSET_X = 0.05f;    // 最小 X 偏移（避免贴边）
    private static final float MAX_OFFSET_X = 0.85f;    // 最大 X 偏移
    private static final float MIN_OFFSET_Y = 0.08f;    // 最小 Y 偏移（留出标题空间）
    private static final float MAX_OFFSET_Y = 0.82f;    // 最大 Y 偏移
    private static final float ROTATION_RANGE = 15.0f;  // 最大旋转角度（±15°）

    // 碰撞检测阈值（同层内）
    private static final float COLLISION_THRESHOLD_X = 0.15f;  // X 方向最小间距
    private static final float COLLISION_THRESHOLD_Y = 0.18f;  // Y 方向最小间距

    // 最大重试次数
    private static final int MAX_ATTEMPTS = 100;

    private StickyNoteLayoutGenerator() {}

    /**
     * 生成便签布局
     * @param noteCount 便签数量
     * @param random 随机数生成器
     * @return 布局列表
     */
    public static List<StickyNoteLayout> generate(int noteCount, Random random) {
        List<StickyNoteLayout> layouts = new ArrayList<>();
        List<StickyNoteLayout> layer0 = new ArrayList<>();  // 底层便签
        List<StickyNoteLayout> layer1 = new ArrayList<>();  // 顶层便签

        for (int i = 0; i < noteCount; i++) {
            // 奇偶分层：第 1,3,5... 张底层，第 2,4,6... 张顶层
            int layer = (i % 2 == 0) ? 0 : 1;
            List<StickyNoteLayout> sameLayer = (layer == 0) ? layer0 : layer1;

            // 尝试生成不重叠的位置
            StickyNoteLayout layout = generateNonOverlapping(i, layer, sameLayer, random);
            layouts.add(layout);
            sameLayer.add(layout);
        }

        return layouts;
    }

    /**
     * 生成不重叠的布局
     */
    private static StickyNoteLayout generateNonOverlapping(
            int index, int layer,
            List<StickyNoteLayout> sameLayer,
            Random random) {

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // 随机生成位置
            float offsetX = MIN_OFFSET_X + random.nextFloat() * (MAX_OFFSET_X - MIN_OFFSET_X);
            float offsetY = MIN_OFFSET_Y + random.nextFloat() * (MAX_OFFSET_Y - MIN_OFFSET_Y);
            float rotation = (random.nextFloat() - 0.5f) * 2 * ROTATION_RANGE;

            // 检查是否与同层便签重叠
            boolean overlaps = false;
            for (StickyNoteLayout existing : sameLayer) {
                if (checkOverlap(offsetX, offsetY, existing)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                return new StickyNoteLayout(index, offsetX, offsetY, rotation, layer);
            }
        }

        // 如果多次尝试都失败，使用最后一个位置（允许轻微重叠）
        float offsetX = MIN_OFFSET_X + random.nextFloat() * (MAX_OFFSET_X - MIN_OFFSET_X);
        float offsetY = MIN_OFFSET_Y + random.nextFloat() * (MAX_OFFSET_Y - MIN_OFFSET_Y);
        float rotation = (random.nextFloat() - 0.5f) * 2 * ROTATION_RANGE;
        return new StickyNoteLayout(index, offsetX, offsetY, rotation, layer);
    }

    /**
     * 检查两个便签是否重叠
     */
    private static boolean checkOverlap(float offsetX, float offsetY, StickyNoteLayout existing) {
        float dx = Math.abs(offsetX - existing.offsetX());
        float dy = Math.abs(offsetY - existing.offsetY());
        return dx < COLLISION_THRESHOLD_X && dy < COLLISION_THRESHOLD_Y;
    }

    /**
     * 从周期索引生成确定性的随机种子
     * 同一周期内，同一方块的布局相同
     * @param cycleIndex 周期索引
     * @param blockPos 方块位置
     * @return 随机种子
     */
    public static long generateSeed(long cycleIndex, BlockPos blockPos) {
        return cycleIndex * 31L + blockPos.asLong();
    }
}