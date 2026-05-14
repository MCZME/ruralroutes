package github.mczme.ruralroutes.blockentity;

import github.mczme.ruralroutes.core.rumor.RumorEntry;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayout;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayoutGenerator;
import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 传闻板方块实体
 * 存储节点ID和贸易站位置用于校验，以及便签布局数据和传闻缓存
 */
public class RumorBoardBlockEntity extends TradeNodeBlockEntity {

    // 布局数据
    private long layoutCycleIndex = -1;
    private List<StickyNoteLayout> layouts = new ArrayList<>();

    // 传闻数据缓存（避免每次打开重新生成市场事件）
    private long rumorCycleIndex = -1;
    private List<RumorEntry> cachedRumors = new ArrayList<>();

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
        tag.putLong("RumorCycleIndex", rumorCycleIndex);
        if (!cachedRumors.isEmpty()) {
            ListTag rumorsTag = new ListTag();
            for (RumorEntry rumor : cachedRumors) {
                rumorsTag.add(StringTag.valueOf(rumor.serialize()));
            }
            tag.put("CachedRumors", rumorsTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        layoutCycleIndex = tag.getLong("LayoutCycleIndex");
        layouts.clear();
        if (tag.contains("Layouts")) {
            ListTag layoutsTag = tag.getList("Layouts", Tag.TAG_COMPOUND);
            for (int i = 0; i < layoutsTag.size(); i++) {
                StickyNoteLayout.CODEC.parse(NbtOps.INSTANCE, layoutsTag.getCompound(i))
                        .resultOrPartial(err -> {})
                        .ifPresent(layouts::add);
            }
        }
        rumorCycleIndex = tag.getLong("RumorCycleIndex");
        cachedRumors.clear();
        if (tag.contains("CachedRumors")) {
            ListTag rumorsTag = tag.getList("CachedRumors", Tag.TAG_STRING);
            for (int i = 0; i < rumorsTag.size(); i++) {
                RumorEntry.deserialize(rumorsTag.getString(i))
                        .ifPresent(cachedRumors::add);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putLong("LayoutCycleIndex", layoutCycleIndex);
        tag.putLong("RumorCycleIndex", rumorCycleIndex);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        layoutCycleIndex = tag.getLong("LayoutCycleIndex");
        rumorCycleIndex = tag.getLong("RumorCycleIndex");
    }

    // ===== 布局管理 =====

    /**
     * 获取或生成布局数据
     * 如果周期已更新或布局为空，重新生成布局
     */
    public List<StickyNoteLayout> getOrGenerateLayouts(int noteCount, long currentCycleIndex, Random random) {
        if (layoutCycleIndex != currentCycleIndex || layouts.isEmpty()) {
            layouts = StickyNoteLayoutGenerator.generate(noteCount, random);
            layoutCycleIndex = currentCycleIndex;
            setChanged();
        }
        return layouts;
    }

    public List<StickyNoteLayout> getLayouts() {
        return layouts;
    }

    public long getLayoutCycleIndex() {
        return layoutCycleIndex;
    }

    // ===== 传闻缓存 =====

    /**
     * 检查当前周期是否已有缓存的传闻数据
     */
    public boolean hasRumorCacheFor(long cycleIndex) {
        return rumorCycleIndex == cycleIndex && !cachedRumors.isEmpty();
    }

    /**
     * 获取缓存的传闻条目
     */
    public List<RumorEntry> getCachedRumors() {
        return cachedRumors;
    }

    /**
     * 更新传闻缓存
     */
    public void setRumorCache(long cycleIndex, List<RumorEntry> rumors) {
        this.rumorCycleIndex = cycleIndex;
        this.cachedRumors = rumors;
        setChanged();
    }
}