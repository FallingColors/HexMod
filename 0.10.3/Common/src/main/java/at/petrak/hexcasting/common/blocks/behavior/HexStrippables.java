package at.petrak.hexcasting.common.blocks.behavior;

import at.petrak.hexcasting.common.lib.HexBlocks;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class HexStrippables {
    public static final Map<Block, Block> STRIPPABLES = new HashMap<>();

    public static void init() {
        STRIPPABLES.put(HexBlocks.EDIFIED_LOG, HexBlocks.STRIPPED_EDIFIED_LOG);
        STRIPPABLES.put(HexBlocks.EDIFIED_WOOD, HexBlocks.STRIPPED_EDIFIED_WOOD);
    }
}
