package github.mczme.ruralroutes.menu;

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

import java.util.ArrayList;
import java.util.List;

/**
 * 配置工具 GUI 菜单
 */
public class ConfigToolMenu extends AbstractContainerMenu {

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
     * 获取当前主题 - 直接从客户端 BlockEntity 读取（已同步）
     */
    public ResourceLocation getCurrentTheme() {
        Level level = Minecraft.getInstance().level;
        if (level != null && level.getBlockEntity(blockPos) instanceof TradeStationBlockEntity station) {
            return station.getVillageTheme();
        }
        return null;
    }

    public void selectTheme(ResourceLocation theme) {
        this.selectedTheme = theme;
    }
}