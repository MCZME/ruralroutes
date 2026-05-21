package github.mczme.ruralroutes.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import github.mczme.ruralroutes.core.market.MarketEventRule;
import github.mczme.ruralroutes.core.market.MarketEventScopeRule;
import github.mczme.ruralroutes.core.theme.ThemeTemplate;
import github.mczme.ruralroutes.core.theme.TradeProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedDataCodecTest {

    // NeoForge 的 junit 工作目录会切到 build/minecraft-junit，因此不能直接信任 user.dir。
    private static final Path PROJECT_ROOT = resolveProjectRoot();

    @Test
    void generatedMarketRulesDecodeSuccessfully() throws IOException {
        // 这层测试先守住“生成出来的规则至少能被当前 Codec 正常读回”。
        Path root = PROJECT_ROOT.resolve(Path.of("src", "generated", "resources", "data", "ruralroutes", "ruralroutes", "market_event_rules"));
        List<Path> files = jsonFiles(root);

        assertFalse(files.isEmpty(), "Expected generated market rule files");
        assertAll(files.stream().map(file -> (Executable) () -> {
            MarketEventRule rule = parseCodec(file, MarketEventRule.CODEC);
            assertTrue(rule.effectiveWeight() > 0, () -> "Rule weight must be positive: " + file);
            assertTrue(rule.scopes().stream().allMatch(MarketEventScopeRule::hasValidTargets),
                    () -> "Rule scope targets must be valid: " + file);
        }));
    }

    @Test
    void generatedThemesDecodeSuccessfully() throws IOException {
        // 主题文件现在只承载主题级字段，因此至少要能解析并引用 profile。
        Path root = PROJECT_ROOT.resolve(Path.of("src", "generated", "resources", "data", "ruralroutes", "ruralroutes", "themes"));
        List<Path> files = jsonFiles(root);

        assertFalse(files.isEmpty(), "Expected generated theme files");
        assertAll(files.stream().map(file -> (Executable) () -> {
            ThemeTemplate theme = parseCodec(file, ThemeTemplate.CODEC);
            assertTrue(theme.tradeProfiles().isPresent() && !theme.tradeProfiles().orElseThrow().isEmpty(),
                () -> "Theme must reference trade profiles: " + file);
        }));
    }

    @Test
    void generatedTradeProfilesDecodeSuccessfully() throws IOException {
        Path root = PROJECT_ROOT.resolve(Path.of("src", "generated", "resources", "data", "ruralroutes", "ruralroutes", "trade_profiles"));
        List<Path> files = jsonFiles(root);

        assertFalse(files.isEmpty(), "Expected generated trade profile files");
        assertAll(files.stream().map(file -> (Executable) () -> {
            TradeProfile profile = parseCodec(file, TradeProfile.CODEC);
            assertTrue(!profile.sellItems().isEmpty() || !profile.buyItems().isEmpty(),
                () -> "Trade profile must define at least one buy/sell item: " + file);
        }));
    }

    private static List<Path> jsonFiles(Path root) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
    }

    private static <T> T parseCodec(Path file, com.mojang.serialization.Codec<T> codec) throws IOException {
        JsonElement json = JsonParser.parseString(Files.readString(file));
        return codec.parse(JsonOps.INSTANCE, json)
                .getOrThrow(message -> new IllegalStateException(file + ": " + message));
    }

    private static Path resolveProjectRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("build.gradle")) && Files.exists(current.resolve("src"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Failed to locate project root from test working directory");
    }
}
