package github.mczme.ruralroutes.menu;

import github.mczme.ruralroutes.Config;
import github.mczme.ruralroutes.blockentity.RumorBoardBlockEntity;
import github.mczme.ruralroutes.core.market.MarketState;
import github.mczme.ruralroutes.core.market.MarketStateGenerator;
import github.mczme.ruralroutes.core.rumor.RumorEntry;
import github.mczme.ruralroutes.core.rumor.RumorGenerator;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayout;
import github.mczme.ruralroutes.core.rumor.StickyNoteLayoutGenerator;
import github.mczme.ruralroutes.register.RRMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 传闻板 GUI 菜单
 * 只读展示市场情报，无交互槽位
 * 客户端直接获取数据，无需服务端同步
 */
public class RumorBoardMenu extends AbstractContainerMenu {

    private final Player player;
    private final Level level;
    private final BlockPos blockPos;
    private final Random random;

    // 情报数据
    private List<Component> displayTexts = new ArrayList<>();
    private List<StickyNoteLayout> layouts = new ArrayList<>();
    private String timeRemainingDesc = "";

    public RumorBoardMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public RumorBoardMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(RRMenuTypes.RUMOR_BOARD.get(), containerId);
        this.player = playerInventory.player;
        this.level = player.level();
        this.blockPos = blockPos;
        this.random = new Random();
    }

    /**
     * 客户端：收集数据
     * 在 Screen 渲染时调用，确保客户端已完全初始化
     */
    public void collectClientData() {
        if (!level.isClientSide) return;

        // 1. 计算当前周期索引
        long gameTime = level.getGameTime();
        long cycleIndex = gameTime / Config.getCycleLengthInTicks();

        // 2. 生成市场状态（确定性）
        MarketState marketState = MarketStateGenerator.generateDeterministic(cycleIndex);

        // 3. 生成布局种子
        random.setSeed(StickyNoteLayoutGenerator.generateSeed(cycleIndex, blockPos));

        // 4. 生成情报
        List<RumorEntry> rumors = RumorGenerator.generateFromMarketState(marketState, random);

        // 5. 生成显示文本
        displayTexts.clear();
        for (RumorEntry rumor : rumors) {
            displayTexts.add(rumor.getDisplayText(random));
        }

        // 6. 获取/生成布局
        BlockEntity be = level.getBlockEntity(blockPos);
        if (be instanceof RumorBoardBlockEntity rumorBoard) {
            layouts = rumorBoard.getOrGenerateLayouts(rumors.size(), cycleIndex, random);
        } else {
            layouts = StickyNoteLayoutGenerator.generate(rumors.size(), random);
        }

        // 7. 计算周期剩余时间描述
        timeRemainingDesc = calculateTimeRemaining(gameTime);
    }

    /**
     * 计算周期剩余时间描述
     */
    private String calculateTimeRemaining(long gameTime) {
        long cycleLength = Config.getCycleLengthInTicks();
        long ticksRemaining = cycleLength - (gameTime % cycleLength);

        // 转换为游戏日和小时（24000 ticks = 1 游戏日，1000 ticks = 1 小时）
        long daysRemaining = ticksRemaining / 24000;
        long hoursRemaining = (ticksRemaining % 24000) / 1000;

        if (daysRemaining > 0) {
            return Component.translatable("gui.ruralroutes.rumor_board.time_days", daysRemaining).getString();
        } else if (hoursRemaining > 0) {
            return Component.translatable("gui.ruralroutes.rumor_board.time_hours", hoursRemaining).getString();
        } else {
            return Component.translatable("gui.ruralroutes.rumor_board.time_soon").getString();
        }
    }

    // ===== Getters =====

    public List<Component> getDisplayTexts() {
        return displayTexts;
    }

    public List<StickyNoteLayout> getLayouts() {
        return layouts;
    }

    public String getTimeRemainingDesc() {
        return timeRemainingDesc;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}