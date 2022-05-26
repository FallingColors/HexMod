package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.datagen.*;
import at.petrak.hexcasting.datagen.recipe.HexplatRecipes;
import at.petrak.hexcasting.forge.datagen.xplat.HexBlockStatesAndModels;
import at.petrak.hexcasting.forge.datagen.xplat.HexItemModels;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.paucal.api.forge.datagen.PaucalForgeDatagenWrappers;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.util.EnumMap;
import java.util.stream.Stream;

public class HexForgeDataGenerators {
    @SubscribeEvent
    public static void generateData(GatherDataEvent ev) {
        if (System.getProperty("hexcasting.xplat_datagen") != null) {
            configureXplatDatagen(ev);
        }
        if (System.getProperty("hexcasting.forge_datagen") != null) {
            configureForgeDatagen(ev);
        }
    }

    private static void configureXplatDatagen(GatherDataEvent ev) {
        HexAPI.LOGGER.info("Starting cross-platform datagen");

        DataGenerator gen = ev.getGenerator();
        ExistingFileHelper efh = ev.getExistingFileHelper();
        if (ev.includeClient()) {
            gen.addProvider(new HexItemModels(gen, efh));
            gen.addProvider(new HexBlockStatesAndModels(gen, efh));
        }
        if (ev.includeServer()) {
            gen.addProvider(PaucalForgeDatagenWrappers.addEFHToAdvancements(new HexAdvancements(gen), efh));
        }
    }

    private static void configureForgeDatagen(GatherDataEvent ev) {
        HexAPI.LOGGER.info("Starting Forge-specific datagen");

        DataGenerator gen = ev.getGenerator();
        ExistingFileHelper efh = ev.getExistingFileHelper();
        if (ev.includeServer()) {
            gen.addProvider(new HexLootTables(gen));
            gen.addProvider(new HexplatRecipes(gen, INGREDIENTS));

            var xtags = IXplatAbstractions.INSTANCE.tags();
            var blockTagProvider = PaucalForgeDatagenWrappers.addEFHToTagProvider(
                new HexBlockTagProvider(gen, xtags), efh);
            gen.addProvider(blockTagProvider);
            var itemTagProvider = PaucalForgeDatagenWrappers.addEFHToTagProvider(
                new HexItemTagProvider(gen, blockTagProvider, IXplatAbstractions.INSTANCE.tags()), efh);
            gen.addProvider(itemTagProvider);
        }
    }

    private static IXplatIngredients INGREDIENTS = new IXplatIngredients() {
        @Override
        public Ingredient glowstoneDust() {
            return Ingredient.of(Tags.Items.DUSTS_GLOWSTONE);
        }

        @Override
        public Ingredient leather() {
            return Ingredient.of(Tags.Items.LEATHER);
        }

        @Override
        public Ingredient ironNugget() {
            return Ingredient.of(Tags.Items.NUGGETS_IRON);
        }

        @Override
        public Ingredient goldNugget() {
            return Ingredient.of(Tags.Items.NUGGETS_GOLD);
        }

        @Override
        public Ingredient copperIngot() {
            return Ingredient.of(Tags.Items.INGOTS_COPPER);
        }

        @Override
        public Ingredient ironIngot() {
            return Ingredient.of(Tags.Items.INGOTS_IRON);
        }

        @Override
        public Ingredient goldIngot() {
            return Ingredient.of(Tags.Items.INGOTS_GOLD);
        }

        @Override
        public EnumMap<DyeColor, Ingredient> dyes() {
            var out = new EnumMap<DyeColor, Ingredient>(DyeColor.class);
            for (var col : DyeColor.values()) {
                out.put(col, Ingredient.of(col.getTag()));
            }
            return out;
        }

        @Override
        public Ingredient stick() {
            return Ingredient.fromValues(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.STICK)),
                new Ingredient.TagValue(ItemTags.create(new ResourceLocation("forge", "rods/wooden")))
            ));
        }
    };
}