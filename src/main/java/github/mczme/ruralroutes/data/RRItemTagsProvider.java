package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.register.RRItemTags;
import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

/**
 * 物品池标签数据生成器。
 * 定义各物品池包含的物品。
 */
public class RRItemTagsProvider extends ItemTagsProvider {

        public RRItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                        CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
                        @Nullable ExistingFileHelper existingFileHelper) {
                super(output, lookupProvider, blockTags, RuralRoutes.MODID, existingFileHelper);
        }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 货币标签
        tag(RRItemTags.CURRENCY)
                .add(RRItems.COPPER_COIN.get())
                .add(RRItems.IRON_COIN.get())
                .add(RRItems.GOLD_COIN.get());

        tag(RRItemTags.CURRENCY_BASE)
                .add(RRItems.COPPER_COIN.get());
    }
}