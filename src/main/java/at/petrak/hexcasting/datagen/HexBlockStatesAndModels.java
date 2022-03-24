package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.BlockCircleComponent;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

public class HexBlockStatesAndModels extends BlockStateProvider {
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

        var slateTex = modLoc("block/slate");
        arrowCircleBlock(HexBlocks.EMPTY_IMPETUS.get(), "empty_impetus", "block/impetus/empty", slateTex);
        arrowCircleBlock(HexBlocks.IMPETUS_RIGHTCLICK.get(), "impetus_rightclick", "block/impetus/rightclick",
            slateTex);

        blockAndItem(HexBlocks.SLATE_BLOCK.get(), models().cubeAll("slate_block", modLoc("block/slate")));
        reallySimpleBlockAndItem(HexBlocks.AMETHYST_DUST_BLOCK.get(), "amethyst_dust_block");
        reallySimpleBlockAndItem(HexBlocks.AMETHYST_TILES.get(), "amethyst_tiles");
        reallySimpleBlockAndItem(HexBlocks.SCROLL_PAPER.get(), "scroll_paper");
        reallySimpleBlockAndItem(HexBlocks.ANCIENT_SCROLL_PAPER.get(), "ancient_scroll_paper");
        reallySimpleBlockAndItem(HexBlocks.SCROLL_PAPER_LANTERN.get(), "scroll_paper_lantern");
        reallySimpleBlockAndItem(HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN.get(), "ancient_scroll_paper_lantern");

        var sconceModel = models().getExistingFile(modLoc("amethyst_sconce"));
        simpleBlock(HexBlocks.SCONCE.get(), sconceModel);
        simpleBlockItem(HexBlocks.SCONCE.get(), sconceModel);
    }

    private void blockAndItem(Block block, BlockModelBuilder model) {
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    private void reallySimpleBlockAndItem(Block block, String name) {
        blockAndItem(block, models().cubeAll(name, modLoc("block/" + name)));
    }

    private void arrowCircleBlock(Block block, String name, String stub, ResourceLocation particle) {
        getVariantBuilder(block).forAllStates(bs -> {
            var isLit = bs.getValue(BlockCircleComponent.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var dir = bs.getValue(BlockStateProperties.FACING);

            var up = modLoc(stub + "/up_" + litness);
            var front = modLoc(stub + "/front_" + litness);
            var back = modLoc(stub + "/back_" + litness);
            var left = modLoc(stub + "/left_" + litness);
            var right = modLoc(stub + "/right_" + litness);
            var down = modLoc(stub + "/down_" + litness);

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
            if (!isLit && dir == Direction.NORTH) {
                simpleBlockItem(block, model);
            }
            return ConfiguredModel.builder()
                .modelFile(model)
                .build();
        });
    }
}
