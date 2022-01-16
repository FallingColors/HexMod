package at.petrak.hex.datagen;

import at.petrak.hex.common.items.HexItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipes) {
        // this is actually the worst system i have ever seen
        ShapedRecipeBuilder.shaped(HexItems.WAND.get())
                .define('L', Tags.Items.LEATHER)
                .define('S', Items.STICK)
                .define('A', Items.AMETHYST_SHARD)
                .pattern(" LA")
                .pattern("LSL")
                .pattern("SL ")
                .unlockedBy("has_item", has(Items.AMETHYST_SHARD))
                .save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.FOCUS.get())
                .define('L', Tags.Items.LEATHER)
                .define('Q', Tags.Items.DUSTS_GLOWSTONE)
                .define('A', HexItems.CHARGED_AMETHYST.get())
                .pattern("LQL")
                .pattern("QAQ")
                .pattern("LQL")
                .unlockedBy("has_item", has(HexItems.WAND.get()))
                .save(recipes);
        // i cannot believe they couldn't have thought of anything better

        ShapedRecipeBuilder.shaped(HexItems.SPELLBOOK.get())
                .define('N', Tags.Items.NUGGETS_GOLD)
                .define('B', Items.WRITABLE_BOOK)
                .define('A', HexItems.CHARGED_AMETHYST.get())
                .define('F', Items.CHORUS_FRUIT) // i wanna gate this behind the end SOMEHOW
                // hey look its my gender ^^
                .pattern("NBA")
                .pattern("NFA")
                .pattern("NBA")
                .unlockedBy("has_focus", has(HexItems.FOCUS.get()))
                .unlockedBy("has_chorus", has(Items.CHORUS_FRUIT)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.CYPHER.get())
                .define('F', Tags.Items.NUGGETS_IRON) // f for frame
                .define('A', HexItems.AMETHYST_DUST.get())
                .pattern(" F ")
                .pattern("FAF")
                .pattern(" F ")
                .unlockedBy("has_item", has(HexItems.WAND.get())).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.TRINKET.get())
                .define('F', Tags.Items.INGOTS_GOLD)
                .define('A', Items.AMETHYST_SHARD)
                .pattern(" F ")
                .pattern("FAF")
                .pattern(" F ")
                .unlockedBy("has_item", has(HexItems.WAND.get())).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ARTIFACT.get())
                .define('F', Tags.Items.INGOTS_GOLD)
                .define('A', HexItems.CHARGED_AMETHYST.get())
                // pretty sure that's all of them
                .define('D', Ingredient.of(Items.MUSIC_DISC_11, Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT,
                        Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_MALL,
                        Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_PIGSTEP,
                        Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_WARD))
                .pattern(" F ")
                .pattern("FAF")
                .pattern(" D ")
                .unlockedBy("has_item", has(HexItems.WAND.get())).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SCRYING_LENS.get())
                .define('G', Tags.Items.GLASS)
                .define('A', HexItems.AMETHYST_DUST.get())
                .pattern(" G ")
                .pattern("GAG")
                .pattern(" G ")
                .unlockedBy("has_item", has(HexItems.WAND.get())).save(recipes);
    }
}
