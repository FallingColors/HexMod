package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlockTags;
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
            .add(HexBlocks.AKASHIC_RECORD.get(), HexBlocks.AKASHIC_BOOKSHELF.get(), HexBlocks.AKASHIC_CONNECTOR.get(),
                HexBlocks.AKASHIC_LOG.get(), HexBlocks.AKASHIC_LOG_STRIPPED.get(),
                HexBlocks.AKASHIC_WOOD.get(), HexBlocks.AKASHIC_WOOD_STRIPPED.get(),
                HexBlocks.AKASHIC_PLANKS.get(), HexBlocks.AKASHIC_PANEL.get(), HexBlocks.AKASHIC_TILE.get(),
                HexBlocks.AKASHIC_DOOR.get(), HexBlocks.AKASHIC_TRAPDOOR.get());

        tag(BlockTags.MINEABLE_WITH_HOE)
            .add(HexBlocks.AKASHIC_LEAVES1.get(), HexBlocks.AKASHIC_LEAVES2.get(), HexBlocks.AKASHIC_LEAVES3.get());

        tag(BlockTags.CRYSTAL_SOUND_BLOCKS)
            .add(HexBlocks.CONJURED.get(), HexBlocks.AMETHYST_TILES.get(), HexBlocks.SCONCE.get());

        tag(HexBlockTags.AKASHIC_LOGS)
            .add(HexBlocks.AKASHIC_LOG.get(), HexBlocks.AKASHIC_LOG_STRIPPED.get(),
                HexBlocks.AKASHIC_WOOD.get(), HexBlocks.AKASHIC_WOOD_STRIPPED.get());
        tag(BlockTags.LOGS)
            .add(HexBlocks.AKASHIC_LOG.get(), HexBlocks.AKASHIC_LOG_STRIPPED.get(),
                HexBlocks.AKASHIC_WOOD.get(), HexBlocks.AKASHIC_WOOD_STRIPPED.get());
        tag(BlockTags.LOGS_THAT_BURN)
            .add(HexBlocks.AKASHIC_LOG.get(), HexBlocks.AKASHIC_LOG_STRIPPED.get(),
                HexBlocks.AKASHIC_WOOD.get(), HexBlocks.AKASHIC_WOOD_STRIPPED.get());
        tag(BlockTags.LEAVES)
            .add(HexBlocks.AKASHIC_LEAVES1.get(), HexBlocks.AKASHIC_LEAVES2.get(), HexBlocks.AKASHIC_LEAVES3.get());

        tag(BlockTags.PLANKS)
            .add(HexBlocks.AKASHIC_PLANKS.get(), HexBlocks.AKASHIC_PANEL.get(), HexBlocks.AKASHIC_TILE.get());
        tag(HexBlockTags.AKASHIC_PLANKS)
            .add(HexBlocks.AKASHIC_PLANKS.get(), HexBlocks.AKASHIC_PANEL.get(), HexBlocks.AKASHIC_TILE.get());
        tag(BlockTags.DOORS)
            .add(HexBlocks.AKASHIC_DOOR.get());
        tag(BlockTags.WOODEN_DOORS)
            .add(HexBlocks.AKASHIC_DOOR.get());
        tag(BlockTags.TRAPDOORS)
            .add(HexBlocks.AKASHIC_TRAPDOOR.get());
        tag(BlockTags.WOODEN_TRAPDOORS)
            .add(HexBlocks.AKASHIC_TRAPDOOR.get());
    }
}
