package at.petrak.hexcasting.mixin.client;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// Do this with a mixin instead of a model like you're supposed to because
// 1. cause it's easier
// 2. players wearing normal elytra and altiora won't get z fighting
@Mixin(ElytraLayer.class)
public class MixinElytraLayer {
    private static final ResourceLocation ALTIORA_LOC = modLoc("textures/misc/altiora.png");

    // The "do i draw" check is one of the thing forge clobbers, so we do that in side-specific mixins

    @ModifyVariable(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;" +
            "ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0),
        ordinal = 0
    )
    private ResourceLocation injectAltioraLocation(ResourceLocation texLoc, PoseStack ps, MultiBufferSource mbs,
        int packedLight, LivingEntity e) {
        if (e instanceof Player player) {
            if (IXplatAbstractions.INSTANCE.getAltiora(player) != null) {
                return ALTIORA_LOC;
            }
        }

        return texLoc;
    }
}
