package github.mczme.ruralroutes.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Accessor 接口，用于访问 StructureTemplatePool 的私有字段
 */
@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {

    /**
     * 获取模板列表（用于随机选择）
     * 权重越大，元素在此列表中出现次数越多
     * 类型为 ObjectArrayList<StructurePoolElement>
     */
    @Accessor("templates")
    ObjectArrayList<StructurePoolElement> ruralroutes$getTemplates();

    /**
     * 设置模板列表
     */
    @Accessor("templates")
    @Mutable
    void ruralroutes$setTemplates(ObjectArrayList<StructurePoolElement> templates);

    /**
     * 获取带权重的原始模板列表
     */
    @Accessor("rawTemplates")
    List<Pair<StructurePoolElement, Integer>> ruralroutes$getRawTemplates();

    /**
     * 设置带权重的原始模板列表
     */
    @Accessor("rawTemplates")
    @Mutable
    void ruralroutes$setRawTemplates(List<Pair<StructurePoolElement, Integer>> rawTemplates);
}
