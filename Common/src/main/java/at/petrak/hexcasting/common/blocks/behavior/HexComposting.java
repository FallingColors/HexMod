package at.petrak.hexcasting.common.blocks.behavior;

import at.petrak.hexcasting.common.lib.HexBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;

public final class HexComposting {

    public static void setup() {
        compost(HexBlocks.AKASHIC_LEAVES1, 0.3F);
        compost(HexBlocks.AKASHIC_LEAVES2, 0.3F);
        compost(HexBlocks.AKASHIC_LEAVES3, 0.3F);
    }

    private static void compost(ItemLike itemLike, float chance) {
        Item item = itemLike.asItem();

        if (item != Items.AIR) {
            ComposterBlock.COMPOSTABLES.put(item, chance);
        }
    }
}
