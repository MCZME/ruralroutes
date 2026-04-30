package github.mczme.ruralroutes.blockentity;

import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 传闻板方块实体 - 存储情报更新时间
 */
public class RumorBoardBlockEntity extends BlockEntity implements MenuProvider {

    private long lastUpdateTime = 0;

    public RumorBoardBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.RUMOR_BOARD.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("LastUpdateTime", lastUpdateTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lastUpdateTime = tag.getLong("LastUpdateTime");
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ruralroutes.rumor_board");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        // TODO: 实现 GUI
        return null;
    }
}
