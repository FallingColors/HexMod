package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class HexBlockTags extends BlockTagsProvider {
    public HexBlockTags(DataGenerator pGenerator,
        @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, HexMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(HexBlocks.SLATE_BLOCK.get(), HexBlocks.SLATE.get(), HexBlocks.EMPTY_IMPETUS.get(),
                HexBlocks.IMPETUS_RIGHTCLICK.get(),
                HexBlocks.AMETHYST_TILES.get(), HexBlocks.SCONCE.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .add(HexBlocks.AMETHYST_DUST_BLOCK.get());

        tag(BlockTags.CRYSTAL_SOUND_BLOCKS)
            .add(HexBlocks.CONJURED.get(), HexBlocks.AMETHYST_TILES.get(), HexBlocks.SCONCE.get());
    }
}
