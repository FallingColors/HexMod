package at.petrak.hexcasting.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

// https://github.com/VazkiiMods/Botania/blob/1.19.x/Xplat/src/main/java/vazkii/botania/client/model/armor/ArmorModel.java
public class MyOwnArmorModelWithBlackjackAndHookers extends HumanoidModel<LivingEntity> {
    protected final EquipmentSlot slot;

    public MyOwnArmorModelWithBlackjackAndHookers(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.slot = slot;
    }

    // [VanillaCopy] ArmorStandArmorModel.setupAnim because armor stands are dumb
    // This fixes the armor "breathing" and helmets always facing south on armor stands
    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
        float netHeadYaw, float headPitch) {
        if (!(entity instanceof ArmorStand entityIn)) {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            return;
        }

        this.head.xRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getX();
        this.head.yRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getY();
        this.head.zRot = ((float) Math.PI / 180F) * entityIn.getHeadPose().getZ();
        this.head.setPos(0.0F, 1.0F, 0.0F);
        this.body.xRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getX();
        this.body.yRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getY();
        this.body.zRot = ((float) Math.PI / 180F) * entityIn.getBodyPose().getZ();
        this.leftArm.xRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().getX();
        this.leftArm.yRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().getY();
        this.leftArm.zRot = ((float) Math.PI / 180F) * entityIn.getLeftArmPose().getZ();
        this.rightArm.xRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().getX();
        this.rightArm.yRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().getY();
        this.rightArm.zRot = ((float) Math.PI / 180F) * entityIn.getRightArmPose().getZ();
        this.leftLeg.xRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().getX();
        this.leftLeg.yRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().getY();
        this.leftLeg.zRot = ((float) Math.PI / 180F) * entityIn.getLeftLegPose().getZ();
        this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
        this.rightLeg.xRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().getX();
        this.rightLeg.yRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().getY();
        this.rightLeg.zRot = ((float) Math.PI / 180F) * entityIn.getRightLegPose().getZ();
        this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
        this.hat.copyFrom(this.head);
    }

    @Override
    public void renderToBuffer(PoseStack ms, VertexConsumer buffer, int light, int overlay, float r, float g, float b
        , float a) {
        setPartVisibility(slot);
        super.renderToBuffer(ms, buffer, light, overlay, r, g, b, a);
    }

    // [VanillaCopy] HumanoidArmorLayer
    private void setPartVisibility(EquipmentSlot slot) {
        setAllVisible(false);
        switch (slot) {
            case HEAD -> {
                head.visible = true;
                hat.visible = true;
            }
            case CHEST -> {
                body.visible = true;
                rightArm.visible = true;
                leftArm.visible = true;
            }
            case LEGS -> {
                body.visible = true;
                rightLeg.visible = true;
                leftLeg.visible = true;
            }
            case FEET -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
            }
        }
    }
}
