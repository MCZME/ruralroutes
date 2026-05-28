package github.mczme.ruralroutes.client.gui.component.atlas;

import github.mczme.ruralroutes.core.atlas.AtlasNodeStatus;
import github.mczme.ruralroutes.core.atlas.TradeAtlasNode;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class TradeAtlasUi {

    public static final int PANEL_HEADER_HEIGHT = 18;
    public static final int ROW_HEIGHT = 20;
    public static final int ROW_GAP = 3;

    public static final int PANEL_BG = 0xD9262B30;
    public static final int PANEL_HEADER_BG = 0xCC384049;
    public static final int PANEL_BORDER = 0xFF596470;
    public static final int MAP_BG = 0xC9161A1E;
    public static final int MAP_GRID = 0x305F6B78;
    public static final int MAP_AXIS = 0x66727F8C;
    public static final int ROW_BG = 0xB62A3138;
    public static final int ROW_SELECTED_BG = 0xD1394650;
    public static final int ROW_BORDER = 0xFF49515B;
    public static final int TEXT_PRIMARY = 0xFFF3F5F7;
    public static final int TEXT_MUTED = 0xFFBCC6D1;
    public static final int TEXT_DIM = 0xFF87929E;
    public static final int TEXT_GOOD = 0xFFA9DD82;
    public static final int TEXT_WARN = 0xFFE5BE73;
    public static final int TEXT_BAD = 0xFFF08B7C;
    public static final int TEXT_ACCENT = 0xFF9BD2F7;
    public static final int CLUE_COLOR = 0xDFF4BB6A;
    public static final int RECORDED_COLOR = 0xFF93D17F;
    public static final int INVALID_COLOR = 0xFF9DA7B4;
    public static final int TARGET_COLOR = 0xFFE5BE73;
    public static final int PLAYER_COLOR = 0xFF73C1F2;
    public static final int PLAYER_CORE_COLOR = 0xFFF3F5F7;

    private TradeAtlasUi() {
    }

    public static void renderPanel(GuiGraphics guiGraphics, Font font, int x, int y, int width, int height,
            Component title) {
        guiGraphics.fill(x, y, x + width, y + height, PANEL_BG);
        guiGraphics.renderOutline(x, y, width, height, PANEL_BORDER);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + PANEL_HEADER_HEIGHT, PANEL_HEADER_BG);
        guiGraphics.drawString(font, title, x + 6, y + 5, TEXT_PRIMARY, false);
    }

    public static void drawDetailLine(GuiGraphics guiGraphics, Font font, int x, int y,
            Component label, Component value, int valueColor) {
        guiGraphics.drawString(font, label, x, y, TEXT_DIM, false);
        guiGraphics.drawString(font, value, x, y + 10, valueColor, false);
    }

    public static int colorForStatus(AtlasNodeStatus status) {
        return switch (status) {
            case CLUE -> CLUE_COLOR;
            case RECORDED -> RECORDED_COLOR;
            case INVALID -> INVALID_COLOR;
        };
    }

    public static int styleColor(VillageStyle style) {
        return switch (style) {
            case PLAINS -> 0xFF84C97A;
            case DESERT -> 0xFFE3C47A;
            case SAVANNA -> 0xFFD79A4B;
            case TAIGA -> 0xFF6EAD8A;
            case SNOWY -> 0xFFAED6E8;
        };
    }

    public static String buildNodeLabel(TradeAtlasNode node) {
        return Component.translatable(node.style().translationKey()).getString()
            + " "
            + Component.translatable(node.status().translationKey()).getString()
            + " "
            + formatXZ(node.position());
    }

    public static String buildShortNodeLabel(TradeAtlasNode node) {
        return Component.translatable(node.style().translationKey()).getString()
            + " "
            + formatXZ(node.position());
    }

    public static String formatPosition(BlockPos position) {
        return position.getX() + ", " + position.getY() + ", " + position.getZ();
    }

    public static String formatXZ(BlockPos position) {
        return position.getX() + ", " + position.getZ();
    }

    public static String ellipsize(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (font.width(builder.toString() + ch + ellipsis) > maxWidth) {
                break;
            }
            builder.append(ch);
        }
        return builder + ellipsis;
    }

    public static List<VillageStyle> villageStyles() {
        return List.of(
            VillageStyle.PLAINS,
            VillageStyle.DESERT,
            VillageStyle.SAVANNA,
            VillageStyle.TAIGA,
            VillageStyle.SNOWY
        );
    }

    public static String firstEntryLabelKey(VillageStyle style) {
        return "gui.ruralroutes.trade_atlas.first_entry." + style.getSerializedName();
    }
}
