package at.petrak.hexcasting.api.circles;

import at.petrak.hexcasting.api.spell.casting.FunctionalData;
import net.minecraft.core.BlockPos;

/**
 * What this block passes onto the next block: what block it tries to exit to and where on that block it enters.
 */
public record FlowUpdate(FunctionalData newData, BlockPos nextPos, BlockEdge entryEdge) {
}
