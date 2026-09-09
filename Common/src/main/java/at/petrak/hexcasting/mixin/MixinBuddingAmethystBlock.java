package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.common.lib.HexBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BuddingAmethystBlock.class)
public class MixinBuddingAmethystBlock {
    // When combined with the extra random ticks provided by the neural mesh blocks themselves, this results
    // in an overall speed boost equivalent to each mesh block providing 5 extra random ticks whenever it
    // receives a single random tick. We do it this way (rather than actually ticking the budding block five
    // times per mesh tick) to avoid sudden jumps in growth progress.
    @WrapOperation(
        method = "randomTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0)
    )
    private int increaseSuccessChanceWithMesh(
        RandomSource instance, int originalArg, Operation<Integer> original,
        BlockState selfState, ServerLevel serverLevel, BlockPos selfPos, RandomSource randomSource
    ) {
        int meshCount = 0;
        for (Direction dir : Direction.values()) {
            BlockPos checkPos = selfPos.relative(dir);
            if (serverLevel.getBlockState(checkPos).is(HexBlocks.NEURAL_MESH.get())) {
                meshCount++;
            }
        }
        if (meshCount == 0)
            return original.call(instance, originalArg);
        boolean success = switch (meshCount) {
            case 1 -> (randomSource.nextDouble() < 0.6);
            case 2 -> (randomSource.nextDouble() < 11.0/15.0);
            case 3 -> (randomSource.nextDouble() < 0.8);
            case 4 -> (randomSource.nextDouble() < 0.84);
            case 5 -> (randomSource.nextDouble() < 13.0/15.0);
            default -> false;
        };
        return success ? 0 : -1;
    }
}
