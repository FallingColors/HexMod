package at.petrak.hexcasting.datagen.recipe;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.advancements.OvercastTrigger;
import at.petrak.hexcasting.api.mod.HexItemTags;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.SealFocusRecipe;
import at.petrak.hexcasting.common.recipe.SealSpellbookRecipe;
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper;
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient;
import at.petrak.hexcasting.datagen.IXplatIngredients;
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder;
import at.petrak.paucal.api.datagen.PaucalRecipeProvider;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;


public class HexplatRecipes extends PaucalRecipeProvider {
    public DataGenerator generator;
    public IXplatIngredients ingredients;

    public HexplatRecipes(DataGenerator pGenerator, IXplatIngredients ingredients) {
        super(pGenerator, HexAPI.MOD_ID);
        this.generator = pGenerator;
        this.ingredients = ingredients;
    }

    protected void makeRecipes(Consumer<FinishedRecipe> recipes) {
        specialRecipe(recipes, SealFocusRecipe.SERIALIZER);
        specialRecipe(recipes, SealSpellbookRecipe.SERIALIZER);

        wandRecipe(recipes, HexItems.STAFF_OAK, Items.OAK_PLANKS);
        wandRecipe(recipes, HexItems.STAFF_BIRCH, Items.BIRCH_PLANKS);
        wandRecipe(recipes, HexItems.STAFF_SPRUCE, Items.SPRUCE_PLANKS);
        wandRecipe(recipes, HexItems.STAFF_JUNGLE, Items.JUNGLE_PLANKS);
        wandRecipe(recipes, HexItems.STAFF_DARK_OAK, Items.DARK_OAK_PLANKS);
        wandRecipe(recipes, HexItems.STAFF_ACACIA, Items.ACACIA_PLANKS);
        wandRecipe(recipes, HexItems.STAFF_CRIMSON, Items.CRIMSON_PLANKS);
        wandRecipe(recipes, HexItems.STAFF_WARPED, Items.WARPED_PLANKS);
        wandRecipe(recipes, HexItems.STAFF_EDIFIED, HexBlocks.EDIFIED_PLANKS.asItem());

        ringCornered(HexItems.FOCUS, 1, ingredients.glowstoneDust(),
            ingredients.leather(), Ingredient.of(HexItems.CHARGED_AMETHYST))
            .unlockedBy("has_item", hasItem(HexItemTags.STAVES))
            .save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SPELLBOOK)
            .define('N', ingredients.goldNugget())
            .define('B', Items.WRITABLE_BOOK)
            .define('A', HexItems.CHARGED_AMETHYST)
            .define('F', Items.CHORUS_FRUIT) // i wanna gate this behind the end SOMEHOW
            // hey look its my gender ^^
            .pattern("NBA")
            .pattern("NFA")
            .pattern("NBA")
            .unlockedBy("has_focus", hasItem(HexItems.FOCUS))
            .unlockedBy("has_chorus", hasItem(Items.CHORUS_FRUIT)).save(recipes);

        ringCornerless(HexItems.CYPHER, 1,
            ingredients.copperIngot(),
            Ingredient.of(HexItems.AMETHYST_DUST))
            .unlockedBy("has_item", hasItem(HexItemTags.STAVES)).save(recipes);

        ringCornerless(HexItems.TRINKET, 1,
            ingredients.ironIngot(),
            Ingredient.of(Items.AMETHYST_SHARD))
            .unlockedBy("has_item", hasItem(HexItemTags.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ARTIFACT)
            .define('F', ingredients.goldIngot())
            .define('A', HexItems.CHARGED_AMETHYST)
            // why in god's name does minecraft have two different places for item tags
            .define('D', ItemTags.MUSIC_DISCS)
            .pattern(" F ")
            .pattern("FAF")
            .pattern(" D ")
            .unlockedBy("has_item", hasItem(HexItemTags.STAVES)).save(recipes);

        ringCornerless(HexItems.SCRYING_LENS, 1, Items.GLASS, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", hasItem(HexItemTags.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ABACUS)
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('W', ItemTags.PLANKS)
            .pattern("WAW")
            .pattern("SAS")
            .pattern("WAW")
            .unlockedBy("has_item", hasItem(HexItemTags.STAVES)).save(recipes);

        // Why am I like this
        ShapedRecipeBuilder.shaped(HexItems.SUBMARINE_SANDWICH)
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('C', Items.COOKED_BEEF)
            .define('B', Items.BREAD)
            .pattern(" SA")
            .pattern(" C ")
            .pattern(" B ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);

        for (var dye : DyeColor.values()) {
            var item = HexItems.DYE_COLORIZERS.get(dye);
            ShapedRecipeBuilder.shaped(item)
                .define('D', HexItems.AMETHYST_DUST)
                .define('C', DyeItem.byColor(dye))
                .pattern(" D ")
                .pattern("DCD")
                .pattern(" D ")
                .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes);
        }

        gayRecipe(recipes, ItemPrideColorizer.Type.AGENDER, Items.GLASS);
        gayRecipe(recipes, ItemPrideColorizer.Type.AROACE, Items.WHEAT_SEEDS);
        gayRecipe(recipes, ItemPrideColorizer.Type.AROMANTIC, Items.ARROW);
        gayRecipe(recipes, ItemPrideColorizer.Type.ASEXUAL, Items.BREAD);
        gayRecipe(recipes, ItemPrideColorizer.Type.BISEXUAL, Items.WHEAT);
        gayRecipe(recipes, ItemPrideColorizer.Type.DEMIBOY, Items.RAW_IRON);
        gayRecipe(recipes, ItemPrideColorizer.Type.DEMIGIRL, Items.RAW_COPPER);
        gayRecipe(recipes, ItemPrideColorizer.Type.GAY, Items.STONE_BRICK_WALL);
        gayRecipe(recipes, ItemPrideColorizer.Type.GENDERFLUID, Items.WATER_BUCKET);
        gayRecipe(recipes, ItemPrideColorizer.Type.GENDERQUEER, Items.GLASS_BOTTLE);
        gayRecipe(recipes, ItemPrideColorizer.Type.INTERSEX, Items.AZALEA);
        gayRecipe(recipes, ItemPrideColorizer.Type.LESBIAN, Items.HONEYCOMB);
        gayRecipe(recipes, ItemPrideColorizer.Type.NONBINARY, Items.MOSS_BLOCK);
        gayRecipe(recipes, ItemPrideColorizer.Type.PANSEXUAL, Items.CARROT);
        gayRecipe(recipes, ItemPrideColorizer.Type.PLURAL, Items.REPEATER);
        gayRecipe(recipes, ItemPrideColorizer.Type.TRANSGENDER, Items.EGG);

        ShapedRecipeBuilder.shaped(HexItems.UUID_COLORIZER)
            .define('B', Items.BOWL)
            .define('D', HexItems.AMETHYST_DUST)
            .define('C', Items.AMETHYST_SHARD)
            .pattern(" C ")
            .pattern(" D ")
            .pattern(" B ")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SCROLL_SMOL)
            .define('P', Items.PAPER)
            .define('A', Items.AMETHYST_SHARD)
            .pattern(" A")
            .pattern("P ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);
        ShapedRecipeBuilder.shaped(HexItems.SCROLL_MEDIUM)
            .define('P', Items.PAPER)
            .define('A', Items.AMETHYST_SHARD)
            .pattern("  A")
            .pattern("PP ")
            .pattern("PP ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);
        ShapedRecipeBuilder.shaped(HexItems.SCROLL_LARGE)
            .define('P', Items.PAPER)
            .define('A', Items.AMETHYST_SHARD)
            .pattern("PPA")
            .pattern("PPP")
            .pattern("PPP")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SLATE, 6)
            .define('S', Items.DEEPSLATE)
            .define('A', HexItems.AMETHYST_DUST)
            .pattern(" A ")
            .pattern("SSS")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.JEWELER_HAMMER)
            .define('I', ingredients.ironIngot())
            .define('N', ingredients.ironNugget())
            .define('A', Items.AMETHYST_SHARD)
            .define('S', ingredients.stick())
            .pattern("IAN")
            .pattern(" S ")
            .pattern(" S ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.SLATE_BLOCK)
            .define('S', HexItems.SLATE)
            .pattern("S")
            .pattern("S")
            .unlockedBy("has_item", hasItem(HexItems.SLATE))
            .save(recipes, modLoc("slate_block_from_slates"));

        ringAll(HexBlocks.SLATE_BLOCK, 8, Blocks.DEEPSLATE, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", hasItem(HexItems.SLATE)).save(recipes);

        packing(HexItems.AMETHYST_DUST, HexBlocks.AMETHYST_DUST_BLOCK.asItem(), "amethyst_dust",
            false, recipes);

        ringAll(HexBlocks.AMETHYST_TILES, 8, Blocks.AMETHYST_BLOCK, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.AMETHYST_BLOCK), HexBlocks.AMETHYST_TILES)
            .unlockedBy("has_item", hasItem(Blocks.AMETHYST_BLOCK))
            .save(recipes, modLoc("stonecutting/amethyst_tiles"));

        ringAll(HexBlocks.SCROLL_PAPER, 8, Items.PAPER, Items.AMETHYST_SHARD)
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.ANCIENT_SCROLL_PAPER, 8)
            .requires(ingredients.dyes().get(DyeColor.BROWN))
            .requires(HexBlocks.SCROLL_PAPER, 8)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER)).save(recipes);
        stack(HexBlocks.SCROLL_PAPER_LANTERN, 1, HexBlocks.SCROLL_PAPER, Items.TORCH)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER)).save(recipes);
        stack(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN, 1, HexBlocks.ANCIENT_SCROLL_PAPER, Items.TORCH)
            .unlockedBy("has_item", hasItem(HexBlocks.ANCIENT_SCROLL_PAPER)).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN, 8)
            .requires(ingredients.dyes().get(DyeColor.BROWN))
            .requires(HexBlocks.SCROLL_PAPER_LANTERN, 8)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER_LANTERN))
            .save(recipes, modLoc("ageing_scroll_paper_lantern"));

        stack(HexBlocks.SCONCE, 4, Ingredient.of(HexItems.CHARGED_AMETHYST),
            ingredients.copperIngot())
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST)).save(recipes);

        ShapelessRecipeBuilder.shapeless(HexBlocks.EDIFIED_PLANKS, 4)
            .requires(HexItemTags.EDIFIED_LOGS)
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_LOGS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_WOOD, 3)
            .define('W', HexBlocks.EDIFIED_LOG)
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexBlocks.EDIFIED_LOG)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.STRIPPED_EDIFIED_WOOD, 3)
            .define('W', HexBlocks.STRIPPED_EDIFIED_LOG)
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexBlocks.STRIPPED_EDIFIED_LOG)).save(recipes);
        ring(HexBlocks.EDIFIED_PANEL, 8, HexItemTags.EDIFIED_PLANKS, null)
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_TILE, 6)
            .define('W', HexItemTags.EDIFIED_PLANKS)
            .pattern("WW ")
            .pattern("W W")
            .pattern(" WW")
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_DOOR, 3)
            .define('W', HexItemTags.EDIFIED_PLANKS)
            .pattern("WW")
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_TRAPDOOR, 2)
            .define('W', HexItemTags.EDIFIED_PLANKS)
            .pattern("WWW")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_STAIRS, 4)
            .define('W', HexItemTags.EDIFIED_PLANKS)
            .pattern("W  ")
            .pattern("WW ")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_SLAB, 6)
            .define('W', HexItemTags.EDIFIED_PLANKS)
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_PRESSURE_PLATE, 1)
            .define('W', HexItemTags.EDIFIED_PLANKS)
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_PLANKS)).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.EDIFIED_BUTTON)
            .requires(HexItemTags.EDIFIED_PLANKS)
            .unlockedBy("has_item", hasItem(HexItemTags.EDIFIED_PLANKS)).save(recipes);

        var enlightenment = new OvercastTrigger.Instance(EntityPredicate.Composite.ANY,
            MinMaxBounds.Ints.ANY,
            // add a little bit of slop here
            MinMaxBounds.Doubles.atLeast(0.8),
            MinMaxBounds.Doubles.between(0.1, 2.05));

        ShapedRecipeBuilder.shaped(HexBlocks.EMPTY_IMPETUS)
            .define('B', Items.IRON_BARS)
            .define('A', HexItems.CHARGED_AMETHYST)
            .define('S', HexBlocks.SLATE_BLOCK)
            .define('P', Items.PURPUR_BLOCK)
            .pattern("PSS")
            .pattern("BAB")
            .pattern("SSP")
            .unlockedBy("enlightenment", enlightenment).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.EMPTY_DIRECTRIX)
            .define('C', Items.COMPARATOR)
            .define('O', Items.OBSERVER)
            .define('A', HexItems.CHARGED_AMETHYST)
            .define('S', HexBlocks.SLATE_BLOCK)
            .pattern("CSS")
            .pattern("OAO")
            .pattern("SSC")
            .unlockedBy("enlightenment", enlightenment).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_BOOKSHELF)
            .define('L', HexItemTags.EDIFIED_LOGS)
            .define('P', HexItemTags.EDIFIED_PLANKS)
            .define('C', Items.BOOK)
            /*this is the*/.pattern("LPL") // and what i have for you today is
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_LIGATURE)
            .define('L', HexItemTags.EDIFIED_LOGS)
            .define('P', HexItemTags.EDIFIED_PLANKS)
            .define('C', HexItems.CHARGED_AMETHYST)
            .pattern("LPL")
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes);

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(Blocks.AMETHYST_BLOCK),
            new VillagerIngredient(null, null, 3),
            Blocks.BUDDING_AMETHYST.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/budding_amethyst"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS),
            new VillagerIngredient(new ResourceLocation("toolsmith"), null, 2),
            HexBlocks.IMPETUS_RIGHTCLICK.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_rightclick"));
        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS),
            new VillagerIngredient(new ResourceLocation("fletcher"), null, 2),
            HexBlocks.IMPETUS_LOOK.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_look"));
        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS),
            new VillagerIngredient(new ResourceLocation("cleric"), null, 2),
            HexBlocks.IMPETUS_STOREDPLAYER.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_storedplayer"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_DIRECTRIX),
            new VillagerIngredient(new ResourceLocation("mason"), null, 1),
            HexBlocks.DIRECTRIX_REDSTONE.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/directrix_redstone"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.AKASHIC_LIGATURE),
            new VillagerIngredient(new ResourceLocation("librarian"), null, 5),
            HexBlocks.AKASHIC_RECORD.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/akashic_record"));
    }

    private void wandRecipe(Consumer<FinishedRecipe> recipes, ItemStaff wand, Item plank) {
        ShapedRecipeBuilder.shaped(wand)
            .define('W', plank)
            .define('S', Items.STICK)
            .define('A', HexItems.CHARGED_AMETHYST)
            .pattern(" SA")
            .pattern(" WS")
            .pattern("S  ")
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST))
            .save(recipes);
    }

    private void gayRecipe(Consumer<FinishedRecipe> recipes, ItemPrideColorizer.Type type, Item material) {
        var colorizer = HexItems.PRIDE_COLORIZERS.get(type);
        ShapedRecipeBuilder.shaped(colorizer)
            .define('D', HexItems.AMETHYST_DUST)
            .define('C', material)
            .pattern(" D ")
            .pattern("DCD")
            .pattern(" D ")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes);
    }

    protected void specialRecipe(Consumer<FinishedRecipe> consumer, SimpleRecipeSerializer<?> serializer) {
        var name = Registry.RECIPE_SERIALIZER.getKey(serializer);
        SpecialRecipeBuilder.special(serializer).save(consumer, HexAPI.MOD_ID + ":dynamic/" + name.getPath());
    }
}
