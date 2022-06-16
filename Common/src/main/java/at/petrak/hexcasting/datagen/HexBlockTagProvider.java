package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.api.mod.HexBlockTags;
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
            .add(HexBlocks.AKASHIC_RECORD, HexBlocks.AKASHIC_BOOKSHELF, HexBlocks.AKASHIC_LIGATURE,
                HexBlocks.EDIFIED_LOG, HexBlocks.STRIPPED_EDIFIED_LOG,
                HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD,
                HexBlocks.EDIFIED_PLANKS, HexBlocks.EDIFIED_PANEL, HexBlocks.EDIFIED_TILE,
                HexBlocks.EDIFIED_DOOR, HexBlocks.EDIFIED_TRAPDOOR, HexBlocks.EDIFIED_SLAB,
                HexBlocks.EDIFIED_BUTTON);

        tag(BlockTags.MINEABLE_WITH_HOE)
            .add(HexBlocks.AMETHYST_EDIFIED_LEAVES, HexBlocks.AVENTURINE_EDIFIED_LEAVES,
                HexBlocks.CITRINE_EDIFIED_LEAVES);

        tag(BlockTags.CRYSTAL_SOUND_BLOCKS)
            .add(HexBlocks.CONJURED_LIGHT, HexBlocks.CONJURED_BLOCK, HexBlocks.AMETHYST_TILES,
                HexBlocks.SCONCE);

        tag(HexBlockTags.EDIFIED_LOGS)
            .add(HexBlocks.EDIFIED_LOG, HexBlocks.STRIPPED_EDIFIED_LOG,
                HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD);
        tag(BlockTags.LOGS)
            .add(HexBlocks.EDIFIED_LOG, HexBlocks.STRIPPED_EDIFIED_LOG,
                HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD);
        tag(BlockTags.LOGS_THAT_BURN)
            .add(HexBlocks.EDIFIED_LOG, HexBlocks.STRIPPED_EDIFIED_LOG,
                HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD);
        tag(BlockTags.LEAVES)
            .add(HexBlocks.AMETHYST_EDIFIED_LEAVES, HexBlocks.AVENTURINE_EDIFIED_LEAVES,
                HexBlocks.CITRINE_EDIFIED_LEAVES);

        tag(BlockTags.PLANKS)
            .add(HexBlocks.EDIFIED_PLANKS, HexBlocks.EDIFIED_PANEL, HexBlocks.EDIFIED_TILE);
        tag(HexBlockTags.EDIFIED_PLANKS)
            .add(HexBlocks.EDIFIED_PLANKS, HexBlocks.EDIFIED_PANEL, HexBlocks.EDIFIED_TILE);
        tag(BlockTags.SLABS)
            .add(HexBlocks.EDIFIED_SLAB);
        tag(BlockTags.WOODEN_SLABS)
            .add(HexBlocks.EDIFIED_SLAB);
        tag(BlockTags.DOORS)
            .add(HexBlocks.EDIFIED_DOOR);
        tag(BlockTags.WOODEN_DOORS)
            .add(HexBlocks.EDIFIED_DOOR);
        tag(BlockTags.TRAPDOORS)
            .add(HexBlocks.EDIFIED_TRAPDOOR);
        tag(BlockTags.WOODEN_TRAPDOORS)
            .add(HexBlocks.EDIFIED_TRAPDOOR);
        tag(BlockTags.PRESSURE_PLATES)
            .add(HexBlocks.EDIFIED_PRESSURE_PLATE);
        tag(BlockTags.WOODEN_PRESSURE_PLATES)
            .add(HexBlocks.EDIFIED_PRESSURE_PLATE);
        tag(BlockTags.BUTTONS)
            .add(HexBlocks.EDIFIED_BUTTON);
        tag(BlockTags.WOODEN_BUTTONS)
            .add(HexBlocks.EDIFIED_BUTTON);
    }

    @Override
    public String getName() {
        return "Hexcasting Block Tags";
    }
}
