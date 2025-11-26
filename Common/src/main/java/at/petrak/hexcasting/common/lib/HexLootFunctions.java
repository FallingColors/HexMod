package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.loot.AddPerWorldPatternToScrollFunc;
import at.petrak.hexcasting.common.loot.AddHexToAncientCypherFunc;
import at.petrak.hexcasting.common.loot.AmethystReducerFunc;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexLootFunctions {
    public static void registerSerializers(BiConsumer<LootItemFunctionType<?>, ResourceLocation> r) {
        for (var e : LOOT_FUNCS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, LootItemFunctionType<?>> LOOT_FUNCS = new LinkedHashMap<>();

    public static final LootItemFunctionType<? extends LootItemConditionalFunction> PATTERN_SCROLL = register("pattern_scroll",
        new LootItemFunctionType<>(AddPerWorldPatternToScrollFunc.CODEC));
    public static final LootItemFunctionType<? extends LootItemConditionalFunction> HEX_CYPHER = register("hex_cypher",
        new LootItemFunctionType<>(AddHexToAncientCypherFunc.CODEC));
    public static final LootItemFunctionType<? extends LootItemConditionalFunction> AMETHYST_SHARD_REDUCER = register("amethyst_shard_reducer",
        new LootItemFunctionType<>(AmethystReducerFunc.CODEC));

    private static LootItemFunctionType<? extends LootItemConditionalFunction> register(
            String id,
            LootItemFunctionType<? extends LootItemConditionalFunction> lift
    ) {
        var old = LOOT_FUNCS.put(modLoc(id), lift);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return lift;
    }
}
