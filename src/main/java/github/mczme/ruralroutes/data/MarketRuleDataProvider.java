package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.core.market.MarketEventRule;
import github.mczme.ruralroutes.data.content.CommonMarketContent;
import github.mczme.ruralroutes.data.content.MarketRuleRegistrar;
import github.mczme.ruralroutes.data.content.RareMarketContent;
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

/**
 * 市场规则数据生成器。
 * 具体内容按通用层、群系层和稀有层拆分在 data/content/ 中维护。
 */
public class MarketRuleDataProvider extends JsonCodecProvider<MarketEventRule> {

    public MarketRuleDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  ExistingFileHelper existingFileHelper) {
        super(output, Target.DATA_PACK, "ruralroutes/market_event_rules", PackType.SERVER_DATA,
                MarketEventRule.CODEC, lookupProvider, "ruralroutes", existingFileHelper);
    }

    @Override
    protected void gather() {
        MarketRuleRegistrar rules = new MarketRuleRegistrar(this::unconditional);
        CommonMarketContent.defineMarketRules(rules);
        PlainsContent.defineMarketRules(rules);
        DesertContent.defineMarketRules(rules);
        SavannaContent.defineMarketRules(rules);
        TaigaContent.defineMarketRules(rules);
        SnowyContent.defineMarketRules(rules);
        RareMarketContent.defineMarketRules(rules);
    }
}
