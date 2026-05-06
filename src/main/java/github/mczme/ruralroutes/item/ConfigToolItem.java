package github.mczme.ruralroutes.item;

import github.mczme.ruralroutes.blockentity.RumorBoardBlockEntity;
import github.mczme.ruralroutes.blockentity.DisplayCaseBlockEntity;
import github.mczme.ruralroutes.blockentity.TradeStationBlockEntity;
import github.mczme.ruralroutes.menu.ConfigToolMenu;
import github.mczme.ruralroutes.register.RRDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.UUID;

/**
 * 配置工具 - 用于查看和配置核心方块
 * 贸易站：可编辑主题、复制节点信息
 * 展示柜/传闻板：可粘贴节点信息
 */
public class ConfigToolItem extends Item {

    public ConfigToolItem(Properties properties) {
        super(properties);
    }

    // ===== Data Components 操作方法 =====

    public static UUID getCopiedNodeId(ItemStack stack) {
        return stack.get(RRDataComponents.COPIED_NODE_ID.get());
    }

    public static BlockPos getCopiedStationPos(ItemStack stack) {
        return stack.get(RRDataComponents.COPIED_STATION_POS.get());
    }

    public static void setCopiedNodeInfo(ItemStack stack, UUID nodeId, BlockPos stationPos) {
        stack.set(RRDataComponents.COPIED_NODE_ID.get(), nodeId);
        stack.set(RRDataComponents.COPIED_STATION_POS.get(), stationPos);
    }

    public static void clearCopiedNodeInfo(ItemStack stack) {
        stack.remove(RRDataComponents.COPIED_NODE_ID.get());
        stack.remove(RRDataComponents.COPIED_STATION_POS.get());
    }

    public static boolean hasCopiedNodeInfo(ItemStack stack) {
        return stack.has(RRDataComponents.COPIED_NODE_ID.get())
            && stack.has(RRDataComponents.COPIED_STATION_POS.get());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (level.isClientSide || player == null) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TradeStationBlockEntity) {
            // 贸易站：打开配置 GUI
            openMenu(player, pos);
            return InteractionResult.SUCCESS;
        } else if (be instanceof DisplayCaseBlockEntity || be instanceof RumorBoardBlockEntity) {
            // 展示柜/传闻板：打开配置 GUI
            openMenu(player, pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * 打开配置工具菜单
     */
    private void openMenu(Player player, BlockPos pos) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.ruralroutes.config_tool.title");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return new ConfigToolMenu(containerId, inventory, pos);
            }
        }, buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(
            "item.ruralroutes.config_tool.tooltip"
        ));
    }
}