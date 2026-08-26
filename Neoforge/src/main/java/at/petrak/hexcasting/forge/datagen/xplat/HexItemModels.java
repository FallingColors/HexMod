package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.client.render.GaslightingTracker;
import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.items.pigment.ItemPridePigment;
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.items.storage.ItemSlate;
import at.petrak.hexcasting.common.items.storage.ItemThoughtKnot;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.paucal.forge.api.datagen.PaucalItemModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

public class HexItemModels extends PaucalItemModelProvider {

    public HexItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, HexAPI.MOD_ID, existingFileHelper);
    }

    private static final String[] PHIAL_SIZES = {"small", "medium", "large", "larger", "largest"};
    private static final Integer[] PACKAGED_SPELL_HANDHELD_VARIANTS = {5};

    // TODO port: maybe consider using registry lookup? But it's completable future... Not sure
    private String getPath(Item item) {
        return Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)).getPath();
    }

    private String getPath(Block block) {
        return Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath();
    }

    @Override
    protected void registerModels() {
        simpleItem(HexItems.AMETHYST_DUST.get());
        simpleItem(HexItems.CHARGED_AMETHYST.get());
        simpleItem(HexItems.SUBMARINE_SANDWICH.get());
        simpleItem(HexItems.ABACUS.get());
        brandishedItem(HexItems.JEWELER_HAMMER.get());
        simpleItem(HexItems.CREATIVE_UNLOCKER.get());
        simpleItem(HexItems.LORE_FRAGMENT.get());
        simpleItem(HexItems.NEURAL_FIBER.get());

        singleTexture(getPath(HexBlocks.CONJURED_BLOCK.get()),
            ResourceLocation.withDefaultNamespace("item/generated"),
            "layer0", ResourceLocation.withDefaultNamespace("item/amethyst_shard"));
        singleTexture(getPath(HexBlocks.CONJURED_LIGHT.get()),
            ResourceLocation.withDefaultNamespace("item/generated"),
            "layer0", ResourceLocation.withDefaultNamespace("item/amethyst_shard"));

        for (var age : new String[]{"pristine", "ancient"}) {
            for (var size : new String[]{"small", "medium", "large"}) {
                simpleItem(modLoc("scroll_" + age + "_" + size));
            }
        }
        buildScroll(HexItems.SCROLL_SMOL.get(), "small");
        buildScroll(HexItems.SCROLL_MEDIUM.get(), "medium");
        buildScroll(HexItems.SCROLL_LARGE.get(), "large");

        simpleItem(HexItems.SCRYING_LENS.get());
        getBuilder(getPath(HexItems.SCRYING_LENS.get()))
            .transforms()
            .transform(ItemDisplayContext.HEAD)
            .rotation(0f, 0f, 0f)
            .translation(-2.5f, 0f, -8f)
            .scale(0.4f);

        buildRobes(HexItems.ROBES_HOOD.get(), "hood", HexItems.ROBES_HOOD.get().numVariants());
        buildRobes(HexItems.ROBES_TUNIC.get(), "tunic", HexItems.ROBES_HOOD.get().numVariants());
        buildRobes(HexItems.ROBES_LEGS.get(), "legs", HexItems.ROBES_HOOD.get().numVariants());
        buildRobes(HexItems.ROBES_BOOTS.get(), "boots", HexItems.ROBES_HOOD.get().numVariants());

        singleTexture("old_staff", ResourceLocation.withDefaultNamespace("item/handheld_rod"),
            "layer0", modLoc("item/staff/old"));
        singleTexture("cherry_staff", ResourceLocation.withDefaultNamespace("item/handheld_rod"),
            "layer0", modLoc("item/staff/cherry"));

        buildStaff(HexItems.STAFF_OAK.get(), "oak");
        buildStaff(HexItems.STAFF_BIRCH.get(), "birch");
        buildStaff(HexItems.STAFF_SPRUCE.get(), "spruce");
        buildStaff(HexItems.STAFF_JUNGLE.get(), "jungle");
        buildStaff(HexItems.STAFF_DARK_OAK.get(), "dark_oak");
        buildStaff(HexItems.STAFF_ACACIA.get(), "acacia");
        buildStaff(HexItems.STAFF_CRIMSON.get(), "crimson");
        buildStaff(HexItems.STAFF_WARPED.get(), "warped");
        buildStaff(HexItems.STAFF_MANGROVE.get(), "mangrove");
        buildStaff(HexItems.STAFF_CHERRY.get(), "cherry");
        buildStaff(HexItems.STAFF_BAMBOO.get(), "bamboo");
        buildStaff(HexItems.STAFF_EDIFIED.get(), "edified");
        buildStaff(HexItems.STAFF_MINDSPLICE.get(), "mindsplice");

        // again, doesn't like paths with slashes in them, so we do it manually
        buildFourVariantGaslight("item/staff/quenched", "item/staff/quenched", (name, path) ->
            singleTexture(path.getPath(), ResourceLocation.withDefaultNamespace("item/handheld_rod"),
                "layer0", modLoc(path.getPath())));
        buildFourVariantGaslight(getPath(HexItems.QUENCHED_SHARD.get()), "item/quenched_shard", (name, path) ->
            singleTexture(path.getPath(), ResourceLocation.withDefaultNamespace("item/handheld"),
                "layer0", modLoc(path.getPath())));
        buildFourVariantGaslight(getPath(HexBlocks.QUENCHED_ALLAY.get()), "block/quenched_allay", (name, path) ->
            cubeAll(path.getPath(), path));
        buildFourVariantGaslight(getPath(HexBlocks.QUENCHED_ALLAY_TILES.get()), "block/deco/quenched_allay_tiles", (name, path) ->
                cubeAll(path.getPath(), path));
        buildFourVariantGaslight(getPath(HexBlocks.QUENCHED_ALLAY_BRICKS.get()), "block/deco/quenched_allay_bricks", (name, path) ->
                cubeAll(path.getPath(), path));
        buildFourVariantGaslight(getPath(HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get()), "block/deco/quenched_allay_bricks_small", (name, path) ->
                cubeAll(path.getPath(), path));

        simpleItem(modLoc("patchouli_book"));

        buildThoughtKnot();
        buildSealableIotaHolder(HexItems.FOCUS.get(), "focus", HexItems.FOCUS.get().numVariants());
        buildSealableIotaHolder(HexItems.SPELLBOOK.get(), "spellbook", HexItems.SPELLBOOK.get().numVariants());

        buildPackagedSpell(HexItems.ANCIENT_CYPHER.get(), "ancient_cypher", HexItems.ANCIENT_CYPHER.get().numVariants());
        buildPackagedSpell(HexItems.CYPHER.get(), "cypher", HexItems.CYPHER.get().numVariants());
        buildPackagedSpell(HexItems.TRINKET.get(), "trinket", HexItems.TRINKET.get().numVariants());
        buildPackagedSpell(HexItems.ARTIFACT.get(), "artifact", HexItems.ARTIFACT.get().numVariants());

        int maxFill = 4;
        for (int size = 0; size < PHIAL_SIZES.length; size++) {
            for (int fill = 0; fill <= maxFill; fill++) {
                String name = "phial_" + PHIAL_SIZES[size] + "_" + fill;
                singleTexture(
                    name,
                    ResourceLocation.withDefaultNamespace("item/generated"),
                    "layer0", modLoc("item/phial/" + name));

                float fillProp = (float) fill / maxFill;
                getBuilder(getPath(HexItems.BATTERY.get())).override()
                    .predicate(ItemMediaBattery.MEDIA_PREDICATE, fillProp)
                    .predicate(ItemMediaBattery.MAX_MEDIA_PREDICATE, size)
                    .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name)))
                    .end();
            }
        }

        for (var dye : DyeColor.values()) {
            singleTexture(getPath(HexItems.DYE_PIGMENTS.get(dye).get()),
                ResourceLocation.withDefaultNamespace("item/generated"),
                "layer0", modLoc("item/colorizer/dye_" + dye.getName()));
        }
        for (var type : ItemPridePigment.Type.values()) {
            singleTexture(getPath(HexItems.PRIDE_PIGMENTS.get(type).get()),
                ResourceLocation.withDefaultNamespace("item/generated"),
                "layer0", modLoc("item/colorizer/pride_" + type.getName()));
        }
        singleTexture(getPath(HexItems.UUID_PIGMENT.get()), ResourceLocation.withDefaultNamespace("item/generated"),
            "layer0", modLoc("item/colorizer/uuid"));
        singleTexture(getPath(HexItems.DEFAULT_PIGMENT.get()), ResourceLocation.withDefaultNamespace("item/generated"),
            "layer0", modLoc("item/colorizer/default"));
        singleTexture(getPath(HexItems.ANCIENT_PIGMENT.get()), ResourceLocation.withDefaultNamespace("item/generated"),
            "layer0", modLoc("item/colorizer/ancient"));

        simpleItem(modLoc("slate_blank"));
        simpleItem(modLoc("slate_written"));
        getBuilder(getPath(HexItems.SLATE.get())).override()
            .predicate(ItemSlate.WRITTEN_PRED, 0)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_blank")))
            .end()
            .override()
            .predicate(ItemSlate.WRITTEN_PRED, 1)
            .model(new ModelFile.UncheckedModelFile(modLoc("item/slate_written")))
            .end();

        getBuilder(getPath(HexBlocks.SLATE_PILLAR.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/slate_pillar")));
        getBuilder(getPath(HexBlocks.AMETHYST_PILLAR.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/deco/amethyst_pillar")));
        getBuilder(getPath(HexBlocks.SLATE_AMETHYST_PILLAR.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/slate_amethyst_pillar")));

        getBuilder(getPath(HexBlocks.AKASHIC_RECORD.get())).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/akashic_record")));
        simpleItem(modLoc("edified_door"));
        getBuilder(getPath(HexBlocks.EDIFIED_TRAPDOOR.get())).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_trapdoor_bottom")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG.get())).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_log")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG_AMETHYST.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_log_amethyst")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG_AVENTURINE.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_log_aventurine")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG_CITRINE.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_log_citrine")));
        getBuilder(getPath(HexBlocks.EDIFIED_LOG_PURPLE.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_log_purple")));
        getBuilder(getPath(HexBlocks.STRIPPED_EDIFIED_LOG.get())).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/stripped_edified_log")));
        getBuilder(getPath(HexBlocks.EDIFIED_WOOD.get())).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_wood")));
        getBuilder(getPath(HexBlocks.STRIPPED_EDIFIED_WOOD.get())).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/stripped_edified_wood")));
        getBuilder(getPath(HexBlocks.EDIFIED_STAIRS.get())).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_stairs")));
        getBuilder(getPath(HexBlocks.EDIFIED_FENCE.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_fence_inventory")));
        getBuilder(getPath(HexBlocks.EDIFIED_FENCE_GATE.get())).parent(
                new ModelFile.UncheckedModelFile(modLoc("block/edified_fence_gate")));
        getBuilder(getPath(HexBlocks.EDIFIED_SLAB.get())).parent(
            new ModelFile.UncheckedModelFile(modLoc("block/edified_slab")));
        getBuilder(getPath(HexBlocks.EDIFIED_BUTTON.get())).parent(
                new ModelFile.UncheckedModelFile(ResourceLocation.withDefaultNamespace("block/button_inventory")))
            .texture("texture", modLoc("block/edified_planks"));
        getBuilder(getPath(HexBlocks.EDIFIED_PRESSURE_PLATE.get()))
            .parent(new ModelFile.UncheckedModelFile(modLoc("block/edified_pressure_plate")));
    }

    private void buildThoughtKnot() {
        var unwritten = singleTexture("thought_knot", ResourceLocation.withDefaultNamespace("item/generated"),
            "layer0", modLoc("item/thought_knot"));
        var written = withExistingParent("thought_knot_written", ResourceLocation.withDefaultNamespace("item/generated"))
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
            var plain = i == 0 ? singleTexture(name, ResourceLocation.withDefaultNamespace("item/generated"),
                    "layer0", modLoc("item/cad/" + i + "_" + stub + "_empty"))
                    : withExistingParent(name + "_" + i, ResourceLocation.withDefaultNamespace("item/generated"))
                    .texture("layer0", modLoc("item/cad/" + i + "_" + stub + "_empty"));
            var unsealed = withExistingParent(name + "_" + i + "_filled", ResourceLocation.withDefaultNamespace("item/generated"))
                    .texture("layer0", modLoc("item/cad/" + i + "_" + stub + "_filled"))
                    .texture("layer1", modLoc("item/cad/" + i + "_" + stub + "_filled_overlay"));
            var sealed = withExistingParent(name + "_" + i + "_sealed", ResourceLocation.withDefaultNamespace("item/generated"))
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
        singleTexture("item/" + getPath(item), ResourceLocation.withDefaultNamespace("item/handheld_rod"),
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

    private void buildRobes(Item item, String type, int numVariants) {
        // TODO: actually handle the variants
//        singleTexture("item/" + getPath(item), ResourceLocation.withDefaultNamespace("item/generated"),
//            "layer0", modLoc("item/robes/1_"+type));
        var name = "item/" + getPath(item);
        var builder = getBuilder(name);
        for (int i = 0; i < numVariants; i++) {
            var parent_tag = "item/generated";
            var model = i == 0 ? singleTexture(name, ResourceLocation.withDefaultNamespace(parent_tag),
                "layer0", modLoc("item/robes/" + i + "_" + type))
                : withExistingParent(name + "_" + i, ResourceLocation.withDefaultNamespace(parent_tag))
                .texture("layer0", modLoc("item/robes/" + i + "_" + type));
            builder.override().predicate(ItemFocus.VARIANT_PRED, i).model(model).end();
        }
    }

    private void buildPackagedSpell(Item item, String stub, int numVariants) {
        var name = getPath(item);
        var builder = getBuilder(name);
        for (int i = 0; i < numVariants; i++) {

            var parent_tag = Arrays.asList(PACKAGED_SPELL_HANDHELD_VARIANTS).contains(i) ? "item/handheld_rod" : "item/generated";

            var plain = i == 0 ? singleTexture(name, ResourceLocation.withDefaultNamespace(parent_tag),
                    "layer0", modLoc("item/cad/" + i + "_" + stub))
                    : withExistingParent(name + "_" + i, ResourceLocation.withDefaultNamespace(parent_tag))
                    .texture("layer0", modLoc("item/cad/" + i + "_" + stub));
            var filled = withExistingParent(name + "_" + i + "_filled", ResourceLocation.withDefaultNamespace(parent_tag))
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
