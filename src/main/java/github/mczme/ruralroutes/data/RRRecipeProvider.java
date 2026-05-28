package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import github.mczme.ruralroutes.register.RRItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

/**
 * RuralRoutes 配方数据生成器。
 */
public class RRRecipeProvider extends RecipeProvider {

    public RRRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, RRItems.TRADE_ATLAS.get())
            .define('P', Items.PAPER)
            .define('L', Items.LEATHER)
            .define('C', Items.COMPASS)
            .define('B', Items.BOOK)
            .pattern("PPP")
            .pattern("LCL")
            .pattern("PBP")
            .unlockedBy(getHasName(Items.COMPASS), has(Items.COMPASS))
            .save(output);
    }
}
