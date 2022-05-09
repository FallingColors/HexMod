package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.items.*;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedSpell;
import at.petrak.paucal.api.datagen.PaucalItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class HexItemModels extends PaucalItemModelProvider {
    public HexItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, HexMod.MOD_ID, existingFileHelper);
    }

    private static final String[] DATUM_TYPES = {"empty", "entity", "double", "vec3", "widget", "list", "pattern"};
    private static final String[] PHIAL_SIZES = {"small", "medium", "large"};

    @Override
    protected void registerModels() {
        simpleItem(HexItems.AMETHYST_DUST.get());
        simpleItem(HexItems.CHARGED_AMETHYST.get());
        simpleItem(HexItems.SUBMARINE_SANDWICH.get());
        simpleItem(HexItems.ABACUS.get());

        simpleItem(modLoc("scroll_pristine"));
        simpleItem(modLoc("scroll_ancient"));
        getBuilder(HexItems.SCROLL.get().getRegistryName().getPath())
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 0f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_pristine"))).end()
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 1f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_ancient"))).end();

        simpleItem(HexItems.SCRYING_LENS.get());
        getBuilder(HexItems.SCRYING_LENS.get().getRegistryName().getPath())
            .transforms()
            .transform(ModelBuilder.Perspective.HEAD)
            .rotation(0f, 0f, 0f)
            .translation(-2.5f, 0f, -8f)
            .scale(0.4f);

        singleTexture("wand_old", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/wands/old"));
        singleTexture("wand_bosnia", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/wands/bosnia"));

        buildWand(HexItems.WAND_OAK.get(), "oak");
        buildWand(HexItems.WAND_BIRCH.get(), "birch");
        buildWand(HexItems.WAND_SPRUCE.get(), "spruce");
        buildWand(HexItems.WAND_JUNGLE.get(), "jungle");
        buildWand(HexItems.WAND_DARK_OAK.get(), "dark_oak");
        buildWand(HexItems.WAND_ACACIA.get(), "acacia");
        buildWand(HexItems.WAND_CRIMSON.get(), "crimson");
        buildWand(HexItems.WAND_WARPED.get(), "warped");
        buildWand(HexItems.WAND_AKASHIC.get(), "akashic");

        simpleItem(modLoc("patchouli_book"));

        // For stupid bad reasons we need to do this in ascending order.
        for (int sealedIdx = 0; sealedIdx <= 1; sealedIdx++) {
            var sealed = sealedIdx == 1;
            for (int i = 0, stringsLength = DATUM_TYPES.length; i < stringsLength; i++) {
                var type = DATUM_TYPES[i];

                var suffix = type + (sealed ? "_sealed" : "");

                var focusName = "focus_" + suffix;
                singleTexture(focusName, new ResourceLocation("item/generated"),
                    "layer0", modLoc("item/focus/" + suffix));
                getBuilder(HexItems.FOCUS.get().getRegistryName().getPath())
                    .override()
                    .predicate(ItemFocus.DATATYPE_PRED, i)
                    .predicate(ItemFocus.SEALED_PRED, sealed ? 1f : 0f)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + focusName)))
                    .end();

                var spellbookName = "spellbook_" + type + (sealed ? "_sealed" : "");
                singleTexture(spellbookName, new ResourceLocation("item/generated"),
                    "layer0", modLoc("item/spellbook/" + suffix));
                getBuilder(HexItems.SPELLBOOK.get().getRegistryName().getPath())
                    .override()
                    .predicate(ItemFocus.DATATYPE_PRED, i)
                    .predicate(ItemFocus.SEALED_PRED, sealed ? 1f : 0f)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + spellbookName)))
                    .end();
            }
        }

        buildPackagedSpell(HexItems.CYPHER.get(), "cypher");
        buildPackagedSpell(HexItems.TRINKET.get(), "trinket");
        buildPackagedSpell(HexItems.ARTIFACT.get(), "artifact");

        int maxFill = 4;
        for (int size = 0; size < PHIAL_SIZES.length; size++) {
            for (int fill = 0; fill <= maxFill; fill++) {
                String name = "phial_" + PHIAL_SIZES[size] + "_" + fill;
                singleTexture(
                    name,
                    new ResourceLocation("item/generated"),
                    "layer0", new ResourceLocation(HexMod.MOD_ID, "item/phial/" + name));

                float fillProp = (float) fill / maxFill;
                getBuilder(HexItems.BATTERY.getId().getPath()).override()
                    .predicate(ItemManaBattery.MANA_PREDICATE, fillProp)
                    .predicate(ItemManaBattery.MAX_MANA_PREDICATE, size)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name)))
                    .end();
            }
        }

        for (int i = 0; i < DyeColor.values().length; i++) {
            singleTexture(HexItems.DYE_COLORIZERS.get(DyeColor.values()[i]).getId().getPath(),
                new ResourceLocation("item/generated"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/dye" + i));
        }
        for (int i = 0; i < 14; i++) {
            singleTexture(HexItems.PRIDE_COLORIZERS[i].getId().getPath(), new ResourceLocation("item/generated"),
                "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/pride" + i));
        }
        singleTexture(HexItems.UUID_COLORIZER.getId().getPath(), new ResourceLocation("item/generated"),
            "layer0", new ResourceLocation(HexMod.MOD_ID, "item/colorizer/uuid"));

        simpleItem(modLoc("slate_blank"));
        simpleItem(modLoc("slate_written"));
        getBuilder(HexItems.SLATE.getId().getPath()).override()
            .predicate(ItemSlate.WRITTEN_PRED, 0)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_blank")))
            .end()
            .override()
            .predicate(ItemSlate.WRITTEN_PRED, 1)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_written")))
            .end();

        getBuilder(HexBlocks.AKASHIC_RECORD.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_record")));
        simpleItem(modLoc("akashic_door"));
        getBuilder(HexBlocks.AKASHIC_TRAPDOOR.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_trapdoor_bottom")));
        getBuilder(HexBlocks.AKASHIC_LOG.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_log")));
        getBuilder(HexBlocks.AKASHIC_LOG_STRIPPED.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_log_stripped")));
        getBuilder(HexBlocks.AKASHIC_STAIRS.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_stairs")));
        getBuilder(HexBlocks.AKASHIC_SLAB.getId().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_slab")));
        getBuilder(HexBlocks.AKASHIC_BUTTON.getId().getPath()).parent(
                new ModelFile.UncheckedModelFile(new ResourceLocation("block/button_inventory")))
            .texture("texture", modLoc("block/akashic/planks1"));
        getBuilder(HexBlocks.AKASHIC_PRESSURE_PLATE.getId().getPath())
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/akashic_pressure_plate")));
    }

    private void buildWand(Item item, String name) {
        singleTexture(item.getRegistryName().getPath(), new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/wands/" + name));
        getBuilder(item.getRegistryName().getPath())
            .override()
            .predicate(ItemWand.FUNNY_LEVEL_PREDICATE, 0)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/wand_" + name)))
            .end().override()
            .predicate(ItemWand.FUNNY_LEVEL_PREDICATE, 1)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/wand_old")))
            .end().override()
            .predicate(ItemWand.FUNNY_LEVEL_PREDICATE, 2)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/wand_bosnia")))
            .end();
    }

    private void buildPackagedSpell(Item item, String name) {
        simpleItem(modLoc(name));
        simpleItem(modLoc(name + "_filled"));
        getBuilder(item.getRegistryName().getPath())
            .override()
            .predicate(ItemPackagedSpell.HAS_PATTERNS_PRED, -0.01f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name)))
            .end()
            .override()
            .predicate(ItemPackagedSpell.HAS_PATTERNS_PRED, 1f - 0.01f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name + "_filled")))
            .end();
    }
}
