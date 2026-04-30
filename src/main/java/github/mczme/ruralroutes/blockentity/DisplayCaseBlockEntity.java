package github.mczme.ruralroutes.blockentity;

import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * 展示柜方块实体
 * 仅存储节点ID用于校验，特产数据存储在区块中
 */
public class DisplayCaseBlockEntity extends BlockEntity implements MenuProvider {

    private UUID tradeNodeId;

    public DisplayCaseBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.DISPLAY_CASE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (tradeNodeId != null) {
            tag.putUUID("TradeNodeId", tradeNodeId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("TradeNodeId")) {
            tradeNodeId = tag.getUUID("TradeNodeId");
        }
    }

    // ===== 数据同步 =====

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (tradeNodeId != null) {
            tag.putUUID("TradeNodeId", tradeNodeId);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains("TradeNodeId")) {
            tradeNodeId = tag.getUUID("TradeNodeId");
        } else {
            tradeNodeId = null;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
    }

    // ===== Getters and Setters =====

    public UUID getTradeNodeId() {
        return tradeNodeId;
    }

    public void setTradeNodeId(UUID tradeNodeId) {
        this.tradeNodeId = tradeNodeId;
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    // ===== MenuProvider =====

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ruralroutes.display_case");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        // TODO: 第一阶段暂不实现 GUI
        return null;
    }
}
