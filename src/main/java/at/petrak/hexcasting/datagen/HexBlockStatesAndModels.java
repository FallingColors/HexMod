package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.circles.BlockRedirector;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.blocks.circles.impetuses.BlockAbstractImpetus;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
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

        getVariantBuilder(HexBlocks.REDIRECTOR.get()).forAllStates(bs -> {
            var isLit = bs.getValue(BlockRedirector.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var dir = bs.getValue(BlockRedirector.FACING);

            final ResourceLocation up = modLoc("block/impetus/rightclick/up_" + litness),
                down = modLoc("block/impetus/rightclick/down_" + litness),
                left = modLoc("block/impetus/rightclick/left_" + litness),
                right = modLoc("block/impetus/rightclick/right_" + litness),
                front = modLoc("block/impetus/rightclick/front_" + litness),
                back = modLoc("block/impetus/rightclick/back_" + litness);

            var name = "redirector_" + litness + "_" + dir.getName();
            var f = oriented(dir, up, down, left, right, front, back);
            var model = models().cube(name, f[0], f[1], f[2], f[3], f[4], f[5])
                .texture("particle", modLoc("block/slate"));
            if (!isLit && dir == Direction.SOUTH) {
                simpleBlockItem(HexBlocks.REDIRECTOR.get(), model);
            }
            return ConfiguredModel.builder()
                .modelFile(model)
                .uvLock(true)
                .build();
        });

        var emptyImpetus = models().cubeAll("empty_impetus", modLoc("block/impetus/empty"));
        simpleBlock(HexBlocks.EMPTY_IMPETUS.get(), emptyImpetus);
        simpleBlockItem(HexBlocks.EMPTY_IMPETUS.get(), emptyImpetus);

        getVariantBuilder(HexBlocks.IMPETUS_RIGHTCLICK.get()).forAllStates(bs -> {
            var isLit = bs.getValue(BlockAbstractImpetus.ENERGIZED);
            var litness = isLit ? "lit" : "dim";
            var dir = bs.getValue(BlockAbstractImpetus.FACING);

            // Assuming it's facing north
            var up = modLoc("block/impetus/rightclick/up_" + litness);
            var front = modLoc("block/impetus/rightclick/front_" + litness);
            var back = modLoc("block/impetus/rightclick/back_" + litness);
            var left = modLoc("block/impetus/rightclick/left_" + litness);
            var right = modLoc("block/impetus/rightclick/right_" + litness);
            var down = modLoc("block/impetus/rightclick/down_" + litness);

            var name = "impetus_rightclick_" + litness + "_" + dir.getName();
            var f = oriented(dir, up, down, left, right, front, back);
            var model = models().cube(name, f[0], f[1], f[2], f[3], f[4], f[5])
                .texture("particle", modLoc("block/slate"));
            if (!isLit && dir == Direction.SOUTH) {
                simpleBlockItem(HexBlocks.IMPETUS_RIGHTCLICK.get(), model);
            }
            return ConfiguredModel.builder()
                .modelFile(model)
                .build();
        });

        var slateBlock = models().cubeAll("slate_block", modLoc("block/slate"));
        simpleBlock(HexBlocks.SLATE_BLOCK.get(), slateBlock);
        simpleBlockItem(HexBlocks.SLATE_BLOCK.get(), slateBlock);
    }

    // return [bottom, top, north, south, east, west
    private static ResourceLocation[] oriented(Direction dir, ResourceLocation up, ResourceLocation down,
        ResourceLocation left, ResourceLocation right, ResourceLocation front, ResourceLocation back) {
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
                bottom = right;
            }
            case EAST -> {
                east = front;
                west = back;
                north = left;
                south = right;
                top = right;
                bottom = left;
            }
        }
        return new ResourceLocation[]{bottom, top, north, south, east, west};
    }
}
