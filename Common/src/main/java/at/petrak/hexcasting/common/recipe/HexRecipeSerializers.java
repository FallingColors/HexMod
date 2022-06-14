package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.api.HexAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexRecipeSerializers {
    public static void registerSerializers(BiConsumer<RecipeSerializer<?>, ResourceLocation> r) {
        for (var e : SERIALIZERS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, RecipeSerializer<?>> SERIALIZERS = new LinkedHashMap<>();

    // TODO: custom costs in brainsweeping. also custom entities but we'll getting there
    public static final RecipeSerializer<?> BRAINSWEEP = register("brainsweep", new BrainsweepRecipe.Serializer());
    public static RecipeType<BrainsweepRecipe> BRAINSWEEP_TYPE;
    public static final RecipeSerializer<SealFocusRecipe> SEAL_FOCUS = register("seal_focus",
        SealFocusRecipe.SERIALIZER);
    public static final RecipeSerializer<SealSpellbookRecipe> SEAL_SPELLBOOK = register(
        "seal_spellbook",
        SealSpellbookRecipe.SERIALIZER);

    private static <T extends Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> rs) {
        var old = SERIALIZERS.put(modLoc(name), rs);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return rs;
    }

    // Like in the statistics, gotta register it at some point
    public static void registerTypes() {
        BRAINSWEEP_TYPE = RecipeType.register(HexAPI.MOD_ID + ":brainsweep");
    }
}
