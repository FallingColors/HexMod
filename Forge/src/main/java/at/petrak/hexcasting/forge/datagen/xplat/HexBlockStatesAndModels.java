package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicBookshelf;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.blocks.circles.directrix.BlockRedstoneDirectrix;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.paucal.api.forge.datagen.PaucalBlockStateAndModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

        impetus(HexBlocks.IMPETUS_RIGHTCLICK, "impetus_rightclick", "rightclick");
        impetus(HexBlocks.IMPETUS_LOOK, "impetus_look", "look");
        impetus(HexBlocks.IMPETUS_STOREDPLAYER, "impetus_storedplayer", "storedplayer");
        arrowCircleBlock(HexBlocks.EMPTY_IMPETUS, "empty_impetus", modLoc("block/slate"),
            "impetus/front_empty",
            "impetus/back_empty",
            "impetus/up_empty",
            "impetus/down_empty",
            "impetus/left_empty",
            "impetus/right_empty"
        );

        // auugh
        getVariantBuilder(HexBlocks.DIRECTRIX_REDSTONE).forAllStates(bs -> {
            var isLit = bs.getValue(BlockCircleComponent.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var isPowered = bs.getValue(BlockRedstoneDirectrix.REDSTONE_POWERED);
            var poweredness = isPowered ? "powered" : "unpowered";
            var dir = bs.getValue(BlockStateProperties.FACING);

            var up = modLoc("block/directrix/redstone/up_" + poweredness + "_" + litness);
            var left = modLoc("block/directrix/redstone/left_" + poweredness + "_" + litness);
            var right = modLoc("block/directrix/redstone/right_" + poweredness + "_" + litness);
            var down = modLoc("block/directrix/redstone/down_" + poweredness + "_" + litness);
            var front = modLoc("block/directrix/redstone/front_" + litness);
            var back = modLoc("block/directrix/redstone/back_" + poweredness);

            var routing = routeReslocsForArrowBlock(dir, front, back, up, down, left, right);

            var modelName = "redstone_directrix_" + poweredness + "_" + litness + "_" + dir.getName();
            var model = models().cube(modelName, routing[0], routing[1], routing[2], routing[3], routing[4], routing[5])
                .texture("particle", modLoc("block/slate"));
            if (!isLit && !isPowered && dir == Direction.NORTH) {
                simpleBlockItem(HexBlocks.DIRECTRIX_REDSTONE, model);
            }
            return ConfiguredModel.builder()
                .modelFile(model)
                .build();
        });
        getVariantBuilder(HexBlocks.EMPTY_DIRECTRIX).forAllStates(bs -> {
            var isLit = bs.getValue(BlockCircleComponent.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var axis = bs.getValue(BlockStateProperties.AXIS);

            var horiz = modLoc("block/directrix/empty/horiz_" + litness);
            var vert = modLoc("block/directrix/empty/vert_" + litness);
            var end = modLoc("block/directrix/empty/end_" + litness);

            ResourceLocation x = null, y = null, z = null;
            switch (axis) {
                case X -> {
                    x = end;
                    y = horiz;
                    z = horiz;
                }
                case Y -> {
                    x = vert;
                    y = end;
                    z = vert;
                }
                case Z -> {
                    x = horiz;
                    y = vert;
                    z = end;
                }
            }

            var modelName = "empty_directrix_" + litness + "_" + axis.getName();
            var model = models().cube(modelName, y, y, z, z, x, x)
                .texture("particle", modLoc("block/slate"));
            if (!isLit && axis == Direction.Axis.Z) {
                simpleBlockItem(HexBlocks.EMPTY_DIRECTRIX, model);
            }
            return ConfiguredModel.builder()
                .modelFile(model)
                .build();
        });

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

        var conjuredModel = models().getBuilder("conjured").texture("particle", mcLoc("block/amethyst_block"));
        simpleBlock(HexBlocks.CONJURED_BLOCK, conjuredModel);
        simpleBlock(HexBlocks.CONJURED_LIGHT, conjuredModel);
    }

    private void impetus(Block block, String name, String stub) {
        arrowCircleBlock(block, name, modLoc("block/slate"),
            "impetus/" + stub,
            "impetus/back",
            "impetus/up",
            "impetus/down",
            "impetus/left",
            "impetus/right"
        );
    }

    private void arrowCircleBlock(Block block, String name, ResourceLocation particle, String frontStub,
        String backStub, String upStub, String downStub, String leftStub, String rightStub) {
        getVariantBuilder(block).forAllStates(bs -> {
            var isLit = bs.getValue(BlockCircleComponent.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var dir = bs.getValue(BlockStateProperties.FACING);

            var up = modLoc("block/" + upStub + "_" + litness);
            var front = modLoc("block/" + frontStub + "_" + litness);
            var back = modLoc("block/" + backStub + "_" + litness);
            var left = modLoc("block/" + leftStub + "_" + litness);
            var right = modLoc("block/" + rightStub + "_" + litness);
            var down = modLoc("block/" + downStub + "_" + litness);

            var routing = routeReslocsForArrowBlock(dir, front, back, up, down, left, right);

            var modelName = name + "_" + litness + "_" + dir.getName();
            var model = models().cube(modelName, routing[0], routing[1], routing[2], routing[3], routing[4], routing[5])
                .texture("particle", particle);
            // Ordinarily i would use north, because north is the lower-right direction in the inv
            // and that's where other blocks face.
            // But impetuses are only distinguished by their front faces and I don't want it covered
            // by the number.
            if (!isLit && dir == Direction.EAST) {
                simpleBlockItem(block, model);
            }
            return ConfiguredModel.builder()
                .modelFile(model)
                .build();
        });
    }

    private static ResourceLocation[] routeReslocsForArrowBlock(Direction dir, ResourceLocation front,
        ResourceLocation back,
        ResourceLocation up, ResourceLocation down,
        ResourceLocation left, ResourceLocation right) {
        ResourceLocation bottom = null, top = null, north = null, south = null, east = null, west = null;
        switch (dir) {
            case UP -> {
                top = front;
                bottom = back;
                north = east = south = west = up;
            }
            case DOWN -> {
                bottom = front;
                top = back;
                north = east = south = west = down;
            }
            case NORTH -> {
                north = front;
                south = back;
                west = left;
                east = right;
                top = up;
                bottom = down;
            }
            case SOUTH -> {
                south = front;
                north = back;
                west = right;
                east = left;
                top = down;
                bottom = up;
            }
            case WEST -> {
                west = front;
                east = back;
                north = right;
                south = left;
                top = left;
                bottom = left;
            }
            case EAST -> {
                east = front;
                west = back;
                north = left;
                south = right;
                top = right;
                bottom = right;
            }
        }
        return new ResourceLocation[]{bottom, top, north, south, east, west};
    }

}
