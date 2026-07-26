package at.petrak.hexcasting.datagen.tag;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.xplat.IXplatTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class HexBlockTagProvider extends TagsProvider<Block> {
    public final IXplatTags xtags;

    public HexBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, IXplatTags xtags) {
        super(output, Registries.BLOCK, lookupProvider);
        this.xtags = xtags;
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        add(tag(HexTags.Blocks.IMPETI),
            HexBlocks.IMPETUS_LOOK.get(), HexBlocks.IMPETUS_RIGHTCLICK.get(), HexBlocks.IMPETUS_REDSTONE.get());
        add(tag(HexTags.Blocks.DIRECTRICES),
            HexBlocks.DIRECTRIX_REDSTONE.get(), HexBlocks.DIRECTRIX_BOOLEAN.get());
        tag(HexTags.Blocks.MINDFLAYED_CIRCLE_COMPONENTS)
            .addTag(HexTags.Blocks.IMPETI)
            .addTag(HexTags.Blocks.DIRECTRICES);

        add(tag(BlockTags.MINEABLE_WITH_PICKAXE),
            HexBlocks.SLATE_BLOCK.get(), HexBlocks.SLATE_TILES.get(), HexBlocks.SLATE_BRICKS.get(),
            HexBlocks.SLATE_BRICKS_SMALL.get(), HexBlocks.SLATE_PILLAR.get(), HexBlocks.SLATE.get(),
            HexBlocks.EMPTY_DIRECTRIX.get(), HexBlocks.DIRECTRIX_REDSTONE.get(), HexBlocks.DIRECTRIX_BOOLEAN.get(),
            HexBlocks.IMPETUS_EMPTY.get(),
            HexBlocks.IMPETUS_RIGHTCLICK.get(), HexBlocks.IMPETUS_LOOK.get(), HexBlocks.IMPETUS_REDSTONE.get(),
            HexBlocks.AMETHYST_TILES.get(), HexBlocks.AMETHYST_BRICKS.get(), HexBlocks.AMETHYST_BRICKS_SMALL.get(),
            HexBlocks.AMETHYST_PILLAR.get(), HexBlocks.SLATE_AMETHYST_TILES.get(), HexBlocks.SLATE_AMETHYST_BRICKS.get(),
            HexBlocks.SLATE_AMETHYST_BRICKS_SMALL.get(), HexBlocks.SLATE_AMETHYST_PILLAR.get(), HexBlocks.SCONCE.get(),
            HexBlocks.QUENCHED_ALLAY.get(), HexBlocks.QUENCHED_ALLAY_TILES.get(), HexBlocks.QUENCHED_ALLAY_BRICKS.get(),
            HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get());

        add(tag(BlockTags.MINEABLE_WITH_SHOVEL),
            HexBlocks.AMETHYST_DUST_BLOCK.get());

        add(tag(BlockTags.MINEABLE_WITH_AXE),
            HexBlocks.AKASHIC_RECORD.get(), HexBlocks.AKASHIC_BOOKSHELF.get(), HexBlocks.AKASHIC_LIGATURE.get(),
            HexBlocks.EDIFIED_LOG.get(), HexBlocks.EDIFIED_LOG_AMETHYST.get(),
            HexBlocks.EDIFIED_LOG_AVENTURINE.get(), HexBlocks.EDIFIED_LOG_CITRINE.get(),
            HexBlocks.EDIFIED_LOG_PURPLE.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get(),
            HexBlocks.EDIFIED_WOOD.get(), HexBlocks.STRIPPED_EDIFIED_WOOD.get(),
            HexBlocks.EDIFIED_PLANKS.get(), HexBlocks.EDIFIED_PANEL.get(), HexBlocks.EDIFIED_TILE.get(),
            HexBlocks.EDIFIED_DOOR.get(), HexBlocks.EDIFIED_TRAPDOOR.get(), HexBlocks.EDIFIED_SLAB.get(),
            HexBlocks.EDIFIED_BUTTON.get(), HexBlocks.EDIFIED_STAIRS.get(), HexBlocks.EDIFIED_FENCE.get(), HexBlocks.EDIFIED_FENCE_GATE.get());

        add(tag(BlockTags.MINEABLE_WITH_HOE),
            HexBlocks.AMETHYST_EDIFIED_LEAVES.get(), HexBlocks.AVENTURINE_EDIFIED_LEAVES.get(),
            HexBlocks.CITRINE_EDIFIED_LEAVES.get());

        add(tag(BlockTags.CRYSTAL_SOUND_BLOCKS),
            HexBlocks.CONJURED_LIGHT.get(), HexBlocks.CONJURED_BLOCK.get(), HexBlocks.AMETHYST_TILES.get(),
            HexBlocks.SCONCE.get());

        add(tag(HexTags.Blocks.EDIFIED_LOGS),
            HexBlocks.EDIFIED_LOG.get(), HexBlocks.EDIFIED_LOG_AMETHYST.get(),
            HexBlocks.EDIFIED_LOG_AVENTURINE.get(), HexBlocks.EDIFIED_LOG_CITRINE.get(),
            HexBlocks.EDIFIED_LOG_PURPLE.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get(),
            HexBlocks.EDIFIED_WOOD.get(), HexBlocks.STRIPPED_EDIFIED_WOOD.get());
        add(tag(BlockTags.LOGS),
            HexBlocks.EDIFIED_LOG.get(), HexBlocks.EDIFIED_LOG_AMETHYST.get(),
            HexBlocks.EDIFIED_LOG_AVENTURINE.get(), HexBlocks.EDIFIED_LOG_CITRINE.get(),
            HexBlocks.EDIFIED_LOG_PURPLE.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get(),
            HexBlocks.EDIFIED_WOOD.get(), HexBlocks.STRIPPED_EDIFIED_WOOD.get());
        add(tag(BlockTags.LOGS_THAT_BURN),
            HexBlocks.EDIFIED_LOG.get(), HexBlocks.EDIFIED_LOG_AMETHYST.get(),
            HexBlocks.EDIFIED_LOG_AVENTURINE.get(), HexBlocks.EDIFIED_LOG_CITRINE.get(),
            HexBlocks.EDIFIED_LOG_PURPLE.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get(),
            HexBlocks.EDIFIED_WOOD.get(), HexBlocks.STRIPPED_EDIFIED_WOOD.get());
        add(tag(BlockTags.LEAVES),
            HexBlocks.AMETHYST_EDIFIED_LEAVES.get(), HexBlocks.AVENTURINE_EDIFIED_LEAVES.get(),
            HexBlocks.CITRINE_EDIFIED_LEAVES.get());

        add(tag(BlockTags.PLANKS),
            HexBlocks.EDIFIED_PLANKS.get(), HexBlocks.EDIFIED_PANEL.get(), HexBlocks.EDIFIED_TILE.get());
        add(tag(HexTags.Blocks.EDIFIED_PLANKS),
            HexBlocks.EDIFIED_PLANKS.get(), HexBlocks.EDIFIED_PANEL.get(), HexBlocks.EDIFIED_TILE.get());
        add(tag(BlockTags.SLABS),
            HexBlocks.EDIFIED_SLAB.get());
        add(tag(BlockTags.WOODEN_SLABS),
            HexBlocks.EDIFIED_SLAB.get());
        add(tag(BlockTags.STAIRS),
            HexBlocks.EDIFIED_STAIRS.get());
        add(tag(BlockTags.FENCES),
            HexBlocks.EDIFIED_FENCE.get());
        add(tag(BlockTags.WOODEN_FENCES),
            HexBlocks.EDIFIED_FENCE.get());
        add(tag(BlockTags.FENCE_GATES),
            HexBlocks.EDIFIED_FENCE_GATE.get());
        add(tag(BlockTags.UNSTABLE_BOTTOM_CENTER),
            HexBlocks.EDIFIED_FENCE_GATE.get());


        add(tag(BlockTags.WOODEN_FENCES),
            HexBlocks.EDIFIED_FENCE.get());
        add(tag(BlockTags.WOODEN_STAIRS),
            HexBlocks.EDIFIED_STAIRS.get());
        add(tag(BlockTags.DOORS),
            HexBlocks.EDIFIED_DOOR.get());
        add(tag(BlockTags.WOODEN_DOORS),
            HexBlocks.EDIFIED_DOOR.get());
        add(tag(BlockTags.TRAPDOORS),
            HexBlocks.EDIFIED_TRAPDOOR.get());
        add(tag(BlockTags.WOODEN_TRAPDOORS),
            HexBlocks.EDIFIED_TRAPDOOR.get());
        add(tag(BlockTags.PRESSURE_PLATES),
            HexBlocks.EDIFIED_PRESSURE_PLATE.get());
        add(tag(BlockTags.WOODEN_PRESSURE_PLATES),
            HexBlocks.EDIFIED_PRESSURE_PLATE.get());
        add(tag(BlockTags.BUTTONS),
            HexBlocks.EDIFIED_BUTTON.get());
        add(tag(BlockTags.WOODEN_BUTTONS),
            HexBlocks.EDIFIED_BUTTON.get());

        add(tag(HexTags.Blocks.WATER_PLANTS),
            Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS);
        add(tag(HexTags.Blocks.CHEAP_TO_BREAK_BLOCK),
            HexBlocks.CONJURED_BLOCK.get(), HexBlocks.CONJURED_LIGHT.get());

        add(tag(HexTags.Blocks.SLATE_BLOCKS),
            HexBlocks.SLATE_BLOCK.get(), HexBlocks.SLATE_BRICKS.get(), HexBlocks.SLATE_BRICKS_SMALL.get(), HexBlocks.SLATE_TILES.get(), HexBlocks.SLATE_PILLAR.get());
        add(tag(HexTags.Blocks.AMETHYST_BLOCKS),
            Blocks.AMETHYST_BLOCK, HexBlocks.AMETHYST_BRICKS.get(), HexBlocks.AMETHYST_BRICKS_SMALL.get(), HexBlocks.AMETHYST_TILES.get(), HexBlocks.AMETHYST_PILLAR.get());
        add(tag(HexTags.Blocks.QUENCHED_ALLAY_BLOCKS),
            HexBlocks.QUENCHED_ALLAY.get(), HexBlocks.QUENCHED_ALLAY_BRICKS.get(), HexBlocks.QUENCHED_ALLAY_BRICKS_SMALL.get(), HexBlocks.QUENCHED_ALLAY_TILES.get());

        // this is a hack but fixes #532
        var createBrittle = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("create", "brittle"));
        tag(createBrittle).addOptionalTag(BuiltInRegistries.BLOCK.getKey(HexBlocks.SLATE.get()));
    }

    void add(TagsProvider.TagAppender<Block> appender, Block... blocks) {
        for (Block block : blocks) {
            appender.add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow());
        }
    }
}
