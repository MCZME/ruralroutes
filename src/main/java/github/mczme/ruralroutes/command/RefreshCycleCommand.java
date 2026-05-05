package github.mczme.ruralroutes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.cycle.CycleManager;
import github.mczme.ruralroutes.core.market.MarketState;
import github.mczme.ruralroutes.core.market.MarketStateGenerator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

/**
 * 强制刷新贸易周期命令
 */
public class RefreshCycleCommand {

    /**
     * 注册命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ruralroutes")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .then(Commands.literal("refresh")
                        .executes(RefreshCycleCommand::refreshCycle)
                )
                .then(Commands.literal("market")
                        .then(Commands.literal("refresh")
                                .executes(RefreshCycleCommand::refreshMarket)
                        )
                        .then(Commands.literal("info")
                                .executes(RefreshCycleCommand::showMarketInfo)
                        )
                )
        );
    }

    /**
     * 强制刷新贸易周期
     */
    private static int refreshCycle(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        CycleManager cycleManager = CycleManager.get(level);
        long currentGameTime = level.getGameTime();
        long currentCycle = cycleManager.getCycleIndex(currentGameTime);

        // 强制生成新的市场状态
        MarketState newState = MarketStateGenerator.generate(currentCycle + 1, new Random());
        cycleManager.setMarketState(newState);

        source.sendSuccess(() -> Component.literal("贸易周期已刷新，新周期索引: " + (currentCycle + 1)
                + "，市场事件数量: " + newState.events().size()), true);

        RuralRoutes.LOGGER.info("Manual cycle refresh triggered by {}, new cycle: {}",
                source.getTextName(), currentCycle + 1);

        return 1;
    }

    /**
     * 强制刷新市场状态
     */
    private static int refreshMarket(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        CycleManager cycleManager = CycleManager.get(level);
        long currentGameTime = level.getGameTime();
        long currentCycle = cycleManager.getCycleIndex(currentGameTime);

        // 强制重新生成当前周期的市场状态
        MarketState newState = MarketStateGenerator.generate(currentCycle, new Random());
        cycleManager.setMarketState(newState);

        source.sendSuccess(() -> Component.literal("市场状态已刷新，周期索引: " + currentCycle
                + "，市场事件数量: " + newState.events().size()), true);

        RuralRoutes.LOGGER.info("Manual market refresh triggered by {}, cycle: {}",
                source.getTextName(), currentCycle);

        return 1;
    }

    /**
     * 显示当前市场状态信息
     */
    private static int showMarketInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        CycleManager cycleManager = CycleManager.get(level);
        MarketState state = cycleManager.getMarketState();
        long currentGameTime = level.getGameTime();
        long currentCycle = cycleManager.getCycleIndex(currentGameTime);

        if (state == null) {
            source.sendSuccess(() -> Component.literal("当前无市场状态，周期索引: " + currentCycle), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal(
                "市场状态信息:\n"
                        + "  周期索引: " + state.cycleIndex() + " (当前: " + currentCycle + ")\n"
                        + "  事件数量: " + state.events().size() + "\n"
                        + "  是否同步: " + (state.cycleIndex() == currentCycle ? "是" : "否")
        ), false);

        return 1;
    }
}