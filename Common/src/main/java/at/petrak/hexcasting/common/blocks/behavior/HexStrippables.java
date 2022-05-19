package at.petrak.hexcasting.common.blocks.behavior;

import at.petrak.hexcasting.common.lib.HexBlocks;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class HexStrippables {
    public static final Map<Block, Block> STRIPPABLES = new HashMap<>();

    public static void init() {
        STRIPPABLES.put(HexBlocks.AKASHIC_LOG, HexBlocks.AKASHIC_LOG_STRIPPED);
        STRIPPABLES.put(HexBlocks.AKASHIC_WOOD, HexBlocks.AKASHIC_WOOD_STRIPPED);
    }
}
