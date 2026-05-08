package github.mczme.ruralroutes.blockentity;

import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 展示柜方块实体
 * 继承 TradeNodeBlockEntity，新增展示物品
 */
public class DisplayCaseBlockEntity extends TradeNodeBlockEntity {

    private ItemStack displayItem = ItemStack.EMPTY;

    public DisplayCaseBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.DISPLAY_CASE.get(), pos, state);
    }

    // ===== 数据持久化 =====

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!displayItem.isEmpty()) {
            tag.put("DisplayItem", displayItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("DisplayItem")) {
            displayItem = ItemStack.parseOptional(registries, tag.getCompound("DisplayItem"));
        } else {
            displayItem = ItemStack.EMPTY;
        }
    }

    // ===== 数据同步 =====

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains("DisplayItem")) {
            displayItem = ItemStack.parseOptional(registries, tag.getCompound("DisplayItem"));
        } else {
            displayItem = ItemStack.EMPTY;
        }
    }

    // ===== Getters and Setters =====

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    /**
     * 设置展示物品
     */
    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem.copy();
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
