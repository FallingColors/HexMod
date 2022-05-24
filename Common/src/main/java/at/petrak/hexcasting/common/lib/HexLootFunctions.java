package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.loot.AmethystReducerFunc;
import at.petrak.hexcasting.common.loot.PatternScrollFunc;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexLootFunctions {
    public static void registerSerializers(BiConsumer<LootItemFunctionType, ResourceLocation> r) {
        for (var e : LOOT_FUNCS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, LootItemFunctionType> LOOT_FUNCS = new LinkedHashMap<>();

    public static final LootItemFunctionType PATTERN_SCROLL = register("pattern_scroll",
        new LootItemFunctionType(new PatternScrollFunc.Serializer()));
    public static final LootItemFunctionType AMETHYST_SHARD_REDUCER = register("amethyst_shard_reducer",
        new LootItemFunctionType(new AmethystReducerFunc.Serializer()));

    private static LootItemFunctionType register(String id, LootItemFunctionType lift) {
        var old = LOOT_FUNCS.put(modLoc(id), lift);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return lift;
    }
}
