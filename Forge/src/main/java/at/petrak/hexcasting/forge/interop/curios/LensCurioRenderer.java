package at.petrak.hexcasting.forge.interop.curios;

import at.petrak.hexcasting.api.HexAPI;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class LensCurioRenderer implements ICurioRenderer {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(HexAPI.MOD_ID, "lens"), "lens");

	private final HumanoidModel<LivingEntity> model;

	public LensCurioRenderer(ModelPart part) {
		this.model = new HumanoidModel<>(part);
	}

	@Override
	public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		// https://github.com/Creators-of-Create/Create/blob/ee33823ed0b5084af10ed131a1626ce71db4c07e/src/main/java/com/simibubi/create/compat/curios/GogglesCurioRenderer.java

		// Prepare values for transformation
		model.setupAnim(slotContext.entity(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		model.prepareMobModel(slotContext.entity(), limbSwing, limbSwingAmount, partialTicks);
		ICurioRenderer.followHeadRotations(slotContext.entity(), model.head);

		// Translate and rotate with our head
		matrixStack.pushPose();
		matrixStack.translate(model.head.x / 16.0, model.head.y / 16.0, model.head.z / 16.0);
		matrixStack.mulPose(Vector3f.YP.rotation(model.head.yRot));
		matrixStack.mulPose(Vector3f.XP.rotation(model.head.xRot));

		// Translate and scale to our head
		matrixStack.translate(0, -0.25, 0);
		matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
		matrixStack.scale(0.625f, 0.625f, 0.625f);

		// Render
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.HEAD, light, OverlayTexture.NO_OVERLAY, matrixStack, renderTypeBuffer, 0);
		matrixStack.popPose();
	}
}
