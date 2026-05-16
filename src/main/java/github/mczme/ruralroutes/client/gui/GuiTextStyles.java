package github.mczme.ruralroutes.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * GUI 文本样式辅助工具。
 */
public final class GuiTextStyles {

    private static final ResourceLocation UNIFORM_FONT = ResourceLocation.withDefaultNamespace("uniform");

    private GuiTextStyles() {
    }

    public static Component uniform(Component component) {
        return component.copy().withStyle(style -> style.withFont(UNIFORM_FONT));
    }

    public static Component uniformLiteral(String text) {
        return uniform(Component.literal(text));
    }
}
