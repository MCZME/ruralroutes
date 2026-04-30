package github.mczme.ruralroutes.core.structure;

import com.mojang.datafixers.util.Pair;
import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.mixin.StructureTemplatePoolAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;

/**
 * 村庄结构注入工具类
 * 用于向原版村庄模板池注入自定义建筑
 */
public class VillageStructureInjector {

    /**
     * 村庄类型配置
     */
    public record VillageConfig(
        String poolId,           // 目标模板池 ID，如 "minecraft:village/plains/houses"
        String structurePath,    // 注入的结构模板路径
        int weight               // 权重
    ) {}

    /**
     * 所有需要注入的村庄配置
     */
    private static final List<VillageConfig> VILLAGE_CONFIGS = List.of(
        // 平原村庄
        new VillageConfig(
            "minecraft:village/plains/houses",
            "ruralroutes:village/plains/trade_station",
            10
        )
        // // 沙漠村庄
        // new VillageConfig(
        //     "minecraft:village/desert/houses",
        //     "ruralroutes:village/desert/trade_station",
        //     10
        // ),
        // // 热带草原村庄
        // new VillageConfig(
        //     "minecraft:village/savanna/houses",
        //     "ruralroutes:village/savanna/trade_station",
        //     10
        // ),
        // // 针叶林村庄
        // new VillageConfig(
        //     "minecraft:village/taiga/houses",
        //     "ruralroutes:village/taiga/trade_station",
        //     10
        // ),
        // // 积雪村庄
        // new VillageConfig(
        //     "minecraft:village/snowy/houses",
        //     "ruralroutes:village/snowy/trade_station",
        //     10
        // )
    );

    /**
     * 空处理器列表的 ResourceKey
     */
    private static final ResourceLocation EMPTY_PROCESSOR_LIST_ID = ResourceLocation.withDefaultNamespace("empty");

    /**
     * 注入所有配置的贸易站到村庄模板池
     *
     * @param registryAccess 注册表访问器
     */
    public static void injectAll(RegistryAccess registryAccess) {
        RuralRoutes.LOGGER.debug("开始注入贸易站结构到村庄模板池...");

        for (VillageConfig config : VILLAGE_CONFIGS) {
            try {
                injectStructure(registryAccess, config);
            } catch (Exception e) {
                RuralRoutes.LOGGER.error("注入结构失败: {} -> {}", config.poolId(), config.structurePath(), e);
            }
        }

        RuralRoutes.LOGGER.debug("贸易站结构注入完成");
    }

    /**
     * 向指定模板池注入结构
     *
     * @param registryAccess 注册表访问器
     * @param config 村庄配置
     */
    private static void injectStructure(RegistryAccess registryAccess, VillageConfig config) {
        // 获取空处理器列表
        var processorRegistry = registryAccess.lookupOrThrow(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> emptyProcessorList = processorRegistry
                .getOrThrow(ResourceKey.create(
                    Registries.PROCESSOR_LIST,
                    EMPTY_PROCESSOR_LIST_ID
                ));

        // 创建结构池元素
        StructurePoolElement element = StructurePoolElement.legacy(config.structurePath(), emptyProcessorList)
                .apply(StructureTemplatePool.Projection.RIGID);

        // 获取目标模板池
        var poolRegistry = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL);
        ResourceLocation poolId = ResourceLocation.parse(config.poolId());
        ResourceKey<StructureTemplatePool> poolKey = ResourceKey.create(Registries.TEMPLATE_POOL, poolId);

        StructureTemplatePool pool = poolRegistry.get(poolKey).map(Holder::value).orElse(null);
        if (pool == null) {
            RuralRoutes.LOGGER.warn("模板池不存在: {}", config.poolId());
            return;
        }

        RuralRoutes.LOGGER.debug("找到模板池: {}, 原大小: {}", config.poolId(), pool.size());

        // 使用 Accessor 修改模板池
        StructureTemplatePoolAccessor accessor = (StructureTemplatePoolAccessor) pool;

        // 修改 templates 列表（原列表可能不可变，创建新列表）
        ObjectArrayList<StructurePoolElement> templates = new ObjectArrayList<>(accessor.ruralroutes$getTemplates());
        for (int i = 0; i < config.weight(); i++) {
            templates.add(element);
        }
        accessor.ruralroutes$setTemplates(templates);

        // 修改 rawTemplates 列表
        List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<>(accessor.ruralroutes$getRawTemplates());
        rawTemplates.add(new Pair<>(element, config.weight()));
        accessor.ruralroutes$setRawTemplates(rawTemplates);

        RuralRoutes.LOGGER.debug("成功注入结构: {} -> {} (权重: {}, 新大小: {})", config.poolId(), config.structurePath(), config.weight(), templates.size());
    }
}
