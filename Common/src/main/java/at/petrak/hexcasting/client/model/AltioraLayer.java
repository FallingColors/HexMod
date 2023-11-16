package at.petrak.hexcasting.client.model;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class AltioraLayer<M extends EntityModel<AbstractClientPlayer>> extends RenderLayer<AbstractClientPlayer, M> {
    private static final ResourceLocation TEX_LOC = modLoc("textures/misc/altiora.png");

    private final ElytraModel<AbstractClientPlayer> elytraModel;

    public AltioraLayer(RenderLayerParent<AbstractClientPlayer, M> renderer, EntityModelSet ems) {
        super(renderer);
        this.elytraModel = new ElytraModel<>(ems.bakeLayer(HexModelLayers.ALTIORA));
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
        float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
        float headPitch) {
        var altiora = IXplatAbstractions.INSTANCE.getAltiora(player);
        // do a best effort to not render over other elytra, although we can never patch up everything
        var chestSlot = player.getItemBySlot(EquipmentSlot.CHEST);
        if (altiora != null && !chestSlot.is(Items.ELYTRA)) {
            ps.pushPose();
            ps.translate(0.0, 0.0, 0.125);

            this.getParentModel().copyPropertiesTo(this.elytraModel);
            this.elytraModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer verts = ItemRenderer.getArmorFoilBuffer(
                buffer, RenderType.armorCutoutNoCull(TEX_LOC), false, true);
            this.elytraModel.renderToBuffer(ps, verts, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

            ps.popPose();
        }
    }
}
