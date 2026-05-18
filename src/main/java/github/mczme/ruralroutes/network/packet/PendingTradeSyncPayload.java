package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.trade.TradeContractType;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 暂存区交易同步网络包
 * 从服务端发送到客户端，同步暂存区的多物品交易数据
 */
public record PendingTradeSyncPayload(
    int containerId,
    boolean isBuyTrade,
    List<PendingSlotData> pendingSlots,    // 暂存槽位列表
    Map<Integer, Integer> pendingCountMap   // slotIndex -> pendingCount
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PendingTradeSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "pending_trade_sync"));

    /**
     * 单个暂存槽位数据
     */
    public record PendingSlotData(
        ResourceLocation itemId,
        ItemStack displayStack,
        int stockCount,      // 数量（baseStock）
        int price,
        boolean isBuy,
        int sourceSlotIndex,
        TradeContractType tradeType,
        List<ItemStack> priceStacks,
        List<ItemStack> inputStacks
    ) {
        /**
         * 序列化 PendingSlotData
         */
        public static void encode(RegistryFriendlyByteBuf buf, PendingSlotData data) {
            buf.writeResourceLocation(data.itemId);
            // 使用标准 ItemStack codec 序列化（支持组件数据）
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, data.displayStack);
            buf.writeVarInt(data.stockCount);
            buf.writeVarInt(data.price);
            buf.writeBoolean(data.isBuy);
            buf.writeVarInt(data.sourceSlotIndex);
            buf.writeEnum(data.tradeType);
            // 序列化 priceStacks
            buf.writeVarInt(data.priceStacks.size());
            for (ItemStack stack : data.priceStacks) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            }
            // 序列化 inputStacks
            buf.writeVarInt(data.inputStacks.size());
            for (ItemStack stack : data.inputStacks) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            }
        }

        /**
         * 反序列化 PendingSlotData
         */
        public static PendingSlotData decode(RegistryFriendlyByteBuf buf) {
            ResourceLocation itemId = buf.readResourceLocation();
            ItemStack displayStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            int stockCount = buf.readVarInt();
            int price = buf.readVarInt();
            boolean isBuy = buf.readBoolean();
            int sourceSlotIndex = buf.readVarInt();
            TradeContractType tradeType = buf.readEnum(TradeContractType.class);
            // 反序列化 priceStacks
            int priceCount = buf.readVarInt();
            List<ItemStack> priceStacks = new ArrayList<>();
            for (int i = 0; i < priceCount; i++) {
                priceStacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            // 反序列化 inputStacks
            int inputCount = buf.readVarInt();
            List<ItemStack> inputStacks = new ArrayList<>();
            for (int i = 0; i < inputCount; i++) {
                inputStacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            return new PendingSlotData(itemId, displayStack, stockCount, price, isBuy, sourceSlotIndex, tradeType, priceStacks, inputStacks);
        }
    }

    /**
     * StreamCodec 使用 RegistryFriendlyByteBuf 以支持 ItemStack 组件数据
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, PendingTradeSyncPayload> STREAM_CODEC =
        StreamCodec.of(
            PendingTradeSyncPayload::encode,
            PendingTradeSyncPayload::decode
        );

    private static void encode(RegistryFriendlyByteBuf buf, PendingTradeSyncPayload payload) {
        buf.writeVarInt(payload.containerId);
        buf.writeBoolean(payload.isBuyTrade);

        // 写入槽位列表
        buf.writeVarInt(payload.pendingSlots.size());
        for (PendingSlotData slot : payload.pendingSlots) {
            PendingSlotData.encode(buf, slot);
        }

        // 写入 pendingCountMap
        buf.writeVarInt(payload.pendingCountMap.size());
        for (Map.Entry<Integer, Integer> mapEntry : payload.pendingCountMap.entrySet()) {
            buf.writeVarInt(mapEntry.getKey());
            buf.writeVarInt(mapEntry.getValue());
        }
    }

    private static PendingTradeSyncPayload decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        boolean isBuyTrade = buf.readBoolean();

        // 读取槽位列表
        int slotCount = buf.readVarInt();
        List<PendingSlotData> pendingSlots = new ArrayList<>();
        for (int i = 0; i < slotCount; i++) {
            pendingSlots.add(PendingSlotData.decode(buf));
        }

        // 读取 pendingCountMap
        int mapSize = buf.readVarInt();
        Map<Integer, Integer> pendingCountMap = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            int key = buf.readVarInt();
            int value = buf.readVarInt();
            pendingCountMap.put(key, value);
        }

        return new PendingTradeSyncPayload(containerId, isBuyTrade, pendingSlots, pendingCountMap);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理
     */
    public static void handleClient(PendingTradeSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;

            if (player.containerMenu instanceof TradeStationMenu tradeMenu
                && player.containerMenu.containerId == payload.containerId) {
                tradeMenu.receivePendingTradeData(payload);
            }
        });
    }
}
