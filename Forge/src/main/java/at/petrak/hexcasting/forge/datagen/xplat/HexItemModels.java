package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.client.render.GaslightingTracker;
import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.pigment.ItemPridePigment;
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.items.storage.ItemSlate;
import at.petrak.hexcasting.common.items.storage.ItemThoughtKnot;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.paucal.api.forge.datagen.PaucalItemModelProvider;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.BiFunction;

public class HexItemModels extends PaucalItemModelProvider {
    public HexItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, HexAPI.MOD_ID, existingFileHelper);
    }

    private static final String[] PHIAL_SIZES = {"small", "medium", "large", "larger", "largest"};

    private static String getPath(Item item) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)).getPath();
    }

    private static String getPath(Block block) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getPath();
    }

    @Override
    protected void registerModels() {
        simpleItem(HexItems.AMETHYST_DUST);
        simpleItem(HexItems.CHARGED_AMETHYST);
        simpleItem(HexItems.SUBMARINE_SANDWICH);
        simpleItem(HexItems.ABACUS);
        brandishedItem(HexItems.JEWELER_HAMMER);
        simpleItem(HexItems.CREATIVE_UNLOCKER);
        simpleItem(HexItems.LORE_FRAGMENT);

        singleTexture(getPath(HexBlocks.CONJURED_BLOCK),
            new ResourceLocation("item/generated"),
            "layer0", new ResourceLocation("item/amethyst_shard"));
        singleTexture(getPath(HexBlocks.CONJURED_LIGHT),
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
        getBuilder(getPath(HexItems.SCRYING_LENS))
            .transforms()
            .transform(ItemDisplayContext.HEAD)
            .rotation(0f, 0f, 0f)
            .translation(-2.5f, 0f, -8f)
            .scale(0.4f);

        singleTexture("old_staff", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/staff/old"));
        singleTexture("cherry_staff", new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/staff/cherry"));

        buildStaff(HexItems.STAFF_OAK, "oak");
        buildStaff(HexItems.STAFF_BIRCH, "birch");
        buildStaff(HexItems.STAFF_SPRUCE, "spruce");
        buildStaff(HexItems.STAFF_JUNGLE, "jungle");
        buildStaff(HexItems.STAFF_DARK_OAK, "dark_oak");
        buildStaff(HexItems.STAFF_ACACIA, "acacia");
        buildStaff(HexItems.STAFF_CRIMSON, "crimson");
        buildStaff(HexItems.STAFF_WARPED, "warped");
        buildStaff(HexItems.STAFF_MANGROVE, "mangrove");
        buildStaff(HexItems.STAFF_EDIFIED, "edified");
        buildStaff(HexItems.STAFF_MINDSPLICE, "mindsplice");

        // again, doesn't like paths with slashes in them, so we do it manually
        buildFourVariantGaslight("item/staff/quenched", "item/staff/quenched", (name, path) ->
            singleTexture(path.getPath(), new ResourceLocation("item/handheld_rod"),
                "layer0", modLoc(path.getPath())));
        buildFourVariantGaslight(getPath(HexItems.QUENCHED_SHARD), "item/quenched_shard", (name, path) ->
            singleTexture(path.getPath(), new ResourceLocation("item/handheld"),
                "layer0", modLoc(path.getPath())));
        buildFourVariantGaslight(getPath(HexBlocks.QUENCHED_ALLAY), "block/quenched_allay", (name, path) ->
            cubeAll(path.getPath(), path));
        buildFourVariantGaslight(getPath(HexBlocks.QUENCHED_ALLAY_TILES), "block/deco/quenched_allay_tiles", (name, path) ->
                cubeAll(path.getPath(), path));
        buildFourVariantGaslight(getPath(HexBlocks.QUENCHED_ALLAY_BRICKS), "block/deco/quenched_allay_bricks", (name, path) ->
                cubeAll(path.getPath(), path));
        buildFourVariantGaslight(getPath(HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL), "block/deco/quenched_allay_bricks_small", (name, path) ->
                cubeAll(path.getPath(), path));

        simpleItem(modLoc("patchouli_book"));

        buildThoughtKnot();
        buildSealableIotaHolder(HexItems.FOCUS, "focus", HexItems.FOCUS.numVariants());
        buildSealableIotaHolder(HexItems.SPELLBOOK, "spellbook", HexItems.SPELLBOOK.numVariants());

        buildPackagedSpell(HexItems.CYPHER, "cypher", HexItems.CYPHER.numVariants());
        buildPackagedSpell(HexItems.TRINKET, "trinket", HexItems.TRINKET.numVariants());
        buildPackagedSpell(HexItems.ARTIFACT, "artifact", HexItems.ARTIFACT.numVariants());

        int maxFill = 4;
        for (int size = 0; size < PHIAL_SIZES.length; size++) {
            for (int fill = 0; fill <= maxFill; fill++) {
                String name = "phial_" + PHIAL_SIZES[size] + "_" + fill;
                singleTexture(
                    name,
                    new ResourceLocation("item/generated"),
                    "layer0", modLoc("item/phial/" + name));

                float fillProp = (float) fill / maxFill;
                getBuilder(getPath(HexItems.BATTERY)).override()
                    .predicate(ItemMediaBattery.MEDIA_PREDICATE, fillProp)
                    .predicate(ItemMediaBattery.MAX_MEDIA_PREDICATE, size)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name)))
                    .end();
            }
        }

        for (var dye : DyeColor.values()) {
            singleTexture(getPath(HexItems.DYE_PIGMENTS.get(dye)),
                new ResourceLocation("item/generated"),
                "layer0", modLoc("item/colorizer/dye_" + dye.getName()));
        }
        for (var type : ItemPridePigment.Type.values()) {
            singleTexture(getPath(HexItems.PRIDE_PIGMENTS.get(type)),
                new ResourceLocation("item/generated"),
                "layer0", modLoc("item/colorizer/pride_" + type.getName()));
        }
        singleTexture(getPath(HexItems.UUID_PIGMENT), new ResourceLocation("item/generated"),
            "layer0", modLoc("item/colorizer/uuid"));
        singleTexture(getPath(HexItems.DEFAULT_PIGMENT), new ResourceLocation("item/generated"),
            "layer0", modLoc("item/colorizer/uuid"));

        simpleItem(modLoc("slate_blank"));
        simpleItem(modLoc("slate_written"));
        getBuilder(getPath(HexItems.SLATE)).override()
            .predicate(ItemSlate.WRITTEN_PRED, 0)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_blank")))
            .end()
            .override()
            .predicate(ItemSlate.WRITTEN_PRED, 1)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_written")))
            .end();

        getBuilder(getPath(HexBlocks.SLATE_PILLAR)).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/slate_pillar")));
        getBuilder(getPath(HexBlocks.AMETHYST_PILLAR)).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/deco/amethyst_pillar")));
        getBuilder(getPath(HexBlocks.SLATE_AMETHYST_PILLAR)).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/slate_amethyst_pillar")));

        getBuilder(getPath(HexBlocks.AKASHIC_RECORD)).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_record")));
        simpleItem(modLoc("edified_door"));
        getBuilder(getPath(HexBlocks.EDIFIED_TRAPDOOR)).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_trapdoor_bottom")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG)).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_log")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG_AMETHYST)).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_log_amethyst")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG_AVENTURINE)).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_log_aventurine")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG_CITRINE)).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_log_citrine")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG_PURPLE)).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_log_purple")));
        getBuilder(getPath(HexBlocks.STRIPPED_EDIFIED_LOG)).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/stripped_edified_log")));
        getBuilder(getPath(HexBlocks.EDIFIED_WOOD)).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_wood")));
        getBuilder(getPath(HexBlocks.STRIPPED_EDIFIED_WOOD)).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/stripped_edified_wood")));
        getBuilder(getPath(HexBlocks.EDIFIED_STAIRS)).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_stairs")));
        getBuilder(getPath(HexBlocks.EDIFIED_SLAB)).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_slab")));
        getBuilder(getPath(HexBlocks.EDIFIED_BUTTON)).parent(
                new ModelFile.UncheckedModelFile(new ResourceLocation("block/button_inventory")))
            .texture("texture", modLoc("block/edified_planks"));
        getBuilder(getPath(HexBlocks.EDIFIED_PRESSURE_PLATE))
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/edified_pressure_plate")));
    }

    private void buildThoughtKnot() {
        var unwritten = singleTexture("thought_knot", new ResourceLocation("item/generated"),
            "layer0", modLoc("item/thought_knot"));
        var written = withExistingParent("thought_knot_written", new ResourceLocation("item/generated"))
            .texture("layer0", modLoc("item/thought_knot"))
            .texture("layer1", modLoc("item/thought_knot_overlay"));
        getBuilder("thought_knot")
            .override().predicate(ItemThoughtKnot.WRITTEN_PRED, 0f)
            .model(unwritten).end()
            .override().predicate(ItemThoughtKnot.WRITTEN_PRED, 1f)
            .model(written).end();
    }

    private void buildSealableIotaHolder(Item item, String stub, int numVariants) {
        var name = getPath(item);
        var builder = getBuilder(name);
        for (int i = 0; i < numVariants; i++) {
            var plain = i == 0 ? singleTexture(name, new ResourceLocation("item/generated"),
                    "layer0", modLoc("item/cad/" + i + "_" + stub + "_empty"))
                    : withExistingParent(name + "_" + i, new ResourceLocation("item/generated"))
                    .texture("layer0", modLoc("item/cad/" + i + "_" + stub + "_empty"));
            var unsealed = withExistingParent(name + "_" + i + "_filled", new ResourceLocation("item/generated"))
                    .texture("layer0", modLoc("item/cad/" + i + "_" + stub + "_filled"))
                    .texture("layer1", modLoc("item/cad/" + i + "_" + stub + "_filled_overlay"));
            var sealed = withExistingParent(name + "_" + i + "_sealed", new ResourceLocation("item/generated"))
                    .texture("layer0", modLoc("item/cad/" + i + "_" + stub + "_sealed"))
                    .texture("layer1", modLoc("item/cad/" + i + "_" + stub + "_sealed_overlay"));
            builder.override().predicate(ItemFocus.VARIANT_PRED, i).predicate(ItemFocus.OVERLAY_PRED, 0f)
                .model(plain).end()
                .override().predicate(ItemFocus.VARIANT_PRED, i).predicate(ItemFocus.OVERLAY_PRED, 1f)
                .model(unsealed).end()
                .override().predicate(ItemFocus.VARIANT_PRED, i).predicate(ItemFocus.OVERLAY_PRED, 2f)
                .model(sealed).end();
        }
    }

    private void buildScroll(Item item, String size) {
        getBuilder(getPath(item))
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 0f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_pristine_" + size))).end()
            .override()
            .predicate(ItemScroll.ANCIENT_PREDICATE, 1f)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/scroll_ancient_" + size))).end();
    }

    private void buildStaff(Item item, String name) {
        singleTexture("item/" + getPath(item), new ResourceLocation("item/handheld_rod"),
            "layer0", modLoc("item/staff/" + name));
        getBuilder(getPath(item))
            .override()
            .predicate(ItemStaff.FUNNY_LEVEL_PREDICATE, 0)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name + "_staff")))
            .end().override()
            .predicate(ItemStaff.FUNNY_LEVEL_PREDICATE, 1)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/old_staff")))
            .predicate(ItemStaff.FUNNY_LEVEL_PREDICATE, 2)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/cherry_staff")))
            .end();
    }

    private void buildPackagedSpell(Item item, String stub, int numVariants) {
        var name = getPath(item);
        var builder = getBuilder(name);
        for (int i = 0; i < numVariants; i++) {
            var plain = i == 0 ? singleTexture(name, new ResourceLocation("item/generated"),
                    "layer0", modLoc("item/cad/" + i + "_" + stub))
                    : withExistingParent(name + "_" + i, new ResourceLocation("item/generated"))
                    .texture("layer0", modLoc("item/cad/" + i + "_" + stub));
            var filled = withExistingParent(name + "_" + i + "_filled", new ResourceLocation("item/generated"))
                .texture("layer0", modLoc("item/cad/" + i + "_" + stub))
                .texture("layer1", modLoc("item/cad/" + i + "_" + stub + "_overlay"));
            builder.override().predicate(ItemFocus.VARIANT_PRED, i).predicate(ItemPackagedHex.HAS_PATTERNS_PRED, -0.01f)
                .model(plain).end()
                .override().predicate(ItemFocus.VARIANT_PRED, i).predicate(ItemPackagedHex.HAS_PATTERNS_PRED, 1f - 0.01f)
                .model(filled).end();
        }
    }

    private void buildFourVariantGaslight(String name, String path,
        BiFunction<String, ResourceLocation, ModelFile> makeModel) {
        var builder = getBuilder(name);
        for (int i = 0; i < BlockQuenchedAllay.VARIANTS; i++) {
            var textureLoc = modLoc(path + "_" + i);
            var model = makeModel.apply(name, textureLoc);

            builder.override()
                .predicate(GaslightingTracker.GASLIGHTING_PRED, i)
                .model(model)
                .end();
        }
    }
}
