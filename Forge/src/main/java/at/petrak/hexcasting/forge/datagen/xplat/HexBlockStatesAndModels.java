package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockBooleanDirectrix;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockRedstoneDirectrix;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.paucal.api.forge.datagen.PaucalBlockStateAndModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import static net.minecraftforge.client.model.generators.ModelProvider.BLOCK_FOLDER;

public class HexBlockStatesAndModels extends PaucalBlockStateAndModelProvider {
    public HexBlockStatesAndModels(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, HexAPI.MOD_ID, exFileHelper);
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

        impetus(HexBlocks.IMPETUS_EMPTY, "impetus/empty", "empty", false);
        impetus(HexBlocks.IMPETUS_RIGHTCLICK, "impetus/rightclick", "rightclick", true);
        impetus(HexBlocks.IMPETUS_LOOK, "impetus/look", "look", true);
        impetus(HexBlocks.IMPETUS_REDSTONE, "impetus/redstone", "redstone", true);
        doAllTheDirectrices();

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
        blockAndItem(HexBlocks.SLATE_TILES, models().cubeAll("block/deco/slate_tiles", modLoc("block/deco/slate_tiles")));
        blockAndItem(HexBlocks.SLATE_BRICKS, models().cubeAll("block/deco/slate_bricks", modLoc("block/deco/slate_bricks")));
        blockAndItem(HexBlocks.SLATE_BRICKS_SMALL, models().cubeAll("block/deco/slate_bricks_small", modLoc("block/deco/slate_bricks_small")));
        axisBlock(HexBlocks.SLATE_PILLAR, modLoc("block/deco/slate_pillar"));
        blockAndItem(HexBlocks.AMETHYST_DUST_BLOCK,
            models().singleTexture("amethyst_dust_block", modLoc(BLOCK_FOLDER + "/cube_half_mirrored"), "all",
                modLoc("block/amethyst_dust_block")));
        blockAndItem(HexBlocks.AMETHYST_TILES, models().cubeAll("block/deco/amethyst_tiles", modLoc("block/deco/amethyst_tiles")));
        blockAndItem(HexBlocks.AMETHYST_BRICKS, models().cubeAll("block/deco/amethyst_bricks", modLoc("block/deco/amethyst_bricks")));
        blockAndItem(HexBlocks.AMETHYST_BRICKS_SMALL, models().cubeAll("block/deco/amethyst_bricks_small", modLoc("block/deco/amethyst_bricks_small")));
        directionalBlock(HexBlocks.AMETHYST_PILLAR,
                models().cubeBottomTop("block/deco/amethyst_pillar",
                        modLoc("block/deco/amethyst_pillar_side"),
                        modLoc("block/deco/amethyst_pillar_bottom"),
                        modLoc("block/deco/amethyst_pillar_top")));
        blockAndItem(HexBlocks.SLATE_AMETHYST_TILES, models().cubeAll("block/deco/slate_amethyst_tiles", modLoc("block/deco/slate_amethyst_tiles")));

        simpleBlock(HexBlocks.SLATE_AMETHYST_BRICKS,
            new ConfiguredModel(models().cubeAll("block/deco/slate_amethyst_bricks_0", modLoc("block/deco/slate_amethyst_bricks_0"))),
            new ConfiguredModel(models().cubeAll("block/deco/slate_amethyst_bricks_1", modLoc("block/deco/slate_amethyst_bricks_1"))),
            new ConfiguredModel(models().cubeAll("block/deco/slate_amethyst_bricks_2", modLoc("block/deco/slate_amethyst_bricks_2")))
        );
        simpleBlockItem(HexBlocks.SLATE_AMETHYST_BRICKS, models().cubeAll("block/deco/slate_amethyst_bricks_0", modLoc("block/deco/slate_amethyst_bricks_0")));

        simpleBlock(HexBlocks.SLATE_AMETHYST_BRICKS_SMALL,
                new ConfiguredModel(models().cubeAll("block/deco/slate_amethyst_bricks_small_0", modLoc("block/deco/slate_amethyst_bricks_small_0"))),
                new ConfiguredModel(models().cubeAll("block/deco/slate_amethyst_bricks_small_1", modLoc("block/deco/slate_amethyst_bricks_small_1"))),
                new ConfiguredModel(models().cubeAll("block/deco/slate_amethyst_bricks_small_2", modLoc("block/deco/slate_amethyst_bricks_small_2")))
        );
        simpleBlockItem(HexBlocks.SLATE_AMETHYST_BRICKS_SMALL, models().cubeAll("block/deco/slate_amethyst_bricks_small_0", modLoc("block/deco/slate_amethyst_bricks_small_0")));

        axisBlock(HexBlocks.SLATE_AMETHYST_PILLAR, modLoc("block/deco/slate_amethyst_pillar"));
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
        axisBlock(HexBlocks.EDIFIED_LOG_AMETHYST, modLoc("block/deco/edified_log_amethyst"), modLoc("block/edified_log_top"));
        axisBlock(HexBlocks.EDIFIED_LOG_AVENTURINE, modLoc("block/deco/edified_log_aventurine"), modLoc("block/edified_log_top"));
        axisBlock(HexBlocks.EDIFIED_LOG_CITRINE, modLoc("block/deco/edified_log_citrine"), modLoc("block/edified_log_top"));
        axisBlock(HexBlocks.EDIFIED_LOG_PURPLE, modLoc("block/deco/edified_log_purple"), modLoc("block/edified_log_top"));
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
        simpleBlock(HexBlocks.QUENCHED_ALLAY_TILES, models().cubeAll("quenched_allay_tiles", modLoc("block/deco/quenched_allay_tiles_0")));
        simpleBlock(HexBlocks.QUENCHED_ALLAY_BRICKS, models().cubeAll("quenched_allay_bricks", modLoc("block/deco/quenched_allay_bricks_0")));
        simpleBlock(HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL, models().cubeAll("quenched_allay_bricks_small", modLoc("block/deco/quenched_allay_bricks_small_0")));
    }

    // Assumes that the bottom are always the same
    private void arrowCircleBlock(Block block, String name, ResourceLocation particle,
        String frontStub,
        String topStob,
        String leftStub,
        String rightStub,
        String backStub,
        boolean itemModelIsLit
    ) {
        getVariantBuilder(block).forAllStates(bs -> {
            boolean isLit = bs.getValue(BlockCircleComponent.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var dir = bs.getValue(BlockStateProperties.FACING);

            // I wish we didn't have to put the top "upside down" but there you go
            var front = "block/circle/" + frontStub + "_" + litness;
            var top = "block/circle/" + topStob + "_" + litness;
            var left = "block/circle/" + leftStub + "_" + litness;
            var right = "block/circle/" + rightStub + "_" + litness;
            var back = "block/circle/" + backStub + "_" + litness;
            // and never light the bottom
            var bottom = "block/circle/bottom";

            var modelName = "block/circle/" + name + "/" + litness + "_" + dir.getName();
            var model = models().cube(modelName, modLoc(bottom), modLoc(top), modLoc(front), modLoc(back),
                    modLoc(left), modLoc(right))
                .texture("particle", particle);

            // Most blocks point north in the inv, but we have these point east so that their faces aren't obscured
            // by the count number
            if (isLit == itemModelIsLit && dir == Direction.EAST) {
                itemModels().getBuilder("item/" + name).parent(model);
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

    private void impetus(Block block, String name, String stub, boolean itemModelIsLit) {
        arrowCircleBlock(block, name, modLoc("block/slate"),
            "impetus/" + stub + "/front",
            "impetus/" + stub + "/top",
            "impetus/" + stub + "/left",
            "impetus/" + stub + "/right",
            "impetus/back",
            itemModelIsLit
        );
    }

    private void doAllTheDirectrices() {
        arrowCircleBlock(HexBlocks.EMPTY_DIRECTRIX, "directrix/empty", modLoc("block/slate"),
            "directrix/empty/front", "directrix/empty/top", "directrix/empty/left",
            "directrix/empty/right", "directrix/empty/back", false);

        // Note that "unpowered" means the jowls of the back face are ON.
        getVariantBuilder(HexBlocks.DIRECTRIX_REDSTONE).forAllStates(bs -> {
            var isLit = bs.getValue(BlockCircleComponent.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var isPowered = bs.getValue(BlockRedstoneDirectrix.REDSTONE_POWERED);
            var poweredness = isPowered ? "powered" : "unpowered";
            var dir = bs.getValue(BlockStateProperties.FACING);

            var top = "block/circle/directrix/redstone/top_" + poweredness;
            var left = "block/circle/directrix/redstone/left_" + poweredness;
            var right = "block/circle/directrix/redstone/right_" + poweredness;

            // The front face can never be both lit and unpowered (b/c otherwise it would exit the other way)
            String frontEnding, backEnding;
            if (isLit) {
                if (isPowered) {
                    frontEnding = "lit_powered";
                    backEnding = "dim_powered";
                } else {
                    frontEnding = "dim_unpowered";
                    backEnding = "lit_unpowered";
                }
            } else {
                frontEnding = "dim_" + poweredness;
                backEnding = "dim_" + poweredness;
            }
            var front = "block/circle/directrix/redstone/front_" + frontEnding;
            var back = "block/circle/directrix/redstone/back_" + backEnding;
            // and always the same
            var bottom = "block/circle/bottom";


            var modelName = "block/circle/directrix/redstone/" + litness + "_" + poweredness + "_" + dir.getName();
            var model = models().cube(modelName, modLoc(bottom), modLoc(top), modLoc(front), modLoc(back),
                    modLoc(left), modLoc(right))
                .texture("particle", modLoc("block/slate"));

            if (isLit && !isPowered && dir == Direction.EAST) {
                // getBuilder does not add the block/etc to the front if the path contains any slashes
                // this is a problem because the block IDs have slashes in them
                itemModels().getBuilder("item/directrix/redstone").parent(model);
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

        getVariantBuilder(HexBlocks.DIRECTRIX_BOOLEAN).forAllStates(bs -> {
            var isLit = bs.getValue(BlockCircleComponent.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var boolState = bs.getValue(BlockBooleanDirectrix.STATE);
            var boolStateString = boolState.toString().toLowerCase();
            var dir = bs.getValue(BlockStateProperties.FACING);

            var top = "block/circle/directrix/boolean/top_" + boolStateString;
            var left = "block/circle/directrix/boolean/left_" + boolStateString;
            var right = "block/circle/directrix/boolean/right_" + boolStateString;

            // The front face can never be both lit and unpowered (b/c otherwise it would exit the other way)
            String frontEnding = null, backEnding = null;
            switch (boolState) {
                case NEITHER -> {
                    frontEnding = "not_false";
                    backEnding = "not_true";
                }
                case TRUE -> {
                    frontEnding = "not_false";
                    backEnding = litness + "_true";
                }
                case FALSE -> {
                    frontEnding = litness + "_false";
                    backEnding = "not_true";
                }
            }

            var front = "block/circle/directrix/boolean/front_" + frontEnding;
            var back = "block/circle/directrix/boolean/back_" + backEnding;
            // and always the same
            var bottom = "block/circle/bottom";


            var modelName = "block/circle/directrix/boolean/" + litness + "_" + boolStateString + "_" + dir.getName();
            var model = models().cube(modelName, modLoc(bottom), modLoc(top), modLoc(front), modLoc(back),
                            modLoc(left), modLoc(right))
                    .texture("particle", modLoc("block/slate"));

            if (isLit && boolState == BlockBooleanDirectrix.State.FALSE && dir == Direction.EAST) {
                // getBuilder does not add the block/etc to the front if the path contains any slashes
                // this is a problem because the block IDs have slashes in them
                itemModels().getBuilder("item/directrix/boolean").parent(model);
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
