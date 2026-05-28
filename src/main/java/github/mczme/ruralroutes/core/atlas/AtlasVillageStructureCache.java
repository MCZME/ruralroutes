package github.mczme.ruralroutes.core.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.core.theme.VillageStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Level 级商路图册村庄结构定位缓存。
 *
 * 这里只记录“图册已经通过结构定位找到过的村庄结构”，不代表玩家已知，
 * 也不代表该位置已经确认有有效商业节点。
 */
public final class AtlasVillageStructureCache extends SavedData {

    private static final String DATA_NAME = RuralRoutes.MODID + "_atlas_village_structures";
    private static final String ENTRIES_KEY = "entries";
    private static final Codec<List<Entry>> ENTRIES_CODEC = Entry.CODEC.listOf();

    private final List<Entry> entries;

    public AtlasVillageStructureCache() {
        this(List.of());
    }

    private AtlasVillageStructureCache(List<Entry> entries) {
        this.entries = new ArrayList<>(Objects.requireNonNull(entries, "entries"));
    }

    /**
     * 获取当前维度的图册村庄结构缓存。
     */
    public static AtlasVillageStructureCache get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(AtlasVillageStructureCache::create, AtlasVillageStructureCache::load),
            DATA_NAME
        );
    }

    public static AtlasVillageStructureCache create() {
        return new AtlasVillageStructureCache();
    }

    /**
     * 从 SavedData NBT 中恢复缓存表。
     */
    public static AtlasVillageStructureCache load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        if (!tag.contains(ENTRIES_KEY)) {
            return create();
        }

        List<Entry> loadedEntries = ENTRIES_CODEC.parse(NbtOps.INSTANCE, tag.get(ENTRIES_KEY))
            .resultOrPartial(error -> RuralRoutes.LOGGER.error("Failed to decode atlas village structure cache: {}", error))
            .orElseGet(List::of);
        return new AtlasVillageStructureCache(loadedEntries);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ENTRIES_CODEC.encodeStart(NbtOps.INSTANCE, entries)
            .resultOrPartial(error -> RuralRoutes.LOGGER.error("Failed to encode atlas village structure cache: {}", error))
            .ifPresent(encoded -> tag.put(ENTRIES_KEY, encoded));
        return tag;
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    /**
     * 按结构事实主键判断候选是否已经被图册定位过。
     */
    public boolean contains(ResourceLocation dimensionId, VillageStyle style, ResourceLocation structureId,
            ChunkPos structureChunk) {
        return find(dimensionId, style, structureId, structureChunk).isPresent();
    }

    /**
     * 查找指定结构事实对应的缓存条目。
     */
    public Optional<Entry> find(ResourceLocation dimensionId, VillageStyle style, ResourceLocation structureId,
            ChunkPos structureChunk) {
        return entries.stream()
            .filter(entry -> entry.sameStructure(dimensionId, style, structureId, structureChunk))
            .findFirst();
    }

    public Optional<Entry> findNearest(ResourceLocation dimensionId, VillageStyle style, BlockPos origin) {
        return findNearest(dimensionId, style, origin, entry -> true);
    }

    /**
     * 从缓存中挑选离 origin 最近、且调用方认为可用的同风格村庄结构。
     *
     * usableEntry 通常用于过滤“当前玩家已经拥有的线索”。
     */
    public Optional<Entry> findNearest(ResourceLocation dimensionId, VillageStyle style, BlockPos origin,
            Predicate<Entry> usableEntry) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(usableEntry, "usableEntry");

        return entries.stream()
            .filter(entry -> entry.dimensionId().equals(dimensionId))
            .filter(entry -> entry.style() == style)
            .filter(usableEntry)
            .min(Comparator.comparingDouble(entry -> entry.locatePos().distSqr(origin)));
    }

    /**
     * 登记一次新的结构定位结果。
     *
     * 如果同一结构事实已经存在，返回旧条目，避免不同玩家重复定位时写入重复缓存。
     */
    public Entry remember(ResourceLocation dimensionId, VillageStyle style, ResourceLocation structureId,
            ChunkPos structureChunk, BlockPos locatePos) {
        return remember(new Entry(
            UUID.randomUUID(),
            dimensionId,
            style,
            structureId,
            structureChunk.x,
            structureChunk.z,
            locatePos
        ));
    }

    public Entry remember(Entry entry) {
        Objects.requireNonNull(entry, "entry");
        Optional<Entry> existing = find(entry.dimensionId(), entry.style(), entry.structureId(), entry.structureChunk());
        if (existing.isPresent()) {
            return existing.get();
        }

        entries.add(entry);
        // 只有新增结构事实时才标脏；重复命中缓存不需要触发存档写入。
        setDirty();
        return entry;
    }

    /**
     * 一条服务器已经定位过的村庄结构事实。
     */
    public record Entry(
        UUID id,
        ResourceLocation dimensionId,
        VillageStyle style,
        ResourceLocation structureId,
        int structureChunkX,
        int structureChunkZ,
        BlockPos locatePos
    ) {

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(Entry::id),
            ResourceLocation.CODEC.fieldOf("dimension_id").forGetter(Entry::dimensionId),
            VillageStyle.CODEC.fieldOf("style").forGetter(Entry::style),
            ResourceLocation.CODEC.fieldOf("structure_id").forGetter(Entry::structureId),
            Codec.INT.fieldOf("structure_chunk_x").forGetter(Entry::structureChunkX),
            Codec.INT.fieldOf("structure_chunk_z").forGetter(Entry::structureChunkZ),
            BlockPos.CODEC.fieldOf("locate_pos").forGetter(Entry::locatePos)
        ).apply(instance, Entry::new));

        public Entry {
            id = Objects.requireNonNull(id, "id");
            dimensionId = Objects.requireNonNull(dimensionId, "dimensionId");
            style = Objects.requireNonNull(style, "style");
            structureId = Objects.requireNonNull(structureId, "structureId");
            locatePos = Objects.requireNonNull(locatePos, "locatePos");
        }

        public ChunkPos structureChunk() {
            return new ChunkPos(structureChunkX, structureChunkZ);
        }

        public boolean sameStructure(ResourceLocation otherDimensionId, VillageStyle otherStyle,
                ResourceLocation otherStructureId, ChunkPos otherStructureChunk) {
            return dimensionId.equals(otherDimensionId)
                && style == otherStyle
                && structureId.equals(otherStructureId)
                && structureChunkX == otherStructureChunk.x
                && structureChunkZ == otherStructureChunk.z;
        }
    }
}
