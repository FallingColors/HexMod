package at.petrak.hexcasting.datagen.recipe;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.SealThingsRecipe;
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.EntityTypeIngredient;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.VillagerIngredient;
import at.petrak.hexcasting.datagen.HexAdvancements;
import at.petrak.hexcasting.datagen.IXplatConditionsBuilder;
import at.petrak.hexcasting.datagen.IXplatIngredients;
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder;
import at.petrak.hexcasting.datagen.recipe.builders.CompatIngredientValue;
import at.petrak.hexcasting.datagen.recipe.builders.CreateCrushingRecipeBuilder;
import at.petrak.hexcasting.datagen.recipe.builders.FarmersDelightCuttingRecipeBuilder;
import at.petrak.paucal.api.datagen.PaucalRecipeProvider;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;
import java.util.function.Function;

// TODO: need to do a big refactor of this class cause it's giant and unwieldy, probably as part of #360
public class HexplatRecipes extends PaucalRecipeProvider {
    private final DataGenerator generator;
    private final IXplatIngredients ingredients;
    private final Function<RecipeBuilder, IXplatConditionsBuilder> conditions;

    public HexplatRecipes(DataGenerator generator, IXplatIngredients ingredients,
        Function<RecipeBuilder, IXplatConditionsBuilder> conditions) {
        super(generator, HexAPI.MOD_ID);
        this.generator = generator;
        this.ingredients = ingredients;
        this.conditions = conditions;
    }

    @Override
    protected void makeRecipes(Consumer<FinishedRecipe> recipes) {
        specialRecipe(recipes, SealThingsRecipe.FOCUS_SERIALIZER);
        specialRecipe(recipes, SealThingsRecipe.SPELLBOOK_SERIALIZER);

        staffRecipe(recipes, HexItems.STAFF_OAK, Items.OAK_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_BIRCH, Items.BIRCH_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_SPRUCE, Items.SPRUCE_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_JUNGLE, Items.JUNGLE_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_DARK_OAK, Items.DARK_OAK_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_ACACIA, Items.ACACIA_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_CRIMSON, Items.CRIMSON_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_WARPED, Items.WARPED_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_MANGROVE, Items.MANGROVE_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_EDIFIED, HexBlocks.EDIFIED_PLANKS.asItem());
        staffRecipe(recipes, HexItems.STAFF_QUENCHED, HexItems.QUENCHED_SHARD);
        staffRecipe(recipes, HexItems.STAFF_MINDSPLICE, Ingredient.of(HexTags.Items.MINDFLAYED_CIRCLE_COMPONENTS));

        ShapelessRecipeBuilder.shapeless(HexItems.THOUGHT_KNOT)
            .requires(HexItems.AMETHYST_DUST)
            .requires(Items.STRING)
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
            .save(recipes);
        ShapedRecipeBuilder.shaped(HexItems.FOCUS)
            .define('G', ingredients.glowstoneDust())
            .define('L', ingredients.leather())
            .define('P', Items.PAPER)
            .define('A', HexItems.CHARGED_AMETHYST)
            .pattern("GLG")
            .pattern("PAP")
            .pattern("GLG")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
            .save(recipes);
        ShapedRecipeBuilder.shaped(HexItems.FOCUS)
            .define('G', ingredients.glowstoneDust())
            .define('L', ingredients.leather())
            .define('P', Items.PAPER)
            .define('A', HexItems.CHARGED_AMETHYST)
            .pattern("GPG")
            .pattern("LAL")
            .pattern("GPG")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
            .save(recipes, modLoc("focus_rotated"));

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

        ringCornerless(
            HexItems.CYPHER, 1,
            ingredients.copperIngot(),
            Ingredient.of(HexItems.AMETHYST_DUST))
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ringCornerless(
            HexItems.TRINKET, 1,
            ingredients.ironIngot(),
            Ingredient.of(Items.AMETHYST_SHARD))
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ARTIFACT)
            .define('F', ingredients.goldIngot())
            .define('A', HexItems.CHARGED_AMETHYST)
            // why in god's name does minecraft have two different places for item tags
            .define('D', ItemTags.MUSIC_DISCS)
            .pattern(" F ")
            .pattern("FAF")
            .pattern(" D ")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ringCornerless(HexItems.SCRYING_LENS, 1, Items.GLASS, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ABACUS)
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('W', ItemTags.PLANKS)
            .pattern("WAW")
            .pattern("SAS")
            .pattern("WAW")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

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

        gayRecipe(recipes, ItemPrideColorizer.Type.AGENDER, Ingredient.of(Items.GLASS));
        gayRecipe(recipes, ItemPrideColorizer.Type.AROACE, Ingredient.of(Items.WHEAT_SEEDS));
        gayRecipe(recipes, ItemPrideColorizer.Type.AROMANTIC, Ingredient.of(Items.ARROW));
        gayRecipe(recipes, ItemPrideColorizer.Type.ASEXUAL, Ingredient.of(Items.BREAD));
        gayRecipe(recipes, ItemPrideColorizer.Type.BISEXUAL, Ingredient.of(Items.WHEAT));
        gayRecipe(recipes, ItemPrideColorizer.Type.DEMIBOY, Ingredient.of(Items.RAW_IRON));
        gayRecipe(recipes, ItemPrideColorizer.Type.DEMIGIRL, Ingredient.of(Items.RAW_COPPER));
        gayRecipe(recipes, ItemPrideColorizer.Type.GAY, Ingredient.of(Items.STONE_BRICK_WALL));
        gayRecipe(recipes, ItemPrideColorizer.Type.GENDERFLUID, Ingredient.of(Items.WATER_BUCKET));
        gayRecipe(recipes, ItemPrideColorizer.Type.GENDERQUEER, Ingredient.of(Items.GLASS_BOTTLE));
        gayRecipe(recipes, ItemPrideColorizer.Type.INTERSEX, Ingredient.of(Items.AZALEA));
        gayRecipe(recipes, ItemPrideColorizer.Type.LESBIAN, Ingredient.of(Items.HONEYCOMB));
        gayRecipe(recipes, ItemPrideColorizer.Type.NONBINARY, Ingredient.of(Items.MOSS_BLOCK));
        gayRecipe(recipes, ItemPrideColorizer.Type.PANSEXUAL, ingredients.whenModIngredient(
            Ingredient.of(Items.CARROT),
            "farmersdelight",
            CompatIngredientValue.of("farmersdelight:skillet")
        ));
        gayRecipe(recipes, ItemPrideColorizer.Type.PLURAL, Ingredient.of(Items.REPEATER));
        gayRecipe(recipes, ItemPrideColorizer.Type.TRANSGENDER, Ingredient.of(Items.EGG));

        ring(HexItems.UUID_COLORIZER, 1, HexItems.AMETHYST_DUST, Items.AMETHYST_SHARD)
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes);
        ring(HexItems.DEFAULT_COLORIZER, 1, HexItems.AMETHYST_DUST, Items.COPPER_INGOT)
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SCROLL_SMOL)
            .define('P', Items.PAPER)
            .define('A', HexItems.AMETHYST_DUST)
            .pattern(" A")
            .pattern("P ")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SCROLL_MEDIUM)
            .define('P', Items.PAPER)
            .define('A', HexItems.AMETHYST_DUST)
            .pattern("  A")
            .pattern("PP ")
            .pattern("PP ")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SCROLL_LARGE)
            .define('P', Items.PAPER)
            .define('A', HexItems.AMETHYST_DUST)
            .pattern("PPA")
            .pattern("PPP")
            .pattern("PPP")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

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

        ShapelessRecipeBuilder.shapeless(HexItems.AMETHYST_DUST,
                (MediaConstants.QUENCHED_SHARD_UNIT / MediaConstants.DUST_UNIT) + 1)
            .requires(HexItems.QUENCHED_SHARD)
            .requires(HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", hasItem(HexItems.QUENCHED_SHARD))
            .save(recipes, modLoc("decompose_quenched_shard/dust"));
        ShapelessRecipeBuilder.shapeless(Items.AMETHYST_SHARD,
                (MediaConstants.QUENCHED_SHARD_UNIT / MediaConstants.SHARD_UNIT) + 1)
            .requires(HexItems.QUENCHED_SHARD)
            .requires(Items.AMETHYST_SHARD)
            .unlockedBy("has_item", hasItem(HexItems.QUENCHED_SHARD))
            .save(recipes, modLoc("decompose_quenched_shard/shard"));
        ShapelessRecipeBuilder.shapeless(HexItems.CHARGED_AMETHYST,
                (MediaConstants.QUENCHED_SHARD_UNIT / MediaConstants.CRYSTAL_UNIT) + 1)
            .requires(HexItems.QUENCHED_SHARD)
            .requires(HexItems.CHARGED_AMETHYST)
            .unlockedBy("has_item", hasItem(HexItems.QUENCHED_SHARD))
            .save(recipes, modLoc("decompose_quenched_shard/charged"));

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

        stack(HexBlocks.SCONCE, 4,
            Ingredient.of(HexItems.CHARGED_AMETHYST),
            ingredients.copperIngot())
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST)).save(recipes);

        ShapelessRecipeBuilder.shapeless(HexBlocks.EDIFIED_PLANKS, 4)
            .requires(HexTags.Items.EDIFIED_LOGS)
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_LOGS)).save(recipes);

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

        ring(HexBlocks.EDIFIED_PANEL, 8,
            HexTags.Items.EDIFIED_PLANKS, null)
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_TILE, 6)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WW ")
            .pattern("W W")
            .pattern(" WW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_DOOR, 3)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WW")
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_TRAPDOOR, 2)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WWW")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_STAIRS, 4)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("W  ")
            .pattern("WW ")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_SLAB, 6)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.EDIFIED_PRESSURE_PLATE, 1)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapelessRecipeBuilder.shapeless(HexBlocks.EDIFIED_BUTTON)
            .requires(HexTags.Items.EDIFIED_PLANKS)
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        var enlightenment = HexAdvancements.ENLIGHTEN;
        ShapedRecipeBuilder.shaped(HexBlocks.IMPETUS_EMPTY)
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
            .define('L', HexTags.Items.EDIFIED_LOGS)
            .define('P', HexTags.Items.EDIFIED_PLANKS)
            .define('C', Items.BOOK)
            /*this is the*/.pattern("LPL") // and what i have for you today is
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_LIGATURE, 4)
            .define('L', HexTags.Items.EDIFIED_LOGS)
            .define('P', HexTags.Items.EDIFIED_PLANKS)
            .define('1', HexItems.AMETHYST_DUST)
            .define('2', Items.AMETHYST_SHARD)
            .define('3', HexItems.CHARGED_AMETHYST)
            .pattern("LPL")
            .pattern("123")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes);

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(Blocks.AMETHYST_BLOCK),
            new VillagerIngredient(null, null, 3),
            Blocks.BUDDING_AMETHYST.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/budding_amethyst"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.IMPETUS_EMPTY),
            new VillagerIngredient(VillagerProfession.TOOLSMITH, null, 2),
            HexBlocks.IMPETUS_RIGHTCLICK.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_rightclick"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.IMPETUS_EMPTY),
            new VillagerIngredient(VillagerProfession.TOOLSMITH, null, 2),
            HexBlocks.IMPETUS_LOOK.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_look"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.IMPETUS_EMPTY),
            new VillagerIngredient(VillagerProfession.CLERIC, null, 2),
            HexBlocks.IMPETUS_REDSTONE.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_storedplayer"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_DIRECTRIX),
            new VillagerIngredient(VillagerProfession.MASON, null, 1),
            HexBlocks.DIRECTRIX_REDSTONE.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/directrix_redstone"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.AKASHIC_LIGATURE),
            new VillagerIngredient(VillagerProfession.LIBRARIAN, null, 5),
            HexBlocks.AKASHIC_RECORD.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/akashic_record"));

        // Temporary tests
        new BrainsweepRecipeBuilder(StateIngredientHelper.of(Blocks.AMETHYST_BLOCK),
            new EntityTypeIngredient(EntityType.ALLAY),
            HexBlocks.QUENCHED_ALLAY.defaultBlockState(), MediaConstants.CRYSTAL_UNIT)
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/quench_allay"));

        // Create compat
        this.conditions.apply(new CreateCrushingRecipeBuilder()
                .withInput(Blocks.AMETHYST_CLUSTER)
                .duration(150)
                .withOutput(Items.AMETHYST_SHARD, 7)
                .withOutput(HexItems.AMETHYST_DUST, 5)
                .withOutput(0.25f, HexItems.CHARGED_AMETHYST))
            .whenModLoaded("create")
            .save(recipes, new ResourceLocation("create", "crushing/amethyst_cluster"));

        this.conditions.apply(new CreateCrushingRecipeBuilder()
                .withInput(Blocks.AMETHYST_BLOCK)
                .duration(150)
                .withOutput(Items.AMETHYST_SHARD, 3)
                .withOutput(0.5f, HexItems.AMETHYST_DUST, 4))
            .whenModLoaded("create")
            .save(recipes, new ResourceLocation("create", "crushing/amethyst_block"));

        this.conditions.apply(new CreateCrushingRecipeBuilder()
                .withInput(Items.AMETHYST_SHARD)
                .duration(150)
                .withOutput(HexItems.AMETHYST_DUST, 4)
                .withOutput(0.5f, HexItems.AMETHYST_DUST))
            .whenModLoaded("create")
            .save(recipes, modLoc("compat/create/crushing/amethyst_shard"));

        // FD compat
        this.conditions.apply(new FarmersDelightCuttingRecipeBuilder()
                .withInput(HexBlocks.EDIFIED_LOG)
                .withTool(ingredients.axeStrip())
                .withOutput(HexBlocks.STRIPPED_EDIFIED_LOG)
                .withOutput("farmersdelight:tree_bark")
                .withSound(SoundEvents.AXE_STRIP))
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_log"));

        this.conditions.apply(new FarmersDelightCuttingRecipeBuilder()
                .withInput(HexBlocks.EDIFIED_WOOD)
                .withTool(ingredients.axeStrip())
                .withOutput(HexBlocks.STRIPPED_EDIFIED_WOOD)
                .withOutput("farmersdelight:tree_bark")
                .withSound(SoundEvents.AXE_STRIP))
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_wood"));

        this.conditions.apply(new FarmersDelightCuttingRecipeBuilder()
                .withInput(HexBlocks.EDIFIED_TRAPDOOR)
                .withTool(ingredients.axeDig())
                .withOutput(HexBlocks.EDIFIED_PLANKS))
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_trapdoor"));

        this.conditions.apply(new FarmersDelightCuttingRecipeBuilder()
                .withInput(HexBlocks.EDIFIED_DOOR)
                .withTool(ingredients.axeDig())
                .withOutput(HexBlocks.EDIFIED_PLANKS))
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_door"));
    }

    private void staffRecipe(Consumer<FinishedRecipe> recipes, ItemStaff staff, Item plank) {
        staffRecipe(recipes, staff, Ingredient.of(plank));
    }

    private void staffRecipe(Consumer<FinishedRecipe> recipes, ItemStaff staff, Ingredient plank) {
        ShapedRecipeBuilder.shaped(staff)
            .define('W', plank)
            .define('S', Items.STICK)
            .define('A', HexItems.CHARGED_AMETHYST)
            .pattern(" SA")
            .pattern(" WS")
            .pattern("S  ")
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST))
            .save(recipes);
    }

    private void gayRecipe(Consumer<FinishedRecipe> recipes, ItemPrideColorizer.Type type, Ingredient material) {
        var colorizer = HexItems.PRIDE_COLORIZERS.get(type);
        ShapedRecipeBuilder.shaped(colorizer)
            .define('D', HexItems.AMETHYST_DUST)
            .define('C', material)
            .pattern(" D ")
            .pattern("DCD")
            .pattern(" D ")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST))
            .save(recipes);
    }

    private void specialRecipe(Consumer<FinishedRecipe> consumer, SimpleRecipeSerializer<?> serializer) {
        var name = Registry.RECIPE_SERIALIZER.getKey(serializer);
        SpecialRecipeBuilder.special(serializer).save(consumer, HexAPI.MOD_ID + ":dynamic" + name.getPath());
    }
}
