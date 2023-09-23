package at.petrak.hexcasting.client.model;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ModelArmor extends HumanoidModel<LivingEntity> {

    public final EquipmentSlot slot;
    public float armorScale = 1.05f;

    public final ModelPart rightFoot;
    public final ModelPart leftFoot;

    public ModelArmor(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.rightFoot = root.getChild("right_foot");
        this.leftFoot = root.getChild("left_foot");

        this.slot = slot;
        this.young = false;
    }


    public static MeshDefinition createArmorMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_foot", CubeListBuilder.create(), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_foot", CubeListBuilder.create(), PartPose.ZERO);

        return meshdefinition;
    }

    @Override
    public void setupAnim(LivingEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entityIn instanceof ArmorStand)) {
            super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            return;
        }
        if (entityIn instanceof ArmorStand armorStand) {
            this.head.xRot = ((float) Math.PI / 180F) * armorStand.getHeadPose().getX();
            this.head.yRot = ((float) Math.PI / 180F) * armorStand.getHeadPose().getY();
            this.head.zRot = ((float) Math.PI / 180F) * armorStand.getHeadPose().getZ();
            this.head.setPos(0.0F, 1.0F, 0.0F);
            this.body.xRot = ((float) Math.PI / 180F) * armorStand.getBodyPose().getX();
            this.body.yRot = ((float) Math.PI / 180F) * armorStand.getBodyPose().getY();
            this.body.zRot = ((float) Math.PI / 180F) * armorStand.getBodyPose().getZ();
            this.leftArm.xRot = ((float) Math.PI / 180F) * armorStand.getLeftArmPose().getX();
            this.leftArm.yRot = ((float) Math.PI / 180F) * armorStand.getLeftArmPose().getY();
            this.leftArm.zRot = ((float) Math.PI / 180F) * armorStand.getLeftArmPose().getZ();
            this.rightArm.xRot = ((float) Math.PI / 180F) * armorStand.getRightArmPose().getX();
            this.rightArm.yRot = ((float) Math.PI / 180F) * armorStand.getRightArmPose().getY();
            this.rightArm.zRot = ((float) Math.PI / 180F) * armorStand.getRightArmPose().getZ();
            this.leftLeg.xRot = ((float) Math.PI / 180F) * armorStand.getLeftLegPose().getX();
            this.leftLeg.yRot = ((float) Math.PI / 180F) * armorStand.getLeftLegPose().getY();
            this.leftLeg.zRot = ((float) Math.PI / 180F) * armorStand.getLeftLegPose().getZ();
            this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
            this.rightLeg.xRot = ((float) Math.PI / 180F) * armorStand.getRightLegPose().getX();
            this.rightLeg.yRot = ((float) Math.PI / 180F) * armorStand.getRightLegPose().getY();
            this.rightLeg.zRot = ((float) Math.PI / 180F) * armorStand.getRightLegPose().getZ();
            this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
            this.hat.copyFrom(this.head);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        poseStack.scale(armorScale, armorScale, armorScale);
        this.setHeadRotation();
        this.setChestRotation();
        this.setLegsRotation();
        this.setBootRotation();
        head.visible = slot == EquipmentSlot.HEAD;
        body.visible = slot == EquipmentSlot.CHEST;
        rightArm.visible = slot == EquipmentSlot.CHEST;
        leftArm.visible = slot == EquipmentSlot.CHEST;
        rightLeg.visible = slot == EquipmentSlot.LEGS;
        leftLeg.visible = slot == EquipmentSlot.LEGS;
        rightFoot.visible = slot == EquipmentSlot.FEET;
        leftFoot.visible = slot == EquipmentSlot.FEET;
        if (this.young) {
            float f = 2.0F;
            poseStack.pushPose();
            poseStack.scale(1.5F / f, 1.5F / f, 1.5F / f);
            poseStack.translate(0.0F, 16.0F * 1, 0.0F);
            head.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            poseStack.popPose();
            poseStack.pushPose();
            poseStack.scale(1.0F / f, 1.0F / f, 1.0F / f);
            poseStack.translate(0.0F, 24.0F * 1, 0.0F);
            body.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            poseStack.popPose();
        } else {
            head.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            if (crouching) {
                poseStack.translate(0.0F, 0.2F, 0.0F);
            }
            body.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

            poseStack.pushPose();
            if (crouching) {
                poseStack.translate(0.0F, -0.15F, 0.0F);
            }
            rightArm.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            leftArm.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            poseStack.popPose();
        }
        poseStack.translate(0.0F, 1.25F, 0.0F);
        if (crouching) {
            poseStack.translate(0.0F, -0.15F, 0.05F);
        }
        rightLeg.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftLeg.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        rightFoot.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftFoot.render(poseStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        poseStack.popPose();
    }

    public void setHeadRotation() {
        setRotation(head, head.xRot, head.yRot, head.zRot);
    }

    public void setChestRotation() {
        /* if (e instanceof EntityPlayer){ ((EntityPlayer)e).get } */
        this.body.y = body.y - 1;
        this.rightArm.x = rightArm.x + 5;
        this.rightArm.y = rightArm.y - 1;
        this.leftArm.x = leftArm.x - 5;
        this.leftArm.y = leftArm.y - 1;
        setRotation(body, body.xRot, body.yRot, body.zRot);
        setRotation(rightArm, rightArm.xRot, rightArm.yRot, rightArm.zRot);
        setRotation(leftArm, leftArm.xRot, leftArm.yRot, leftArm.zRot);
    }

    public void setLegsRotation() {
        this.rightLeg.x = rightLeg.x + 2;
        this.rightLeg.y = rightLeg.y - 22;
        this.leftLeg.x = leftLeg.x - 2;
        this.leftLeg.y = leftLeg.y - 22;
        setRotation(rightLeg, rightLeg.xRot, rightLeg.yRot, rightLeg.zRot);
        setRotation(leftLeg, leftLeg.xRot, leftLeg.yRot, leftLeg.zRot);
    }

    public void setBootRotation() {
        this.rightFoot.y = rightLeg.y - 0;
        this.rightFoot.z = rightLeg.z;
        this.leftFoot.y = leftLeg.y - 0;
        this.leftFoot.z = leftLeg.z;
        setRotation(rightFoot, rightLeg.xRot, rightLeg.yRot, rightLeg.zRot);
        setRotation(leftFoot, leftLeg.xRot, leftLeg.yRot, leftLeg.zRot);
    }

    public static void setRotation(ModelPart model, float x, float y, float z) {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
