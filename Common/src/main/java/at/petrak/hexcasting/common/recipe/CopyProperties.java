package at.petrak.hexcasting.common.recipe;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class CopyProperties {
    // Because kotlin doesn't like doing raw, unchecked types
    // Can't blame it, but that's what we need to do
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BlockState copyProperties(BlockState original, BlockState copyTo) {
        for (Property prop : original.getProperties()) {
            if (copyTo.hasProperty(prop)) {
                copyTo = copyTo.setValue(prop, original.getValue(prop));
            }
        }

        return copyTo;
    }
}
