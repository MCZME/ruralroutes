package github.mczme.ruralroutes.core.cycle;

import github.mczme.ruralroutes.Config;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 贸易周期管理器
 *
 * 全局周期核心，用于计算周期索引。
 * 所有商业节点在玩家交互时与周期核心对比，判断是否需要刷新。
 */
public class CycleManager extends SavedData {

    private static final String DATA_NAME = "ruralroutes_cycle";

    /**
     * 获取或创建 CycleManager
     */
    public static CycleManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(CycleManager::create, CycleManager::load),
            DATA_NAME
        );
    }

    /**
     * 创建新实例
     */
    public static CycleManager create() {
        return new CycleManager();
    }

    /**
     * 从 NBT 加载
     */
    public static CycleManager load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return create();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return tag;
    }

    /**
     * 计算指定 tick 所在的周期索引
     * @param gameTime 游戏时间（tick）
     * @return 周期索引
     */
    public long getCycleIndex(long gameTime) {
        return gameTime / Config.getCycleLengthInTicks();
    }

    /**
     * 检查节点是否需要刷新
     * @param nodeTimestamp 节点的刷新时间戳
     * @param currentGameTime 当前游戏时间
     * @return true 表示节点所在的周期已过期，需要刷新
     */
    public boolean needsRefresh(long nodeTimestamp, long currentGameTime) {
        long nodeCycle = getCycleIndex(nodeTimestamp);
        long currentCycle = getCycleIndex(currentGameTime);
        return currentCycle > nodeCycle;
    }

    /**
     * 获取当前周期起始 tick
     * @param gameTime 当前游戏时间
     * @return 当前周期开始的 tick
     */
    public long getCurrentCycleStartTick(long gameTime) {
        long cycleIndex = getCycleIndex(gameTime);
        return cycleIndex * Config.getCycleLengthInTicks();
    }
}