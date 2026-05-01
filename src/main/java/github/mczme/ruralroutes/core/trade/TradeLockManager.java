package github.mczme.ruralroutes.core.trade;

import net.minecraft.world.level.ChunkPos;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

/**
 * 交易并发锁管理器
 * 基于 ChunkPos 的 Striped Lock 实现
 * 同一区块的交易请求串行执行，不同区块可并行
 */
public final class TradeLockManager {

    private static final int STRIPES = 1024;
    private final ReentrantLock[] locks;

    public TradeLockManager() {
        locks = new ReentrantLock[STRIPES];
        for (int i = 0; i < STRIPES; i++) {
            locks[i] = new ReentrantLock(true);
        }
    }

    /**
     * 根据区块坐标获取锁索引
     */
    private int getStripeIndex(ChunkPos chunkPos) {
        int hash = chunkPos.x ^ chunkPos.z;
        return (hash & 0x7FFFFFFF) % STRIPES;
    }

    /**
     * 获取锁
     * @param chunkPos 区块坐标
     * @return 是否成功获取锁
     */
    public boolean acquire(ChunkPos chunkPos) {
        int index = getStripeIndex(chunkPos);
        try {
            return locks[index].tryLock(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放锁
     */
    public void release(ChunkPos chunkPos) {
        int index = getStripeIndex(chunkPos);
        if (locks[index].isHeldByCurrentThread()) {
            locks[index].unlock();
        }
    }

    /**
     * 尝试立即获取锁（不等待）
     */
    public boolean tryAcquireImmediate(ChunkPos chunkPos) {
        int index = getStripeIndex(chunkPos);
        return locks[index].tryLock();
    }
}
