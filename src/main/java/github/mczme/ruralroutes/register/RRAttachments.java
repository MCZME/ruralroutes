package github.mczme.ruralroutes.register;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.advancement.RRPlayerProgressState;
import github.mczme.ruralroutes.core.node.CommercialNodeData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * 区块数据 Attachment 注册
 */
public class RRAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RuralRoutes.MODID);

    /**
     * 商业节点数据 - 存储在区块中
     * 包含 tradeNodeId、themeName、stocks、refreshTimestamp
     */
    public static final Supplier<AttachmentType<CommercialNodeData>> COMMERCIAL_NODE =
        ATTACHMENTS.register("commercial_node", () ->
            AttachmentType.builder(CommercialNodeData::empty)
                .serialize(CommercialNodeData.CODEC)
                .build());

    public static final Supplier<AttachmentType<RRPlayerProgressState>> PLAYER_PROGRESS =
        ATTACHMENTS.register("player_progress", () ->
            AttachmentType.builder(() -> RRPlayerProgressState.EMPTY)
                .serialize(RRPlayerProgressState.CODEC)
                .copyOnDeath()
                .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}
