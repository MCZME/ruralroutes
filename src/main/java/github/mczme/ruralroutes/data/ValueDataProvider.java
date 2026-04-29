package github.mczme.ruralroutes.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;


/**
 * 价值表数据生成器
 */
public class ValueDataProvider extends DataMapProvider {

    public ValueDataProvider(PackOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather() {
        
    }

    @Override
    public String getName() {
        return "RuralRoutes Value Table";
    }
}
