package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.HexMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class HexBlockTags {
    public static final TagKey<Block> AKASHIC_LOGS = create("akashic_logs");
    public static final TagKey<Block> AKASHIC_PLANKS = create("akashic_planks");

    private static TagKey<Block> create(String name) {
        return BlockTags.create(new ResourceLocation(HexMod.MOD_ID, name));
    }
}
