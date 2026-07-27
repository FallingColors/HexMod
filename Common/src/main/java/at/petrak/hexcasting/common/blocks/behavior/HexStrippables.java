package at.petrak.hexcasting.common.blocks.behavior;

import at.petrak.hexcasting.common.lib.HexBlocks;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class HexStrippables {
    public static final Map<Block, Block> STRIPPABLES = new HashMap<>();

    public static void init() {
        STRIPPABLES.put(HexBlocks.EDIFIED_LOG.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get());
        STRIPPABLES.put(HexBlocks.EDIFIED_LOG_AMETHYST.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get());
        STRIPPABLES.put(HexBlocks.EDIFIED_LOG_AVENTURINE.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get());
        STRIPPABLES.put(HexBlocks.EDIFIED_LOG_CITRINE.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get());
        STRIPPABLES.put(HexBlocks.EDIFIED_LOG_PURPLE.get(), HexBlocks.STRIPPED_EDIFIED_LOG.get());
        STRIPPABLES.put(HexBlocks.EDIFIED_WOOD.get(), HexBlocks.STRIPPED_EDIFIED_WOOD.get());
    }
}
