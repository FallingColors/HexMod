package at.petrak.hexcasting.common.items.colorizer;

import at.petrak.hexcasting.common.blocks.BlockConjured;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public abstract class ItemColorizer extends Item {
    public ItemColorizer(Properties pProperties) {
        super(pProperties);
    }

    public abstract int[] getColors();

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        boolean enabled = false;
        if (enabled && !pContext.getLevel().isClientSide()) {
            pContext.getLevel().setBlock(pContext.getClickedPos().relative(pContext.getClickedFace()), HexBlocks.CONJURED.get().defaultBlockState(), 3);
            if (pContext.getLevel().getBlockState(pContext.getClickedPos().relative(pContext.getClickedFace())).getBlock() instanceof BlockConjured) {
                BlockConjured.setColor(pContext.getLevel(), pContext.getClickedPos().relative(pContext.getClickedFace()), pContext.getItemInHand());
            }
        }
        return super.useOn(pContext);
    }
}
