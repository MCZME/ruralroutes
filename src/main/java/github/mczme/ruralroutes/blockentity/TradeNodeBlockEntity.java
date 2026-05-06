package github.mczme.ruralroutes.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * 商业节点方块实体的基类
 * 存储节点ID和贸易站位置用于校验
 */
public abstract class TradeNodeBlockEntity extends BlockEntity {

    private UUID tradeNodeId;
    private BlockPos tradeStationPos;  // 关联的贸易站位置

    protected TradeNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (tradeNodeId != null) {
            tag.putUUID("TradeNodeId", tradeNodeId);
        }
        if (tradeStationPos != null) {
            tag.putLong("TradeStationPos", tradeStationPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("TradeNodeId")) {
            tradeNodeId = tag.getUUID("TradeNodeId");
        }
        if (tag.contains("TradeStationPos")) {
            tradeStationPos = BlockPos.of(tag.getLong("TradeStationPos"));
        }
    }

    // ===== 数据同步 =====

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (tradeNodeId != null) {
            tag.putUUID("TradeNodeId", tradeNodeId);
        }
        if (tradeStationPos != null) {
            tag.putLong("TradeStationPos", tradeStationPos.asLong());
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
        if (tag.contains("TradeStationPos")) {
            tradeStationPos = BlockPos.of(tag.getLong("TradeStationPos"));
        } else {
            tradeStationPos = null;
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

    public BlockPos getTradeStationPos() {
        return tradeStationPos;
    }

    public void setTradeStationPos(BlockPos tradeStationPos) {
        this.tradeStationPos = tradeStationPos;
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * 同时设置节点ID和贸易站位置
     */
    public void setTradeNodeInfo(UUID tradeNodeId, BlockPos tradeStationPos) {
        this.tradeNodeId = tradeNodeId;
        this.tradeStationPos = tradeStationPos;
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}