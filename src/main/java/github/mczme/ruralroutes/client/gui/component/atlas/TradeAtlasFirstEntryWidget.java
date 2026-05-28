package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.TradeAtlasState;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TradeAtlasFirstEntryWidget extends AbstractWidget {

    private final TradeAtlasState state;
    private final Consumer<VillageStyle> onLocate;
    private final List<Button> buttons = new ArrayList<>();

    public TradeAtlasFirstEntryWidget(int x, int y, int width, int height,
            TradeAtlasState state, Consumer<VillageStyle> onLocate) {
        super(x, y, width, height, Component.translatable("gui.ruralroutes.trade_atlas.first_entry_prompt"));
        this.state = state;
        this.onLocate = onLocate;
        buildButtons();
    }

    private void buildButtons() {
        int startX = getX() + 24;
        int startY = getY() + 76;
        int gap = 6;
        int buttonWidth = (getWidth() - 48 - gap) / 2;

        List<VillageStyle> styles = TradeAtlasUi.villageStyles();
        for (int i = 0; i < styles.size(); i++) {
            VillageStyle style = styles.get(i);
            int col = i % 2;
            int row = i / 2;
            Button button = Button.builder(
                Component.translatable(TradeAtlasUi.firstEntryLabelKey(style)),
                pressed -> onLocate.accept(style)
            ).bounds(startX + col * (buttonWidth + gap), startY + row * 24, buttonWidth, 20).build();
            buttons.add(button);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        int boxX = getX() + 80;
        int boxY = getY() + 48;
        int boxWidth = getWidth() - 160;
        int boxHeight = 118;

        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, TradeAtlasUi.PANEL_BG);
        guiGraphics.renderOutline(boxX, boxY, boxWidth, boxHeight, TradeAtlasUi.PANEL_BORDER);
        guiGraphics.drawCenteredString(font, getMessage(), boxX + boxWidth / 2, boxY + 14, TradeAtlasUi.TEXT_ACCENT);
        guiGraphics.drawCenteredString(font,
            Component.translatable("gui.ruralroutes.trade_atlas.first_entry_hint"),
            boxX + boxWidth / 2,
            boxY + 30,
            TradeAtlasUi.TEXT_DIM);
        guiGraphics.drawString(font,
            Component.translatable("gui.ruralroutes.trade_atlas.section.locate"),
            boxX + 8,
            boxY + 50,
            TradeAtlasUi.TEXT_WARN,
            false);

        boolean canLocate = !state.locating() && !state.hasPendingClue();
        for (Button button : buttons) {
            button.active = canLocate;
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Button entryButton : buttons) {
            if (entryButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        narration.add(NarratedElementType.TITLE, getMessage());
    }
}
