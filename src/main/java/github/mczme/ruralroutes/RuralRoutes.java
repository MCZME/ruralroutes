package github.mczme.ruralroutes;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import github.mczme.ruralroutes.core.theme.ThemeManager;
import github.mczme.ruralroutes.data.RRBlockTagsProvider;
import github.mczme.ruralroutes.data.RRItemTagsProvider;
import github.mczme.ruralroutes.data.ThemeDataProvider;
import github.mczme.ruralroutes.data.ValueDataProvider;
import github.mczme.ruralroutes.data.lang.RREnUsLanguageProvider;
import github.mczme.ruralroutes.data.lang.RRZhCnLanguageProvider;
import github.mczme.ruralroutes.register.RRBlockEntities;
import github.mczme.ruralroutes.register.RRBlocks;
import github.mczme.ruralroutes.register.RRCreativeTabs;
import github.mczme.ruralroutes.register.RRDataMaps;
import github.mczme.ruralroutes.register.RRItems;
import github.mczme.ruralroutes.register.RRMenuTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(RuralRoutes.MODID)
public class RuralRoutes {
    public static final String MODID = "ruralroutes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RuralRoutes(IEventBus modEventBus, ModContainer modContainer) {
        // 注册方块、物品、BlockEntity、创造模式标签页、菜单类型
        RRBlocks.register(modEventBus);
        RRItems.register(modEventBus);
        RRBlockEntities.register(modEventBus);
        RRCreativeTabs.register(modEventBus);
        RRMenuTypes.register(modEventBus);

        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(RRDataMaps::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 注册到 NeoForge 游戏事件总线
        NeoForge.EVENT_BUS.addListener(this::addReloadListener);
    }

    private void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(ThemeManager.INSTANCE);
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
        generator.addProvider(event.includeServer(), new ThemeDataProvider(output, lookupProvider, existingFileHelper));

        // 客户端数据 - 语言文件
        generator.addProvider(event.includeClient(), new RREnUsLanguageProvider(output));
        generator.addProvider(event.includeClient(), new RRZhCnLanguageProvider(output));
    }
}