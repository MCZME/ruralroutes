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

import java.util.List;

/**
 * Mixin 到 JigsawPlacement.Placer，拦截 houses 池访问，实现贸易站的必定且唯一生成。
 *
 * 关键设计：1.21.1 的 jigsaw 使用队列机制而非递归。
 * tryPlacingChildren 中成功放置的片段先加入 this.pieces 列表，然后加入 processing
 * 队列延后处理。同一 piece 的多个 houses junctions 在同一个 tryPlacingChildren
 * 调用中依次处理，因此 HEAD 注入无法及时检测放置——必须通过扫描 this.pieces
 * 来判断贸易站是否已被本调用中更早的 junction 放置。
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public class JigsawPlacementPlacerMixin {

    @Final
    @Shadow
    private Registry<StructureTemplatePool> pools;

    @Final
    @Shadow
    private List<PoolElementStructurePiece> pieces;

    @Unique
    private int ruralroutes$tradeStationSwapCount = 0;
    private static final int MAX_SWAP_ATTEMPTS = 5;
    private static final String MODID_STR = RuralRoutes.MODID;

    /**
     * 拦截 Registry.getHolder() 调用，将 houses 池替换为贸易站池。
     * 替换前扫描 this.pieces 确认贸易站未被本次调用中已处理的 junction 放置。
     */
    @ModifyArg(
        method = "tryPlacingChildren(Lnet/minecraft/world/level/levelgen/structure/PoolElementStructurePiece;Lorg/apache/commons/lang3/mutable/MutableObject;IZLnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/Registry;getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"
        )
    )
    private ResourceKey<StructureTemplatePool> ruralroutes$forceTradeStationPool(ResourceKey<StructureTemplatePool> resourceKey) {
        if (ruralroutes$tradeStationSwapCount >= MAX_SWAP_ATTEMPTS) {
            return resourceKey;
        }

        String poolPath = resourceKey.location().getPath();
        if (poolPath.endsWith("/houses")) {
            // 扫描已放置的 pieces，检查贸易站是否已被同一 tryPlacingChildren 中
            // 更早的 junction 放置。1.21.1 使用队列而非递归，必须在 getHolder 时检查。
            if (hasTradeStationInPieces()) {
                ruralroutes$tradeStationSwapCount = MAX_SWAP_ATTEMPTS; // 阻止后续替换
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
                    ruralroutes$tradeStationSwapCount++;
                    RuralRoutes.LOGGER.debug("第{}次尝试将 {} houses 替换为贸易站池", ruralroutes$tradeStationSwapCount, villageType);
                    return tradeStationPoolKey;
                }
            }
        }

        return resourceKey;
    }

    @Unique
    private boolean hasTradeStationInPieces() {
        for (PoolElementStructurePiece piece : pieces) {
            if (piece.getElement().toString().contains(MODID_STR)) {
                return true;
            }
        }
        return false;
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
