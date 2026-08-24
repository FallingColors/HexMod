package at.petrak.hexcasting.client.model;

// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import at.petrak.hexcasting.api.HexAPI;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class HexRobesModel extends HumanoidArmorModel<LivingEntity> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into
    // this model's constructor
    // public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(modLoc("robes"), "main");

    final EquipmentSlot slot;

    public final ModelPart head;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;

    public HexRobesModel(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.slot = slot;

        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition variant1() {
        MeshDefinition meshdef = new MeshDefinition();
        PartDefinition root = meshdef.getRoot();

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        var head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("Hood",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, -8.0F, -4.0F));

        head.addOrReplaceChild("Horns",
            CubeListBuilder.create()
                .texOffs(24, 0).addBox(8.5F, 0.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).mirror().addBox(-7F, 0.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .mirror(false),
            PartPose.offset(-4.8F, -8.2F, 0.0F));

        var body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);

        body.addOrReplaceChild("Torso",
            CubeListBuilder.create()
                .texOffs(40, 0).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 16).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 0.0F, -2.0F));

        var rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);

        rightArm.addOrReplaceChild("right_sleeve",
            CubeListBuilder.create()
                .texOffs(0, 32).addBox(0.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-3.0F, -2.0F, -2.0F));

        var leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);

        leftArm.addOrReplaceChild("left_sleeve",
            CubeListBuilder.create()
                .texOffs(0, 32).mirror().addBox(0, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .mirror(false),
            PartPose.offset(-1.0F, -2.0F, -2.0F));

        var rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition Right_r1 = rightLeg.addOrReplaceChild("right_skirt",
            CubeListBuilder.create()
                .texOffs(48, 32).mirror().addBox(-2.2F, -0.5F, -6.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .mirror(false),
            PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.0F, 0.1309F));

        rightLeg.addOrReplaceChild("right_boot",
            CubeListBuilder.create()
                .texOffs(16, 41).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.0F, 9.0F, -2.0F));

        var leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition Left_r1 = leftLeg.addOrReplaceChild("left_skirt",
            CubeListBuilder.create()
                .texOffs(48, 32).addBox(-1.8F, -0.5F, -6.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.0F, -0.1309F));

        leftLeg.addOrReplaceChild("left_boot",
            CubeListBuilder.create()
                .texOffs(16, 41).addBox(0.0F, 0.0F, 0.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, 9.0F, -2.0F));

        return LayerDefinition.create(meshdef, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack ms, VertexConsumer buffer, int light, int overlay, int color) {
        renderArmorPart(slot);
        super.renderToBuffer(ms, buffer, light, overlay, color);
    }

    private void renderArmorPart(EquipmentSlot slot) {
        setAllVisible(false);
        rightLeg.getChild("right_skirt").visible = false;
        leftLeg.getChild("left_skirt").visible = false;
        rightLeg.getChild("right_boot").visible = false;
        leftLeg.getChild("left_boot").visible = false;

        switch (slot) {
            case HEAD -> head.visible = true;
            case CHEST -> {
                body.visible = true;
                rightArm.visible = true;
                leftArm.visible = true;
            }
            case LEGS -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
                rightLeg.getChild("right_skirt").visible = true;
                leftLeg.getChild("left_skirt").visible = true;
            }
            case FEET -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
                rightLeg.getChild("right_boot").visible = true;
                leftLeg.getChild("left_boot").visible = true;
            }
            case MAINHAND, OFFHAND -> {
            }
        }
    }
}