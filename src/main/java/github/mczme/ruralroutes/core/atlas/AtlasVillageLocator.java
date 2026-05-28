package github.mczme.ruralroutes.core.atlas;

import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;

/**
 * 商路图册专用村庄结构定位器。
 *
 * 复制原版随机分散结构定位的核心流程，但在候选确认前允许跳过
 * Rural Routes 已定位缓存中的村庄结构。
 */
public final class AtlasVillageLocator {

    private AtlasVillageLocator() {
    }

    /**
     * 从 origin 开始按原版随机分散结构逻辑寻找最近的未缓存村庄结构。
     *
     * @param level 当前服务端世界
     * @param style 玩家指定的村庄风格
     * @param origin 搜索起点，通常是玩家当前位置
     * @param searchRadius 原版结构定位扩圈半径，不是方块距离
     * @param isCached 判断候选结构是否已经进入 Rural Routes 的 level 缓存
     * @return 最近的未缓存村庄结构；如果没有可用候选则为空
     */
    public static Optional<LocatedVillageStructure> findNearestUncached(
            ServerLevel level,
            VillageStyle style,
            BlockPos origin,
            int searchRadius,
            Predicate<LocatedVillageStructure> isCached
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(style, "style");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(isCached, "isCached");

        if (!level.structureManager().shouldGenerateStructures()) {
            return Optional.empty();
        }

        Optional<Holder.Reference<Structure>> structure = level.registryAccess()
            .registryOrThrow(Registries.STRUCTURE)
            .getHolder(ResourceKey.create(Registries.STRUCTURE, style.structureId()));
        if (structure.isEmpty()) {
            return Optional.empty();
        }

        ChunkGeneratorStructureState structureState = level.getChunkSource().getGeneratorState();
        Map<StructurePlacement, Set<Holder<Structure>>> placements =
            placementsFor(structureState, List.of(structure.get()));
        if (placements.isEmpty()) {
            return Optional.empty();
        }

        int sectionX = SectionPos.blockToSectionCoord(origin.getX());
        int sectionZ = SectionPos.blockToSectionCoord(origin.getZ());
        StructureManager structureManager = level.structureManager();
        ResourceLocation dimensionId = level.dimension().location();
        LocatedVillageStructure nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        List<Entry<StructurePlacement, Set<Holder<Structure>>>> spreadPlacements = new ArrayList<>(placements.size());

        for (Entry<StructurePlacement, Set<Holder<Structure>>> entry : placements.entrySet()) {
            StructurePlacement placement = entry.getKey();
            if (placement instanceof ConcentricRingsStructurePlacement ringsPlacement) {
                Optional<LocatedVillageStructure> candidate = findNearestInRings(
                    entry.getValue(),
                    level,
                    structureManager,
                    dimensionId,
                    style,
                    origin,
                    ringsPlacement,
                    isCached
                );
                if (candidate.isPresent()) {
                    double distance = origin.distSqr(candidate.get().locatePos());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = candidate.get();
                    }
                }
            } else if (placement instanceof RandomSpreadStructurePlacement) {
                spreadPlacements.add(entry);
            }
        }

        // 原版结构定位按 placement 从内向外扩圈；同一圈里可能有多个 placement，
        // 因此先挑出本圈距离玩家最近的候选，再决定是否返回。
        for (int radius = 0; radius <= searchRadius; radius++) {
            LocatedVillageStructure nearestInRing = null;
            double nearestDistanceInRing = nearestDistance;
            boolean foundInRing = false;

            for (Entry<StructurePlacement, Set<Holder<Structure>>> entry : spreadPlacements) {
                RandomSpreadStructurePlacement spreadPlacement = (RandomSpreadStructurePlacement) entry.getKey();
                Optional<LocatedVillageStructure> candidate = findInRing(
                    entry.getValue(),
                    level,
                    structureManager,
                    dimensionId,
                    style,
                    sectionX,
                    sectionZ,
                    radius,
                    structureState.getLevelSeed(),
                    spreadPlacement,
                    isCached
                );

                if (candidate.isPresent()) {
                    foundInRing = true;
                    double distance = origin.distSqr(candidate.get().locatePos());
                    if (distance < nearestDistanceInRing) {
                        nearestDistanceInRing = distance;
                        nearestInRing = candidate.get();
                    }
                }
            }

            if (foundInRing) {
                return Optional.ofNullable(nearestInRing == null ? nearest : nearestInRing);
            }
        }

        return Optional.ofNullable(nearest);
    }

    /**
     * 按 placement 聚合可搜索结构，保持原版 ChunkGenerator.findNearestMapStructure 的数据形状。
     */
    private static Map<StructurePlacement, Set<Holder<Structure>>> placementsFor(
            ChunkGeneratorStructureState structureState,
            Iterable<Holder<Structure>> structures
    ) {
        Map<StructurePlacement, Set<Holder<Structure>>> placements = new HashMap<>();

        for (Holder<Structure> structure : structures) {
            for (StructurePlacement placement : structureState.getPlacementsForStructure(structure)) {
                placements.computeIfAbsent(placement, ignored -> new HashSet<>()).add(structure);
            }
        }

        return placements;
    }

    private static Optional<LocatedVillageStructure> findNearestInRings(
            Set<Holder<Structure>> structureHolders,
            ServerLevel level,
            StructureManager structureManager,
            ResourceLocation dimensionId,
            VillageStyle style,
            BlockPos origin,
            ConcentricRingsStructurePlacement placement,
            Predicate<LocatedVillageStructure> isCached
    ) {
        List<ChunkPos> ringPositions = level.getChunkSource().getGeneratorState().getRingPositionsFor(placement);
        if (ringPositions == null) {
            throw new IllegalStateException("Tried to find structures for a placement that does not exist");
        }

        LocatedVillageStructure nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (ChunkPos chunkPos : ringPositions) {
            mutablePos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
            double distance = mutablePos.distSqr(origin);
            if (nearest == null || distance < nearestDistance) {
                Optional<LocatedVillageStructure> candidate = structureGeneratingAt(
                    structureHolders,
                    level,
                    structureManager,
                    dimensionId,
                    style,
                    placement,
                    chunkPos,
                    isCached
                );
                if (candidate.isPresent()) {
                    nearest = candidate.get();
                    nearestDistance = distance;
                }
            }
        }

        return Optional.ofNullable(nearest);
    }

    private static Optional<LocatedVillageStructure> findInRing(
            Set<Holder<Structure>> structureHolders,
            ServerLevel level,
            StructureManager structureManager,
            ResourceLocation dimensionId,
            VillageStyle style,
            int sectionX,
            int sectionZ,
            int radius,
            long seed,
            RandomSpreadStructurePlacement placement,
            Predicate<LocatedVillageStructure> isCached
    ) {
        int spacing = placement.spacing();

        for (int dz = -radius; dz <= radius; dz++) {
            boolean onHorizontalEdge = dz == -radius || dz == radius;

            for (int dx = -radius; dx <= radius; dx++) {
                boolean onVerticalEdge = dx == -radius || dx == radius;
                if (onHorizontalEdge || onVerticalEdge) {
                    // 只检查当前环的边界，避免重复扫描内圈已经检查过的候选区域。
                    int regionX = sectionX + spacing * dz;
                    int regionZ = sectionZ + spacing * dx;
                    ChunkPos chunkPos = placement.getPotentialStructureChunk(seed, regionX, regionZ);
                    Optional<LocatedVillageStructure> located = structureGeneratingAt(
                        structureHolders,
                        level,
                        structureManager,
                        dimensionId,
                        style,
                        placement,
                        chunkPos,
                        isCached
                    );
                    if (located.isPresent()) {
                        return located;
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<LocatedVillageStructure> structureGeneratingAt(
            Set<Holder<Structure>> structureHolders,
            ServerLevel level,
            StructureManager structureManager,
            ResourceLocation dimensionId,
            VillageStyle style,
            StructurePlacement placement,
            ChunkPos chunkPos,
            Predicate<LocatedVillageStructure> isCached
    ) {
        for (Holder<Structure> holder : structureHolders) {
            ResourceLocation structureId = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getKey(holder.value());
            if (structureId == null) {
                continue;
            }

            LocatedVillageStructure candidate = new LocatedVillageStructure(
                dimensionId,
                style,
                structureId,
                chunkPos,
                placement.getLocatePos(chunkPos)
            );
            if (isCached.test(candidate)) {
                // 缓存命中的候选不触发结构存在性确认，避免重复定位把最近村庄卡住。
                continue;
            }

            StructureCheckResult checkResult = structureManager.checkStructurePresence(
                chunkPos,
                holder.value(),
                placement,
                false
            );
            if (checkResult == StructureCheckResult.START_NOT_PRESENT) {
                continue;
            }
            if (checkResult == StructureCheckResult.START_PRESENT) {
                return Optional.of(candidate);
            }

            // CHUNK_LOAD_NEEDED 时才推进到 STRUCTURE_STARTS 阶段做确认。
            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart structureStart = structureManager.getStartForStructure(
                SectionPos.bottomOf(chunk),
                holder.value(),
                chunk
            );
            if (structureStart != null && structureStart.isValid()) {
                ChunkPos structureChunk = structureStart.getChunkPos();
                LocatedVillageStructure loadedCandidate = new LocatedVillageStructure(
                    dimensionId,
                    style,
                    structureId,
                    structureChunk,
                    placement.getLocatePos(structureChunk)
                );
                if (!isCached.test(loadedCandidate)) {
                    return Optional.of(loadedCandidate);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 一次图册结构定位得到的村庄候选。
     *
     * structureChunk 用于稳定判重，locatePos 用于玩家图册显示。
     */
    public record LocatedVillageStructure(
        ResourceLocation dimensionId,
        VillageStyle style,
        ResourceLocation structureId,
        ChunkPos structureChunk,
        BlockPos locatePos
    ) {

        public LocatedVillageStructure {
            dimensionId = Objects.requireNonNull(dimensionId, "dimensionId");
            style = Objects.requireNonNull(style, "style");
            structureId = Objects.requireNonNull(structureId, "structureId");
            structureChunk = Objects.requireNonNull(structureChunk, "structureChunk");
            locatePos = Objects.requireNonNull(locatePos, "locatePos");
        }
    }
}
