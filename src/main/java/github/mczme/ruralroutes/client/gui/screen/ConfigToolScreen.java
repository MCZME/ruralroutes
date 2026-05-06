package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.item.ConfigToolItem;
import github.mczme.ruralroutes.menu.ConfigToolMenu;
import github.mczme.ruralroutes.network.ConfigToolApplyPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

/**
 * 配置工具 GUI 屏幕
 * 贸易站：可编辑主题、复制节点信息
 * 展示柜/传闻板：可粘贴节点信息
 */
public class ConfigToolScreen extends Screen implements MenuAccess<ConfigToolMenu> {

    private final ConfigToolMenu menu;
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 2;

    public ConfigToolScreen(ConfigToolMenu menu, Inventory ignored, Component title) {
        super(title);
        this.menu = menu;
    }

    @Override
    public ConfigToolMenu getMenu() {
        return menu;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 200;
        int centerX = (this.width - buttonWidth) / 2;

        // 仅贸易站显示主题选择列表
        if (menu.canEditTheme()) {
            List<ResourceLocation> themes = menu.getAvailableThemes();

            int startY = 70;
            for (int i = 0; i < Math.min(MAX_VISIBLE, themes.size() - scrollOffset); i++) {
                int index = scrollOffset + i;
                ResourceLocation theme = themes.get(index);

                Button button = Button.builder(
                    Component.literal(theme.toString()),
                    b -> selectTheme(theme)
                ).bounds(centerX, startY + i * (BUTTON_HEIGHT + BUTTON_SPACING),
                    buttonWidth, BUTTON_HEIGHT).build();

                addRenderableWidget(button);
            }
        }

        // 底部按钮区域
        int bottomY = this.height - 80;

        if (menu.canEditTheme()) {
            // 贸易站：应用主题按钮 + 复制节点信息按钮
            ResourceLocation selected = menu.getSelectedTheme();
            if (selected != null) {
                addRenderableWidget(Button.builder(
                    Component.translatable("gui.ruralroutes.config_tool.apply"),
                    b -> {
                        PacketDistributor.sendToServer(
                            ConfigToolApplyPayload.setTheme(menu.getBlockPos(), selected)
                        );
                        this.onClose();
                    }
                ).bounds(centerX, bottomY, buttonWidth, BUTTON_HEIGHT).build());
                bottomY += 25;
            }

            // 复制节点信息按钮（仅当有节点ID时显示）
            UUID nodeId = menu.getCurrentTradeNodeId();
            BlockPos stationPos = menu.getCurrentStationPos();
            if (nodeId != null && stationPos != null) {
                String shortId = nodeId.toString().substring(0, 8) + "...";
                addRenderableWidget(Button.builder(
                    Component.translatable("gui.ruralroutes.config_tool.copy_node_info", shortId),
                    b -> {
                        // 直接更新客户端 ItemStack
                        ItemStack mainHand = this.minecraft.player.getMainHandItem();
                        if (mainHand.getItem() instanceof ConfigToolItem) {
                            ConfigToolItem.setCopiedNodeInfo(mainHand, nodeId, stationPos);
                        }
                        // 发送到服务端同步
                        PacketDistributor.sendToServer(
                            ConfigToolApplyPayload.copyNodeInfo(menu.getBlockPos(), nodeId, stationPos)
                        );
                        this.onClose();
                    }
                ).bounds(centerX, bottomY, buttonWidth, BUTTON_HEIGHT).build());
                bottomY += 25;
            }

            // 取消按钮
            addRenderableWidget(Button.builder(
                Component.translatable("gui.ruralroutes.config_tool.cancel"),
                b -> this.onClose()
            ).bounds(centerX, bottomY, buttonWidth, BUTTON_HEIGHT).build());
        } else {
            // 展示柜/传闻板：粘贴节点信息按钮 + 关闭按钮
            if (menu.hasCopiedNodeInfo()) {
                String shortId = menu.getCopiedNodeId().toString().substring(0, 8) + "...";
                addRenderableWidget(Button.builder(
                    Component.translatable("gui.ruralroutes.config_tool.paste_node_info", shortId),
                    b -> {
                        PacketDistributor.sendToServer(
                            ConfigToolApplyPayload.pasteNodeInfo(
                                menu.getBlockPos(),
                                menu.getCopiedNodeId(),
                                menu.getCopiedStationPos()
                            )
                        );
                        this.onClose();
                    }
                ).bounds(centerX, bottomY, buttonWidth, BUTTON_HEIGHT).build());
                bottomY += 25;
            }

            // 关闭按钮
            addRenderableWidget(Button.builder(
                Component.translatable("gui.ruralroutes.config_tool.close"),
                b -> this.onClose()
            ).bounds(centerX, bottomY, buttonWidth, BUTTON_HEIGHT).build());
        }
    }

    private void selectTheme(ResourceLocation theme) {
        menu.selectTheme(theme);
        this.rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制标题
        guiGraphics.drawCenteredString(font, this.title, this.width / 2, 15, 0xFFFFFF);

        int lineY = 28;

        // 显示方块类型
        ConfigToolMenu.BlockType blockType = menu.getBlockType();
        String typeKey = switch (blockType) {
            case TRADE_STATION -> "gui.ruralroutes.config_tool.type.trade_station";
            case DISPLAY_CASE -> "gui.ruralroutes.config_tool.type.display_case";
            case RUMOR_BOARD -> "gui.ruralroutes.config_tool.type.rumor_board";
            default -> "gui.ruralroutes.config_tool.type.unknown";
        };
        guiGraphics.drawCenteredString(font,
            Component.translatable(typeKey),
            this.width / 2, lineY, 0xAAAAAA);

        lineY += 12;

        // 显示节点ID
        UUID nodeId = menu.getCurrentTradeNodeId();
        if (nodeId != null) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.config_tool.node_id", nodeId.toString().substring(0, 8) + "..."),
                this.width / 2, lineY, 0x888888);
        } else {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.config_tool.no_node_id"),
                this.width / 2, lineY, 0x888888);
        }

        // 仅贸易站显示主题相关信息
        if (menu.canEditTheme()) {
            lineY += 12;

            ResourceLocation current = menu.getCurrentTheme();
            if (current != null) {
                guiGraphics.drawCenteredString(font,
                    Component.translatable("gui.ruralroutes.config_tool.current_theme", current.toString()),
                    this.width / 2, lineY, 0xAAAAAA);
            } else {
                guiGraphics.drawCenteredString(font,
                    Component.translatable("gui.ruralroutes.config_tool.no_current_theme"),
                    this.width / 2, lineY, 0xAAAAAA);
            }

            lineY += 10;

            ResourceLocation selected = menu.getSelectedTheme();
            if (selected != null) {
                guiGraphics.drawCenteredString(font,
                    Component.translatable("gui.ruralroutes.config_tool.selected", selected.toString()),
                    this.width / 2, lineY, 0xFFFF00);
            }
        } else {
            // 展示柜/传闻板：显示复制的节点信息
            if (menu.hasCopiedNodeInfo()) {
                lineY += 12;
                String shortId = menu.getCopiedNodeId().toString().substring(0, 8) + "...";
                guiGraphics.drawCenteredString(font,
                    Component.translatable("gui.ruralroutes.config_tool.copied_node_info", shortId),
                    this.width / 2, lineY, 0x88FF88);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.canEditTheme()) {
            List<ResourceLocation> themes = menu.getAvailableThemes();
            int maxScroll = Math.max(0, themes.size() - MAX_VISIBLE);

            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
            rebuildWidgets();
            return true;
        }
        return false;
    }
}