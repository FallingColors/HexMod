package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.block.circle.BlockSidedCircleWidget;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.paucal.api.forge.datagen.PaucalBlockStateAndModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

import static net.minecraftforge.client.model.generators.ModelProvider.BLOCK_FOLDER;

public class HexBlockStatesAndModels extends PaucalBlockStateAndModelProvider {
    public HexBlockStatesAndModels(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, HexAPI.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var slateModel = models().getExistingFile(modLoc("slate"));

        var akashicRecordModel = models().getExistingFile(modLoc("block/akashic_record"));
        simpleBlock(HexBlocks.AKASHIC_RECORD, akashicRecordModel);
        simpleBlockItem(HexBlocks.AKASHIC_RECORD, akashicRecordModel);
        blockAndItem(HexBlocks.AKASHIC_LIGATURE,
            models().cubeAll("akashic_ligature", modLoc("block/akashic_ligature")));

        getVariantBuilder(HexBlocks.AKASHIC_BOOKSHELF).forAllStates(bs -> {
            Direction dir = bs.getValue(BlockAkashicBookshelf.FACING);

            var builder = ConfiguredModel.builder();

            if (bs.getValue(BlockAkashicBookshelf.HAS_BOOKS)) {
                for (int i = 1; i <= 4; i++) {
                    var model = models().withExistingParent("akashic_bookshelf_" + i,
                            modLoc("block/akashic_bookshelf"))
                        .texture("overlay", modLoc("block/akashic_bookshelf_overlay_" + i));

                    builder.modelFile(model)
                        .rotationY(dir.getOpposite().get2DDataValue() * 90)
                        .uvLock(true);
                    if (i < 4) {
                        builder = builder.nextModel();
                    }
                }
            } else {
                var model = models().orientable("akashic_bookshelf_empty",
                    modLoc("block/akashic_bookshelf_horiz"),
                    modLoc("block/akashic_bookshelf"),
                    modLoc("block/akashic_bookshelf_vert"));

                if (dir == Direction.NORTH) {
                    simpleBlockItem(HexBlocks.AKASHIC_BOOKSHELF, model);
                }

                builder.modelFile(model)
                    .rotationY(dir.getOpposite().get2DDataValue() * 90)
                    .uvLock(true);
            }

            return builder.build();
        });


        blockAndItem(HexBlocks.SLATE_BLOCK, models().cubeAll("slate_block", modLoc("block/slate")));
        blockAndItem(HexBlocks.AMETHYST_DUST_BLOCK,
            models().singleTexture("amethyst_dust_block", modLoc(BLOCK_FOLDER + "/cube_half_mirrored"), "all",
                modLoc("block/amethyst_dust_block")));
        cubeBlockAndItem(HexBlocks.AMETHYST_TILES, "amethyst_tiles");
        cubeBlockAndItem(HexBlocks.SCROLL_PAPER, "scroll_paper");
        cubeBlockAndItem(HexBlocks.ANCIENT_SCROLL_PAPER, "ancient_scroll_paper");

        blockAndItem(HexBlocks.SCROLL_PAPER_LANTERN, models().cubeBottomTop("scroll_paper_lantern",
            modLoc("block/scroll_paper_lantern_side"),
            modLoc("block/scroll_paper_lantern_bottom"),
            modLoc("block/scroll_paper_lantern_top")));

        blockAndItem(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN,
            models().cubeBottomTop("ancient_scroll_paper_lantern",
                modLoc("block/ancient_scroll_paper_lantern_side"),
                modLoc("block/ancient_scroll_paper_lantern_bottom"),
                modLoc("block/ancient_scroll_paper_lantern_top")));

        axisBlock(HexBlocks.EDIFIED_LOG, modLoc("block/edified_log"), modLoc("block/edified_log_top"));
        axisBlock(HexBlocks.STRIPPED_EDIFIED_LOG, modLoc("block/stripped_edified_log"),
            modLoc("block/stripped_edified_log_top"));
        axisBlock(HexBlocks.EDIFIED_WOOD, modLoc("block/edified_log"), modLoc("block/edified_log"));
        axisBlock(HexBlocks.STRIPPED_EDIFIED_WOOD, modLoc("block/stripped_edified_log"),
            modLoc("block/stripped_edified_log"));

        blockAndItem(HexBlocks.EDIFIED_PANEL, models().cubeAll("edified_panel", modLoc("block/edified_panel")));
        blockAndItem(HexBlocks.EDIFIED_TILE, models().cubeAll("edified_tile", modLoc("block/edified_tile")));

        ResourceLocation leavesParent = new ResourceLocation("block/leaves");
        blockAndItem(HexBlocks.AMETHYST_EDIFIED_LEAVES,
            models().withExistingParent("amethyst_edified_leaves", leavesParent)
                .texture("all", modLoc("block/amethyst_edified_leaves")));
        blockAndItem(HexBlocks.AVENTURINE_EDIFIED_LEAVES,
            models().withExistingParent("aventurine_edified_leaves", leavesParent)
                .texture("all", modLoc("block/aventurine_edified_leaves")));
        blockAndItem(HexBlocks.CITRINE_EDIFIED_LEAVES,
            models().withExistingParent("citrine_edified_leaves", leavesParent)
                .texture("all", modLoc("block/citrine_edified_leaves")));

        doorBlock(HexBlocks.EDIFIED_DOOR, modLoc("block/edified_door_lower"), modLoc("block/edified_door_upper"));
        // door model via the given texture
        trapdoorBlock(HexBlocks.EDIFIED_TRAPDOOR, modLoc("block/edified_trapdoor"), true);

        ResourceLocation planks1 = modLoc("block/edified_planks");
        BlockModelBuilder planksModel = models().cubeAll("edified_planks", planks1);
        simpleBlock(HexBlocks.EDIFIED_PLANKS, ConfiguredModel.builder()
            .modelFile(planksModel)
            .weight(3)
            .nextModel()
            .modelFile(models().cubeAll("edified_planks_2", modLoc("block/edified_planks_2")))
            .weight(3)
            .nextModel()
            .modelFile(models().cubeAll("edified_planks_3", modLoc("block/edified_planks_3")))
            .build());
        simpleBlockItem(HexBlocks.EDIFIED_PLANKS, planksModel);

        stairsBlock(HexBlocks.EDIFIED_STAIRS, planks1);
        slabBlock(HexBlocks.EDIFIED_SLAB, planks1, planks1);
        buttonBlock(HexBlocks.EDIFIED_BUTTON, planks1);
        pressurePlateBlock(HexBlocks.EDIFIED_PRESSURE_PLATE, planks1);

        var sconceModel = models().getExistingFile(modLoc("amethyst_sconce"));
        simpleBlock(HexBlocks.SCONCE, sconceModel);
        simpleBlockItem(HexBlocks.SCONCE, sconceModel);

        var conjuredModel = models().getExistingFile(modLoc("conjured"));
        simpleBlock(HexBlocks.CONJURED_BLOCK, conjuredModel);
        simpleBlock(HexBlocks.CONJURED_LIGHT, conjuredModel);
    }

    private void sidedCircleWidget(BlockSidedCircleWidget widget, String commonName, String coreName) {
        // We do really need to do this with a multipart builder. Unfortunately we cannot combine this with a variant builder,
        // so there's gonna be a *lot* of model files.
        // Each block needs a body + 4 connector models, for unenergized and energized versions, for non-margin
        // and margin versions. ("Margin" = the overlay model we stick on top, so some of the model rotates and some doesn't.)
        //
        // To avoid the generated folder becoming incredibly thicc and to save resource pack makers we put everything in a folder.
        //
        // Files are named {blockpath}/{part}['_' margin]['_' energized].
        // So, `toolsmith_impetus/body_energized` for the large chunk in the middle when active,
        // `farmer_locus/north_margin` for the north connector when rendering the overlay, etc.
    }
}
