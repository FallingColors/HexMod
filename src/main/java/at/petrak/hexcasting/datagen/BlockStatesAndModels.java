package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.impetuses.BlockAbstractImpetus;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStatesAndModels extends BlockStateProvider {
    public BlockStatesAndModels(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, HexMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(HexBlocks.SLATE.get(), models().getExistingFile(modLoc("slate")));
        var slateBlock = models().cubeAll("slate_block", modLoc("block/slate"));
        simpleBlock(HexBlocks.SLATE_BLOCK.get(), slateBlock);
        simpleBlockItem(HexBlocks.SLATE_BLOCK.get(), slateBlock);

        getVariantBuilder(HexBlocks.IMPETUS_RIGHTCLICK.get()).forAllStates(bs -> {
            var litness = bs.getValue(BlockAbstractImpetus.LIT) ? "lit" : "dim";
            var dir = bs.getValue(BlockAbstractImpetus.FACING);

            // Assuming it's facing north
            var top = modLoc("block/impetus/rightclick/top_" + litness);
            var north = modLoc("block/impetus/rightclick/front_" + litness);
            var south = modLoc("block/impetus/rightclick/back_" + litness);
            var west = modLoc("block/impetus/rightclick/left_" + litness);
            var east = modLoc("block/impetus/rightclick/right_" + litness);
            var bottom = modLoc("block/slate");

            var name = "impetus_rightclick_" + litness;
            var model = models().cube(name, bottom, top, north, south, east, west)
                .texture("particle", bottom);
            if (!bs.getValue(BlockAbstractImpetus.LIT) && dir == Direction.SOUTH) {
                simpleBlockItem(HexBlocks.IMPETUS_RIGHTCLICK.get(), model);
            }
            return ConfiguredModel.builder().modelFile(model).rotationY((dir.get2DDataValue() + 2) % 4 * 90).build();
        });
    }
}
