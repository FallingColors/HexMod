package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemSlate extends BlockItem {
    public static final ResourceLocation WRITTEN_PRED = new ResourceLocation(HexMod.MOD_ID, "written");

    public ItemSlate(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        var key = HexMod.MOD_ID + ".block.slate." + (hasPattern(pStack) ? "written" : "blank");
        return new TranslatableComponent(key);
    }

    public static boolean hasPattern(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null && tag.contains("BlockEntityTag")) {
            var bet = tag.getCompound("BlockEntityTag");
            return bet.contains(BlockEntitySlate.TAG_PATTERN);
        }
        return false;
    }
}
