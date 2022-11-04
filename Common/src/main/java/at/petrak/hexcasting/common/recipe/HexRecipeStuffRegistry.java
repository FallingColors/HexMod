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

public class HexRecipeStuffRegistry {
    public static void registerSerializers(BiConsumer<RecipeSerializer<?>, ResourceLocation> r) {
        for (var e : SERIALIZERS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    public static void registerTypes(BiConsumer<RecipeType<?>, ResourceLocation> r) {
        for (var e : TYPES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, RecipeSerializer<?>> SERIALIZERS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, RecipeType<?>> TYPES = new LinkedHashMap<>();

    // TODO: custom costs in brainsweeping. also custom entities but we'll getting there
    public static final RecipeSerializer<?> BRAINSWEEP = registerSerializer("brainsweep",
        new BrainsweepRecipe.Serializer());
    public static final RecipeSerializer<SealFocusRecipe> SEAL_FOCUS = registerSerializer("seal_focus",
        SealFocusRecipe.SERIALIZER);
    public static final RecipeSerializer<SealSpellbookRecipe> SEAL_SPELLBOOK = registerSerializer(
        "seal_spellbook",
        SealSpellbookRecipe.SERIALIZER);

    public static RecipeType<BrainsweepRecipe> BRAINSWEEP_TYPE = registerType("brainsweep");

    private static <T extends Recipe<?>> RecipeSerializer<T> registerSerializer(String name, RecipeSerializer<T> rs) {
        var old = SERIALIZERS.put(modLoc(name), rs);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return rs;
    }

    private static <T extends Recipe<?>> RecipeType<T> registerType(String name) {
        var type = new RecipeType<T>() {
            @Override
            public String toString() {
                return HexAPI.MOD_ID + ":" + name;
            }
        };
        // never will be a collision because it's a new object
        TYPES.put(modLoc(name), type);
        return type;
    }
}
