package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.items.ItemFocus;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.items.ItemSlate;
import at.petrak.hexcasting.common.items.ItemWand;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.paucal.api.forge.datagen.PaucalItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class HexItemModels extends PaucalItemModelProvider {
    public HexItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, HexAPI.MOD_ID, existingFileHelper);
    }

    private static final String[] DATUM_TYPES = {"empty", "entity", "double", "vec3", "widget", "list", "pattern"};
    private static final String[] PHIAL_SIZES = {"small", "medium", "large"};

    @Override
    protected void registerModels() {
        simpleItem(HexItems.AMETHYST_DUST);
        simpleItem(HexItems.CHARGED_AMETHYST);
        simpleItem(HexItems.SUBMARINE_SANDWICH);
        simpleItem(HexItems.ABACUS);
        brandishedItem(HexItems.JEWELER_HAMMER);
        simpleItem(HexItems.CREATIVE_UNLOCKER);

        singleTexture(HexBlocks.CONJURED_BLOCK.getRegistryName().getPath(),
            new ResourceLocation("item/generated"),
            "layer0", new ResourceLocation("item/amethyst_shard"));
        singleTexture(HexBlocks.CONJURED_LIGHT.getRegistryName().getPath(),
            new ResourceLocation("item/generated"),
            "layer0", new ResourceLocation("item/amethyst_shard"));

        for (var age : new String[]{"pristine", "ancient"}) {
            for (var size : new String[]{"small", "medium", "large"}) {
                simpleItem(modLoc("scroll_" + age + "_" + size));
            }
        }
        buildScroll(HexItems.SCROLL_SMOL, "small");
        buildScroll(HexItems.SCROLL_MEDIUM, "medium");
        buildScroll(HexItems.SCROLL_LARGE, "large");

        simpleItem(HexItems.SCRYING_LENS);
        getBuilder(HexItems.SCRYING_LENS.getRegistryName().getPath())
            .transforms()
            .transform(ModelBuilder.Perspective.HEAD)
            .rotation(0f, 0f, 0f)
            .translation(-2.5f, 0f, -8f)
            .scale(0.4f);

        singleTexture("wand_old", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/wands/old"));
        singleTexture("wand_bosnia", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/wands/bosnia"));

        buildWand(HexItems.WAND_OAK, "oak");
        buildWand(HexItems.WAND_BIRCH, "birch");
        buildWand(HexItems.WAND_SPRUCE, "spruce");
        buildWand(HexItems.WAND_JUNGLE, "jungle");
        buildWand(HexItems.WAND_DARK_OAK, "dark_oak");
        buildWand(HexItems.WAND_ACACIA, "acacia");
        buildWand(HexItems.WAND_CRIMSON, "crimson");
        buildWand(HexItems.WAND_WARPED, "warped");
        buildWand(HexItems.WAND_AKASHIC, "akashic");

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
                getBuilder(HexItems.FOCUS.getRegistryName().getPath())
                    .override()
                    .predicate(ItemFocus.DATATYPE_PRED, i)
                    .predicate(ItemFocus.SEALED_PRED, sealed ? 1f : 0f)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + focusName)))
                    .end();

                var spellbookName = "spellbook_" + type + (sealed ? "_sealed" : "");
                singleTexture(spellbookName, new ResourceLocation("item/generated"),
                    "layer0", modLoc("item/spellbook/" + suffix));
                getBuilder(HexItems.SPELLBOOK.getRegistryName().getPath())
                    .override()
                    .predicate(ItemFocus.DATATYPE_PRED, i)
                    .predicate(ItemFocus.SEALED_PRED, sealed ? 1f : 0f)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + spellbookName)))
                    .end();
            }
        }

        buildPackagedSpell(HexItems.CYPHER, "cypher");
        buildPackagedSpell(HexItems.TRINKET, "trinket");
        buildPackagedSpell(HexItems.ARTIFACT, "artifact");

        int maxFill = 4;
        for (int size = 0; size < PHIAL_SIZES.length; size++) {
            for (int fill = 0; fill <= maxFill; fill++) {
                String name = "phial_" + PHIAL_SIZES[size] + "_" + fill;
                singleTexture(
                    name,
                    new ResourceLocation("item/generated"),
                    "layer0", modLoc("item/phial/" + name));

                float fillProp = (float) fill / maxFill;
                getBuilder(HexItems.BATTERY.getRegistryName().getPath()).override()
                    .predicate(ItemManaBattery.MANA_PREDICATE, fillProp)
                    .predicate(ItemManaBattery.MAX_MANA_PREDICATE, size)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name)))
                    .end();
            }
        }

        for (var dye : DyeColor.values()) {
            singleTexture(HexItems.DYE_COLORIZERS.get(dye).getRegistryName().getPath(),
                new ResourceLocation("item/generated"),
                "layer0", modLoc("item/colorizer/dye_" + dye.getName()));
        }
        for (var type : ItemPrideColorizer.Type.values()) {
            singleTexture(HexItems.PRIDE_COLORIZERS.get(type).getRegistryName().getPath(),
                new ResourceLocation("item/generated"),
                "layer0", modLoc("item/colorizer/pride_" + type.getName()));
        }
        singleTexture(HexItems.UUID_COLORIZER.getRegistryName().getPath(), new ResourceLocation("item/generated"),
            "layer0", modLoc("item/colorizer/uuid"));

        simpleItem(modLoc("slate_blank"));
        simpleItem(modLoc("slate_written"));
        getBuilder(HexItems.SLATE.getRegistryName().getPath()).override()
            .predicate(ItemSlate.WRITTEN_PRED, 0)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_blank")))
            .end()
            .override()
            .predicate(ItemSlate.WRITTEN_PRED, 1)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_written")))
            .end();

        getBuilder(HexBlocks.AKASHIC_RECORD.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_record")));
        simpleItem(modLoc("akashic_door"));
        getBuilder(HexBlocks.AKASHIC_TRAPDOOR.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_trapdoor_bottom")));
        getBuilder(HexBlocks.AKASHIC_LOG.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_log")));
        getBuilder(HexBlocks.AKASHIC_LOG_STRIPPED.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_log_stripped")));
        getBuilder(HexBlocks.AKASHIC_WOOD.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_wood")));
        getBuilder(HexBlocks.AKASHIC_WOOD_STRIPPED.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_wood_stripped")));
        getBuilder(HexBlocks.AKASHIC_STAIRS.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_stairs")));
        getBuilder(HexBlocks.AKASHIC_SLAB.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_slab")));
        getBuilder(HexBlocks.AKASHIC_BUTTON.getRegistryName().getPath()).parent(
                new ModelFile.UncheckedModelFile(new ResourceLocation("block/button_inventory")))
            .texture("texture", modLoc("block/akashic/planks1"));
        getBuilder(HexBlocks.AKASHIC_PRESSURE_PLATE.getRegistryName().getPath())
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/akashic_pressure_plate")));
    }

    private void buildScroll(Item item, String size) {
        getBuilder(item.getRegistryName().getPath())
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 0f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_pristine_" + size))).end()
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 1f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_ancient_" + size))).end();
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
            .predicate(ItemPackagedHex.HAS_PATTERNS_PRED, -0.01f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name)))
            .end()
            .override()
            .predicate(ItemPackagedHex.HAS_PATTERNS_PRED, 1f - 0.01f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name + "_filled")))
            .end();
    }
}
