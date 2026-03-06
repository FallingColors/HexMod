package at.petrak.hexcasting.mixin.accessor.client;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MouseHandler.class, remap = false)
public interface AccessorMouseHandler {
    @Accessor("accumulatedScrollY")
    double hex$getAccumulatedScroll();

    @Accessor("accumulatedScrollY")
    void hex$setAccumulatedScroll(double scroll);
}
