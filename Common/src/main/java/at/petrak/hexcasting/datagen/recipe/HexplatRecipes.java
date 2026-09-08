package at.petrak.hexcasting.datagen.recipe;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.blocks.decoration.BlockAkashicLog;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.pigment.ItemPridePigment;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.SealThingsRecipe;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.EntityTypeIngredient;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.VillagerIngredient;
import at.petrak.hexcasting.common.lib.HexStateIngredients;
import at.petrak.hexcasting.datagen.HexAdvancements;
import at.petrak.hexcasting.datagen.IXplatConditionsBuilder;
import at.petrak.hexcasting.datagen.IXplatIngredients;
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder;
import at.petrak.hexcasting.datagen.recipe.builders.CreateCrushingRecipeBuilder;
import at.petrak.hexcasting.datagen.recipe.builders.FarmersDelightCuttingRecipeBuilder;
import at.petrak.paucal.api.PaucalAPI;
import at.petrak.paucal.api.datagen.PaucalAdvancementSubProvider;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// TODO: need to do a big refactor of this class cause it's giant and unwieldy, probably as part of #360
public class HexplatRecipes extends RecipeProvider {
    private final IXplatIngredients ingredients;
    private final Function<RecipeBuilder, IXplatConditionsBuilder> conditions;

    private final List<BlockAkashicLog> EDIFIED_LOGS = List.of(
        HexBlocks.EDIFIED_LOG.get(), HexBlocks.EDIFIED_LOG_AMETHYST.get(),
        HexBlocks.EDIFIED_LOG_AVENTURINE.get(), HexBlocks.EDIFIED_LOG_CITRINE.get(),
        HexBlocks.EDIFIED_LOG_PURPLE.get());

    private final Map<BlockAkashicLog, BlockAkashicLog> EDIFIED_LOG_TO_WOOD = Map.ofEntries(
        Map.entry(HexBlocks.EDIFIED_LOG.get(), HexBlocks.EDIFIED_WOOD.get()),
//      These don't exist, idk if they should
//        Map.entry(HexBlocks.EDIFIED_LOG_AMETHYST.get(), HexBlocks.EDIFIED_WOOD_AMETHYST.get()),
//        Map.entry(HexBlocks.EDIFIED_LOG_AVENTURINE.get(), HexBlocks.EDIFIED_WOOD_AVENTURINE.get()),
//        Map.entry(HexBlocks.EDIFIED_LOG_CITRINE.get(), HexBlocks.EDIFIED_WOOD_CITRINE.get()),
//        Map.entry(HexBlocks.EDIFIED_LOG_PURPLE.get(), HexBlocks.EDIFIED_WOOD_PURPLE.get()),
        Map.entry(HexBlocks.STRIPPED_EDIFIED_LOG.get(), HexBlocks.STRIPPED_EDIFIED_WOOD.get())
    );

    public HexplatRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, IXplatIngredients ingredients,
                          Function<RecipeBuilder, IXplatConditionsBuilder> conditions) {
        super(output, registries);
        this.ingredients = ingredients;
        this.conditions = conditions;
    }

    @Override
    public void buildRecipes(RecipeOutput recipes) {
        specialRecipe(recipes, SealThingsRecipe.FOCUS_SERIALIZER, SealThingsRecipe::focus);
        specialRecipe(recipes, SealThingsRecipe.SPELLBOOK_SERIALIZER, SealThingsRecipe::spellbook);

        staffRecipe(recipes, HexItems.STAFF_OAK.get(), Items.OAK_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_BIRCH.get(), Items.BIRCH_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_SPRUCE.get(), Items.SPRUCE_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_JUNGLE.get(), Items.JUNGLE_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_DARK_OAK.get(), Items.DARK_OAK_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_ACACIA.get(), Items.ACACIA_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_CRIMSON.get(), Items.CRIMSON_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_WARPED.get(), Items.WARPED_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_MANGROVE.get(), Items.MANGROVE_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_CHERRY.get(), Items.CHERRY_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_BAMBOO.get(), Items.BAMBOO_PLANKS);
        staffRecipe(recipes, HexItems.STAFF_EDIFIED.get(), HexBlocks.EDIFIED_PLANKS.get().asItem());
        staffRecipe(recipes, HexItems.STAFF_QUENCHED.get(), HexItems.QUENCHED_SHARD.get());
        staffRecipe(recipes, HexItems.STAFF_MINDSPLICE.get(), Ingredient.of(HexTags.Items.MINDFLAYED_CIRCLE_COMPONENTS));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, HexItems.STAFF_NEURAL.get())
            .define('F', HexItems.NEURAL_FIBER.get())
            .define('B', Items.BONE)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .pattern(" FA")
            .pattern(" BF")
            .pattern("B  ")
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST.get()))
            .save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, HexItems.THOUGHT_KNOT.get())
            .requires(HexItems.AMETHYST_DUST.get())
            .requires(Items.STRING)
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
            .save(recipes);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, HexItems.FOCUS.get())
            .define('G', ingredients.glowstoneDust())
            .define('L', ingredients.leather())
            .define('P', Items.PAPER)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .pattern("GLG")
            .pattern("PAP")
            .pattern("GLG")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
            .save(recipes);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, HexItems.FOCUS.get())
            .define('G', ingredients.glowstoneDust())
            .define('L', ingredients.leather())
            .define('P', Items.PAPER)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .pattern("GPG")
            .pattern("LAL")
            .pattern("GPG")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES))
            .save(recipes, modLoc("focus_rotated"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, HexItems.SPELLBOOK.get())
            .define('N', ingredients.goldNugget())
            .define('B', Items.WRITABLE_BOOK)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .define('F', Items.CHORUS_FRUIT) // i wanna gate this behind the end SOMEHOW
            // hey look its my gender ^^
            .pattern("NBA")
            .pattern("NFA")
            .pattern("NBA")
            .unlockedBy("has_focus", hasItem(HexItems.FOCUS.get()))
            .unlockedBy("has_chorus", hasItem(Items.CHORUS_FRUIT)).save(recipes);

        ringCornerless(RecipeCategory.TOOLS,
            HexItems.CYPHER.get(), 1,
            ingredients.copperIngot(),
            Ingredient.of(HexItems.AMETHYST_DUST.get()))
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ringCornerless(RecipeCategory.TOOLS,
            HexItems.TRINKET.get(), 1,
            ingredients.ironIngot(),
            Ingredient.of(Items.AMETHYST_SHARD))
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, HexItems.ARTIFACT.get())
            .define('F', ingredients.goldIngot())
            .define('A', HexItems.CHARGED_AMETHYST.get())
            // 1.21 removed the vanilla music discs tag so we need to use this instead
            .define('D', TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c","music_discs")))
            .pattern(" F ")
            .pattern("FAF")
            .pattern(" D ")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ringCornerless(RecipeCategory.TOOLS, HexItems.SCRYING_LENS.get(), 1, Items.GLASS, HexItems.AMETHYST_DUST.get())
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, HexItems.ABACUS.get())
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('W', ItemTags.PLANKS)
            .pattern("WAW")
            .pattern("SAS")
            .pattern("WAW")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        // Why am I like this
        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, HexItems.SUBMARINE_SANDWICH.get())
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('C', Items.COOKED_BEEF)
            .define('B', Items.BREAD)
            .pattern(" SA")
            .pattern(" C ")
            .pattern(" B ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);

        for (var dye : DyeColor.values()) {
            var item = HexItems.DYE_PIGMENTS.get(dye);
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, item.get())
                .define('D', HexItems.AMETHYST_DUST.get())
                .define('C', DyeItem.byColor(dye))
                .pattern(" D ")
                .pattern("DCD")
                .pattern(" D ")
                .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST.get())).save(recipes);
        }

        gayRecipe(recipes, ItemPridePigment.Type.AGENDER, Ingredient.of(Items.GLASS));
        gayRecipe(recipes, ItemPridePigment.Type.AROACE, Ingredient.of(Items.WHEAT_SEEDS));
        gayRecipe(recipes, ItemPridePigment.Type.AROMANTIC, Ingredient.of(Items.ARROW));
        gayRecipe(recipes, ItemPridePigment.Type.ASEXUAL, Ingredient.of(Items.BREAD));
        gayRecipe(recipes, ItemPridePigment.Type.BISEXUAL, Ingredient.of(Items.WHEAT));
        gayRecipe(recipes, ItemPridePigment.Type.DEMIBOY, Ingredient.of(Items.RAW_IRON));
        gayRecipe(recipes, ItemPridePigment.Type.DEMIGIRL, Ingredient.of(Items.RAW_COPPER));
        gayRecipe(recipes, ItemPridePigment.Type.GAY, Ingredient.of(Items.STONE_BRICK_WALL));
        gayRecipe(recipes, ItemPridePigment.Type.GENDERFLUID, Ingredient.of(Items.WATER_BUCKET));
        gayRecipe(recipes, ItemPridePigment.Type.GENDERQUEER, Ingredient.of(Items.GLASS_BOTTLE));
        gayRecipe(recipes, ItemPridePigment.Type.INTERSEX, Ingredient.of(Items.AZALEA));
        gayRecipe(recipes, ItemPridePigment.Type.LESBIAN, Ingredient.of(Items.HONEYCOMB));
        gayRecipe(recipes, ItemPridePigment.Type.NONBINARY, Ingredient.of(Items.MOSS_BLOCK));
        gayRecipe(recipes, ItemPridePigment.Type.PANSEXUAL, HexTags.Items.PANS, Ingredient.of(Items.CARROT));
        gayRecipe(recipes, ItemPridePigment.Type.PLURAL, Ingredient.of(Items.REPEATER));
        gayRecipe(recipes, ItemPridePigment.Type.TRANSGENDER, Ingredient.of(Items.EGG));

        ring(RecipeCategory.MISC, HexItems.UUID_PIGMENT.get(), 1, HexItems.AMETHYST_DUST.get(), Items.AMETHYST_SHARD)
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST.get())).save(recipes);
        ringCornerless(RecipeCategory.MISC, HexItems.DEFAULT_PIGMENT.get(), 1, HexItems.AMETHYST_DUST.get(), Items.AMETHYST_SHARD)
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST.get())).save(recipes);
        ringCornerless(RecipeCategory.MISC, HexItems.ANCIENT_PIGMENT.get(), 1, HexItems.AMETHYST_DUST.get(), Items.COPPER_INGOT)
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST.get())).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, HexItems.SCROLL_SMOL.get())
            .define('P', Items.PAPER)
            .define('A', HexItems.AMETHYST_DUST.get())
            .pattern(" A")
            .pattern("P ")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, HexItems.SCROLL_MEDIUM.get())
            .define('P', Items.PAPER)
            .define('A', HexItems.AMETHYST_DUST.get())
            .pattern("  A")
            .pattern("PP ")
            .pattern("PP ")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, HexItems.SCROLL_LARGE.get())
            .define('P', Items.PAPER)
            .define('A', HexItems.AMETHYST_DUST.get())
            .pattern("PPA")
            .pattern("PPP")
            .pattern("PPP")
            .unlockedBy("has_item", hasItem(HexTags.Items.STAVES)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, HexItems.SLATE.get(), 6)
            .define('S', Items.DEEPSLATE)
            .define('A', HexItems.AMETHYST_DUST.get())
            .pattern(" A ")
            .pattern("SSS")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST.get())).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, HexItems.JEWELER_HAMMER.get())
            .define('I', ingredients.ironIngot())
            .define('N', ingredients.ironNugget())
            .define('A', Items.AMETHYST_SHARD)
            .define('S', ingredients.stick())
            .pattern("IAN")
            .pattern(" S ")
            .pattern(" S ")
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, HexItems.ROBES_HOOD.get())
            .define('F', HexItems.NEURAL_FIBER.get())
            .define('B', Items.BONE)
            .pattern("BFB")
            .pattern("F F")
            .unlockedBy("has_item", hasItem(HexItems.NEURAL_FIBER.get())).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, HexItems.ROBES_TUNIC.get())
            .define('F', HexItems.NEURAL_FIBER.get())
            .pattern("F F")
            .pattern("FFF")
            .pattern("FFF")
            .unlockedBy("has_item", hasItem(HexItems.NEURAL_FIBER.get())).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, HexItems.ROBES_LEGS.get())
            .define('F', HexItems.NEURAL_FIBER.get())
            .pattern("FFF")
            .pattern("F F")
            .pattern("F F")
            .unlockedBy("has_item", hasItem(HexItems.NEURAL_FIBER.get())).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, HexItems.ROBES_BOOTS.get())
            .define('F', HexItems.NEURAL_FIBER.get())
            .define('B', Items.BONE)
            .pattern("   ")
            .pattern("B B")
            .pattern("F F")
            .unlockedBy("has_item", hasItem(HexItems.NEURAL_FIBER.get())).save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, HexItems.AMETHYST_DUST.get(),
                (int) (MediaConstants.QUENCHED_SHARD_UNIT / MediaConstants.DUST_UNIT) + 1)
            .requires(HexItems.QUENCHED_SHARD.get())
            .requires(HexItems.AMETHYST_DUST.get())
            .unlockedBy("has_item", hasItem(HexItems.QUENCHED_SHARD.get()))
            .save(recipes, modLoc("decompose_quenched_shard/dust"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.AMETHYST_SHARD,
                (int) (MediaConstants.QUENCHED_SHARD_UNIT / MediaConstants.SHARD_UNIT) + 1)
            .requires(HexItems.QUENCHED_SHARD.get())
            .requires(Items.AMETHYST_SHARD)
            .unlockedBy("has_item", hasItem(HexItems.QUENCHED_SHARD.get()))
            .save(recipes, modLoc("decompose_quenched_shard/shard"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, HexItems.CHARGED_AMETHYST.get(),
                (int) (MediaConstants.QUENCHED_SHARD_UNIT / MediaConstants.CRYSTAL_UNIT) + 1)
            .requires(HexItems.QUENCHED_SHARD.get())
            .requires(HexItems.CHARGED_AMETHYST.get())
            .unlockedBy("has_item", hasItem(HexItems.QUENCHED_SHARD.get()))
            .save(recipes, modLoc("decompose_quenched_shard/charged"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HexBlocks.SLATE_BLOCK.get())
            .define('S', HexItems.SLATE.get())
            .pattern("S")
            .pattern("S")
            .unlockedBy("has_item", hasItem(HexItems.SLATE.get()))
            .save(recipes, modLoc("slate_block_from_slates"));

        ringAll(RecipeCategory.BUILDING_BLOCKS, HexBlocks.SLATE_BLOCK.get(), 8, Blocks.DEEPSLATE, HexItems.AMETHYST_DUST.get())
            .unlockedBy("has_item", hasItem(HexItems.SLATE.get())).save(recipes);

        packing(RecipeCategory.BUILDING_BLOCKS, HexItems.AMETHYST_DUST.get(), HexBlocks.AMETHYST_DUST_BLOCK.get().asItem(), "amethyst_dust",
            false, recipes);

        ringAll(RecipeCategory.BUILDING_BLOCKS, HexBlocks.SCROLL_PAPER.get(), 8, Items.PAPER, Items.AMETHYST_SHARD)
            .unlockedBy("has_item", hasItem(Items.AMETHYST_SHARD)).save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, HexBlocks.ANCIENT_SCROLL_PAPER.get(), 8)
            .requires(ingredients.dyes().get(DyeColor.BROWN))
            .requires(HexBlocks.SCROLL_PAPER.get(), 8)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER.get())).save(recipes);

        stack(RecipeCategory.DECORATIONS, HexBlocks.SCROLL_PAPER_LANTERN.get(), 1, HexBlocks.SCROLL_PAPER.get(), Items.TORCH)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER.get())).save(recipes);

        stack(RecipeCategory.DECORATIONS, HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN.get(), 1, HexBlocks.ANCIENT_SCROLL_PAPER.get(), Items.TORCH)
            .unlockedBy("has_item", hasItem(HexBlocks.ANCIENT_SCROLL_PAPER.get())).save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN.get(), 8)
            .requires(ingredients.dyes().get(DyeColor.BROWN))
            .requires(HexBlocks.SCROLL_PAPER_LANTERN.get(), 8)
            .unlockedBy("has_item", hasItem(HexBlocks.SCROLL_PAPER_LANTERN.get()))
            .save(recipes, modLoc("ageing_scroll_paper_lantern"));

        stack(RecipeCategory.DECORATIONS, HexBlocks.SCONCE.get(), 4,
            Ingredient.of(HexItems.CHARGED_AMETHYST.get()),
            ingredients.copperIngot())
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST.get())).save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, HexBlocks.EDIFIED_PLANKS.get(), 4)
            .requires(HexTags.Items.EDIFIED_LOGS)
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_LOGS)).save(recipes);

        for (var entry : EDIFIED_LOG_TO_WOOD.entrySet()) {
            var log = entry.getKey();
            var wood = entry.getValue();
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood, 3)
                    .define('W', log)
                    .pattern("WW")
                    .pattern("WW")
                    .unlockedBy("has_item", hasItem(log)).save(recipes);
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HexBlocks.EDIFIED_PANEL.get(), 9)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WWW")
            .pattern("WWW")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HexBlocks.EDIFIED_TILE.get(), 6)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WW ")
            .pattern("W W")
            .pattern(" WW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexBlocks.EDIFIED_DOOR.get(), 3)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WW")
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexBlocks.EDIFIED_TRAPDOOR.get(), 2)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WWW")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HexBlocks.EDIFIED_STAIRS.get(), 4)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("W  ")
            .pattern("WW ")
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HexBlocks.EDIFIED_FENCE.get(), 3)
                .define('W', HexTags.Items.EDIFIED_PLANKS)
                .define('S', Items.STICK)
                .pattern("WSW")
                .pattern("WSW")
                .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HexBlocks.EDIFIED_FENCE_GATE.get(), 1)
                .define('W', HexTags.Items.EDIFIED_PLANKS)
                .define('S', Items.STICK)
                .pattern("SWS")
                .pattern("SWS")
                .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);


        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, HexBlocks.EDIFIED_SLAB.get(), 6)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WWW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexBlocks.EDIFIED_PRESSURE_PLATE.get(), 1)
            .define('W', HexTags.Items.EDIFIED_PLANKS)
            .pattern("WW")
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, HexBlocks.EDIFIED_BUTTON.get())
            .requires(HexTags.Items.EDIFIED_PLANKS)
            .unlockedBy("has_item", hasItem(HexTags.Items.EDIFIED_PLANKS)).save(recipes);

        var enlightenment = HexAdvancements.ENLIGHTEN;
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexBlocks.IMPETUS_EMPTY.get())
            .define('B', Items.IRON_BARS)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .define('S', HexBlocks.SLATE_BLOCK.get())
            .define('P', Items.PURPUR_BLOCK)
            .pattern("PSS")
            .pattern("BAB")
            .pattern("SSP")
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexBlocks.EMPTY_DIRECTRIX.get())
            .define('C', Items.COMPARATOR)
            .define('O', Items.OBSERVER)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .define('S', HexBlocks.SLATE_BLOCK.get())
            .pattern("CSS")
            .pattern("OAO")
            .pattern("SSC")
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexBlocks.AKASHIC_BOOKSHELF.get())
            .define('L', HexTags.Items.EDIFIED_LOGS)
            .define('P', HexTags.Items.EDIFIED_PLANKS)
            .define('C', Items.BOOK)
            /*this is the*/.pattern("LPL") // and what i have for you today is
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment)).save(recipes);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HexBlocks.AKASHIC_LIGATURE.get(), 4)
            .define('L', HexTags.Items.EDIFIED_LOGS)
            .define('P', HexTags.Items.EDIFIED_PLANKS)
            .define('1', HexItems.AMETHYST_DUST.get())
            .define('2', Items.AMETHYST_SHARD)
            .define('3', HexItems.CHARGED_AMETHYST.get())
            .pattern("LPL")
            .pattern("123")
            .pattern("LPL")
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment)).save(recipes);

        // Stone sets
        stoneSet(recipes, HexBlocks.SLATE_BLOCK.get().asItem(), HexBlocks.SLATE_BRICKS.get().asItem(), HexBlocks.SLATE_BRICKS_SMALL.get().asItem(), HexBlocks.SLATE_TILES.get().asItem(), HexBlocks.SLATE_PILLAR.get().asItem());
        stoneSet(recipes, Blocks.AMETHYST_BLOCK.asItem(), HexBlocks.AMETHYST_BRICKS.get().asItem(), HexBlocks.AMETHYST_BRICKS_SMALL.get().asItem(), HexBlocks.AMETHYST_TILES.get().asItem(), HexBlocks.AMETHYST_PILLAR.get().asItem());
        stoneSet(recipes, HexBlocks.QUENCHED_ALLAY.get().asItem(), HexBlocks.QUENCHED_ALLAY_BRICKS.get().asItem(), HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get().asItem(), HexBlocks.QUENCHED_ALLAY_TILES.get().asItem(), null);

        // Stone sets in stonecutter
        stoneCutterFromTag(recipes, HexTags.Items.SLATE_BLOCKS, HexBlocks.SLATE_BRICKS.get().asItem(), HexBlocks.SLATE_BRICKS_SMALL.get().asItem(), HexBlocks.SLATE_TILES.get().asItem(), HexBlocks.SLATE_PILLAR.get().asItem());
        stoneCutterFromTag(recipes, HexTags.Items.AMETHYST_BLOCKS, HexBlocks.AMETHYST_BRICKS.get().asItem(), HexBlocks.AMETHYST_BRICKS_SMALL.get().asItem(), HexBlocks.AMETHYST_TILES.get().asItem(), HexBlocks.AMETHYST_PILLAR.get().asItem());
        stoneCutterFromTag(recipes, HexTags.Items.QUENCHED_ALLAY_BLOCKS, HexBlocks.QUENCHED_ALLAY_BRICKS.get().asItem(), HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get().asItem(), HexBlocks.QUENCHED_ALLAY_TILES.get().asItem());

        // Slate & Amethyst block set
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, HexBlocks.SLATE_AMETHYST_BRICKS.get().asItem(), 2)
                .requires(HexBlocks.SLATE_BRICKS.get())
                .requires(HexBlocks.AMETHYST_BRICKS.get())
                .unlockedBy("has_item", has(HexBlocks.SLATE.get())).save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, HexBlocks.SLATE_AMETHYST_BRICKS_SMALL.get().asItem(), 2)
                .requires(HexBlocks.SLATE_BRICKS_SMALL.get())
                .requires(HexBlocks.AMETHYST_BRICKS_SMALL.get())
                .unlockedBy("has_item", has(HexBlocks.SLATE.get())).save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, HexBlocks.SLATE_AMETHYST_TILES.get().asItem(), 2)
                .requires(HexBlocks.SLATE_TILES.get())
                .requires(HexBlocks.AMETHYST_TILES.get())
                .unlockedBy("has_item", has(HexBlocks.SLATE.get())).save(recipes);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, HexBlocks.SLATE_AMETHYST_PILLAR.get().asItem(), 2)
                .requires(HexBlocks.SLATE_PILLAR.get())
                .requires(HexBlocks.AMETHYST_PILLAR.get())
                .unlockedBy("has_item", has(HexBlocks.SLATE.get())).save(recipes);

        new BrainsweepRecipeBuilder(HexStateIngredients.of(Blocks.AMETHYST_BLOCK),
            new VillagerIngredient(null, null, 3),
            Blocks.BUDDING_AMETHYST.defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment))
            .save(recipes, modLoc("budding_amethyst"));

        new BrainsweepRecipeBuilder(HexStateIngredients.of(HexBlocks.IMPETUS_EMPTY.get()),
            new VillagerIngredient(VillagerProfession.TOOLSMITH, null, 2),
            HexBlocks.IMPETUS_RIGHTCLICK.get().defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment))
            .save(recipes, modLoc("impetus_rightclick"));

        new BrainsweepRecipeBuilder(HexStateIngredients.of(HexBlocks.IMPETUS_EMPTY.get()),
            new VillagerIngredient(VillagerProfession.FLETCHER, null, 2),
            HexBlocks.IMPETUS_LOOK.get().defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment))
            .save(recipes, modLoc("impetus_look"));

        new BrainsweepRecipeBuilder(HexStateIngredients.of(HexBlocks.IMPETUS_EMPTY.get()),
            new VillagerIngredient(VillagerProfession.CLERIC, null, 2),
            HexBlocks.IMPETUS_REDSTONE.get().defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment))
            .save(recipes, modLoc("impetus_storedplayer"));

        new BrainsweepRecipeBuilder(HexStateIngredients.of(HexBlocks.EMPTY_DIRECTRIX.get()),
            new VillagerIngredient(VillagerProfession.MASON, null, 1),
            HexBlocks.DIRECTRIX_REDSTONE.get().defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment))
            .save(recipes, modLoc("directrix_redstone"));

        new BrainsweepRecipeBuilder(HexStateIngredients.of(HexBlocks.EMPTY_DIRECTRIX.get()),
                new VillagerIngredient(VillagerProfession.SHEPHERD, null, 1),
                HexBlocks.DIRECTRIX_BOOLEAN.get().defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
                .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment))
                .save(recipes, modLoc("directrix_boolean"));

        new BrainsweepRecipeBuilder(HexStateIngredients.of(HexBlocks.AKASHIC_LIGATURE.get()),
            new VillagerIngredient(VillagerProfession.LIBRARIAN, null, 5),
            HexBlocks.AKASHIC_RECORD.get().defaultBlockState(), MediaConstants.CRYSTAL_UNIT * 10)
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment))
            .save(recipes, modLoc("akashic_record"));

        // Temporary tests
        new BrainsweepRecipeBuilder(HexStateIngredients.of(Blocks.AMETHYST_BLOCK),
            new EntityTypeIngredient(EntityType.ALLAY),
            HexBlocks.QUENCHED_ALLAY.get().defaultBlockState(), MediaConstants.CRYSTAL_UNIT)
            .unlockedBy("enlightenment", new Criterion<>(HexAdvancementTriggers.OVERCAST_TRIGGER.get(), enlightenment))
            .save(recipes, modLoc("quench_allay"));

        // Create compat; will need to be Neo only as Create 1.21 will not exist on Fabric
        /**
        this.conditions.apply(new CreateCrushingRecipeBuilder()
                .withInput(Blocks.AMETHYST_CLUSTER)
                .duration(150)
                .withOutput(Items.AMETHYST_SHARD, 7)
                .withOutput(HexItems.AMETHYST_DUST.get(), 5)
                .withOutput(0.25f, HexItems.CHARGED_AMETHYST.get()))
            .whenModLoaded("create")
            .save(recipes, ResourceLocation.fromNamespaceAndPath("create", "crushing/amethyst_cluster"));

        this.conditions.apply(new CreateCrushingRecipeBuilder()
                .withInput(Blocks.AMETHYST_BLOCK)
                .duration(150)
                .withOutput(Items.AMETHYST_SHARD, 3)
                .withOutput(0.5f, HexItems.AMETHYST_DUST.get(), 4))
            .whenModLoaded("create")
            .save(recipes, ResourceLocation.fromNamespaceAndPath("create", "crushing/amethyst_block"));

        this.conditions.apply(new CreateCrushingRecipeBuilder()
                .withInput(Items.AMETHYST_SHARD)
                .duration(150)
                .withOutput(HexItems.AMETHYST_DUST.get(), 4)
                .withOutput(0.5f, HexItems.AMETHYST_DUST.get()))
            .whenModLoaded("create")
            .save(recipes, modLoc("compat/create/crushing/amethyst_shard"));
         */

        // FD compat
        for (var log : EDIFIED_LOGS) {
            this.conditions.apply(new FarmersDelightCuttingRecipeBuilder()
                    .withInput(log)
                    .withTool(ingredients.axeStrip())
                    .withOutput(HexBlocks.STRIPPED_EDIFIED_LOG.get())
                    .withOutput("farmersdelight:tree_bark")
                    .withSound(SoundEvents.AXE_STRIP))
                .whenModLoaded("farmersdelight")
                .save(recipes, modLoc("compat/farmersdelight/cutting/" + BuiltInRegistries.BLOCK.getKey(log).getPath()));
        }

        this.conditions.apply(new FarmersDelightCuttingRecipeBuilder()
                .withInput(HexBlocks.EDIFIED_WOOD.get())
                .withTool(ingredients.axeStrip())
                .withOutput(HexBlocks.STRIPPED_EDIFIED_WOOD.get())
                .withOutput("farmersdelight:tree_bark")
                .withSound(SoundEvents.AXE_STRIP))
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_wood"));

        this.conditions.apply(new FarmersDelightCuttingRecipeBuilder()
                .withInput(HexBlocks.EDIFIED_TRAPDOOR.get())
                .withTool(ingredients.axeDig())
                .withOutput(HexBlocks.EDIFIED_PLANKS.get()))
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_trapdoor"));

        this.conditions.apply(new FarmersDelightCuttingRecipeBuilder()
                .withInput(HexBlocks.EDIFIED_DOOR.get())
                .withTool(ingredients.axeDig())
                .withOutput(HexBlocks.EDIFIED_PLANKS.get()))
            .whenModLoaded("farmersdelight")
            .save(recipes, modLoc("compat/farmersdelight/cutting/akashic_door"));
    }

    private void staffRecipe(RecipeOutput recipes, ItemStaff staff, Item plank) {
        staffRecipe(recipes, staff, Ingredient.of(plank));
    }

    private void staffRecipe(RecipeOutput recipes, ItemStaff staff, Ingredient plank) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, staff)
            .define('W', plank)
            .define('S', Items.STICK)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .pattern(" SA")
            .pattern(" WS")
            .pattern("S  ")
            .unlockedBy("has_item", hasItem(HexItems.CHARGED_AMETHYST.get()))
            .save(recipes);
    }

    private void gayRecipe(RecipeOutput recipes, ItemPridePigment.Type type, Ingredient material) {
        var colorizer = HexItems.PRIDE_PIGMENTS.get(type);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, colorizer.get())
            .define('D', HexItems.AMETHYST_DUST.get())
            .define('C', material)
            .pattern(" D ")
            .pattern("DCD")
            .pattern(" D ")
            .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST.get()))
            .save(recipes);
    }

    private void gayRecipe(RecipeOutput recipes, ItemPridePigment.Type type, TagKey<Item> mainMaterial, Ingredient fallback) {
        var colorizer = HexItems.PRIDE_PIGMENTS.get(type);
        var defaultLocation = BuiltInRegistries.ITEM.getKey(colorizer.get());

        ShapedRecipeBuilder mainRecipe = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, colorizer.get())
                .define('D', HexItems.AMETHYST_DUST.get())
                .define('C', mainMaterial)
                .pattern(" D ")
                .pattern("DCD")
                .pattern(" D ")
                .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST.get()));
        conditions.apply(mainRecipe)
                .whenTagPopulated(mainMaterial)
                .save(recipes);

        ShapedRecipeBuilder fallbackRecipe = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, colorizer.get())
                .define('D', HexItems.AMETHYST_DUST.get())
                .define('C', fallback)
                .pattern(" D ")
                .pattern("DCD")
                .pattern(" D ")
                .unlockedBy("has_item", hasItem(HexItems.AMETHYST_DUST.get()));
        conditions.apply(fallbackRecipe)
                .whenTagEmpty(mainMaterial)
                .save(recipes, defaultLocation.withPath(original -> original + "_fallback"));
    }

    private <T extends Recipe<?>> void specialRecipe(RecipeOutput consumer, RecipeSerializer<T> serializer, Function<CraftingBookCategory, T> recipeFunc) {
        var name = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
        SpecialRecipeBuilder.special(recipeFunc::apply).save(consumer, HexAPI.MOD_ID + ":dynamic" + name.getPath());
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike itemLike) {
        return paucalInventoryTrigger(ItemPredicate.Builder.item().of(itemLike).build());
    }

    protected static Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(TagKey<Item> itemLike) {
        return paucalInventoryTrigger(ItemPredicate.Builder.item().of(itemLike).build());
    }

    /**
     * Prefixed with {@code paucal} to avoid collisions when Forge ATs {@link RecipeProvider#inventoryTrigger}.
     */
    protected static Criterion<InventoryChangeTrigger.TriggerInstance> paucalInventoryTrigger(ItemPredicate... $$0) {

        return new Criterion<>(
                CriteriaTriggers.INVENTORY_CHANGED,
                new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of($$0))
        );
    }

    // ================================= From PAUCAL 1.20

    protected ShapedRecipeBuilder ring(RecipeCategory category, ItemLike out, int count, Ingredient outer, @Nullable Ingredient inner) {
        return ringCornered(category, out, count, outer, outer, inner);
    }

    protected ShapedRecipeBuilder ring(RecipeCategory category, ItemLike out, int count, ItemLike outer, @Nullable ItemLike inner) {
        return ring(category, out, count, Ingredient.of(outer), ingredientOf(inner));
    }

    protected ShapedRecipeBuilder ring(RecipeCategory category, ItemLike out, int count, TagKey<Item> outer, @Nullable TagKey<Item> inner) {
        return ring(category, out, count, Ingredient.of(outer), ingredientOf(inner));
    }

    protected ShapedRecipeBuilder ringCornerless(RecipeCategory category, ItemLike out, int count, Ingredient outer,
                                                 @Nullable Ingredient inner) {
        return ringCornered(category, out, count, outer, null, inner);
    }

    protected ShapedRecipeBuilder ringCornerless(RecipeCategory category, ItemLike out, int count, ItemLike outer, @Nullable ItemLike inner) {
        return ringCornerless(category, out, count, Ingredient.of(outer), ingredientOf(inner));
    }

    protected ShapedRecipeBuilder ringAll(RecipeCategory category, ItemLike out, int count, Ingredient outer, @Nullable Ingredient inner) {
        return ringCornered(category, out, count, outer, outer, inner);
    }

    protected ShapedRecipeBuilder ringAll(RecipeCategory category, ItemLike out, int count, ItemLike outer, @Nullable ItemLike inner) {
        return ringAll(category, out, count, Ingredient.of(outer), ingredientOf(inner));
    }

    protected ShapedRecipeBuilder ringCornered(RecipeCategory category, ItemLike out, int count, @Nullable Ingredient cardinal,
                                               @Nullable Ingredient diagonal, @Nullable Ingredient inner) {
        if (cardinal == null && diagonal == null && inner == null) {
            throw new IllegalArgumentException("at least one ingredient must be non-null");
        }
        if (inner != null && cardinal == null && diagonal == null) {
            throw new IllegalArgumentException("if inner is non-null, either cardinal or diagonal must not be");
        }

        var builder = ShapedRecipeBuilder.shaped(category, out, count);
        var C = ' ';
        if (cardinal != null) {
            builder.define('C', cardinal);
            C = 'C';
        }
        var D = ' ';
        if (diagonal != null) {
            builder.define('D', diagonal);
            D = 'D';
        }
        var I = ' ';
        if (inner != null) {
            builder.define('I', inner);
            I = 'I';
        }

        builder
                .pattern(String.format("%c%c%c", D, C, D))
                .pattern(String.format("%c%c%c", C, I, C))
                .pattern(String.format("%c%c%c", D, C, D));

        return builder;
    }

    protected ShapedRecipeBuilder stack(RecipeCategory category, ItemLike out, int count, Ingredient top, Ingredient bottom) {
        return ShapedRecipeBuilder.shaped(category, out, count)
                .define('T', top)
                .define('B', bottom)
                .pattern("T")
                .pattern("B");
    }

    protected ShapedRecipeBuilder stack(RecipeCategory category, ItemLike out, int count, ItemLike top, ItemLike bottom) {
        return stack(category, out, count, Ingredient.of(top), Ingredient.of(bottom));
    }

    protected ShapedRecipeBuilder stack(RecipeCategory category, ItemLike out, int count, TagKey<Item> top, TagKey<Item> bottom) {
        return stack(category, out, count, Ingredient.of(top), Ingredient.of(bottom));
    }


    protected ShapedRecipeBuilder stick(RecipeCategory category, ItemLike out, int count, Ingredient input) {
        return stack(category, out, count, input, input);
    }

    protected ShapedRecipeBuilder stick(RecipeCategory category, ItemLike out, int count, ItemLike input) {
        return stick(category, out, count, Ingredient.of(input));
    }

    protected ShapedRecipeBuilder stick(RecipeCategory category, ItemLike out, int count, TagKey<Item> input) {
        return stick(category, out, count, Ingredient.of(input));
    }

    /**
     * @param largeSize True for a 3x3, false for a 2x2
     */
    protected void packing(RecipeCategory category, ItemLike free, ItemLike compressed, String freeName, boolean largeSize, RecipeOutput recipes) {
        var pack = ShapedRecipeBuilder.shaped(category, compressed)
                .define('X', free);
        if (largeSize) {
            pack.pattern("XXX").pattern("XXX").pattern("XXX");
        } else {
            pack.pattern("XX").pattern("XX");
        }
        pack.unlockedBy("has_item", hasItem(free)).save(recipes, modLoc(freeName + "_packing"));

        ShapelessRecipeBuilder.shapeless(category, free, largeSize ? 9 : 4)
                .requires(compressed)
                .unlockedBy("has_item", hasItem(free)).save(recipes, modLoc(freeName + "_unpacking"));
    }

    @Nullable
    protected Ingredient ingredientOf(@Nullable ItemLike item) {
        return item == null ? null : Ingredient.of(item);
    }

    @Nullable
    protected Ingredient ingredientOf(@Nullable TagKey<Item> item) {
        return item == null ? null : Ingredient.of(item);
    }

    private void stoneSet(RecipeOutput recipes, Item base, Item bricks, Item smallBricks, Item tiles, @Nullable Item pillar) {
        String smallBricksPath = BuiltInRegistries.ITEM.getKey(smallBricks).getPath();
        String bricksPath = BuiltInRegistries.ITEM.getKey(bricks).getPath();
        // Bricks from base block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, bricks, 4)
                .define('#', base)
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_item", hasItem(base))
                .save(recipes);

        // Bricks from small bricks
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, bricks)
                .requires(smallBricks)
                .unlockedBy("has_item", hasItem(base))
                .save(recipes, modLoc(bricksPath + "_from_" + smallBricksPath));

        // Small bricks from bricks
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, smallBricks)
                .requires(bricks)
                .unlockedBy("has_item", hasItem(base))
                .save(recipes, modLoc(smallBricksPath + "_from_" + bricksPath));

        // Tiles from bricks
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, tiles, 4)
                .define('#', bricks)
                .pattern("##")
                .pattern("##")
                .unlockedBy("has_item", hasItem(base))
                .save(recipes);

        // Pillar from base block
        if (pillar != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pillar, 2)
                    .define('#', base)
                    .pattern("#")
                    .pattern("#")
                    .unlockedBy("has_item", hasItem(base))
                    .save(recipes);
        }
    }

    private void stoneCutterFromTag(RecipeOutput recipes, TagKey<Item> tagKey, Item ...results) {
        for (Item result : results) {
            var resultPath = BuiltInRegistries.ITEM.getKey(result).getPath();
            SingleItemRecipeBuilder.stonecutting(Ingredient.of(tagKey), RecipeCategory.BUILDING_BLOCKS, result)
                    .unlockedBy("has_item", hasItem(tagKey))
                    .save(recipes, modLoc("stonecutting/" + resultPath));
        }
    }
}
