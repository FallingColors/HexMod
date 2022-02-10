package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.common.items.HexItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
            .define('F', Items.COPPER_INGOT) // f for frame
            .define('A', HexItems.AMETHYST_DUST.get())
            .pattern(" F ")
            .pattern("FAF")
            .pattern(" F ")
            .unlockedBy("has_item", has(HexItems.WAND.get())).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.TRINKET.get())
            .define('F', Tags.Items.INGOTS_IRON)
            .define('A', Items.AMETHYST_SHARD)
            .pattern(" F ")
            .pattern("FAF")
            .pattern(" F ")
            .unlockedBy("has_item", has(HexItems.WAND.get())).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ARTIFACT.get())
            .define('F', Tags.Items.INGOTS_GOLD)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            // why in god's name does minecraft have two different places for item tags
            .define('D', ItemTags.MUSIC_DISCS)
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

        // Why am I like this
        ShapedRecipeBuilder.shaped(HexItems.SUBMARINE_SANDWICH.get())
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('C', Items.COOKED_BEEF)
            .define('B', Items.BREAD)
            .pattern(" SA")
            .pattern(" C ")
            .pattern(" B ")
            .unlockedBy("has_item", has(Items.AMETHYST_SHARD)).save(recipes);

        for (var dyeColorizer : HexItems.DYE_COLORIZERS) {
            var item = dyeColorizer.get();
            ShapedRecipeBuilder.shaped(item)
                .define('B', Items.BOWL)
                .define('D', HexItems.AMETHYST_DUST.get())
                .define('C', DyeItem.byColor(DyeColor.values()[item.getDyeIdx()]))
                .pattern(" C ")
                .pattern(" D ")
                .pattern(" B ")
                .unlockedBy("has_item", has(HexItems.AMETHYST_DUST.get())).save(recipes);
        }
        Item[] politicsInMyVidya = new Item[]{
            Items.EGG,
            Items.STONE_BRICK_WALL,
            Items.GLASS,
            Items.BREAD,
            Items.WHEAT,
            Items.CARROT,
            Items.GLASS_BOTTLE,
            Items.RAW_COPPER,
            Items.MOSS_BLOCK,
            Items.HONEYCOMB,
            Items.RAW_IRON,
            Items.WATER_BUCKET,
            Items.FEATHER,
            Items.ARROW,
        };
        for (int i = 0; i < politicsInMyVidya.length; i++) {
            var item = HexItems.PRIDE_COLORIZERS[i].get();
            ShapedRecipeBuilder.shaped(item)
                .define('B', Items.BOWL)
                .define('D', HexItems.AMETHYST_DUST.get())
                .define('C', politicsInMyVidya[i])
                .pattern(" C ")
                .pattern(" D ")
                .pattern(" B ")
                .unlockedBy("has_item", has(HexItems.AMETHYST_DUST.get())).save(recipes);
        }
        ShapedRecipeBuilder.shaped(HexItems.UUID_COLORIZER.get())
            .define('B', Items.BOWL)
            .define('D', HexItems.AMETHYST_DUST.get())
            .define('C', Items.AMETHYST_SHARD)
            .pattern(" C ")
            .pattern(" D ")
            .pattern(" B ")
            .unlockedBy("has_item", has(HexItems.AMETHYST_DUST.get())).save(recipes);
    }
}
