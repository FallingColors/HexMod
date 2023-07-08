package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.client.RegisterClientStuff;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

// https://github.com/VazkiiMods/Botania/blob/986dff2e8cd9f40f7e4d6ed7b30c98944bdb3b87/Fabric/src/main/java/vazkii/botania/fabric/mixin/client/ModelManagerFabricMixin.java#L34
@Mixin(ModelManager.class)
public class FabricModelManagerMixin {
    @Shadow
    private Map<ResourceLocation, BakedModel> bakedRegistry;

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/resources/model/ModelBakery;" +
        "getBakedTopLevelModels()Ljava/util/Map;", shift = At.Shift.AFTER),
        method = "Lnet/minecraft/client/resources/model/ModelManager;apply(" +
                "Lnet/minecraft/client/resources/model/ModelManager$ReloadState;" +
                "Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onModelBake(ModelManager.ReloadState reloadState, ProfilerFiller profiler, CallbackInfo ci, ModelBakery modelLoader) {
        RegisterClientStuff.onModelBake(modelLoader, this.bakedRegistry);
    }
}
