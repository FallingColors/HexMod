package at.petrak.hexcasting.mixin.accessor.client;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(BlockEntityRenderDispatcher.class)
public interface AccessorBlockEntityRenderDispatcher {
    @Accessor("blockRenderDispatcher")
    Supplier<BlockRenderDispatcher> hex$getBlockRenderDispatcher();
}
