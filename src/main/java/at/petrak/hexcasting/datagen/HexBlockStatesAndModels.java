package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.circle.BlockCircleComponent;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.paucal.api.datagen.PaucalBlockStateAndModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

public class HexBlockStatesAndModels extends PaucalBlockStateAndModelProvider {
    public HexBlockStatesAndModels(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, HexMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var slateModel = models().getExistingFile(modLoc("slate"));
        getVariantBuilder(HexBlocks.SLATE.get()).forAllStates(bs -> {
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
        });

        impetus(HexBlocks.IMPETUS_RIGHTCLICK.get(), "impetus_rightclick", "rightclick");
        impetus(HexBlocks.IMPETUS_LOOK.get(), "impetus_look", "look");
        arrowCircleBlock(HexBlocks.EMPTY_IMPETUS.get(), "empty_impetus", modLoc("block/slate"),
            "impetus/front_empty",
            "impetus/back_empty",
            "impetus/up_empty",
            "impetus/down_empty",
            "impetus/left_empty",
            "impetus/right_empty"
        );

        blockAndItem(HexBlocks.SLATE_BLOCK.get(), models().cubeAll("slate_block", modLoc("block/slate")));
        cubeBlockAndItem(HexBlocks.AMETHYST_DUST_BLOCK.get(), "amethyst_dust_block");
        cubeBlockAndItem(HexBlocks.AMETHYST_TILES.get(), "amethyst_tiles");
        cubeBlockAndItem(HexBlocks.SCROLL_PAPER.get(), "scroll_paper");
        cubeBlockAndItem(HexBlocks.ANCIENT_SCROLL_PAPER.get(), "ancient_scroll_paper");
        cubeBlockAndItem(HexBlocks.SCROLL_PAPER_LANTERN.get(), "scroll_paper_lantern");
        cubeBlockAndItem(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN.get(), "ancient_scroll_paper_lantern");

        var sconceModel = models().getExistingFile(modLoc("amethyst_sconce"));
        simpleBlock(HexBlocks.SCONCE.get(), sconceModel);
        simpleBlockItem(HexBlocks.SCONCE.get(), sconceModel);
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

            var modelName = name + "_" + litness + "_" + dir.getName();
            var model = models().cube(modelName, bottom, top, north, south, east, west)
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
}
