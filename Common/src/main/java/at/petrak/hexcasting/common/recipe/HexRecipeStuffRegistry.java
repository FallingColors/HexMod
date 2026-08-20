package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexRecipeStuffRegistry {
    private static final IXplatRegister<RecipeSerializer<?>> REGISTER_SERIALIZER = IXplatAbstractions.INSTANCE.createRegistar(Registries.RECIPE_SERIALIZER);
    private static final IXplatRegister<RecipeType<?>> REGISTER_RECIPE_TYPE = IXplatAbstractions.INSTANCE.createRegistar(Registries.RECIPE_TYPE);

    public static void register() {
        REGISTER_SERIALIZER.registerAll();
        REGISTER_RECIPE_TYPE.registerAll();
    }

    public static final Supplier<RecipeSerializer<?>> BRAINSWEEP = REGISTER_SERIALIZER.register("brainsweep", BrainsweepRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<SealThingsRecipe>> SEAL_FOCUS = REGISTER_SERIALIZER.register(
        "seal_focus", () -> SealThingsRecipe.FOCUS_SERIALIZER);
    public static final Supplier<RecipeSerializer<SealThingsRecipe>> SEAL_SPELLBOOK = REGISTER_SERIALIZER.register(
        "seal_spellbook", () -> SealThingsRecipe.SPELLBOOK_SERIALIZER);
    public static final Supplier<RecipeSerializer<CopySpellbookRecipe>> COPY_SPELLBOOK = REGISTER_SERIALIZER.register(
        "copy_spellbook", () -> CopySpellbookRecipe.SERIALIZER);
    public static final Supplier<RecipeSerializer<EraseSpellbookRecipe>> ERASE_SPELLBOOK = REGISTER_SERIALIZER.register(
        "erase_spellbook", () -> EraseSpellbookRecipe.SERIALIZER);

    public static Supplier<RecipeType<BrainsweepRecipe>> BRAINSWEEP_TYPE = REGISTER_RECIPE_TYPE.register("brainsweep", () ->
            new RecipeType<BrainsweepRecipe>() {
                @Override
                public String toString() {
                    return HexAPI.MOD_ID + ":" + "brainsweep";
                }
            });
}
