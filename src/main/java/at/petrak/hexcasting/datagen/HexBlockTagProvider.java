package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class HexBlockTagProvider extends BlockTagsProvider {
    public HexBlockTagProvider(DataGenerator pGenerator,
        @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, HexMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(HexBlocks.SLATE_BLOCK.get(), HexBlocks.SLATE.get(),
                HexBlocks.EMPTY_DIRECTRIX.get(), HexBlocks.DIRECTRIX_REDSTONE.get(),
                HexBlocks.EMPTY_IMPETUS.get(),
                HexBlocks.IMPETUS_RIGHTCLICK.get(), HexBlocks.IMPETUS_LOOK.get(), HexBlocks.IMPETUS_STOREDPLAYER.get(),
                HexBlocks.AMETHYST_TILES.get(), HexBlocks.SCONCE.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .add(HexBlocks.AMETHYST_DUST_BLOCK.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
            .add(HexBlocks.AKASHIC_RECORD.get(), HexBlocks.AKASHIC_BOOKSHELF.get());

        tag(BlockTags.CRYSTAL_SOUND_BLOCKS)
            .add(HexBlocks.CONJURED.get(), HexBlocks.AMETHYST_TILES.get(), HexBlocks.SCONCE.get());

    }
}
