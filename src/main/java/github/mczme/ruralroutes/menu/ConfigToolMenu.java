package github.mczme.ruralroutes.menu;

import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import github.mczme.ruralroutes.blockentity.RumorBoardBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeNodeBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.item.ConfigToolItem;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 配置工具 GUI 菜单
 * 贸易站：可编辑主题、复制节点信息
 * 展示柜/传闻板：可粘贴节点信息
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
    private final UUID copiedNodeId;
    private final BlockPos copiedStationPos;

    public ConfigToolMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public ConfigToolMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(RRMenuTypes.CONFIG_TOOL.get(), containerId);
        this.blockPos = blockPos;
        this.availableThemes = new ArrayList<>(ThemeManager.INSTANCE.getAllThemes().keySet());
        this.availableThemes.sort(Comparator.comparing(ResourceLocation::toString));
        this.selectedTheme = null;

        // 从玩家手持物品读取复制的节点信息
        Player player = playerInventory.player;
        if (player != null) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof ConfigToolItem) {
                this.copiedNodeId = ConfigToolItem.getCopiedNodeId(mainHand);
                this.copiedStationPos = ConfigToolItem.getCopiedStationPos(mainHand);
            } else {
                this.copiedNodeId = null;
                this.copiedStationPos = null;
            }
        } else {
            this.copiedNodeId = null;
            this.copiedStationPos = null;
        }
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
            } else if (be instanceof TradeNodeBlockEntity nodeEntity) {
                return nodeEntity.getTradeNodeId();
            }
        }
        return null;
    }

    /**
     * 获取当前贸易站位置（仅贸易站有效）
     */
    public BlockPos getCurrentStationPos() {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TradeStationBlockEntity) {
                return blockPos;
            }
        }
        return null;
    }

    public void selectTheme(ResourceLocation theme) {
        this.selectedTheme = theme;
    }

    /**
     * 获取复制的节点ID
     */
    public UUID getCopiedNodeId() {
        return copiedNodeId;
    }

    /**
     * 获取复制的贸易站位置
     */
    public BlockPos getCopiedStationPos() {
        return copiedStationPos;
    }

    /**
     * 是否有复制的节点信息
     */
    public boolean hasCopiedNodeInfo() {
        return copiedNodeId != null && copiedStationPos != null;
    }
}
