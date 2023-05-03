package at.petrak.hexcasting.mixin.client;

import at.petrak.hexcasting.client.model.AltioraLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin is the approach Ears uses
// granted, Ears isn't exactly the paragon of "how to make your average minecraft mod" but still
// IDK another way to do it
@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer,
    PlayerModel<AbstractClientPlayer>> {
    public MixinPlayerRenderer(EntityRendererProvider.Context $$0, PlayerModel<AbstractClientPlayer> $$1, float $$2) {
        super($$0, $$1, $$2);
    }

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void hex$init(EntityRendererProvider.Context erp, boolean slimModel, CallbackInfo ci) {
        this.addLayer(new AltioraLayer<>(this, erp.getModelSet()));
    }
}
