package github.mczme.ruralroutes.core.cycle;

import github.mczme.ruralroutes.Config;
import github.mczme.ruralroutes.core.market.MarketState;
import github.mczme.ruralroutes.core.market.MarketStateGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 贸易周期管理器
 *
 * 全局周期核心，用于计算周期索引。
 * 所有商业节点在玩家交互时与周期核心对比，判断是否需要刷新。
 */
public class CycleManager extends SavedData {

    private static final String DATA_NAME = "ruralroutes_cycle";

    /** 缓存的当前周期索引 */
    private long currentCycle = -1;

    /** 当前周期是否需要刷新 */
    private boolean needsRefresh = true;

    /** 当前周期的市场状态 */
    private MarketState marketState;

    /**
     * 获取有效时间
     * 根据配置的时间模式返回对应的时间值
     */
    public static long getEffectiveTime(Level level) {
        return switch (Config.CYCLE_TIME_MODE.get()) {
            case SERVER_TIME -> level.getGameTime();
            case GAME_TIME   -> level.getDayTime();
        };
    }

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
        CycleManager manager = create();

        // 加载周期缓存
        if (tag.contains("current_cycle")) {
            manager.currentCycle = tag.getLong("current_cycle");
        }
        if (tag.contains("needs_refresh")) {
            manager.needsRefresh = tag.getBoolean("needs_refresh");
        }

        // 加载市场状态
        if (tag.contains("market_state")) {
            manager.marketState = MarketState.CODEC.parse(
                    NbtOps.INSTANCE,
                    tag.get("market_state")
            ).result().orElse(null);
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        // 保存周期缓存
        tag.putLong("current_cycle", currentCycle);
        tag.putBoolean("needs_refresh", needsRefresh);

        // 保存市场状态
        if (marketState != null) {
            MarketState.CODEC.encodeStart(
                    NbtOps.INSTANCE,
                    marketState
            ).result().ifPresent(nbt -> tag.put("market_state", nbt));
        }
        return tag;
    }

    /**
     * 计算指定世界的周期索引
     * @param level 世界
     * @return 周期索引
     */
    public long getCycleIndex(ServerLevel level) {
        return getEffectiveTime(level) / Config.getCycleLengthInTicks();
    }

    /**
     * 计算指定时间戳对应的周期索引
     * 用于计算节点时间戳对应的周期
     * @param timestamp 时间戳
     * @return 周期索引
     */
    public long getCycleIndex(long timestamp) {
        return timestamp / Config.getCycleLengthInTicks();
    }

    /**
     * 检查节点是否需要刷新
     * @param nodeTimestamp 节点的刷新时间戳
     * @return true 表示需要刷新（周期推进或需要刷新）
     */
    public boolean needsRefresh(long nodeTimestamp) {
        long nodeCycle = getCycleIndex(nodeTimestamp);
        return currentCycle > nodeCycle || needsRefresh;
    }

    /**
     * 获取当前周期起始时间
     * @param level 世界
     * @return 当前周期开始的时间
     */
    public long getCurrentCycleStartTick(ServerLevel level) {
        long cycleIndex = getCycleIndex(level);
        return cycleIndex * Config.getCycleLengthInTicks();
    }

    // ===== 市场状态相关方法 =====

    /**
     * 获取当前市场状态
     */
    public MarketState getMarketState() {
        return marketState;
    }

    /**
     * 检查是否已有指定周期的市场状态
     */
    public boolean hasMarketStateFor(long cycleIndex) {
        return marketState != null && marketState.cycleIndex() == cycleIndex;
    }

    /**
     * 设置市场状态
     */
    public void setMarketState(MarketState state) {
        this.marketState = state;
        setDirty();
    }

    /**
     * 获取或初始化市场状态
     * 如果市场状态为空或周期不匹配，则生成新的市场状态
     * @return 当前周期的市场状态
     */
    public MarketState getOrInitMarketState() {
        // 如果市场状态不存在或周期已过期，生成新状态
        if (marketState == null || marketState.cycleIndex() != currentCycle) {
            marketState = MarketStateGenerator.generateDeterministic(currentCycle);
            setDirty();
        }

        return marketState;
    }

    // ===== 周期缓存相关方法 =====

    /**
     * 获取当前缓存的周期索引
     */
    public long getCurrentCycle() {
        return currentCycle;
    }

    /**
     * 更新缓存的周期索引
     * @param level 世界
     * @return true 表示周期已推进
     */
    public boolean updateCurrentCycle(ServerLevel level) {
        long newCycle = getCycleIndex(level);
        if (newCycle != currentCycle) {
            currentCycle = newCycle;
            return true;
        }
        return false;
    }

    /**
     * 检查当前周期是否需要刷新
     */
    public boolean isNeedsRefresh() {
        return needsRefresh;
    }

    /**
     * 标记当前周期已刷新
     */
    public void markRefreshed() {
        this.needsRefresh = false;
        setDirty();
    }

    /**
     * 请求刷新当前周期
     */
    public void requestRefresh() {
        this.needsRefresh = true;
        setDirty();
    }
}