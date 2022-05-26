package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.common.lib.HexBlockTags;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.xplat.IXplatTags;
import at.petrak.paucal.api.datagen.PaucalBlockTagProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;

public class HexBlockTagProvider extends PaucalBlockTagProvider {
    public final IXplatTags xtags;

    public HexBlockTagProvider(DataGenerator pGenerator, IXplatTags xtags) {
        super(pGenerator);
        this.xtags = xtags;
    }

    @Override
    public void addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(HexBlocks.SLATE_BLOCK, HexBlocks.SLATE,
                HexBlocks.EMPTY_DIRECTRIX, HexBlocks.DIRECTRIX_REDSTONE,
                HexBlocks.EMPTY_IMPETUS,
                HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_STOREDPLAYER,
                HexBlocks.AMETHYST_TILES, HexBlocks.SCONCE);

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .add(HexBlocks.AMETHYST_DUST_BLOCK);

        tag(BlockTags.MINEABLE_WITH_AXE)
            .add(HexBlocks.AKASHIC_RECORD, HexBlocks.AKASHIC_BOOKSHELF, HexBlocks.AKASHIC_CONNECTOR,
                HexBlocks.AKASHIC_LOG, HexBlocks.AKASHIC_LOG_STRIPPED,
                HexBlocks.AKASHIC_WOOD, HexBlocks.AKASHIC_WOOD_STRIPPED,
                HexBlocks.AKASHIC_PLANKS, HexBlocks.AKASHIC_PANEL, HexBlocks.AKASHIC_TILE,
                HexBlocks.AKASHIC_DOOR, HexBlocks.AKASHIC_TRAPDOOR, HexBlocks.AKASHIC_SLAB,
                HexBlocks.AKASHIC_BUTTON);

        tag(BlockTags.MINEABLE_WITH_HOE)
            .add(HexBlocks.AKASHIC_LEAVES1, HexBlocks.AKASHIC_LEAVES2, HexBlocks.AKASHIC_LEAVES3);

        tag(BlockTags.CRYSTAL_SOUND_BLOCKS)
            .add(HexBlocks.CONJURED_LIGHT, HexBlocks.CONJURED_BLOCK, HexBlocks.AMETHYST_TILES,
                HexBlocks.SCONCE);

        tag(HexBlockTags.AKASHIC_LOGS)
            .add(HexBlocks.AKASHIC_LOG, HexBlocks.AKASHIC_LOG_STRIPPED,
                HexBlocks.AKASHIC_WOOD, HexBlocks.AKASHIC_WOOD_STRIPPED);
        tag(BlockTags.LOGS)
            .add(HexBlocks.AKASHIC_LOG, HexBlocks.AKASHIC_LOG_STRIPPED,
                HexBlocks.AKASHIC_WOOD, HexBlocks.AKASHIC_WOOD_STRIPPED);
        tag(BlockTags.LOGS_THAT_BURN)
            .add(HexBlocks.AKASHIC_LOG, HexBlocks.AKASHIC_LOG_STRIPPED,
                HexBlocks.AKASHIC_WOOD, HexBlocks.AKASHIC_WOOD_STRIPPED);
        tag(BlockTags.LEAVES)
            .add(HexBlocks.AKASHIC_LEAVES1, HexBlocks.AKASHIC_LEAVES2, HexBlocks.AKASHIC_LEAVES3);

        tag(BlockTags.PLANKS)
            .add(HexBlocks.AKASHIC_PLANKS, HexBlocks.AKASHIC_PANEL, HexBlocks.AKASHIC_TILE);
        tag(HexBlockTags.AKASHIC_PLANKS)
            .add(HexBlocks.AKASHIC_PLANKS, HexBlocks.AKASHIC_PANEL, HexBlocks.AKASHIC_TILE);
        tag(BlockTags.SLABS)
            .add(HexBlocks.AKASHIC_SLAB);
        tag(BlockTags.WOODEN_SLABS)
            .add(HexBlocks.AKASHIC_SLAB);
        tag(BlockTags.DOORS)
            .add(HexBlocks.AKASHIC_DOOR);
        tag(BlockTags.WOODEN_DOORS)
            .add(HexBlocks.AKASHIC_DOOR);
        tag(BlockTags.TRAPDOORS)
            .add(HexBlocks.AKASHIC_TRAPDOOR);
        tag(BlockTags.WOODEN_TRAPDOORS)
            .add(HexBlocks.AKASHIC_TRAPDOOR);
        tag(BlockTags.PRESSURE_PLATES)
            .add(HexBlocks.AKASHIC_PRESSURE_PLATE);
        tag(BlockTags.WOODEN_PRESSURE_PLATES)
            .add(HexBlocks.AKASHIC_PRESSURE_PLATE);
        tag(BlockTags.BUTTONS)
            .add(HexBlocks.AKASHIC_BUTTON);
        tag(BlockTags.WOODEN_BUTTONS)
            .add(HexBlocks.AKASHIC_BUTTON);
    }

    @Override
    public String getName() {
        return "Hexcasting Block Tags";
    }
}
