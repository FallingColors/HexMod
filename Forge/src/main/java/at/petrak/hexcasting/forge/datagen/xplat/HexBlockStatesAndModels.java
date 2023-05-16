package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.paucal.api.forge.datagen.PaucalBlockStateAndModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import static net.minecraftforge.client.model.generators.ModelProvider.BLOCK_FOLDER;

public class HexBlockStatesAndModels extends PaucalBlockStateAndModelProvider {
    public HexBlockStatesAndModels(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, HexAPI.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var slateModel = models().getExistingFile(modLoc("slate"));
        getVariantBuilder(HexBlocks.SLATE).forAllStatesExcept(bs -> {
            int rotationX = 0;
            int rotationY = 0;
            switch (bs.getValue(BlockSlate.ATTACH_FACE)) {
                case CEILING -> rotationX = 180;
                case WALL -> {
                    rotationX = 90;
                    rotationY = bs.getValue(BlockSlate.FACING).getOpposite().get2DDataValue() * 90;
                }
            }
            return ConfiguredModel.builder()
                .modelFile(slateModel)
                .rotationX(rotationX)
                .rotationY(rotationY)
                .uvLock(true)
                .build();
        }, BlockSlate.WATERLOGGED);

        arrowCircleBlock(HexBlocks.EMPTY_IMPETUS, "empty_impetus", "impetus/empty", modLoc("block/slate"));
        arrowCircleBlock(HexBlocks.IMPETUS_RIGHTCLICK, "toolsmith_impetus", "impetus/toolsmith", modLoc("block/slate"));
        arrowCircleBlock(HexBlocks.IMPETUS_LOOK, "fletcher_impetus", "impetus/fletcher", modLoc("block/slate"));
        arrowCircleBlock(HexBlocks.IMPETUS_REDSTONE, "cleric_impetus", "impetus/cleric", modLoc("block/slate"));

        var akashicRecordModel = models().withExistingParent("akashic_record", "block/block")
            .renderType("translucent")
            .texture("inner", modLoc("block/akashic_ligature"))
            .texture("outer", modLoc("block/akashic_record"))
            .texture("particle", modLoc("block/akashic_ligature"))
            .element()
            .cube("#outer")
            .end()
            .element()
            .from(15.75f, 15.75f, 15.75f)
            .to(0.25f, 0.25f, 0.25f)
            .allFaces((dir, builder) -> builder.texture("#inner").rotation(ModelBuilder.FaceRotation.UPSIDE_DOWN))
            .end();

        simpleBlock(HexBlocks.AKASHIC_RECORD, akashicRecordModel);
        simpleBlockItem(HexBlocks.AKASHIC_RECORD, akashicRecordModel);
        blockAndItem(HexBlocks.AKASHIC_LIGATURE,
            models().cubeAll("akashic_ligature", modLoc("block/akashic_ligature")));

        models().getBuilder("akashic_bookshelf")
            .renderType("cutout")
            .texture("front", modLoc("block/akashic_bookshelf"))
            .texture("side", modLoc("block/akashic_bookshelf_horiz"))
            .texture("top_bottom", modLoc("block/akashic_bookshelf_vert"))
            .texture("particle", modLoc("block/akashic_bookshelf_vert"))
            .element()
            .allFaces((dir, builder) -> builder.texture(switch (dir) {
                case DOWN, UP -> "#top_bottom";
                case EAST, SOUTH, WEST -> "#side";
                default -> "#front";
            }).cullface(dir))
            .end()
            .element()
            .face(Direction.NORTH).texture("#overlay").cullface(Direction.NORTH).tintindex(0);

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
                .texture("all", modLoc("block/amethyst_edified_leaves"))
                .renderType("cutout_mipped"));
        blockAndItem(HexBlocks.AVENTURINE_EDIFIED_LEAVES,
            models().withExistingParent("aventurine_edified_leaves", leavesParent)
                .texture("all", modLoc("block/aventurine_edified_leaves"))
                .renderType("cutout_mipped"));
        blockAndItem(HexBlocks.CITRINE_EDIFIED_LEAVES,
            models().withExistingParent("citrine_edified_leaves", leavesParent)
                .texture("all", modLoc("block/citrine_edified_leaves"))
                .renderType("cutout_mipped"));

        doorBlockWithRenderType(HexBlocks.EDIFIED_DOOR, modLoc("block/edified_door_lower"), modLoc("block" +
            "/edified_door_upper"), "cutout");
        // door model via the given texture
        trapdoorBlockWithRenderType(HexBlocks.EDIFIED_TRAPDOOR, modLoc("block/edified_trapdoor"), true, "cutout");

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

        var conjuredModel = models().getBuilder("conjured").texture("particle", mcLoc("block/amethyst_block"))
            .renderType("cutout");
        simpleBlock(HexBlocks.CONJURED_BLOCK, conjuredModel);
        simpleBlock(HexBlocks.CONJURED_LIGHT, conjuredModel);

        // for the break particles
        simpleBlock(HexBlocks.QUENCHED_ALLAY, models().cubeAll("quenched_allay", modLoc("block/quenched_allay_0")));
    }

    // Assumes that the bottom and back are always the same
    private void arrowCircleBlock(Block block, String name, String texStub, ResourceLocation particle) {
        getVariantBuilder(block).forAllStates(bs -> {
            var isLit = bs.getValue(BlockCircleComponent.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var dir = bs.getValue(BlockStateProperties.FACING);

            // I wish we didn't have to put the top "upside down" but there you go
            var front = "block/circle/" + texStub + "_front_" + litness;
            var top = "block/circle/" + texStub + "_top_" + litness;
            var left = "block/circle/" + texStub + "_left_" + litness;
            var right = "block/circle/" + texStub + "_right_" + litness;
            // and always the same
            var back = "block/circle/back_" + litness;
            var bottom = "block/circle/bottom";

            var modelName = name + "_" + litness + "_" + dir.getName();
            var model = models().cube(modelName, modLoc(bottom), modLoc(top), modLoc(front), modLoc(back),
                    modLoc(left), modLoc(right))
                .texture("particle", particle);

            // Something about the way I do something changed so now north is the direction
            // not covered by the stack number. Who knows
            if (!isLit && dir == Direction.NORTH) {
                simpleBlockItem(block, model);
            }

            return ConfiguredModel.builder()
                .modelFile(model)
                // this code has been stolen from myself several times
                .rotationX(dir.getAxis() == Direction.Axis.Y
                    ? dir.getAxisDirection().getStep() * -90
                    : 0)
                .rotationY(dir.getAxis() != Direction.Axis.Y
                    ? ((dir.get2DDataValue() + 2) % 4) * 90
                    : 0)
                .build();
        });
    }
}
