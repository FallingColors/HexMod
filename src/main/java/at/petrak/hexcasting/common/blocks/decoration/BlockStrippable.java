package at.petrak.hexcasting.common.blocks.decoration;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

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
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (toolAction == ToolActions.AXE_STRIP) {
            var out = stripped.get().defaultBlockState();
            if (state.hasProperty(AXIS)) {
                out = out.setValue(AXIS, state.getValue(AXIS));
            }
            return out;
        } else {
            return null;
        }
    }
}
