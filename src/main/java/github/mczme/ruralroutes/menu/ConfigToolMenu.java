package github.mczme.ruralroutes.menu;

import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import github.mczme.ruralroutes.blockentity.RumorBoardBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.register.RRMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 配置工具 GUI 菜单
 * 贸易站：可编辑主题
 * 展示柜/传闻板：仅显示节点ID
 */
public class ConfigToolMenu extends AbstractContainerMenu {

    public enum BlockType {
        TRADE_STATION,
        DISPLAY_CASE,
        RUMOR_BOARD,
        UNKNOWN
    }

    private final BlockPos blockPos;
    private final List<ResourceLocation> availableThemes;
    private ResourceLocation selectedTheme;

    public ConfigToolMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public ConfigToolMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(RRMenuTypes.CONFIG_TOOL.get(), containerId);
        this.blockPos = blockPos;
        this.availableThemes = new ArrayList<>(ThemeManager.INSTANCE.getAllThemes().keySet());
        this.selectedTheme = null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public List<ResourceLocation> getAvailableThemes() {
        return availableThemes;
    }

    public ResourceLocation getSelectedTheme() {
        return selectedTheme;
    }

    /**
     * 获取方块类型
     */
    public BlockType getBlockType() {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TradeStationBlockEntity) {
                return BlockType.TRADE_STATION;
            } else if (be instanceof DisplayCaseBlockEntity) {
                return BlockType.DISPLAY_CASE;
            } else if (be instanceof RumorBoardBlockEntity) {
                return BlockType.RUMOR_BOARD;
            }
        }
        return BlockType.UNKNOWN;
    }

    /**
     * 是否可编辑主题（仅贸易站可编辑）
     */
    public boolean canEditTheme() {
        return getBlockType() == BlockType.TRADE_STATION;
    }

    /**
     * 获取当前主题 - 仅贸易站有效
     */
    public ResourceLocation getCurrentTheme() {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TradeStationBlockEntity station) {
                return station.getVillageTheme();
            }
        }
        return null;
    }

    /**
     * 获取当前节点ID
     */
    public UUID getCurrentTradeNodeId() {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TradeStationBlockEntity station) {
                return station.getTradeNodeId();
            } else if (be instanceof DisplayCaseBlockEntity displayCase) {
                return displayCase.getTradeNodeId();
            } else if (be instanceof RumorBoardBlockEntity rumorBoard) {
                return rumorBoard.getTradeNodeId();
            }
        }
        return null;
    }

    public void selectTheme(ResourceLocation theme) {
        this.selectedTheme = theme;
    }
}