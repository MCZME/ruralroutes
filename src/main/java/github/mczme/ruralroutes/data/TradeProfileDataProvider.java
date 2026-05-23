package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.core.theme.TradeProfile;
import github.mczme.ruralroutes.data.builder.TradeProfileBuilder;
import github.mczme.ruralroutes.data.content.desert.DesertContent;
import github.mczme.ruralroutes.data.content.plains.PlainsContent;
import github.mczme.ruralroutes.data.content.savanna.SavannaContent;
import github.mczme.ruralroutes.data.content.snowy.SnowyContent;
import github.mczme.ruralroutes.data.content.taiga.TaigaContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 交易 profile 数据生成器。
 * 具体内容按群系拆分在 data/content/<biome>/ 中维护。
 */
public class TradeProfileDataProvider extends JsonCodecProvider<TradeProfile> {

    public TradeProfileDataProvider(
        PackOutput output,
        CompletableFuture<HolderLookup.Provider> lookupProvider,
        ExistingFileHelper existingFileHelper
    ) {
        super(
            output,
            Target.DATA_PACK,
            "ruralroutes/trade_profiles",
            PackType.SERVER_DATA,
            TradeProfile.CODEC,
            lookupProvider,
            "ruralroutes",
            existingFileHelper
        );
    }

    @Override
    protected void gather() {
        defineTradeProfiles(builder -> builder.registrar(this::unconditional).register());
    }

    public static void defineTradeProfiles(Consumer<TradeProfileBuilder> consumer) {
        PlainsContent.defineTradeProfiles(consumer);
        DesertContent.defineTradeProfiles(consumer);
        SavannaContent.defineTradeProfiles(consumer);
        TaigaContent.defineTradeProfiles(consumer);
        SnowyContent.defineTradeProfiles(consumer);
    }
}
