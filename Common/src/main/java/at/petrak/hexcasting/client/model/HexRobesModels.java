package at.petrak.hexcasting.client.model;

// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexRobesModels {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into
    // this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(modLoc("robes"), "main");

    public static LayerDefinition variant1() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Hood = partdefinition.addOrReplaceChild("Hood",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, -8.0F, -4.0F));

        PartDefinition Horns = partdefinition.addOrReplaceChild("Horns",
            CubeListBuilder.create()
                .texOffs(24, 0).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 0).mirror().addBox(9.5F, 0.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .mirror(false),
            PartPose.offset(-4.8F, -8.2F, 0.0F));

        PartDefinition Torso = partdefinition.addOrReplaceChild("Torso",
            CubeListBuilder.create()
                .texOffs(40, 0).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 16).addBox(-8.0F, 0.0F, 0.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(4.0F, 0.0F, -2.0F));

        PartDefinition Arms = partdefinition.addOrReplaceChild("Arms",
            CubeListBuilder.create()
                .texOffs(0, 32).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).mirror().addBox(8.0F, 0.0F, 0.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .mirror(false),
            PartPose.offset(-4.0F, 0.0F, -2.0F));

        PartDefinition Skirt = partdefinition.addOrReplaceChild("Skirt", CubeListBuilder.create(),
            PartPose.offset(0.0F, 12.0F, -2.0F));

        PartDefinition Left_r1 = Skirt.addOrReplaceChild("Left_r1",
            CubeListBuilder.create()
                .texOffs(48, 32).mirror().addBox(0.1F, 0.0F, -4.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .mirror(false),
            PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.0F, -0.1309F));

        PartDefinition Right_r1 = Skirt.addOrReplaceChild("Right_r1",
            CubeListBuilder.create()
                .texOffs(48, 32).addBox(-4.0F, 0.0F, -4.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.0F, 0.1309F));

        PartDefinition Legs = partdefinition.addOrReplaceChild("Legs",
            CubeListBuilder.create().texOffs(16, 41)
                .addBox(-4.0F, 0.0F, 0.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 41).addBox(0.0F, 0.0F, 0.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 21.0F, -2.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}