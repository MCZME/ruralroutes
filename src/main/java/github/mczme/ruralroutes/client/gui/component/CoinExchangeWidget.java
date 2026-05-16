package github.mczme.ruralroutes.client.gui.component;

import github.mczme.ruralroutes.client.gui.GuiTextStyles;
import github.mczme.ruralroutes.core.trade.CoinExchangeContract;
import github.mczme.ruralroutes.menu.TradeStationMenu;
import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 货币交换面板
 * 以玩家钱包为主入口，点击货币筹码后向上弹出可用兑换。
 */
public class CoinExchangeWidget extends AbstractWidget {

    private static final int CONTENT_INSET_X = 6;
    private static final int TITLE_X_OFFSET = -4;
    private static final int TITLE_Y = 6;
    private static final int HINT_X_OFFSET = -4;
    private static final int HINT_Y = 19;
    private static final int UPPER_AREA_HEIGHT = 51;
    private static final int CHIP_ROW_Y = 60;
    private static final int CHIP_HEIGHT = 13;
    private static final int CHIP_GAP = 2;
    private static final int CHIP_WIDTH_SHRINK = 5;
    private static final int CHIP_GROUP_X_OFFSET = -5;
    private static final int CHIP_ICON_SIZE = 10;
    private static final int CHIP_CONTENT_INSET_X = 1;
    private static final int CHIP_CONTENT_INSET_Y = 1;
    private static final int CHIP_TEXT_RIGHT_INSET = 0;
    private static final float CHIP_COUNT_MIN_SCALE = 0.75f;
    private static final int POPUP_PADDING_X = 4;
    private static final int POPUP_PADDING_Y = 3;
    private static final int POPUP_ROW_HEIGHT = 13;
    private static final int POPUP_ROW_GAP = 2;
    private static final int POPUP_INSET_X = 4;
    private static final int POPUP_INSET_BOTTOM = 3;
    private static final int BADGE_BG = 0x7A5C4934;

    private static final int TITLE_COLOR = 0xFF4B3623;
    private static final int HINT_COLOR = 0xFF705A43;
    private static final int CHIP_BG = 0x30F4E6C5;
    private static final int CHIP_HOVER_BG = 0x46F7EDD6;
    private static final int CHIP_SELECTED_BG = 0x8B7E613B;
    private static final int CHIP_BORDER = 0x7C6D542E;
    private static final int CHIP_SELECTED_BORDER = 0xE7D39C58;
    private static final int CHIP_TEXT = 0xFF352618;
    private static final int CHIP_SELECTED_TEXT = 0xFFF8ECC9;
    private static final int POPUP_BG = 0x18FFF9ED;
    private static final int POPUP_BORDER = 0x7D6E532E;
    private static final int POPUP_HIGHLIGHT = 0x16FFF9E4;
    private static final int ROW_BG = 0x9A87673A;
    private static final int ROW_HOVER_BG = 0xB6997644;
    private static final int ROW_PLAYER_SHORT_BG = 0x986A3A34;
    private static final int ROW_VILLAGE_SHORT_BG = 0x96744F29;
    private static final int ROW_BORDER = 0xE1795E2D;
    private static final int ROW_DISABLED_BORDER = 0xA05D4D38;
    private static final int ROW_TEXT = 0xFFF7EBCB;
    private static final int ROW_DISABLED_TEXT = 0xD3C5B49A;

    private BiConsumer<CoinExchangeContract.ExchangeType, Boolean> onExchangeRequest;
    private Supplier<TradeStationMenu.ClientCurrencyWallet> playerWalletSupplier = () -> TradeStationMenu.ClientCurrencyWallet.EMPTY;
    private TradeStationMenu.ClientCurrencyWallet villageWallet = TradeStationMenu.ClientCurrencyWallet.EMPTY;
    private Item selectedCurrencyItem;

    public CoinExchangeWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    public void init(BiConsumer<CoinExchangeContract.ExchangeType, Boolean> onExchangeRequest) {
        this.onExchangeRequest = onExchangeRequest;
    }

    public void setVillageWallet(TradeStationMenu.ClientCurrencyWallet villageWallet) {
        this.villageWallet = villageWallet != null ? villageWallet : TradeStationMenu.ClientCurrencyWallet.EMPTY;
    }

    public void setPlayerWalletSupplier(Supplier<TradeStationMenu.ClientCurrencyWallet> playerWalletSupplier) {
        this.playerWalletSupplier = playerWalletSupplier != null
            ? playerWalletSupplier
            : () -> TradeStationMenu.ClientCurrencyWallet.EMPTY;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = net.minecraft.client.Minecraft.getInstance().font;
        PlayerWallet playerWallet = getPlayerWallet();
        List<WalletChip> chips = buildWalletChips(playerWallet);
        PopupLayout popupLayout = buildPopupLayout(playerWallet, chips);

        guiGraphics.drawCenteredString(font,
            GuiTextStyles.uniform(Component.translatable("gui.ruralroutes.trade_station.coin_exchange")),
            getX() + getWidth() / 2 + TITLE_X_OFFSET,
            getY() + TITLE_Y,
            TITLE_COLOR);

        if (popupLayout == null) {
            guiGraphics.drawCenteredString(font,
                GuiTextStyles.uniform(Component.translatable("gui.ruralroutes.trade_station.coin_exchange.hint")),
                getX() + getWidth() / 2 + HINT_X_OFFSET,
                getY() + HINT_Y,
                HINT_COLOR);
        }

        renderWalletRow(guiGraphics, font, mouseX, mouseY, chips);
        if (popupLayout != null) {
            renderPopup(guiGraphics, font, mouseX, mouseY, popupLayout);
        }
    }

    private void renderWalletRow(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
                                 int mouseX, int mouseY, List<WalletChip> chips) {
        for (WalletChip chip : chips) {
            boolean hovered = isMouseOverRect(mouseX, mouseY, chip.x(), chip.y(), chip.width(), chip.height());
            boolean selected = chip.item() == selectedCurrencyItem;
            int backgroundColor = selected ? CHIP_SELECTED_BG : (hovered ? CHIP_HOVER_BG : CHIP_BG);
            int borderColor = selected ? CHIP_SELECTED_BORDER : CHIP_BORDER;
            int textColor = selected ? CHIP_SELECTED_TEXT : CHIP_TEXT;
            int contentX = chip.x() + CHIP_CONTENT_INSET_X;
            int contentY = chip.y() + CHIP_CONTENT_INSET_Y;
            guiGraphics.fill(chip.x(), chip.y(), chip.x() + chip.width(), chip.y() + chip.height(), backgroundColor);
            guiGraphics.renderOutline(chip.x(), chip.y(), chip.width(), chip.height(), borderColor);
            enableScissor(guiGraphics, chip.x(), chip.y(), chip.x() + chip.width(), chip.y() + chip.height());

            renderItemScaled(guiGraphics, new ItemStack(chip.item()), contentX, contentY, CHIP_ICON_SIZE);

            String countText = formatChipCount(chip.count());
            int minTextX = contentX + CHIP_ICON_SIZE + 1;
            int textRight = chip.x() + chip.width() - CHIP_TEXT_RIGHT_INSET;
            int availableTextWidth = Math.max(0, textRight - minTextX);
            int textWidth = font.width(countText);
            float textScale = textWidth > 0 && availableTextWidth > 0
                ? Math.max(CHIP_COUNT_MIN_SCALE, Math.min(1.0f, availableTextWidth / (float) textWidth))
                : 1.0f;
            int scaledTextWidth = Math.round(textWidth * textScale);
            int textX = Math.max(minTextX, textRight - scaledTextWidth);
            int textY = chip.y() + (chip.height() - font.lineHeight) / 2 + 1;
            drawScaledString(guiGraphics, font, GuiTextStyles.uniformLiteral(countText), textX, textY, textColor, textScale);

            disableScissor(guiGraphics);
        }
    }

    private void renderPopup(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
                             int mouseX, int mouseY, PopupLayout popupLayout) {
        guiGraphics.fill(popupLayout.x(), popupLayout.y(),
            popupLayout.x() + popupLayout.width(), popupLayout.y() + popupLayout.height(), POPUP_BG);
        guiGraphics.fill(popupLayout.x() + 1, popupLayout.y() + 1,
            popupLayout.x() + popupLayout.width() - 1, popupLayout.y() + 2, POPUP_HIGHLIGHT);
        guiGraphics.renderOutline(popupLayout.x(), popupLayout.y(), popupLayout.width(), popupLayout.height(), POPUP_BORDER);

        for (ExchangeOption option : popupLayout.options()) {
            boolean hovered = isMouseOverRect(mouseX, mouseY, option.x(), option.y(), option.width(), option.height());
            int backgroundColor = switch (option.status()) {
                case AVAILABLE -> hovered ? ROW_HOVER_BG : ROW_BG;
                case PLAYER_SHORT -> ROW_PLAYER_SHORT_BG;
                case VILLAGE_SHORT -> ROW_VILLAGE_SHORT_BG;
            };
            int borderColor = option.status() == ActionStatus.AVAILABLE ? ROW_BORDER : ROW_DISABLED_BORDER;
            int textColor = option.status() == ActionStatus.AVAILABLE ? ROW_TEXT : ROW_DISABLED_TEXT;

            guiGraphics.fill(option.x(), option.y(), option.x() + option.width(), option.y() + option.height(), backgroundColor);
            guiGraphics.renderOutline(option.x(), option.y(), option.width(), option.height(), borderColor);

            renderOptionContents(guiGraphics, font, option, textColor);
        }
    }

    private void renderOptionContents(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
                                      ExchangeOption option, int textColor) {
        int contentY = option.y() + 2;
        String outputCount = String.valueOf(option.type().getOutputCount());
        guiGraphics.drawString(font, GuiTextStyles.uniformLiteral(outputCount), option.x() + 4, contentY, textColor, false);
        renderItemScaled(guiGraphics, new ItemStack(option.type().getOutputItem()),
            option.x() + 4 + font.width(outputCount) + 2, option.y() + 2, CHIP_ICON_SIZE);

        int badgeWidth = option.maxTrades() > 1 ? font.width("x" + option.maxTrades()) + 4 : 0;
        Component actionLabel = GuiTextStyles.uniform(Component.translatable(
            "gui.ruralroutes.trade_station.coin_exchange.popup.exchange"
        ));
        int actionX = option.x() + option.width() - font.width(actionLabel) - 5 - (badgeWidth > 0 ? badgeWidth + 4 : 0);
        guiGraphics.drawString(font, actionLabel, actionX, contentY, textColor, false);

        if (option.maxTrades() > 1) {
            String badgeText = "x" + option.maxTrades();
            int badgeX = option.x() + option.width() - badgeWidth - 3;
            guiGraphics.fill(badgeX, option.y() + 2, badgeX + badgeWidth, option.y() + option.height() - 2, BADGE_BG);
            guiGraphics.drawString(font, GuiTextStyles.uniformLiteral(badgeText), badgeX + 2, contentY, ROW_TEXT, false);
        }
    }

    public List<Component> getTooltip(int mouseX, int mouseY) {
        PlayerWallet playerWallet = getPlayerWallet();
        List<WalletChip> chips = buildWalletChips(playerWallet);
        PopupLayout popupLayout = buildPopupLayout(playerWallet, chips);

        if (popupLayout != null) {
            for (ExchangeOption option : popupLayout.options()) {
                if (isMouseOverRect(mouseX, mouseY, option.x(), option.y(), option.width(), option.height())) {
                    return buildActionTooltip(option);
                }
            }
        }

        for (WalletChip chip : chips) {
            if (isMouseOverRect(mouseX, mouseY, chip.x(), chip.y(), chip.width(), chip.height())) {
                return List.of(
                    new ItemStack(chip.item()).getDisplayName(),
                    Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.player_count", chip.count()),
                    Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.open")
                );
            }
        }
        return null;
    }

    private List<Component> buildActionTooltip(ExchangeOption option) {
        List<Component> tooltip = new ArrayList<>();
        Component summary = Component.literal(option.type().getInputCount() + "x ")
            .append(new ItemStack(option.type().getInputItem()).getDisplayName())
            .append(Component.literal(" -> " + option.type().getOutputCount() + "x "))
            .append(new ItemStack(option.type().getOutputItem()).getDisplayName());
        tooltip.add(summary);
        tooltip.add(Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.player_count", option.playerCount())
            .withStyle(style -> style.withColor(0xD0D0D0)));
        tooltip.add(Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.village_count", option.villageCount())
            .withStyle(style -> style.withColor(0xD0D0D0)));
        tooltip.add(Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.max_trades", option.maxTrades())
            .withStyle(style -> style.withColor(0xE6C16B)));

        if (option.status() == ActionStatus.PLAYER_SHORT) {
            tooltip.add(Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.player_insufficient")
                .withStyle(style -> style.withColor(0xFF7A7A)));
        } else if (option.status() == ActionStatus.VILLAGE_SHORT) {
            tooltip.add(Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.village_insufficient")
                .withStyle(style -> style.withColor(0xFFB77A)));
        } else {
            tooltip.add(Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.single")
                .withStyle(style -> style.withColor(0xA3D48F)));
            tooltip.add(Component.translatable("gui.ruralroutes.trade_station.coin_exchange.tooltip.batch")
                .withStyle(style -> style.withColor(0xA3D48F)));
        }
        return tooltip;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isValidClickButton(button)) {
            return false;
        }

        int x = (int) mouseX;
        int y = (int) mouseY;
        PlayerWallet playerWallet = getPlayerWallet();
        List<WalletChip> chips = buildWalletChips(playerWallet);
        PopupLayout popupLayout = buildPopupLayout(playerWallet, chips);

        if (popupLayout != null) {
            for (ExchangeOption option : popupLayout.options()) {
                if (isMouseOverRect(x, y, option.x(), option.y(), option.width(), option.height())) {
                    if (option.status() == ActionStatus.AVAILABLE && onExchangeRequest != null) {
                        onExchangeRequest.accept(option.type(), Screen.hasShiftDown());
                        selectedCurrencyItem = null;
                    }
                    return true;
                }
            }
        }

        for (WalletChip chip : chips) {
            if (isMouseOverRect(x, y, chip.x(), chip.y(), chip.width(), chip.height())) {
                selectedCurrencyItem = chip.item() == selectedCurrencyItem ? null : chip.item();
                return true;
            }
        }

        if (popupLayout != null && (isMouseOver(mouseX, mouseY) || isMouseOverRect(x, y, popupLayout.x(), popupLayout.y(), popupLayout.width(), popupLayout.height()))) {
            selectedCurrencyItem = null;
            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, Component.translatable("gui.ruralroutes.trade_station.coin_exchange"));
    }

    private List<WalletChip> buildWalletChips(PlayerWallet playerWallet) {
        int availableWidth = getWidth() - CONTENT_INSET_X * 2;
        int baseChipWidth = (availableWidth - CHIP_GAP * 2) / 3;
        int chipWidth = Math.max(22, baseChipWidth - CHIP_WIDTH_SHRINK);
        int groupWidth = chipWidth * 3 + CHIP_GAP * 2;
        int chipY = getY() + CHIP_ROW_Y;
        int chipX = getX() + CONTENT_INSET_X + Math.max(0, (availableWidth - groupWidth) / 2) + CHIP_GROUP_X_OFFSET;
        int slotStep = chipWidth + CHIP_GAP;

        return List.of(
            new WalletChip(RRItems.COPPER_COIN.get(), playerWallet.copperCount(), chipX, chipY, chipWidth, CHIP_HEIGHT),
            new WalletChip(RRItems.IRON_COIN.get(), playerWallet.ironCount(), chipX + slotStep, chipY, chipWidth, CHIP_HEIGHT),
            new WalletChip(RRItems.GOLD_COIN.get(), playerWallet.goldCount(), chipX + slotStep * 2, chipY, chipWidth, CHIP_HEIGHT)
        );
    }

    private PopupLayout buildPopupLayout(PlayerWallet playerWallet, List<WalletChip> chips) {
        if (selectedCurrencyItem == null) {
            return null;
        }

        WalletChip selectedChip = null;
        for (WalletChip chip : chips) {
            if (chip.item() == selectedCurrencyItem) {
                selectedChip = chip;
                break;
            }
        }
        if (selectedChip == null) {
            return null;
        }

        List<CoinExchangeContract.ExchangeType> types = exchangeTypesForCurrency(selectedCurrencyItem);
        if (types.isEmpty()) {
            return null;
        }

        int popupWidth = getWidth() - POPUP_INSET_X * 2;
        int popupHeight = POPUP_PADDING_Y * 2 + types.size() * POPUP_ROW_HEIGHT + (types.size() - 1) * POPUP_ROW_GAP;
        int popupX = getX() + POPUP_INSET_X;
        int popupY = getY() + UPPER_AREA_HEIGHT - POPUP_INSET_BOTTOM - popupHeight;

        List<ExchangeOption> options = new ArrayList<>();
        int rowY = popupY + POPUP_PADDING_Y;
        for (CoinExchangeContract.ExchangeType type : types) {
            int playerCount = playerWallet.count(type.getInputItem());
            int villageCount = villageWallet.count(type.getOutputItem());
            int maxByPlayer = type.getInputCount() > 0 ? playerCount / type.getInputCount() : 0;
            int maxByVillage = type.getOutputCount() > 0 ? villageCount / type.getOutputCount() : 0;
            int maxTrades = Math.min(maxByPlayer, maxByVillage);
            ActionStatus status = maxByPlayer <= 0 ? ActionStatus.PLAYER_SHORT
                : (maxByVillage <= 0 ? ActionStatus.VILLAGE_SHORT : ActionStatus.AVAILABLE);

            options.add(new ExchangeOption(
                type,
                popupX + POPUP_PADDING_X,
                rowY,
                popupWidth - POPUP_PADDING_X * 2,
                POPUP_ROW_HEIGHT,
                playerCount,
                villageCount,
                maxTrades,
                status
            ));
            rowY += POPUP_ROW_HEIGHT + POPUP_ROW_GAP;
        }

        return new PopupLayout(popupX, popupY, popupWidth, popupHeight, options);
    }

    private List<CoinExchangeContract.ExchangeType> exchangeTypesForCurrency(Item item) {
        if (item == RRItems.COPPER_COIN.get()) {
            return List.of(CoinExchangeContract.ExchangeType.COPPER_TO_IRON);
        }
        if (item == RRItems.IRON_COIN.get()) {
            return List.of(CoinExchangeContract.ExchangeType.IRON_TO_GOLD, CoinExchangeContract.ExchangeType.IRON_TO_COPPER);
        }
        if (item == RRItems.GOLD_COIN.get()) {
            return List.of(CoinExchangeContract.ExchangeType.GOLD_TO_IRON);
        }
        return List.of();
    }

    private PlayerWallet getPlayerWallet() {
        TradeStationMenu.ClientCurrencyWallet wallet = playerWalletSupplier.get();
        if (wallet == null) {
            return PlayerWallet.EMPTY;
        }

        return new PlayerWallet(
            wallet.copperCount(),
            wallet.ironCount(),
            wallet.goldCount()
        );
    }

    private String formatChipCount(int count) {
        if (count > 99) {
            return "99";
        }
        return String.valueOf(count);
    }

    private void renderItemScaled(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int targetSize) {
        if (stack.isEmpty()) return;

        float scale = targetSize / 16.0f;
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, 1.0f);
        guiGraphics.renderFakeItem(stack, 0, 0);
        poseStack.popPose();
    }

    private void drawScaledString(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font,
                                  Component text, int x, int y, int color, float scale) {
        if (scale == 1.0f) {
            guiGraphics.drawString(font, text, x, y, color, false);
            return;
        }

        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, 1.0f);
        guiGraphics.drawString(font, text, 0, 0, color, false);
        poseStack.popPose();
    }

    private boolean isMouseOverRect(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void enableScissor(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        guiGraphics.enableScissor(x1, y1, x2, y2);
    }

    private void disableScissor(GuiGraphics guiGraphics) {
        guiGraphics.disableScissor();
    }

    private enum ActionStatus {
        AVAILABLE,
        PLAYER_SHORT,
        VILLAGE_SHORT
    }

    private record WalletChip(Item item, int count, int x, int y, int width, int height) {
    }

    private record ExchangeOption(
        CoinExchangeContract.ExchangeType type,
        int x,
        int y,
        int width,
        int height,
        int playerCount,
        int villageCount,
        int maxTrades,
        ActionStatus status
    ) {
    }

    private record PopupLayout(
        int x,
        int y,
        int width,
        int height,
        List<ExchangeOption> options
    ) {
    }

    private record PlayerWallet(int copperCount, int ironCount, int goldCount) {
        private static final PlayerWallet EMPTY = new PlayerWallet(0, 0, 0);

        public int count(Item item) {
            if (item == RRItems.COPPER_COIN.get()) {
                return copperCount;
            }
            if (item == RRItems.IRON_COIN.get()) {
                return ironCount;
            }
            if (item == RRItems.GOLD_COIN.get()) {
                return goldCount;
            }
            return 0;
        }
    }
}
