package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
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
    }
}
