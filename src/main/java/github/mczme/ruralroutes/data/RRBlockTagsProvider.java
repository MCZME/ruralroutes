package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import github.mczme.ruralroutes.RuralRoutes;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class RRBlockTagsProvider extends BlockTagsProvider  {

    public RRBlockTagsProvider(PackOutput output, CompletableFuture<Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, RuralRoutes.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(Provider arg0) {
        // TODO Auto-generated method stub
    }
    
}
