package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.advancements.OvercastTrigger;
import at.petrak.hexcasting.api.mod.HexItemTags;
import at.petrak.hexcasting.common.items.ItemWand;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.SealFocusRecipe;
import at.petrak.hexcasting.common.recipe.SealSpellbookRecipe;
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper;
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient;
import at.petrak.hexcasting.forge.datagen.recipebuilders.BrainsweepRecipeBuilder;
import at.petrak.paucal.api.forge.datagen.PaucalRecipeProvider;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;


public class HexRecipes extends PaucalRecipeProvider {
    public HexRecipes(DataGenerator pGenerator) {
        super(pGenerator, HexAPI.MOD_ID);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipes) {
        specialRecipe(recipes, SealFocusRecipe.SERIALIZER);
        specialRecipe(recipes, SealSpellbookRecipe.SERIALIZER);

        wandRecipe(recipes, HexItems.WAND_OAK, Items.OAK_PLANKS);
        wandRecipe(recipes, HexItems.WAND_BIRCH, Items.BIRCH_PLANKS);
        wandRecipe(recipes, HexItems.WAND_SPRUCE, Items.SPRUCE_PLANKS);
        wandRecipe(recipes, HexItems.WAND_JUNGLE, Items.JUNGLE_PLANKS);
        wandRecipe(recipes, HexItems.WAND_DARK_OAK, Items.DARK_OAK_PLANKS);
        wandRecipe(recipes, HexItems.WAND_ACACIA, Items.ACACIA_PLANKS);
        wandRecipe(recipes, HexItems.WAND_CRIMSON, Items.CRIMSON_PLANKS);
        wandRecipe(recipes, HexItems.WAND_WARPED, Items.WARPED_PLANKS);
        wandRecipe(recipes, HexItems.WAND_AKASHIC, HexBlocks.AKASHIC_PLANKS.asItem());

        ringCornered(HexItems.FOCUS, 1, Ingredient.of(Tags.Items.DUSTS_GLOWSTONE),
            Ingredient.of(Tags.Items.LEATHER), Ingredient.of(HexItems.CHARGED_AMETHYST))
            .unlockedBy("has_item", has(HexItemTags.WANDS))
            .save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SPELLBOOK)
            .define('N', Tags.Items.NUGGETS_GOLD)
            .define('B', Items.WRITABLE_BOOK)
            .define('A', HexItems.CHARGED_AMETHYST)
            .define('F', Items.CHORUS_FRUIT) // i wanna gate this behind the end SOMEHOW
            // hey look its my gender ^^
            .pattern("NBA")
            .pattern("NFA")
            .pattern("NBA")
            .unlockedBy("has_focus", has(HexItems.FOCUS))
            .unlockedBy("has_chorus", has(Items.CHORUS_FRUIT)).save(recipes);

        ringCornerless(HexItems.CYPHER, 1,
            Ingredient.of(Tags.Items.INGOTS_COPPER),
            Ingredient.of(HexItems.AMETHYST_DUST))
            .unlockedBy("has_item", has(HexItemTags.WANDS)).save(recipes);

        ringCornerless(HexItems.TRINKET, 1,
            Ingredient.of(Tags.Items.INGOTS_IRON),
            Ingredient.of(Items.AMETHYST_SHARD))
            .unlockedBy("has_item", has(HexItemTags.WANDS)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ARTIFACT)
            .define('F', Tags.Items.INGOTS_GOLD)
            .define('A', HexItems.CHARGED_AMETHYST)
            // why in god's name does minecraft have two different places for item tags
            .define('D', ItemTags.MUSIC_DISCS)
            .pattern(" F ")
            .pattern("FAF")
            .pattern(" D ")
            .unlockedBy("has_item", has(HexItemTags.WANDS)).save(recipes);

        ringCornerless(HexItems.SCRYING_LENS, 1, Items.GLASS, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", has(HexItemTags.WANDS)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ABACUS)
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('W', ItemTags.PLANKS)
            .pattern("WAW")
            .pattern("SAS")
            .pattern("WAW")
            .unlockedBy("has_item", has(HexItemTags.WANDS)).save(recipes);

        // Why am I like this
        ShapedRecipeBuilder.shaped(HexItems.SUBMARINE_SANDWICH)
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('C', Items.COOKED_BEEF)
            .define('B', Items.BREAD)
            .pattern(" SA")
            .pattern(" C ")
            .pattern(" B ")
            .unlockedBy("has_item", has(Items.AMETHYST_SHARD)).save(recipes);

        for (var dye : DyeColor.values()) {
            var item = HexItems.DYE_COLORIZERS.get(dye);
            ShapedRecipeBuilder.shaped(item)
                .define('B', Items.BOWL)
                .define('D', HexItems.AMETHYST_DUST)
                .define('C', DyeItem.byColor(dye))
                .pattern(" C ")
                .pattern(" D ")
                .pattern(" B ")
                .unlockedBy("has_item", has(HexItems.AMETHYST_DUST)).save(recipes);
        }

        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[0], Items.EGG); // Trans
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[1], Items.STONE_BRICK_WALL); // Gay
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[2], Items.GLASS); // Agender
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[3], Items.BREAD); // Asexual
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[4], Items.WHEAT); // Bisexual
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[5], Items.CARROT); // Pansexual
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[6], Items.GLASS_BOTTLE); // Genderqueer
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[7], Items.RAW_COPPER); // Demigirl
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[8], Items.MOSS_BLOCK); // Non-Binary
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[9], Items.HONEYCOMB); // Lesbian
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[10], Items.RAW_IRON); // Demiboy
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[11], Items.WATER_BUCKET); // Genderfluid
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[12], Items.AZALEA); // Intersex
        gayRecipe(recipes, HexItems.PRIDE_COLORIZERS[13], Items.ARROW); // Aroace

        ShapedRecipeBuilder.shaped(HexItems.UUID_COLORIZER)
            .define('B', Items.BOWL)
            .define('D', HexItems.AMETHYST_DUST)
            .define('C', Items.AMETHYST_SHARD)
            .pattern(" C ")
            .pattern(" D ")
            .pattern(" B ")
            .unlockedBy("has_item", has(HexItems.AMETHYST_DUST)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SCROLL)
            .define('P', Items.PAPER)
            .define('A', Items.AMETHYST_SHARD)
            .pattern("  A")
            .pattern("PP ")
            .pattern("PP ")
            .unlockedBy("has_item", has(Items.AMETHYST_SHARD)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SLATE, 6)
            .define('S', Items.DEEPSLATE)
            .define('A', HexItems.AMETHYST_DUST)
            .pattern(" A ")
            .pattern("SSS")
            .unlockedBy("has_item", has(HexItems.AMETHYST_DUST)).save(recipes);

<<<<<<< HEAD:Forge/src/main/java/at/petrak/hexcasting/forge/datagen/HexRecipes.java
        ShapedRecipeBuilder.shaped(HexBlocks.SLATE_BLOCK)
            .define('S', HexItems.SLATE)
=======
        ShapedRecipeBuilder.shaped(HexItems.JEWELER_HAMMER.get())
            .define('I', Items.IRON_INGOT)
            .define('N', Items.IRON_NUGGET)
            .define('A', Items.AMETHYST_SHARD)
            .define('S', Items.STICK)
            .pattern("IAN")
            .pattern(" S ")
            .pattern(" S ")
            .unlockedBy("has_item", has(Items.AMETHYST_SHARD)).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.SLATE_BLOCK.get())
            .define('S', HexItems.SLATE.get())
>>>>>>> 65b04c03 (jeweler's hammer):src/main/java/at/petrak/hexcasting/datagen/HexRecipes.java
            .pattern("S")
            .pattern("S")
            .unlockedBy("has_item", has(HexItems.SLATE))
            .save(recipes, modLoc("slate_block_from_slates"));

        ringAll(HexBlocks.SLATE_BLOCK, 8, Blocks.DEEPSLATE, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", has(HexItems.SLATE)).save(recipes);

        packing(HexItems.AMETHYST_DUST, HexBlocks.AMETHYST_DUST_BLOCK.asItem(), "amethyst_dust",
            false, recipes);

        ringAll(HexBlocks.AMETHYST_TILES, 8, Blocks.AMETHYST_BLOCK, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", has(HexItems.AMETHYST_DUST)).save(recipes);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.AMETHYST_BLOCK), HexBlocks.AMETHYST_TILES)
            .unlockedBy("has_item", has(Blocks.AMETHYST_BLOCK))
            .save(recipes, modLoc("stonecutting/amethyst_tiles"));

        ringAll(HexBlocks.SCROLL_PAPER, 8, Items.PAPER, Items.AMETHYST_SHARD)
            .unlockedBy("has_item", has(Items.AMETHYST_SHARD)).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.ANCIENT_SCROLL_PAPER, 8)
            .requires(Tags.Items.DYES_BROWN)
            .requires(HexBlocks.SCROLL_PAPER, 8)
            .unlockedBy("has_item", has(HexBlocks.SCROLL_PAPER)).save(recipes);
        stack(HexBlocks.SCROLL_PAPER_LANTERN, 1, HexBlocks.SCROLL_PAPER, Items.TORCH)
            .unlockedBy("has_item", has(HexBlocks.SCROLL_PAPER)).save(recipes);
        stack(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN, 1, HexBlocks.ANCIENT_SCROLL_PAPER, Items.TORCH)
            .unlockedBy("has_item", has(HexBlocks.ANCIENT_SCROLL_PAPER)).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN, 8)
            .requires(Tags.Items.DYES_BROWN)
            .requires(HexBlocks.SCROLL_PAPER_LANTERN, 8)
            .unlockedBy("has_item", has(HexBlocks.SCROLL_PAPER_LANTERN))
            .save(recipes, modLoc("ageing_scroll_paper_lantern"));

        stack(HexBlocks.SCONCE, 4, Ingredient.of(HexItems.CHARGED_AMETHYST),
            Ingredient.of(Tags.Items.INGOTS_COPPER))
            .unlockedBy("has_item", has(HexItems.CHARGED_AMETHYST)).save(recipes);

        ShapelessRecipeBuilder.shapeless(HexBlocks.AKASHIC_PLANKS, 4)
            .requires(HexItemTags.AKASHIC_LOGS)
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_LOGS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_WOOD, 3)
            .define('W', HexBlocks.AKASHIC_LOG)
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", has(HexBlocks.AKASHIC_LOG)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_WOOD_STRIPPED, 3)
            .define('W', HexBlocks.AKASHIC_LOG_STRIPPED)
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", has(HexBlocks.AKASHIC_LOG_STRIPPED)).save(recipes);
        ring(HexBlocks.AKASHIC_PANEL, 8, HexItemTags.AKASHIC_PLANKS, null)
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_TILE, 6)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW ")
            .pattern("W W")
            .pattern(" WW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_DOOR, 3)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW")
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_TRAPDOOR, 2)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WWW")
            .pattern("WWW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_STAIRS, 4)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("W  ")
            .pattern("WW ")
            .pattern("WWW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_SLAB, 6)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WWW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_PRESSURE_PLATE, 1)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.AKASHIC_BUTTON)
            .requires(HexItemTags.AKASHIC_PLANKS)
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);

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
            .define('L', HexItemTags.AKASHIC_LOGS)
            .define('P', HexItemTags.AKASHIC_PLANKS)
            .define('C', Items.BOOK)
            /*this is the*/.pattern("LPL") // and what i have for you today is
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_CONNECTOR)
            .define('L', HexItemTags.AKASHIC_LOGS)
            .define('P', HexItemTags.AKASHIC_PLANKS)
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

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.AKASHIC_CONNECTOR),
            new VillagerIngredient(new ResourceLocation("librarian"), null, 5),
            HexBlocks.AKASHIC_RECORD.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/akashic_record"));
    }

    private void wandRecipe(Consumer<FinishedRecipe> recipes, ItemWand wand, Item plank) {
        ShapedRecipeBuilder.shaped(wand)
            .define('W', plank)
            .define('S', Items.STICK)
            .define('A', HexItems.CHARGED_AMETHYST)
            .pattern(" SA")
            .pattern(" WS")
            .pattern("S  ")
            .unlockedBy("has_item", has(HexItems.CHARGED_AMETHYST))
            .save(recipes);
    }

    private void gayRecipe(Consumer<FinishedRecipe> recipes, Item colorizer, Item material) {
        ShapedRecipeBuilder.shaped(colorizer)
            .define('B', Items.BOWL)
            .define('D', HexItems.AMETHYST_DUST)
            .define('C', material)
            .pattern(" C ")
            .pattern(" D ")
            .pattern(" B ")
            .unlockedBy("has_item", has(HexItems.AMETHYST_DUST)).save(recipes);
    }

    protected void specialRecipe(Consumer<FinishedRecipe> consumer, SimpleRecipeSerializer<?> serializer) {
        var name = ForgeRegistries.RECIPE_SERIALIZERS.getKey(serializer);
        SpecialRecipeBuilder.special(serializer).save(consumer, HexAPI.MOD_ID + ":dynamic/" + name.getPath());
    }

    // why is this private waa
    protected static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> pTag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(pTag).build());
    }
}
