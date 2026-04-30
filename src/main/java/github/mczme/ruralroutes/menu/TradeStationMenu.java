package github.mczme.ruralroutes.menu;

import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import github.mczme.ruralroutes.core.node.CommercialNodeManager;
import github.mczme.ruralroutes.core.node.StockEntry;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.register.RRMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 贸易站 GUI 菜单
 */
public class TradeStationMenu extends AbstractContainerMenu {

    private final BlockPos blockPos;
    private final ResourceLocation themeName;
    private final String themeDisplayName;
    private final List<StockInfo> sellStocks;  // 村庄出售的物品
    private final List<StockInfo> buyStocks;   // 村庄收购的物品

    public TradeStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public TradeStationMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(RRMenuTypes.TRADE_STATION.get(), containerId);
        this.blockPos = blockPos;

        Player player = playerInventory.player;
        Level level = player.level();

        // 获取 BlockEntity
        TradeStationBlockEntity station = (TradeStationBlockEntity) level.getBlockEntity(blockPos);
        if (station == null) {
            this.themeName = null;
            this.themeDisplayName = "Unknown";
            this.sellStocks = List.of();
            this.buyStocks = List.of();
            return;
        }

        this.themeName = station.getVillageTheme();

        // 获取主题模板
        ThemeTemplate template = ThemeManager.INSTANCE.getTheme(themeName);
        if (template != null) {
            this.themeDisplayName = template.name().getPath();
        } else {
            this.themeDisplayName = themeName != null ? themeName.getPath() : "Unknown";
        }

        // 获取区块数据
        CommercialNodeData nodeData = CommercialNodeManager.getNodeData(level, blockPos);
        if (nodeData == null) {
            this.sellStocks = List.of();
            this.buyStocks = List.of();
            return;
        }

        // 分类库存信息
        this.sellStocks = new ArrayList<>();
        this.buyStocks = new ArrayList<>();

        if (template != null) {
            for (ThemeTemplate.ItemReference item : template.sellItems()) {
                ResourceLocation itemId = ResourceLocation.parse(item.id().startsWith("#") ?
                    item.id().substring(1) : item.id());
                StockEntry entry = nodeData.getStock(itemId);
                if (entry != null) {
                    sellStocks.add(new StockInfo(itemId, entry.current(), entry.max()));
                }
            }

            for (ThemeTemplate.ItemReference item : template.buyItems()) {
                ResourceLocation itemId = ResourceLocation.parse(item.id().startsWith("#") ?
                    item.id().substring(1) : item.id());
                StockEntry entry = nodeData.getStock(itemId);
                if (entry != null) {
                    buyStocks.add(new StockInfo(itemId, entry.current(), entry.max()));
                }
            }
        } else {
            // 无模板时，直接从区块数据获取
            for (Map.Entry<ResourceLocation, StockEntry> entry : nodeData.stocks().entrySet()) {
                StockInfo info = new StockInfo(entry.getKey(), entry.getValue().current(), entry.getValue().max());
                // 简单分类：库存 > 0 的视为可购买，库存 < max 的视为可出售
                if (entry.getValue().current() > 0) {
                    sellStocks.add(info);
                }
                if (entry.getValue().current() < entry.getValue().max()) {
                    buyStocks.add(info);
                }
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(blockPos) instanceof TradeStationBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public ResourceLocation getThemeName() {
        return themeName;
    }

    public String getThemeDisplayName() {
        return themeDisplayName;
    }

    public List<StockInfo> getSellStocks() {
        return sellStocks;
    }

    public List<StockInfo> getBuyStocks() {
        return buyStocks;
    }

    /**
     * 库存信息记录
     */
    public record StockInfo(ResourceLocation itemId, int current, int max) {
        public String getDisplayText() {
            return current + "/" + max;
        }
    }
}