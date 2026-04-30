package github.mczme.ruralroutes.mixin;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Mixin 到 JigsawPlacement.Placer，拦截 houses 池的首次访问
 * 实现贸易站的必定且唯一生成
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public class JigsawPlacementPlacerMixin {

    @Final
    @Shadow
    private Registry<StructureTemplatePool> pools;

    // 标记是否已生成贸易站
    @Unique
    private boolean ruralroutes$hasTradeStation = false;

    /**
     * 拦截 Registry.getHolder() 调用，在首次访问 houses 时替换为贸易站池
     */
    @ModifyArg(
        method = "tryPlacingChildren(Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/Registry;getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"
        )
    )
    private ResourceKey<StructureTemplatePool> ruralroutes$forceTradeStationPool(ResourceKey<StructureTemplatePool> resourceKey) {
        // 已生成贸易站，不替换
        if (ruralroutes$hasTradeStation) {
            return resourceKey;
        }

        // 检查是否是 houses 池
        String poolPath = resourceKey.location().getPath();
        if (poolPath.endsWith("/houses")) {
            // 提取村庄类型（如 village/plains/houses -> plains）
            String villageType = extractVillageType(poolPath);
            if (villageType != null) {
                // 构建贸易站池的 ResourceKey
                ResourceLocation tradeStationPoolId = ResourceLocation.fromNamespaceAndPath(
                    RuralRoutes.MODID,
                    "village/" + villageType + "/trade_stations"
                );
                ResourceKey<StructureTemplatePool> tradeStationPoolKey = ResourceKey.create(
                    Registries.TEMPLATE_POOL,
                    tradeStationPoolId
                );

                // 检查贸易站池是否存在
                if (pools.getHolder(tradeStationPoolKey).isPresent()) {
                    ruralroutes$hasTradeStation = true;
                    RuralRoutes.LOGGER.debug("首次访问 {} houses，替换为贸易站池", villageType);
                    return tradeStationPoolKey;
                }
            }
        }

        return resourceKey;
    }

    /**
     * 从池路径中提取村庄类型
     * 例如 "village/plains/houses" -> "plains"
     */
    @Unique
    private static String extractVillageType(String poolPath) {
        // 格式: village/{type}/houses 或 village/{type}/zombie/houses
        String[] parts = poolPath.split("/");
        if (parts.length >= 3 && "village".equals(parts[0])) {
            return parts[1];  // 返回村庄类型：plains, desert, savanna, taiga, snowy
        }
        return null;
    }
}
