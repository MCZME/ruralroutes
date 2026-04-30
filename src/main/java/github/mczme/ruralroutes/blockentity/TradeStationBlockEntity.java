package github.mczme.ruralroutes.blockentity;

import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 贸易站方块实体 - 存储村庄交易数据
 */
public class TradeStationBlockEntity extends BlockEntity implements MenuProvider {

    private ResourceLocation villageTheme;
    private UUID villageId;
    private final Map<ResourceLocation, Integer> stock = new HashMap<>();

    public TradeStationBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.TRADE_STATION.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (villageTheme != null) {
            tag.putString("VillageTheme", villageTheme.toString());
        }
        if (villageId != null) {
            tag.putUUID("VillageId", villageId);
        }
        // 保存库存
        CompoundTag stockTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Integer> entry : stock.entrySet()) {
            stockTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("Stock", stockTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("VillageTheme")) {
            villageTheme = ResourceLocation.tryParse(tag.getString("VillageTheme"));
        }
        if (tag.contains("VillageId")) {
            villageId = tag.getUUID("VillageId");
        }
        // 加载库存
        stock.clear();
        if (tag.contains("Stock")) {
            CompoundTag stockTag = tag.getCompound("Stock");
            for (String key : stockTag.getAllKeys()) {
                ResourceLocation itemId = ResourceLocation.tryParse(key);
                if (itemId != null) {
                    stock.put(itemId, stockTag.getInt(key));
                }
            }
        }
    }

    /**
     * 同步数据到客户端（用于渲染等）
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (villageTheme != null) {
            tag.putString("VillageTheme", villageTheme.toString());
        }
        return tag;
    }

    /**
     * 客户端接收更新数据
     */
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (tag.contains("VillageTheme")) {
            villageTheme = ResourceLocation.tryParse(tag.getString("VillageTheme"));
        } else {
            villageTheme = null;
        }
    }

    // Getters and Setters
    public ResourceLocation getVillageTheme() {
        return villageTheme;
    }

    public void setVillageTheme(ResourceLocation villageTheme) {
        this.villageTheme = villageTheme;
        setChanged();
        // 同步到客户端
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getVillageId() {
        return villageId;
    }

    public void setVillageId(UUID villageId) {
        this.villageId = villageId;
        setChanged();
    }

    public int getStock(ResourceLocation itemId) {
        return stock.getOrDefault(itemId, 0);
    }

    public void setStock(ResourceLocation itemId, int amount) {
        stock.put(itemId, amount);
        setChanged();
    }

    public Map<ResourceLocation, Integer> getAllStock() {
        return stock;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ruralroutes.trade_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        // TODO: 实现 GUI
        return null;
    }
}