package github.mczme.ruralroutes.blockentity;

import github.mczme.ruralroutes.core.rumor.StickyNoteLayout;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayoutGenerator;
import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 传闻板方块实体
 * 存储节点ID和贸易站位置用于校验，以及便签布局数据
 */
public class RumorBoardBlockEntity extends TradeNodeBlockEntity {

    // 布局数据
    private long layoutCycleIndex = -1;
    private List<StickyNoteLayout> layouts = new ArrayList<>();

    public RumorBoardBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.RUMOR_BOARD.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("LayoutCycleIndex", layoutCycleIndex);
        if (!layouts.isEmpty()) {
            ListTag layoutsTag = new ListTag();
            for (StickyNoteLayout layout : layouts) {
                StickyNoteLayout.CODEC.encodeStart(NbtOps.INSTANCE, layout)
                        .resultOrPartial(err -> {})
                        .ifPresent(layoutsTag::add);
            }
            tag.put("Layouts", layoutsTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        layoutCycleIndex = tag.getLong("LayoutCycleIndex");
        layouts.clear();
        if (tag.contains("Layouts")) {
            ListTag layoutsTag = tag.getList("Layouts", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < layoutsTag.size(); i++) {
                StickyNoteLayout.CODEC.parse(NbtOps.INSTANCE, layoutsTag.getCompound(i))
                        .resultOrPartial(err -> {})
                        .ifPresent(layouts::add);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putLong("LayoutCycleIndex", layoutCycleIndex);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        layoutCycleIndex = tag.getLong("LayoutCycleIndex");
    }

    // ===== 布局管理 =====

    /**
     * 获取或生成布局数据
     * 如果周期已更新或布局为空，重新生成布局
     * @param noteCount 便签数量
     * @param currentCycleIndex 当前周期索引
     * @param random 随机数生成器
     * @return 布局列表
     */
    public List<StickyNoteLayout> getOrGenerateLayouts(int noteCount, long currentCycleIndex, Random random) {
        if (layoutCycleIndex != currentCycleIndex || layouts.isEmpty()) {
            layouts = StickyNoteLayoutGenerator.generate(noteCount, random);
            layoutCycleIndex = currentCycleIndex;
            setChanged();
        }
        return layouts;
    }

    /**
     * 获取当前布局
     */
    public List<StickyNoteLayout> getLayouts() {
        return layouts;
    }

    /**
     * 获取布局周期索引
     */
    public long getLayoutCycleIndex() {
        return layoutCycleIndex;
    }
}