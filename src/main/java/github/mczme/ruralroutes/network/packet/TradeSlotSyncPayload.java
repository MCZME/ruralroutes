package github.mczme.ruralroutes.network.packet;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.trade.TradeContractType;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 贸易槽位同步网络包
 * 从服务端发送到客户端，同步贸易站的展示槽位数据
 * 支持动态槽位数量
 */
public record TradeSlotSyncPayload(
    int containerId,
    int sellSlotCount,
    int buySlotCount,
    List<SlotData> sellSlots,
    List<SlotData> buySlots
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TradeSlotSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RuralRoutes.MODID, "trade_slot_sync"));

    /**
     * 单个槽位数据
     */
    public record SlotData(
        int slotIndex,
        ItemStack displayStack,
        int stockCount,
        int maxStock,
        int basePrice,                      // 基础价格（用于排序/调试）
        List<ItemStack> priceStacks,        // 货币篮报价
        List<ItemStack> inputStacks,        // 以物易物输入物品
        boolean isBuy,
        TradeContractType tradeType         // 交易类型
    ) {
        /**
         * 序列化 SlotData
         */
        public static void encode(RegistryFriendlyByteBuf buf, SlotData data) {
            buf.writeVarInt(data.slotIndex);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, data.displayStack);
            buf.writeVarInt(data.stockCount);
            buf.writeVarInt(data.maxStock);
            buf.writeVarInt(data.basePrice);

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

            buf.writeBoolean(data.isBuy);
            buf.writeEnum(data.tradeType);
        }

        /**
         * 反序列化 SlotData
         */
        public static SlotData decode(RegistryFriendlyByteBuf buf) {
            int slotIndex = buf.readVarInt();
            ItemStack displayStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            int stockCount = buf.readVarInt();
            int maxStock = buf.readVarInt();
            int basePrice = buf.readVarInt();

            // 反序列化 priceStacks
            List<ItemStack> priceStacks = new ArrayList<>();
            int priceSize = buf.readVarInt();
            for (int i = 0; i < priceSize; i++) {
                priceStacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }

            // 反序列化 inputStacks
            List<ItemStack> inputStacks = new ArrayList<>();
            int inputSize = buf.readVarInt();
            for (int i = 0; i < inputSize; i++) {
                inputStacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }

            boolean isBuy = buf.readBoolean();
            TradeContractType tradeType = buf.readEnum(TradeContractType.class);

            return new SlotData(slotIndex, displayStack, stockCount, maxStock, basePrice,
                priceStacks, inputStacks, isBuy, tradeType);
        }
    }

    /**
     * StreamCodec 使用 RegistryFriendlyByteBuf 以支持 ItemStack 组件数据
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, TradeSlotSyncPayload> STREAM_CODEC =
        StreamCodec.of(
            TradeSlotSyncPayload::encode,
            TradeSlotSyncPayload::decode
        );

    private static void encode(RegistryFriendlyByteBuf buf, TradeSlotSyncPayload payload) {
        buf.writeVarInt(payload.containerId);
        buf.writeVarInt(payload.sellSlotCount);
        buf.writeVarInt(payload.buySlotCount);

        // 写入出售槽位列表
        buf.writeVarInt(payload.sellSlots.size());
        for (SlotData data : payload.sellSlots) {
            SlotData.encode(buf, data);
        }

        // 写入收购槽位列表
        buf.writeVarInt(payload.buySlots.size());
        for (SlotData data : payload.buySlots) {
            SlotData.encode(buf, data);
        }
    }

    private static TradeSlotSyncPayload decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        int sellSlotCount = buf.readVarInt();
        int buySlotCount = buf.readVarInt();

        // 读取出售槽位列表
        List<SlotData> sellSlots = new ArrayList<>();
        int sellSize = buf.readVarInt();
        for (int i = 0; i < sellSize; i++) {
            sellSlots.add(SlotData.decode(buf));
        }

        // 读取收购槽位列表
        List<SlotData> buySlots = new ArrayList<>();
        int buySize = buf.readVarInt();
        for (int i = 0; i < buySize; i++) {
            buySlots.add(SlotData.decode(buf));
        }

        return new TradeSlotSyncPayload(containerId, sellSlotCount, buySlotCount, sellSlots, buySlots);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理
     */
    public static void handleClient(TradeSlotSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            if (player.containerMenu instanceof TradeStationMenu tradeMenu
                && player.containerMenu.containerId == payload.containerId) {
                tradeMenu.receiveSlotData(payload.sellSlotCount, payload.buySlotCount, payload.sellSlots, payload.buySlots);
            }
        });
    }
}
