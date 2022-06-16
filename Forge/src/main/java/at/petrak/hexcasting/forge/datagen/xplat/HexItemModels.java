package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.items.ItemFocus;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.items.ItemSlate;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery;
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

    private static final String[] PHIAL_SIZES = {"small", "medium", "large"};

    @Override
    protected void registerModels() {
        simpleItem(HexItems.AMETHYST_DUST);
        simpleItem(HexItems.CHARGED_AMETHYST);
        simpleItem(HexItems.SUBMARINE_SANDWICH);
        simpleItem(HexItems.ABACUS);
        brandishedItem(HexItems.JEWELER_HAMMER);
        simpleItem(HexItems.CREATIVE_UNLOCKER);
        simpleItem(HexItems.LORE_FRAGMENT);

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

        singleTexture("old_staff", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/staves/old"));
        singleTexture("bosnia_staff", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/staves/bosnia"));

        buildStaff(HexItems.STAFF_OAK, "oak");
        buildStaff(HexItems.STAFF_BIRCH, "birch");
        buildStaff(HexItems.STAFF_SPRUCE, "spruce");
        buildStaff(HexItems.STAFF_JUNGLE, "jungle");
        buildStaff(HexItems.STAFF_DARK_OAK, "dark_oak");
        buildStaff(HexItems.STAFF_ACACIA, "acacia");
        buildStaff(HexItems.STAFF_CRIMSON, "crimson");
        buildStaff(HexItems.STAFF_WARPED, "warped");
        buildStaff(HexItems.STAFF_EDIFIED, "edified");

        simpleItem(modLoc("patchouli_book"));

        buildSealableIotaHolder(HexItems.FOCUS, "focus");
        buildSealableIotaHolder(HexItems.SPELLBOOK, "spellbook");

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
                    .predicate(ItemMediaBattery.MANA_PREDICATE, fillProp)
                    .predicate(ItemMediaBattery.MAX_MANA_PREDICATE, size)
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
        simpleItem(modLoc("edified_door"));
        getBuilder(HexBlocks.EDIFIED_TRAPDOOR.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_trapdoor_bottom")));
        getBuilder(HexBlocks.EDIFIED_LOG.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_log")));
        getBuilder(HexBlocks.STRIPPED_EDIFIED_LOG.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/stripped_edified_log")));
        getBuilder(HexBlocks.EDIFIED_WOOD.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_wood")));
        getBuilder(HexBlocks.STRIPPED_EDIFIED_WOOD.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/stripped_edified_wood")));
        getBuilder(HexBlocks.EDIFIED_STAIRS.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_stairs")));
        getBuilder(HexBlocks.EDIFIED_SLAB.getRegistryName().getPath()).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_slab")));
        getBuilder(HexBlocks.EDIFIED_BUTTON.getRegistryName().getPath()).parent(
                new ModelFile.UncheckedModelFile(new ResourceLocation("block/button_inventory")))
            .texture("texture", modLoc("block/edified_planks"));
        getBuilder(HexBlocks.EDIFIED_PRESSURE_PLATE.getRegistryName().getPath())
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/edified_pressure_plate")));
    }

    private void buildSealableIotaHolder(Item item, String stub) {
        var name = item.getRegistryName().getPath();
        var plain = singleTexture(name, new ResourceLocation("item/generated"),
            "layer0", modLoc("item/" + stub));
        var unsealed = withExistingParent(name + "_filled", new ResourceLocation("item/generated"))
            .texture("layer0", modLoc("item/" + stub))
            .texture("layer1", modLoc("item/" + stub) + "_overlay");
        var sealed = withExistingParent(name + "_sealed", new ResourceLocation("item/generated"))
            .texture("layer0", modLoc("item/" + stub))
            .texture("layer1", modLoc("item/" + stub) + "_overlay_sealed");
        getBuilder(name)
            .override().predicate(ItemFocus.OVERLAY_PRED, 0f)
            .model(plain).end()
            .override().predicate(ItemFocus.OVERLAY_PRED, 1f)
            .model(unsealed).end()
            .override().predicate(ItemFocus.OVERLAY_PRED, 2f)
            .model(sealed).end();
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

    private void buildStaff(Item item, String name) {
        singleTexture(item.getRegistryName().getPath(), new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/staves/" + name));
        getBuilder(item.getRegistryName().getPath())
            .override()
            .predicate(ItemStaff.FUNNY_LEVEL_PREDICATE, 0)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name + "_staff")))
            .end().override()
            .predicate(ItemStaff.FUNNY_LEVEL_PREDICATE, 1)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/old_staff")))
            .end().override()
            .predicate(ItemStaff.FUNNY_LEVEL_PREDICATE, 2)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/bosnia_staff")))
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
