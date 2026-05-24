package at.petrak.hexcasting.forge.interop.curios;

import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class CuriosRenderers {
    public static void register() {
        CuriosRendererRegistry.register(HexItems.SCRYING_LENS, () -> new LensCurioRenderer(Minecraft.getInstance().getEntityModels().bakeLayer(LensCurioRenderer.LAYER)));
    }

    public static void onLayerRegister(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LensCurioRenderer.LAYER, () -> {
            CubeListBuilder builder = new CubeListBuilder();
            MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
            mesh.getRoot().addOrReplaceChild("head", builder, PartPose.ZERO);
            return LayerDefinition.create(mesh, 1, 1);
        });
    }
}
