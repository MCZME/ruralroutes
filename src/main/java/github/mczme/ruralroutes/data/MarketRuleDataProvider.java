package github.mczme.ruralroutes.data;

import github.mczme.ruralroutes.core.market.MarketEventRule;
import github.mczme.ruralroutes.core.market.MarketEventScopeRule;
import github.mczme.ruralroutes.core.market.MarketScopeType;
import github.mczme.ruralroutes.data.builder.MarketRuleBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 市场规则数据生成器
 *
 * 生成默认规则文件到 data/<namespace>/ruralroutes/market_event_rules/
 */
public class MarketRuleDataProvider extends JsonCodecProvider<MarketEventRule> {

    public MarketRuleDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  ExistingFileHelper existingFileHelper) {
        super(output, Target.DATA_PACK, "ruralroutes/market_event_rules", PackType.SERVER_DATA,
                MarketEventRule.CODEC, lookupProvider, "ruralroutes", existingFileHelper);
    }

    @Override
    protected void gather() {
        // 规则1：测试事件
        rule("test_event")
                .nameKey("market.ruralroutes.test_event")
                .targetRef("minecraft:bread")
                .scope(MarketEventScopeRule.global())
                .delta(-0.15f)
                .weight(80)
                .register();
    }

    /**
     * 创建规则构建器
     */
    private MarketRuleBuilder rule(String id) {
        return MarketRuleBuilder.create(id, this::unconditional);
    }
}
