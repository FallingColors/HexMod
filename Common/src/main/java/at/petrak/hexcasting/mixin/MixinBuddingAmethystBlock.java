package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.common.lib.HexBlocks;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuddingAmethystBlock.class)
public class MixinBuddingAmethystBlock {
    // This prevents neural mesh from slowing down budding by taking up potential growth faces.
    // If it tries to grow a face with mesh on it, this makes it try to pick another random face
    // which does not have mesh on it. If all faces have mesh, the loop ends and nothing happens.
    @Inject(method = "randomTick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), require = 1)
    private void rerollFaceIfNeuralMesh(
        BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource,
        CallbackInfo ci,
        @Local LocalRef<Direction> targetDir, @Local(ordinal = 1) LocalRef<BlockPos> targetPos, @Local(ordinal = 1) LocalRef<BlockState> targetState
    ) {
        if (targetState.get().is(HexBlocks.NEURAL_MESH.get())
        && MultifaceBlock.availableFaces(targetState.get()).contains(targetDir.get().getOpposite())) {
            for (var checkDir : Direction.allShuffled(randomSource)) {
                BlockPos checkPos = blockPos.relative(checkDir);
                BlockState checkState = serverLevel.getBlockState(checkPos);
                if (!checkState.is(HexBlocks.NEURAL_MESH.get())) {
                    targetDir.set(checkDir);
                    targetPos.set(checkPos);
                    targetState.set(checkState);
                    return;
                }
            }
        }
    }
}
