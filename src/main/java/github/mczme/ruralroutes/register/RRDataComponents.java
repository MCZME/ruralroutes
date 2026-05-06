package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

/**
 * Data Components 注册
 */
public class RRDataComponents {

    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RuralRoutes.MODID);

    /**
     * 配置工具复制的节点ID
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> COPIED_NODE_ID =
        DATA_COMPONENTS.registerComponentType(
            "copied_node_id",
            builder -> builder
                .persistent(UUIDUtil.CODEC)
                .networkSynchronized(UUIDUtil.STREAM_CODEC)
        );

    /**
     * 配置工具复制的贸易站位置
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> COPIED_STATION_POS =
        DATA_COMPONENTS.registerComponentType(
            "copied_station_pos",
            builder -> builder
                .persistent(BlockPos.CODEC)
                .networkSynchronized(BlockPos.STREAM_CODEC)
        );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}