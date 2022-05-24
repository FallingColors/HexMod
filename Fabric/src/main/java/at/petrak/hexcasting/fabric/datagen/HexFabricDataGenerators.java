package at.petrak.hexcasting.fabric.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.datagen.HexBlockTagProvider;
import at.petrak.hexcasting.datagen.HexItemTagProvider;
import at.petrak.hexcasting.datagen.HexLootTables;
import at.petrak.hexcasting.datagen.IXplatIngredients;
import at.petrak.hexcasting.datagen.recipe.HexplatRecipes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.stream.Stream;

public class HexFabricDataGenerators implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        HexAPI.LOGGER.info("Starting Fabric-specific datagen");

        gen.addProvider(new HexplatRecipes(gen, INGREDIENTS));

        var xtags = IXplatAbstractions.INSTANCE.tags();
        var blockTagProvider = new HexBlockTagProvider(gen, xtags);
        gen.addProvider(blockTagProvider);
        gen.addProvider(new HexItemTagProvider(gen, blockTagProvider, xtags));

        gen.addProvider(new HexLootTables(gen));
    }

    private static final IXplatIngredients INGREDIENTS = new IXplatIngredients() {
        @Override
        public Ingredient glowstoneDust() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.GLOWSTONE_DUST)),
                new Ingredient.TagValue(tag("glowstone_dusts"))
            ));
        }

        @Override
        public Ingredient leather() {
            // apparently c:leather also includes rabbit hide
            return Ingredient.of(Items.LEATHER);
        }

        @Override
        public Ingredient ironNugget() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.IRON_NUGGET)),
                new Ingredient.TagValue(tag("iron_nuggets"))
            ));
        }

        @Override
        public Ingredient goldNugget() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.GOLD_NUGGET)),
                new Ingredient.TagValue(tag("gold_nuggets"))
            ));
        }

        @Override
        public Ingredient copperIngot() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.COPPER_INGOT)),
                new Ingredient.TagValue(tag("copper_ingots"))
            ));
        }

        @Override
        public Ingredient ironIngot() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.IRON_INGOT)),
                new Ingredient.TagValue(tag("iron_ingots"))
            ));
        }

        @Override
        public Ingredient goldIngot() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.GOLD_INGOT)),
                new Ingredient.TagValue(tag("gold_ingots"))
            ));
        }

        @Override
        public EnumMap<DyeColor, Ingredient> dyes() {
            var out = new EnumMap<DyeColor, Ingredient>(DyeColor.class);
            for (var col : DyeColor.values()) {
                out.put(col, new Ingredient(Stream.of(
                    new Ingredient.ItemValue(new ItemStack(DyeItem.byColor(col))),
                    new Ingredient.TagValue(
                        TagKey.create(Registry.ITEM_REGISTRY,
                            new ResourceLocation("c", col.getSerializedName() + "_dye"))),
                    new Ingredient.TagValue(
                        TagKey.create(Registry.ITEM_REGISTRY,
                            new ResourceLocation("c", col.getSerializedName() + "_dyes"))
                    ))));
            }
            return out;
        }

        @Override
        public Ingredient stick() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.STICK)),
                new Ingredient.TagValue(tag("wood_sticks"))
            ));
        }
    };

    private static TagKey<Item> tag(String s) {
        return tag("c", s);
    }

    private static TagKey<Item> tag(String namespace, String s) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(namespace, s));
    }
}
