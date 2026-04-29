package github.mczme.ruralroutes;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import github.mczme.ruralroutes.data.RRBlockTagsProvider;
import github.mczme.ruralroutes.data.RRItemTagsProvider;
import github.mczme.ruralroutes.data.ValueDataProvider;
import github.mczme.ruralroutes.register.RRDataMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(RuralRoutes.MODID)
public class RuralRoutes {
    public static final String MODID = "ruralroutes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RuralRoutes(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(RRDataMaps::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        RRBlockTagsProvider blockTagsProvider = new RRBlockTagsProvider(output, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new RRItemTagsProvider(output, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ValueDataProvider(output, lookupProvider));
    }
}
