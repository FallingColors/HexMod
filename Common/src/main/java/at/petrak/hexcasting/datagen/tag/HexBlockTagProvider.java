package at.petrak.hexcasting.datagen.tag;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.xplat.IXplatTags;
import at.petrak.paucal.api.datagen.PaucalBlockTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class HexBlockTagProvider extends PaucalBlockTagProvider {
    public final IXplatTags xtags;

    public HexBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
        IXplatTags xtags) {
        super(output, lookupProvider);
        this.xtags = xtags;
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        add(tag(HexTags.Blocks.IMPETI),
            HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_REDSTONE);
        add(tag(HexTags.Blocks.DIRECTRICES),
            HexBlocks.DIRECTRIX_REDSTONE, HexBlocks.DIRECTRIX_BOOLEAN);
        tag(HexTags.Blocks.MINDFLAYED_CIRCLE_COMPONENTS)
            .addTag(HexTags.Blocks.IMPETI)
            .addTag(HexTags.Blocks.DIRECTRICES);

        add(tag(BlockTags.MINEABLE_WITH_PICKAXE),
            HexBlocks.SLATE_BLOCK, HexBlocks.SLATE_TILES, HexBlocks.SLATE_BRICKS,
            HexBlocks.SLATE_BRICKS_SMALL, HexBlocks.SLATE_PILLAR, HexBlocks.SLATE,
            HexBlocks.EMPTY_DIRECTRIX, HexBlocks.DIRECTRIX_REDSTONE, HexBlocks.DIRECTRIX_BOOLEAN,
            HexBlocks.IMPETUS_EMPTY,
            HexBlocks.IMPETUS_RIGHTCLICK, HexBlocks.IMPETUS_LOOK, HexBlocks.IMPETUS_REDSTONE,
            HexBlocks.AMETHYST_TILES, HexBlocks.AMETHYST_BRICKS, HexBlocks.AMETHYST_BRICKS_SMALL,
            HexBlocks.AMETHYST_PILLAR, HexBlocks.SLATE_AMETHYST_TILES, HexBlocks.SLATE_AMETHYST_BRICKS,
            HexBlocks.SLATE_AMETHYST_BRICKS_SMALL, HexBlocks.SLATE_AMETHYST_PILLAR, HexBlocks.SCONCE,
            HexBlocks.QUENCHED_ALLAY, HexBlocks.QUENCHED_ALLAY_TILES, HexBlocks.QUENCHED_ALLAY_BRICKS,
            HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL);

        add(tag(BlockTags.MINEABLE_WITH_SHOVEL),
            HexBlocks.AMETHYST_DUST_BLOCK);

        add(tag(BlockTags.MINEABLE_WITH_AXE),
            HexBlocks.AKASHIC_RECORD, HexBlocks.AKASHIC_BOOKSHELF, HexBlocks.AKASHIC_LIGATURE,
            HexBlocks.EDIFIED_LOG, HexBlocks.EDIFIED_LOG_AMETHYST,
            HexBlocks.EDIFIED_LOG_AVENTURINE, HexBlocks.EDIFIED_LOG_CITRINE,
            HexBlocks.EDIFIED_LOG_PURPLE, HexBlocks.STRIPPED_EDIFIED_LOG,
            HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD,
            HexBlocks.EDIFIED_PLANKS, HexBlocks.EDIFIED_PANEL, HexBlocks.EDIFIED_TILE,
            HexBlocks.EDIFIED_DOOR, HexBlocks.EDIFIED_TRAPDOOR, HexBlocks.EDIFIED_SLAB,
            HexBlocks.EDIFIED_BUTTON, HexBlocks.EDIFIED_STAIRS, HexBlocks.EDIFIED_FENCE, HexBlocks.EDIFIED_FENCE_GATE);

        add(tag(BlockTags.MINEABLE_WITH_HOE),
            HexBlocks.AMETHYST_EDIFIED_LEAVES, HexBlocks.AVENTURINE_EDIFIED_LEAVES,
            HexBlocks.CITRINE_EDIFIED_LEAVES);

        add(tag(BlockTags.CRYSTAL_SOUND_BLOCKS),
            HexBlocks.CONJURED_LIGHT, HexBlocks.CONJURED_BLOCK, HexBlocks.AMETHYST_TILES,
            HexBlocks.SCONCE);

        add(tag(HexTags.Blocks.EDIFIED_LOGS),
            HexBlocks.EDIFIED_LOG, HexBlocks.EDIFIED_LOG_AMETHYST,
            HexBlocks.EDIFIED_LOG_AVENTURINE, HexBlocks.EDIFIED_LOG_CITRINE,
            HexBlocks.EDIFIED_LOG_PURPLE, HexBlocks.STRIPPED_EDIFIED_LOG,
            HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD);
        add(tag(BlockTags.LOGS),
            HexBlocks.EDIFIED_LOG, HexBlocks.EDIFIED_LOG_AMETHYST,
            HexBlocks.EDIFIED_LOG_AVENTURINE, HexBlocks.EDIFIED_LOG_CITRINE,
            HexBlocks.EDIFIED_LOG_PURPLE, HexBlocks.STRIPPED_EDIFIED_LOG,
            HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD);
        add(tag(BlockTags.LOGS_THAT_BURN),
            HexBlocks.EDIFIED_LOG, HexBlocks.EDIFIED_LOG_AMETHYST,
            HexBlocks.EDIFIED_LOG_AVENTURINE, HexBlocks.EDIFIED_LOG_CITRINE,
            HexBlocks.EDIFIED_LOG_PURPLE, HexBlocks.STRIPPED_EDIFIED_LOG,
            HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD);
        add(tag(BlockTags.LEAVES),
            HexBlocks.AMETHYST_EDIFIED_LEAVES, HexBlocks.AVENTURINE_EDIFIED_LEAVES,
            HexBlocks.CITRINE_EDIFIED_LEAVES);

        add(tag(BlockTags.PLANKS),
            HexBlocks.EDIFIED_PLANKS, HexBlocks.EDIFIED_PANEL, HexBlocks.EDIFIED_TILE);
        add(tag(HexTags.Blocks.EDIFIED_PLANKS),
            HexBlocks.EDIFIED_PLANKS, HexBlocks.EDIFIED_PANEL, HexBlocks.EDIFIED_TILE);
        add(tag(BlockTags.SLABS),
            HexBlocks.EDIFIED_SLAB);
        add(tag(BlockTags.WOODEN_SLABS),
            HexBlocks.EDIFIED_SLAB);
        add(tag(BlockTags.STAIRS),
            HexBlocks.EDIFIED_STAIRS);
        add(tag(BlockTags.FENCES),
            HexBlocks.EDIFIED_FENCE);
        add(tag(BlockTags.WOODEN_FENCES),
            HexBlocks.EDIFIED_FENCE);
        add(tag(BlockTags.FENCE_GATES),
            HexBlocks.EDIFIED_FENCE_GATE);
        add(tag(BlockTags.UNSTABLE_BOTTOM_CENTER),
            HexBlocks.EDIFIED_FENCE_GATE);


        add(tag(BlockTags.WOODEN_FENCES),
            HexBlocks.EDIFIED_FENCE);
        add(tag(BlockTags.WOODEN_STAIRS),
            HexBlocks.EDIFIED_STAIRS);
        add(tag(BlockTags.DOORS),
            HexBlocks.EDIFIED_DOOR);
        add(tag(BlockTags.WOODEN_DOORS),
            HexBlocks.EDIFIED_DOOR);
        add(tag(BlockTags.TRAPDOORS),
            HexBlocks.EDIFIED_TRAPDOOR);
        add(tag(BlockTags.WOODEN_TRAPDOORS),
            HexBlocks.EDIFIED_TRAPDOOR);
        add(tag(BlockTags.PRESSURE_PLATES),
            HexBlocks.EDIFIED_PRESSURE_PLATE);
        add(tag(BlockTags.WOODEN_PRESSURE_PLATES),
            HexBlocks.EDIFIED_PRESSURE_PLATE);
        add(tag(BlockTags.BUTTONS),
            HexBlocks.EDIFIED_BUTTON);
        add(tag(BlockTags.WOODEN_BUTTONS),
            HexBlocks.EDIFIED_BUTTON);

        add(tag(HexTags.Blocks.WATER_PLANTS),
            Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS);
        add(tag(HexTags.Blocks.CHEAP_TO_BREAK_BLOCK),
            HexBlocks.CONJURED_BLOCK, HexBlocks.CONJURED_LIGHT);
    }

    void add(TagAppender<Block> appender, Block... blocks) {
        for (Block block : blocks) {
            appender.add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
        }
    }
}
