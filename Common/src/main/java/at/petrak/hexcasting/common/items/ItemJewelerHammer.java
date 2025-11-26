package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemJewelerHammer extends PickaxeItem {
    public ItemJewelerHammer(Tier tier, Properties props) {
        super(tier, props);
    }

    public static boolean shouldFailToBreak(Player player, BlockState state, BlockPos pos) {
        ItemStack stack = player.getMainHandItem();
        return stack.is(HexItems.JEWELER_HAMMER) && Block.isShapeFullBlock(state.getShape(player.level(), pos));
    }
}
