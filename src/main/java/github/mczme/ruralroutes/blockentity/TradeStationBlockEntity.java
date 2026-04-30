package github.mczme.ruralroutes.blockentity;

import github.mczme.ruralroutes.menu.TradeStationMenu;
import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * 贸易站方块实体
 * 仅存储校验数据（themeName、tradeNodeId），核心业务数据存储在区块中
 */
public class TradeStationBlockEntity extends BlockEntity implements MenuProvider {

    private ResourceLocation villageTheme;
    private UUID tradeNodeId;

    public TradeStationBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.TRADE_STATION.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (villageTheme != null) {
            tag.putString("VillageTheme", villageTheme.toString());
        }
        if (tradeNodeId != null) {
            tag.putUUID("TradeNodeId", tradeNodeId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("VillageTheme")) {
            villageTheme = ResourceLocation.tryParse(tag.getString("VillageTheme"));
        }
        if (tag.contains("TradeNodeId")) {
            tradeNodeId = tag.getUUID("TradeNodeId");
        }
    }

    // ===== 数据同步：区块加载时 =====

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (villageTheme != null) {
            tag.putString("VillageTheme", villageTheme.toString());
        }
        if (tradeNodeId != null) {
            tag.putUUID("TradeNodeId", tradeNodeId);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains("VillageTheme")) {
            villageTheme = ResourceLocation.tryParse(tag.getString("VillageTheme"));
        } else {
            villageTheme = null;
        }
        if (tag.contains("TradeNodeId")) {
            tradeNodeId = tag.getUUID("TradeNodeId");
        } else {
            tradeNodeId = null;
        }
    }

    // ===== 数据同步：方块更新时 =====

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

    public ResourceLocation getVillageTheme() {
        return villageTheme;
    }

    public void setVillageTheme(ResourceLocation villageTheme) {
        this.villageTheme = villageTheme;
        setChanged();
        // 触发方块更新同步到客户端
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

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
        return Component.translatable("block.ruralroutes.trade_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TradeStationMenu(id, inventory, getBlockPos());
    }
}