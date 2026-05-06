package github.mczme.ruralroutes.blockentity;

import github.mczme.ruralroutes.register.RRBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 展示柜方块实体
 * 存储节点ID和贸易站位置用于校验
 */
public class DisplayCaseBlockEntity extends TradeNodeBlockEntity implements MenuProvider {

    public DisplayCaseBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.DISPLAY_CASE.get(), pos, state);
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