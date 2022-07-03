package at.petrak.hexcasting.mixin.accessor.client;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface AccessorMouseHandler {
	@Accessor("accumulatedScroll")
	double hex$getAccumulatedScroll();

	@Accessor("accumulatedScroll")
	void hex$setAccumulatedScroll(double scroll);
}
