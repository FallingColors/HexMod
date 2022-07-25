package at.petrak.hexcasting.forge.datagen.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.spell.DatumType;
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
        blockAndItem(HexBlocks.AKASHIC_CONNECTOR,
            models().cubeAll("akashic_connector", modLoc("block/akashic/connector")));

        getVariantBuilder(HexBlocks.AKASHIC_BOOKSHELF).forAllStates(bs -> {
            var type = bs.getValue(BlockAkashicBookshelf.DATUM_TYPE);

            var side = modLoc("block/akashic/bookshelf/side");
            var end = modLoc("block/akashic/bookshelf/end");

            String[] fronts;
            if (type == DatumType.EMPTY) {
                fronts = new String[]{"empty"};
            } else {
                fronts = new String[4];
                for (int i = 0; i < 4; i++) {
                    fronts[i] = type.getSerializedName() + (i + 1);
                }
            }

            var builder = ConfiguredModel.builder();

            for (int i = 0; i < fronts.length; i++) {
                var front = fronts[i];
                var model = models().orientable("akashic_bookshelf_" + type.getSerializedName() + i,
                    side, modLoc("block/akashic/bookshelf/" + front), end);

                Direction dir = bs.getValue(BlockAkashicBookshelf.FACING);
                if (dir == Direction.NORTH && type == DatumType.EMPTY) {
                    simpleBlockItem(HexBlocks.AKASHIC_BOOKSHELF, model);
                }

                builder.modelFile(model)
                    .rotationY(dir.getOpposite().get2DDataValue() * 90)
                    .uvLock(true);
                if (i < fronts.length - 1) {
                    builder.nextModel();
                }
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

        axisBlock(HexBlocks.AKASHIC_LOG, modLoc("block/akashic/log"), modLoc("block/akashic/log_end"));
        axisBlock(HexBlocks.AKASHIC_LOG_STRIPPED, modLoc("block/akashic/log_stripped"),
            modLoc("block/akashic/log_end_stripped"));
        axisBlock(HexBlocks.AKASHIC_WOOD, modLoc("block/akashic/log"), modLoc("block/akashic/log"));
        axisBlock(HexBlocks.AKASHIC_WOOD_STRIPPED, modLoc("block/akashic/log_stripped"),
            modLoc("block/akashic/log_stripped"));

        blockAndItem(HexBlocks.AKASHIC_PANEL, models().cubeAll("akashic_panel", modLoc("block/akashic/panel")));
        blockAndItem(HexBlocks.AKASHIC_TILE, models().cubeAll("akashic_tile", modLoc("block/akashic/tile")));

        ResourceLocation leavesParent = new ResourceLocation("block/leaves");
        blockAndItem(HexBlocks.AKASHIC_LEAVES1,
            models().withExistingParent("akashic_leaves1", leavesParent)
                .texture("all", modLoc("block/akashic/leaves1")));
        blockAndItem(HexBlocks.AKASHIC_LEAVES2,
            models().withExistingParent("akashic_leaves2", leavesParent)
                .texture("all", modLoc("block/akashic/leaves2")));
        blockAndItem(HexBlocks.AKASHIC_LEAVES3,
            models().withExistingParent("akashic_leaves3", leavesParent)
                .texture("all", modLoc("block/akashic/leaves3")));

        doorBlock(HexBlocks.AKASHIC_DOOR, modLoc("block/akashic/door_lower"), modLoc("block/akashic/door_upper"));
        // door model via the given texture
        trapdoorBlock(HexBlocks.AKASHIC_TRAPDOOR, modLoc("block/akashic/trapdoor"), true);

        ResourceLocation planks1 = modLoc("block/akashic/planks1");
        BlockModelBuilder planks_model = models().cubeAll("akashic_planks1", planks1);
        simpleBlock(HexBlocks.AKASHIC_PLANKS, ConfiguredModel.builder()
            .modelFile(planks_model)
            .weight(3)
            .nextModel()
            .modelFile(models().cubeAll("akashic_planks2", modLoc("block/akashic/planks2")))
            .weight(3)
            .nextModel()
            .modelFile(models().cubeAll("akashic_planks3", modLoc("block/akashic/planks3")))
            .build());
        simpleBlockItem(HexBlocks.AKASHIC_PLANKS, planks_model);

        stairsBlock(HexBlocks.AKASHIC_STAIRS, planks1);
        slabBlock(HexBlocks.AKASHIC_SLAB, modLoc("block/akashic_planks1"), planks1);
        buttonBlock(HexBlocks.AKASHIC_BUTTON, planks1);
        pressurePlateBlock(HexBlocks.AKASHIC_PRESSURE_PLATE, planks1);

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
