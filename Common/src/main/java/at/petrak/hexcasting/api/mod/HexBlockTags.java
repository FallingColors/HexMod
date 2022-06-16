package at.petrak.hexcasting.api.mod;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexBlockTags {
    public static final TagKey<Block> EDIFIED_LOGS = create("edified_logs");
    public static final TagKey<Block> EDIFIED_PLANKS = create("edified_planks");

    public static TagKey<Block> create(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, modLoc(name));
    }
}
