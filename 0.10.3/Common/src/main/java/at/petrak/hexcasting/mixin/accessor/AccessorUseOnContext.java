package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(UseOnContext.class)
public interface AccessorUseOnContext {
    @Invoker("<init>")
    static UseOnContext hex$new(Level $$0, @Nullable Player $$1, InteractionHand $$2, ItemStack $$3,
        BlockHitResult $$4) {
        throw new IllegalStateException();
    }
}
