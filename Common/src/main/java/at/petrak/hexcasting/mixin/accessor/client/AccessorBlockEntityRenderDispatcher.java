package at.petrak.hexcasting.mixin.accessor.client;

import java.util.function.Supplier;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntityRenderDispatcher.class)
public interface AccessorBlockEntityRenderDispatcher {
	@Accessor("blockRenderDispatcher")
	Supplier<BlockRenderDispatcher> hex$getBlockRenderDispatcher();
}
