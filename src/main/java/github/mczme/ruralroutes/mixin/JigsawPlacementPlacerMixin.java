package github.mczme.ruralroutes.mixin;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin 到 JigsawPlacement.Placer，拦截 houses 池访问，实现贸易站的必定且唯一生成。
 *
 * 关键设计：只在贸易站 piece 真正成功加入 pieces 后才标记已生成。
 * 这样可以避免把“候选池被替换”误认为“结构已经放置成功”。
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public class JigsawPlacementPlacerMixin {

    @Final
    @Shadow
    private Registry<StructureTemplatePool> pools;

    private static final String MODID_STR = RuralRoutes.MODID;
    @Unique
    private boolean ruralroutes$tradeStationPlaced = false;

    @Redirect(
        method = "tryPlacingChildren(Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
        )
    )
    private boolean ruralroutes$markTradeStationPlaced(
        java.util.List<Object> pieces,
        Object addedPiece
    ) {
        boolean added = pieces.add(addedPiece);
        if (!ruralroutes$tradeStationPlaced && addedPiece instanceof PoolElementStructurePiece piece
            && piece.getElement().toString().contains(MODID_STR)) {
            ruralroutes$tradeStationPlaced = true;
        }
        return added;
    }

    /**
     * 拦截 Registry.getHolder() 调用，将 houses 池替换为贸易站池。
     * 只有在尚未处理到贸易站片段时才进行替换。
     */
    @ModifyArg(
        method = "tryPlacingChildren(Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/Registry;getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"
        )
    )
    private ResourceKey<StructureTemplatePool> ruralroutes$forceTradeStationPool(ResourceKey<StructureTemplatePool> resourceKey) {
        String poolPath = resourceKey.location().getPath();
        if (poolPath.endsWith("/houses")) {
            // 只要已经处理到贸易站片段，就不再替换后续 houses。
            if (ruralroutes$tradeStationPlaced) {
                return resourceKey;
            }

            String villageType = extractVillageType(poolPath);
            if (villageType != null) {
                ResourceLocation tradeStationPoolId = ResourceLocation.fromNamespaceAndPath(
                    MODID_STR,
                    "village/" + villageType + "/trade_stations"
                );
                ResourceKey<StructureTemplatePool> tradeStationPoolKey = ResourceKey.create(
                    Registries.TEMPLATE_POOL,
                    tradeStationPoolId
                );

                if (pools.getHolder(tradeStationPoolKey).isPresent()) {
                    RuralRoutes.LOGGER.debug("尝试将 {} houses 替换为贸易站池", villageType);
                    return tradeStationPoolKey;
                }
            }
        }

        return resourceKey;
    }

    @Unique
    private static String extractVillageType(String poolPath) {
        String[] parts = poolPath.split("/");
        if (parts.length >= 3 && "village".equals(parts[0])) {
            return parts[1];
        }
        return null;
    }
}
