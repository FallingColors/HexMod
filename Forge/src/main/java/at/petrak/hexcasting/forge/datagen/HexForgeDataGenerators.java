package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.forge.datagen.forge.lootmods.HexLootModifiers;
import at.petrak.hexcasting.forge.datagen.xplat.*;
import at.petrak.hexcasting.xplat.datagen.IXplatIngredients;
import at.petrak.hexcasting.xplat.datagen.recipe.HexplatRecipes;
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
        } else {
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
            HexBlockTagProvider blockTagProvider = new HexBlockTagProvider(gen, efh);
            gen.addProvider(blockTagProvider);
            gen.addProvider(new HexItemTagProvider(gen, blockTagProvider, efh));
            gen.addProvider(new HexAdvancements(gen, efh));
            gen.addProvider(new HexLootTables(gen));
        }
    }

    private static void configureForgeDatagen(GatherDataEvent ev) {
        HexAPI.LOGGER.info("Starting Forge-specific datagen");

        DataGenerator gen = ev.getGenerator();
        ExistingFileHelper efh = ev.getExistingFileHelper();
        if (ev.includeServer()) {
            gen.addProvider(new HexplatRecipes(gen, INGREDIENTS));
            gen.addProvider(new HexLootModifiers(gen));
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