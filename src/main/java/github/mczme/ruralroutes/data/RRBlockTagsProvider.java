package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import github.mczme.ruralroutes.RuralRoutes;
import github.mczme.ruralroutes.register.RRBlocks;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class RRBlockTagsProvider extends BlockTagsProvider  {

    public RRBlockTagsProvider(PackOutput output, CompletableFuture<Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, RuralRoutes.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider provider) {
        // 贸易站 - 需要镐子挖掘
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(RRBlocks.TRADE_STATION.get());

        // 展示柜和传闻板 - 需要斧头挖掘
        tag(BlockTags.MINEABLE_WITH_AXE)
            .add(RRBlocks.DISPLAY_CASE.get())
            .add(RRBlocks.RUMOR_BOARD.get());
    }

}
