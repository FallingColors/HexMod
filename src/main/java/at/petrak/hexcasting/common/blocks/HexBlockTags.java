package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.HexMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class HexBlockTags {
    public static final TagKey<Block> AKASHIC_FLOODFILLER = BlockTags.create(modLoc("akashic_floodfiller"));

    private static ResourceLocation modLoc(String name) {
        return new ResourceLocation(HexMod.MOD_ID, name);
    }
}
