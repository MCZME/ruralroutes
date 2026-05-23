package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.data.builder.ThemeBuilder;
import github.mczme.ruralroutes.data.content.desert.DesertContent;
import github.mczme.ruralroutes.data.content.plains.PlainsContent;
import github.mczme.ruralroutes.data.content.savanna.SavannaContent;
import github.mczme.ruralroutes.data.content.snowy.SnowyContent;
import github.mczme.ruralroutes.data.content.taiga.TaigaContent;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 主题模板数据生成器。
 * 具体内容按群系拆分在 data/content/<biome>/ 中维护。
 */
public class ThemeDataProvider extends JsonCodecProvider<ThemeTemplate> {

    public ThemeDataProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Target.DATA_PACK, "ruralroutes/themes", PackType.SERVER_DATA,
              ThemeTemplate.CODEC, lookupProvider, "ruralroutes", existingFileHelper);
    }

    @Override
    protected void gather() {
        defineThemes(builder -> builder.registrar(this::unconditional).register());
    }

    public static List<ResourceLocation> collectBuiltinThemeIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        defineThemes(builder -> ids.add(builder.getId()));
        return List.copyOf(ids);
    }

    public static void defineThemes(Consumer<ThemeBuilder> consumer) {
        PlainsContent.defineThemes(consumer);
        DesertContent.defineThemes(consumer);
        SavannaContent.defineThemes(consumer);
        TaigaContent.defineThemes(consumer);
        SnowyContent.defineThemes(consumer);
    }
}
