package github.mczme.ruralroutes.client.gui.screen;

import github.mczme.ruralroutes.menu.ConfigToolMenu;
import github.mczme.ruralroutes.network.ConfigToolApplyPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 配置工具 GUI 屏幕
 */
public class ConfigToolScreen extends Screen implements MenuAccess<ConfigToolMenu> {

    private final ConfigToolMenu menu;
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE = 10;
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

        List<ResourceLocation> themes = menu.getAvailableThemes();

        int startY = 53;
        int buttonWidth = 200;
        int centerX = (this.width - buttonWidth) / 2;

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

        // 应用按钮 - 发送网络包到服务端
        addRenderableWidget(Button.builder(
            Component.translatable("gui.ruralroutes.config_tool.apply"),
            b -> {
                ResourceLocation selected = menu.getSelectedTheme();
                if (selected != null) {
                    // 发送网络包到服务端
                    PacketDistributor.sendToServer(
                        new ConfigToolApplyPayload(menu.getBlockPos(), selected)
                    );
                    this.onClose();
                }
            }
        ).bounds(centerX, this.height - 50, buttonWidth, BUTTON_HEIGHT).build());

        // 取消按钮
        addRenderableWidget(Button.builder(
            Component.translatable("gui.ruralroutes.config_tool.cancel"),
            b -> this.onClose()
        ).bounds(centerX, this.height - 25, buttonWidth, BUTTON_HEIGHT).build());
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

        // 显示当前主题
        ResourceLocation current = menu.getCurrentTheme();
        if (current != null) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.config_tool.current_theme", current.toString()),
                this.width / 2, 28, 0xAAAAAA);
        } else {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.config_tool.no_current_theme"),
                this.width / 2, 28, 0xAAAAAA);
        }

        // 显示选中主题
        ResourceLocation selected = menu.getSelectedTheme();
        if (selected != null) {
            guiGraphics.drawCenteredString(font,
                Component.translatable("gui.ruralroutes.config_tool.selected", selected.toString()),
                this.width / 2, 40, 0xFFFF00);
        }

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<ResourceLocation> themes = menu.getAvailableThemes();
        int maxScroll = Math.max(0, themes.size() - MAX_VISIBLE);

        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) scrollY));
        rebuildWidgets();
        return true;
    }
}