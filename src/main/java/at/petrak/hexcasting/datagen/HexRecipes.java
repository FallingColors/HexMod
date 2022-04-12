package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.advancements.OvercastTrigger;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.items.HexItemTags;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.recipe.SealFocusRecipe;
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper;
import at.petrak.hexcasting.common.recipe.ingredient.VillagerIngredient;
import at.petrak.hexcasting.datagen.recipebuilders.BrainsweepRecipeBuilder;
import at.petrak.paucal.api.datagen.PaucalRecipeProvider;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Registry;
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

import java.util.function.Consumer;

import static at.petrak.hexcasting.common.lib.RegisterHelper.prefix;

public class HexRecipes extends PaucalRecipeProvider {
    public HexRecipes(DataGenerator pGenerator) {
        super(pGenerator, HexMod.MOD_ID);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipes) {
        specialRecipe(recipes, SealFocusRecipe.SERIALIZER);

        ShapedRecipeBuilder.shaped(HexItems.WAND.get())
            .define('L', Tags.Items.LEATHER)
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .pattern(" LA")
            .pattern("LSL")
            .pattern("SL ")
            .unlockedBy("has_item", has(Items.AMETHYST_SHARD))
            .save(recipes);

        ringCornered(HexItems.FOCUS.get(), 1, Ingredient.of(Tags.Items.DUSTS_GLOWSTONE),
            Ingredient.of(Tags.Items.LEATHER), Ingredient.of(HexItems.CHARGED_AMETHYST.get()))
            .unlockedBy("has_item", has(HexItems.WAND.get()))
            .save(recipes);

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

        ringCornerless(HexItems.CYPHER.get(), 1,
            Ingredient.of(Tags.Items.INGOTS_COPPER),
            Ingredient.of(HexItems.AMETHYST_DUST.get()))
            .unlockedBy("has_item", has(HexItems.WAND.get())).save(recipes);

        ringCornerless(HexItems.TRINKET.get(), 1,
            Ingredient.of(Tags.Items.INGOTS_IRON),
            Ingredient.of(Items.AMETHYST_SHARD))
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

        ring(HexItems.SCRYING_LENS.get(), 1, Items.GLASS, HexItems.AMETHYST_DUST.get())
            .unlockedBy("has_item", has(HexItems.WAND.get())).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.ABACUS.get())
            .define('S', Items.STICK)
            .define('A', Items.AMETHYST_SHARD)
            .define('W', ItemTags.PLANKS)
            .pattern("WAW")
            .pattern("SAS")
            .pattern("WAW")
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

        for (var dye : DyeColor.values()) {
            var item = HexItems.DYE_COLORIZERS.get(dye).get();
            ShapedRecipeBuilder.shaped(item)
                .define('B', Items.BOWL)
                .define('D', HexItems.AMETHYST_DUST.get())
                .define('C', DyeItem.byColor(dye))
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
            Items.AZALEA,
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

        ShapedRecipeBuilder.shaped(HexItems.SCROLL.get())
            .define('P', Items.PAPER)
            .define('A', Items.AMETHYST_SHARD)
            .pattern("  A")
            .pattern("PP ")
            .pattern("PP ")
            .unlockedBy("has_item", has(Items.AMETHYST_SHARD)).save(recipes);

        ShapedRecipeBuilder.shaped(HexItems.SLATE.get(), 6)
            .define('S', Items.DEEPSLATE)
            .define('A', HexItems.AMETHYST_DUST.get())
            .pattern(" A ")
            .pattern("SSS")
            .unlockedBy("has_item", has(HexItems.AMETHYST_DUST.get())).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.SLATE_BLOCK.get())
            .define('S', HexItems.SLATE.get())
            .pattern("S")
            .pattern("S")
            .unlockedBy("has_item", has(HexItems.SLATE.get()))
            .save(recipes, modLoc("slate_block_from_slates"));

        ringAll(HexBlocks.SLATE_BLOCK.get(), 8, Blocks.DEEPSLATE, HexItems.AMETHYST_DUST.get())
            .unlockedBy("has_item", has(HexItems.SLATE.get())).save(recipes);

        packing(HexItems.AMETHYST_DUST.get(), HexBlocks.AMETHYST_DUST_BLOCK.get().asItem(), "amethyst_dust",
            false, recipes);

        ringAll(HexBlocks.AMETHYST_TILES.get(), 8, Blocks.AMETHYST_BLOCK, HexItems.AMETHYST_DUST.get())
            .unlockedBy("has_item", has(HexItems.AMETHYST_DUST.get())).save(recipes);
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(Blocks.AMETHYST_BLOCK), HexBlocks.AMETHYST_TILES.get())
            .unlockedBy("has_item", has(Blocks.AMETHYST_BLOCK))
            .save(recipes, modLoc("stonecutting/amethyst_tiles"));

        ringAll(HexBlocks.SCROLL_PAPER.get(), 8, Items.PAPER, Items.AMETHYST_SHARD)
            .unlockedBy("has_item", has(Items.AMETHYST_SHARD)).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.ANCIENT_SCROLL_PAPER.get(), 8)
            .requires(Tags.Items.DYES_BROWN)
            .requires(HexBlocks.SCROLL_PAPER.get(), 8)
            .unlockedBy("has_item", has(HexBlocks.SCROLL_PAPER.get())).save(recipes);
        stack(HexBlocks.SCROLL_PAPER_LANTERN.get(), 1, HexBlocks.SCROLL_PAPER.get(), Items.TORCH)
            .unlockedBy("has_item", has(HexBlocks.SCROLL_PAPER.get())).save(recipes);
        stack(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN.get(), 1, HexBlocks.ANCIENT_SCROLL_PAPER.get(), Items.TORCH)
            .unlockedBy("has_item", has(HexBlocks.ANCIENT_SCROLL_PAPER.get())).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN.get(), 8)
            .requires(Tags.Items.DYES_BROWN)
            .requires(HexBlocks.SCROLL_PAPER_LANTERN.get(), 8)
            .unlockedBy("has_item", has(HexBlocks.SCROLL_PAPER_LANTERN.get()))
            .save(recipes, modLoc("ageing_scroll_paper_lantern"));

        stack(HexBlocks.SCONCE.get(), 4, Ingredient.of(HexItems.CHARGED_AMETHYST.get()),
            Ingredient.of(Tags.Items.INGOTS_COPPER))
            .unlockedBy("has_item", has(HexItems.CHARGED_AMETHYST.get())).save(recipes);

        ShapelessRecipeBuilder.shapeless(HexBlocks.AKASHIC_PLANKS.get(), 4)
            .requires(HexItemTags.AKASHIC_LOGS)
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_LOGS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_WOOD.get(), 3)
            .define('W', HexBlocks.AKASHIC_LOG.get())
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", has(HexBlocks.AKASHIC_LOG.get())).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_WOOD_STRIPPED.get(), 3)
            .define('W', HexBlocks.AKASHIC_LOG_STRIPPED.get())
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", has(HexBlocks.AKASHIC_LOG_STRIPPED.get())).save(recipes);
        ring(HexBlocks.AKASHIC_PANEL.get(), 8, HexItemTags.AKASHIC_PLANKS, null)
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_TILE.get(), 6)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW ")
            .pattern("W W")
            .pattern(" WW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_DOOR.get(), 3)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW")
            .pattern("WW")
            .pattern("WW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_TRAPDOOR.get(), 2)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WWW")
            .pattern("WWW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_STAIRS.get(), 4)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("W  ")
            .pattern("WW ")
            .pattern("WWW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_SLAB.get(), 6)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WWW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_PRESSURE_PLATE.get(), 1)
            .define('W', HexItemTags.AKASHIC_PLANKS)
            .pattern("WW")
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);
        ShapelessRecipeBuilder.shapeless(HexBlocks.AKASHIC_BUTTON.get())
            .requires(HexItemTags.AKASHIC_PLANKS)
            .unlockedBy("has_item", has(HexItemTags.AKASHIC_PLANKS)).save(recipes);

        var enlightenment = new OvercastTrigger.Instance(EntityPredicate.Composite.ANY,
            MinMaxBounds.Ints.ANY,
            // add a little bit of slop here
            MinMaxBounds.Doubles.atLeast(17.95),
            MinMaxBounds.Doubles.between(0.1, 2.05));

        ShapedRecipeBuilder.shaped(HexBlocks.EMPTY_IMPETUS.get())
            .define('B', Items.IRON_BARS)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .define('S', HexBlocks.SLATE_BLOCK.get())
            .define('P', Items.PURPUR_BLOCK)
            .pattern("PSS")
            .pattern("BAB")
            .pattern("SSP")
            .unlockedBy("enlightenment", enlightenment).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.EMPTY_DIRECTRIX.get())
            .define('C', Items.COMPARATOR)
            .define('O', Items.OBSERVER)
            .define('A', HexItems.CHARGED_AMETHYST.get())
            .define('S', HexBlocks.SLATE_BLOCK.get())
            .pattern("CSS")
            .pattern("OAO")
            .pattern("SSC")
            .unlockedBy("enlightenment", enlightenment).save(recipes);

        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_BOOKSHELF.get())
            .define('L', HexItemTags.AKASHIC_LOGS)
            .define('P', HexItemTags.AKASHIC_PLANKS)
            .define('C', Items.BOOK)
            /*this is the*/.pattern("LPL") // and what i have for you today is
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes);
        ShapedRecipeBuilder.shaped(HexBlocks.AKASHIC_CONNECTOR.get())
            .define('L', HexItemTags.AKASHIC_LOGS)
            .define('P', HexItemTags.AKASHIC_PLANKS)
            .define('C', HexItems.CHARGED_AMETHYST.get())
            .pattern("LPL")
            .pattern("CCC")
            .pattern("LPL")
            .unlockedBy("enlightenment", enlightenment).save(recipes);

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(Blocks.AMETHYST_BLOCK),
            new VillagerIngredient(null, null, 3),
            Blocks.BUDDING_AMETHYST.defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/budding_amethyst"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS.get()),
            new VillagerIngredient(new ResourceLocation("toolsmith"), null, 2),
            HexBlocks.IMPETUS_RIGHTCLICK.get().defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_rightclick"));
        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS.get()),
            new VillagerIngredient(new ResourceLocation("fletcher"), null, 2),
            HexBlocks.IMPETUS_LOOK.get().defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_look"));
        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_IMPETUS.get()),
            new VillagerIngredient(new ResourceLocation("cleric"), null, 2),
            HexBlocks.IMPETUS_STOREDPLAYER.get().defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/impetus_storedplayer"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.EMPTY_DIRECTRIX.get()),
            new VillagerIngredient(new ResourceLocation("mason"), null, 1),
            HexBlocks.DIRECTRIX_REDSTONE.get().defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/directrix_redstone"));

        new BrainsweepRecipeBuilder(StateIngredientHelper.of(HexBlocks.AKASHIC_CONNECTOR.get()),
            new VillagerIngredient(new ResourceLocation("librarian"), null, 5),
            HexBlocks.AKASHIC_RECORD.get().defaultBlockState())
            .unlockedBy("enlightenment", enlightenment)
            .save(recipes, modLoc("brainsweep/akashic_record"));
    }

    protected void specialRecipe(Consumer<FinishedRecipe> consumer, SimpleRecipeSerializer<?> serializer) {
        var name = Registry.RECIPE_SERIALIZER.getKey(serializer);
        SpecialRecipeBuilder.special(serializer).save(consumer, prefix("dynamic/" + name.getPath()).toString());
    }

    // why is this private waa
    protected static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> pTag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(pTag).build());
    }
}
