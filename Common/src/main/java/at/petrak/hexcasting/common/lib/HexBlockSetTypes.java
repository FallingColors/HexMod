package at.petrak.hexcasting.common.lib;

import net.minecraft.world.level.block.state.properties.BlockSetType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HexBlockSetTypes {
    public static void registerBlocks(Consumer<BlockSetType> r) {
        for (var type : TYPES) {
            r.accept(type);
        }
    }

    private static final List<BlockSetType> TYPES = new ArrayList<>();

    public static final BlockSetType EDIFIED_WOOD = register(
        new BlockSetType("edified_wood")
    );

    private static BlockSetType register(BlockSetType type) {
        TYPES.add(type);
        return type;
    }
}
