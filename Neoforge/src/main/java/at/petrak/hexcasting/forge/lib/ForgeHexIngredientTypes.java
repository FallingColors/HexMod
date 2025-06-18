package at.petrak.hexcasting.forge.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.forge.loot.ForgeHexAmethystLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexCypherLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexLoreLootMod;
import at.petrak.hexcasting.forge.loot.ForgeHexScrollLootMod;
import at.petrak.hexcasting.forge.recipe.ForgeModConditionalIngredient;
import at.petrak.hexcasting.forge.recipe.ForgeUnsealedIngredient;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ForgeHexIngredientTypes {
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, HexAPI.MOD_ID);

    public static final Supplier<IngredientType<ForgeUnsealedIngredient>> UNSEALED_INGREDIENT =
            INGREDIENT_TYPES.register("unsealed",
                    () -> new IngredientType<>(ForgeUnsealedIngredient.CODEC, ForgeUnsealedIngredient.STREAM_CODEC));
    public static final Supplier<IngredientType<ForgeModConditionalIngredient>> MOD_CONDITIONAL_INGREDIENT =
            INGREDIENT_TYPES.register("mod_conditional",
                    () -> new IngredientType<>(ForgeModConditionalIngredient.CODEC, ForgeModConditionalIngredient.STREAM_CODEC));
}
