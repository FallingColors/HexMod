package at.petrak.hexcasting.common.blocks.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;
import java.util.function.Supplier;

// aka, Blockg
// https://github.com/SammySemicolon/Malum-Mod/blob/1.18.1/src/main/java/com/sammy/malum/common/block/misc/MalumLogBlock.java
public class BlockStrippable extends RotatedPillarBlock {
    public final Supplier<? extends Block> stripped;

    public BlockStrippable(Properties p_55926_, Supplier<? extends Block> stripped) {
        super(p_55926_);
        this.stripped = stripped;
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, Level world, BlockPos pos, Player player, ItemStack stack,
        ToolAction toolAction) {
        if (toolAction == ToolActions.AXE_STRIP) {
            return stripped.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        } else {
            return null;
        }
    }
}
