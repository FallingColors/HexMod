package at.petrak.hexcasting.common.lib;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexBlockTags {
    public static final TagKey<Block> AKASHIC_LOGS = create("akashic_logs");
    public static final TagKey<Block> AKASHIC_PLANKS = create("akashic_planks");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, modLoc(name));
    }
}
