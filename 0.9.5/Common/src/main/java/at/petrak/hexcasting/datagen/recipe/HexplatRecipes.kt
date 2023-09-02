package at.petrak.hexcasting.datagen.recipe

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.advancements.OvercastTrigger
import at.petrak.hexcasting.api.mod.HexItemTags
import at.petrak.hexcasting.common.items.ItemWand
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer
import at.petrak.hexcasting.common.lib.HexBlocks
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.recipe.SealFocusRecipe
import at.petrak.hexcasting.common.recipe.SealSpellbookRecipe
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient
import at.petrak.hexcasting.datagen.IXplatConditionsBuilder
import at.petrak.hexcasting.datagen.IXplatIngredients
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder
import at.petrak.hexcasting.datagen.recipe.builders.CompatIngredientValue
import at.petrak.hexcasting.datagen.recipe.builders.CreateCrushingRecipeBuilder
import at.petrak.hexcasting.datagen.recipe.builders.FarmersDelightCuttingRecipeBuilder
import at.petrak.paucal.api.datagen.PaucalRecipeProvider
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.core.Registry
import net.minecraft.data.DataGenerator
import net.minecraft.data.recipes.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.DyeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.SimpleRecipeSerializer
import net.minecraft.world.level.block.Blocks
import java.util.function.Consumer

class HexplatRecipes(
    val generator: DataGenerator,
    val ingredients: IXplatIngredients,
    val conditions: (RecipeBuilder) -> IXplatConditionsBuilder
) : PaucalRecipeProvider(generator, HexAPI.MOD_ID) {

    override fun makeRecipes(recipes: Consumer<FinishedRecipe>) {
        specialRecipe(recipes, SealFocusRecipe.SERIALIZER)
        specialRecipe(recipes, SealSpellbookRecipe.SERIALIZER)

        wandRecipe(recipes, HexItems.WAND_OAK, Items.OAK_PLANKS)
        wandRecipe(recipes, HexItems.WAND_BIRCH, Items.BIRCH_PLANKS)
        wandRecipe(recipes, HexItems.WAND_SPRUCE, Items.SPRUCE_PLANKS)
        wandRecipe(recipes, HexItems.WAND_JUNGLE, Items.JUNGLE_PLANKS)
        wandRecipe(recipes, HexItems.WAND_DARK_OAK, Items.DARK_OAK_PLANKS)
        wandRecipe(recipes, HexItems.WAND_ACACIA, Items.ACACIA_PLANKS)
        wandRecipe(recipes, HexItems.WAND_CRIMSON, Items.CRIMSON_PLANKS)
        wandRecipe(recipes, HexItems.WAND_WARPED, Items.WARPED_PLANKS)
        wandRecipe(recipes, HexItems.WAND_AKASHIC, HexBlocks.AKASHIC_PLANKS.asItem())

        ringCornered(HexItems.FOCUS, 1,
            ingredients.glowstoneDust(),
            ingredients.leather(),
            Ingredient.of(HexItems.CHARGED_AMETHYST))
            .unlockedBy("has_item", hasItem(HexItemTags.WANDS))
            .save(recipes)

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
            .unlockedBy("has_chorus", hasItem(Items.CHORUS_FRUIT)).save(recipes)

        ringCornerless(
            HexItems.CYPHER, 1,
            ingredients.copperIngot(),
            Ingredient.of(HexItems.AMETHYST_DUST))
            .unlockedBy("has_item", hasItem(HexItemTags.WANDS)).save(recipes)

        ringCornerless(
            HexItems.TRINKET, 1,
            ingredients.ironIngot(),
            Ingredient.of(Items.AMETHYST_SHARD))
            .unlockedBy("has_item", hasItem(HexItemTags.WANDS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexItems.ARTIFACT)
            .define('F', ingredients.goldIngot())
            .define('A', HexItems.CHARGED_AMETHYST)
            // why in god's name does minecraft have two different places for item tags
            .define('D', ItemTags.MUSIC_DISCS)
            .pattern(" F ")
            .pattern("FAF")
            .pattern(" D ")
            .unlockedBy("has_item", hasItem(HexItemTags.WANDS)).save(recipes)

        ringCornerless(HexItems.SCRYING_LENS, 1, Items.GLASS, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", hasItem(HexItemTags.WANDS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexItems.ABACUS)
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('W', ItemTags.PLANKS)
            .pattern("WAW")
            .pattern("SAS")
            .pattern("WAW")
            .unlockedBy("has_item", hasItem(HexItemTags.WANDS)).save(recipes)

        // Why am I like this
        ShapedRecipeBuilder.shaped(HexItems.SUBMARINE_SANDWICH)
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('C', Items.COOKED_BEEF)
            .define('B', Items.BREAD)
            .pattern(" SA")
            .pattern(" C ")
            .pattern(" B ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes)

        for (dye in DyeColor.values()) {
            val item = HexItems.DYE_COLORIZERS[dye]!!
            ShapedRecipeBuilder.shaped(item)
                .define('D', HexItems.AMETHYST_DUST)
                .define('C', DyeItem.byColor(dye))
                .pattern(" D ")
                .pattern("DCD")
                .pattern(" D ")
                .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes)
        }

        gayRecipe(recipes, ItemPrideColorizer.Type.AGENDER, Ingredient.of(Items.GLASS))
        gayRecipe(recipes, ItemPrideColorizer.Type.AROACE, Ingredient.of(Items.WHEAT_SEEDS))
        gayRecipe(recipes, ItemPrideColorizer.Type.AROMANTIC, Ingredient.of(Items.ARROW))
        gayRecipe(recipes, ItemPrideColorizer.Type.ASEXUAL, Ingredient.of(Items.BREAD))
        gayRecipe(recipes, ItemPrideColorizer.Type.BISEXUAL, Ingredient.of(Items.WHEAT))
        gayRecipe(recipes, ItemPrideColorizer.Type.DEMIBOY, Ingredient.of(Items.RAW_IRON))
        gayRecipe(recipes, ItemPrideColorizer.Type.DEMIGIRL, Ingredient.of(Items.RAW_COPPER))
        gayRecipe(recipes, ItemPrideColorizer.Type.GAY, Ingredient.of(Items.STONE_BRICK_WALL))
        gayRecipe(recipes, ItemPrideColorizer.Type.GENDERFLUID, Ingredient.of(Items.WATER_BUCKET))
        gayRecipe(recipes, ItemPrideColorizer.Type.GENDERQUEER, Ingredient.of(Items.GLASS_BOTTLE))
        gayRecipe(recipes, ItemPrideColorizer.Type.INTERSEX, Ingredient.of(Items.AZALEA))
        gayRecipe(recipes, ItemPrideColorizer.Type.LESBIAN, Ingredient.of(Items.HONEYCOMB))
        gayRecipe(recipes, ItemPrideColorizer.Type.NONBINARY, Ingredient.of(Items.MOSS_BLOCK))
        gayRecipe(recipes, ItemPrideColorizer.Type.PANSEXUAL, ingredients.whenModIngredient(
            Ingredient.of(Items.CARROT),
            "farmersdelight",
            CompatIngredientValue.of("farmersdelight:skillet")
        ))
        gayRecipe(recipes, ItemPrideColorizer.Type.PLURAL, Ingredient.of(Items.REPEATER))
        gayRecipe(recipes, ItemPrideColorizer.Type.TRANSGENDER, Ingredient.of(Items.EGG))

        ShapedRecipeBuilder.shaped(HexItems.UUID_COLORIZER)
            .define('B', Items.BOWL)
            .define('D', HexItems.AMETHYST_DUST)
            .define('C', Items.AMETHYST_SHARD)
            .pattern(" C ")
            .pattern(" D ")
            .pattern(" B ")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes)

        ShapedRecipeBuilder.shaped(HexItems.SCROLL_SMOL)
            .define('P', Items.PAPER)
            .define('A', Items.AMETHYST_SHARD)
            .pattern(" A")
            .pattern("P ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes)

        ShapedRecipeBuilder.shaped(HexItems.SCROLL_MEDIUM)
            .define('P', Items.PAPER)
            .define('A', Items.AMETHYST_SHARD)
            .pattern("  A")
            .pattern("PP ")
            .pattern("PP ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes)

        ShapedRecipeBuilder.shaped(HexItems.SCROLL_LARGE)
            .define('P', Items.PAPER)
            .define('A', Items.AMETHYST_SHARD)
            .pattern("PPA")
            .pattern("PPP")
            .pattern("PPP")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes)

        ShapedRecipeBuilder.shaped(HexItems.SLATE, 6)
            .define('S', Items.DEEPSLATE)
            .define('A', HexItems.AMETHYST_DUST)
            .pattern(" A ")
            .pattern("SSS")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes)

        ShapedRecipeBuilder.shaped(HexItems.JEWELER_HAMMER)
            .define('I', ingredients.ironIngot())
            .define('N', ingredients.ironNugget())
            .define('A', Items.AMETHYST_SHARD)
            .define('S', ingredients.stick())
            .pattern("IAN")
            .pattern(" S ")
            .pattern(" S ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.SLATE_BLOCK)
            .define('S', HexItems.SLATE)
            .pattern("S")
            .pattern("S")
            .unlockedBy("has_item", hasItem(HexItems.SLATE))
            .save(recipes, modLoc("slate_block_from_slates"))

        ringAll(HexBlocks.SLATE_BLOCK, 8, Blocks.DEEPSLATE, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", hasItem(HexItems.SLATE)).save(recipes)

        packing(HexItems.AMETHYST_DUST, HexBlocks.AMETHYST_DUST_BLOCK.asItem(), "amethyst_dust",
            false, recipes)

        ringAll(HexBlocks.AMETHYST_TILES, 8, Blocks.AMETHYST_BLOCK, HexItems.AMETHYST_DUST)
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST)).save(recipes)

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.AMETHYST_BLOCK), HexBlocks.AMETHYST_TILES)
            .unlockedBy("has_item", hasItem(Blocks.AMETHYST_BLOCK))
            .save(recipes, modLoc("stonecutting/amethyst_tiles"))

        ringAll(HexBlocks.SCROLL_PAPER, 8, Items.PAPER, Items.AMETHYST_SHARD)
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes)

        ShapelessRecipeBuilder.shapeless(HexBlocks.ANCIENT_SCROLL_PAPER, 8)
            .requires(ingredients.dyes()[DyeColor.BROWN]!!)
            .requires(HexBlocks.SCROLL_PAPER, 8)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER)).save(recipes)

        stack(HexBlocks.SCROLL_PAPER_LANTERN, 1, HexBlocks.SCROLL_PAPER, Items.TORCH)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER)).save(recipes)

        stack(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN, 1, HexBlocks.ANCIENT_SCROLL_PAPER, Items.TORCH)
            .unlockedBy("has_item", hasItem(HexBlocks.ANCIENT_SCROLL_PAPER)).save(recipes)

        ShapelessRecipeBuilder.shapeless(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN, 8)
            .requires(ingredients.dyes()[DyeColor.BROWN]!!)
            .requires(HexBlocks.SCROLL_PAPER_LANTERN, 8)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER_LANTERN))
            .save(recipes, modLoc("ageing_scroll_paper_lantern"))

        stack(HexBlocks.SCONCE, 4,
            Ingredient.of(HexItems.CHARGED_AMETHYST),
            ingredients.copperIngot())
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST)).save(recipes)

        ShapelessRecipeBuilder.shapeless(HexBlocks.AKASHIC_PLANKS, 4)
            .requires(HexItemTags.AKASHIC_LOGS)
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_LOGS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_WOOD, 3)
            .define('W', HexBlocks.AKASHIC_LOG)
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexBlocks.AKASHIC_LOG)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_WOOD_STRIPPED, 3)
            .define('W', HexBlocks.AKASHIC_LOG_STRIPPED)
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexBlocks.AKASHIC_LOG_STRIPPED)).save(recipes)

        ring(HexBlocks.AKASHIC_PANEL, 8,
            HexItemTags.AKASHIC_PLANKS, null)
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_PLANKS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_TILE, 6)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW ")
            .pattern("W W")
            .pattern(" WW")
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_PLANKS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_DOOR, 3)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW")
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_PLANKS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_TRAPDOOR, 2)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WWW")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_PLANKS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_STAIRS, 4)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("W  ")
            .pattern("WW ")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_PLANKS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_SLAB, 6)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_PLANKS)).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_PRESSURE_PLATE, 1)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_PLANKS)).save(recipes)

        ShapelessRecipeBuilder.shapeless(HexBlocks.AKASHIC_BUTTON)
            .requires(HexItemTags.AKASHIC_PLANKS)
            .unlockedBy("has_item", hasItem(HexItemTags.AKASHIC_PLANKS)).save(recipes)

        val enlightenment = OvercastTrigger.Instance(
            EntityPredicate.Composite.ANY,
            MinMaxBounds.Ints.ANY,  // add a little bit of slop here
            MinMaxBounds.Doubles.atLeast(0.8),
            MinMaxBounds.Doubles.between(0.1, 2.05)
        )
        ShapedRecipeBuilder.shaped(HexBlocks.EMPTY_IMPETUS)
            .define('B', Items.IRON_BARS)
            .define('A', HexItems.CHARGED_AMETHYST)
            .define('S', HexBlocks.SLATE_BLOCK)
            .define('P', Items.PURPUR_BLOCK)
            .pattern("PSS")
            .pattern("BAB")
            .pattern("SSP")
            .unlockedBy("enlightenment", enlightenment).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.EMPTY_DIRECTRIX)
            .define('C', Items.COMPARATOR)
            .define('O', Items.OBSERVER)
            .define('A', HexItems.CHARGED_AMETHYST)
            .define('S', HexBlocks.SLATE_BLOCK)
            .pattern("CSS")
            .pattern("OAO")
            .pattern("SSC")
            .unlockedBy("enlightenment", enlightenment).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_BOOKSHELF)
            .define('L', HexItemTags.AKASHIC_LOGS)
            .define('P', HexItemTags.AKASHIC_PLANKS)
            .define('C', Items.BOOK)
            /*this is the*/ .pattern("LPL") // and what i have for you today is
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes)

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_CONNECTOR)
            .define('L', HexItemTags.AKASHIC_LOGS)
            .define('P', HexItemTags.AKASHIC_PLANKS)
            .define('C', HexItems.CHARGED_AMETHYST)
            .pattern("LPL")
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes)

        BrainsweepRecipeBuilder(StateIngredientHelper.of(Blocks.AMETHYST_BLOCK),
            VillagerIngredient(null, null, 3),
            Blocks.BUDDING_AMETHYST.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/budding_amethyst"))

        BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS),
            VillagerIngredient(ResourceLocation("toolsmith"), null, 2),
            HexBlocks.IMPETUS_RIGHTCLICK.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_rightclick"))

        BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS),
            VillagerIngredient(ResourceLocation("fletcher"), null, 2),
            HexBlocks.IMPETUS_LOOK.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_look"))

        BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS),
            VillagerIngredient(ResourceLocation("cleric"), null, 2),
            HexBlocks.IMPETUS_STOREDPLAYER.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_storedplayer"))

        BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_DIRECTRIX),
            VillagerIngredient(ResourceLocation("mason"), null, 1),
            HexBlocks.DIRECTRIX_REDSTONE.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/directrix_redstone"))

        BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.AKASHIC_CONNECTOR),
            VillagerIngredient(ResourceLocation("librarian"), null, 5),
            HexBlocks.AKASHIC_RECORD.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/akashic_record"))

        // Create compat
        CreateCrushingRecipeBuilder()
            .withInput(Blocks.AMETHYST_CLUSTER)
            .duration(150)
            .withOutput(Items.AMETHYST_SHARD, 7)
            .withOutput(HexItems.AMETHYST_DUST, 5)
            .withOutput(0.25f, HexItems.CHARGED_AMETHYST)
            .withConditions()
            .whenModLoaded("create")
            .save(recipes, ResourceLocation("create", "crushing/amethyst_cluster"))

        CreateCrushingRecipeBuilder()
            .withInput(Blocks.AMETHYST_BLOCK)
            .duration(150)
            .withOutput(Items.AMETHYST_SHARD, 3)
            .withOutput(0.5f, HexItems.AMETHYST_DUST, 4)
            .withConditions()
            .whenModLoaded("create")
            .save(recipes, ResourceLocation("create", "crushing/amethyst_block"))

        CreateCrushingRecipeBuilder()
            .withInput(Items.AMETHYST_SHARD)
            .duration(150)
            .withOutput(HexItems.AMETHYST_DUST, 4)
            .withOutput(0.5f, HexItems.AMETHYST_DUST)
            .withConditions()
            .whenModLoaded("create")
            .save(recipes, modLoc("compat/create/crushing/amethyst_shard"))

        // FD compat
        FarmersDelightCuttingRecipeBuilder()
            .withInput(HexBlocks.AKASHIC_LOG)
            .withTool(ingredients.axeStrip())
            .withOutput(HexBlocks.AKASHIC_LOG_STRIPPED)
            .withOutput("farmersdelight:tree_bark")
            .withSound(SoundEvents.AXE_STRIP)
            .withConditions()
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_log"))

        FarmersDelightCuttingRecipeBuilder()
            .withInput(HexBlocks.AKASHIC_WOOD)
            .withTool(ingredients.axeStrip())
            .withOutput(HexBlocks.AKASHIC_WOOD_STRIPPED)
            .withOutput("farmersdelight:tree_bark")
            .withSound(SoundEvents.AXE_STRIP)
            .withConditions()
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_wood"))

        FarmersDelightCuttingRecipeBuilder()
            .withInput(HexBlocks.AKASHIC_TRAPDOOR)
            .withTool(ingredients.axeDig())
            .withOutput(HexBlocks.AKASHIC_PLANKS)
            .withConditions()
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_trapdoor"))

        FarmersDelightCuttingRecipeBuilder()
            .withInput(HexBlocks.AKASHIC_DOOR)
            .withTool(ingredients.axeDig())
            .withOutput(HexBlocks.AKASHIC_PLANKS)
            .withConditions()
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_door"))
    }

    private fun wandRecipe(recipes: Consumer<FinishedRecipe>, wand: ItemWand, plank: Item) {
        ShapedRecipeBuilder.shaped(wand)
            .define('W', plank)
            .define('S', Items.STICK)
            .define('A', HexItems.CHARGED_AMETHYST)
            .pattern(" SA")
            .pattern(" WS")
            .pattern("S  ")
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST))
            .save(recipes)
    }

    private fun gayRecipe(recipes: Consumer<FinishedRecipe>, type: ItemPrideColorizer.Type, material: Ingredient) {
        val colorizer = HexItems.PRIDE_COLORIZERS[type]!!
        ShapedRecipeBuilder.shaped(colorizer)
            .define('D', HexItems.AMETHYST_DUST)
            .define('C', material)
            .pattern(" D ")
            .pattern("DCD")
            .pattern(" D ")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST))
            .save(recipes)
    }

    private fun specialRecipe(consumer: Consumer<FinishedRecipe>, serializer: SimpleRecipeSerializer<*>) {
        val name = Registry.RECIPE_SERIALIZER.getKey(serializer)
        SpecialRecipeBuilder.special(serializer).save(consumer, HexAPI.MOD_ID + ":dynamic/" + name!!.path)
    }

    private fun RecipeBuilder.withConditions(): IXplatConditionsBuilder = conditions(this)
}
