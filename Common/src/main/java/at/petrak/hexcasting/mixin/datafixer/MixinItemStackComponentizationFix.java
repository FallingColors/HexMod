package at.petrak.hexcasting.mixin.datafixer;

import com.mojang.serialization.Dynamic;
import java.util.Map;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackComponentizationFix.class)
public class MixinItemStackComponentizationFix {
    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void fixHexItemStack(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic, CallbackInfo ci) {
        if (itemStackData.is("hexcasting:focus")) {
            Map<String, Dynamic<?>> data = itemStackData.removeTag("data").asMap((dynamicx) -> dynamicx.asString(""), (dynamicx) -> dynamicx);
            Dynamic<?> component = dynamic.createMap(Map.of(dynamic.createString("type"), data.get("hexcasting:type"), dynamic.createString("value"), data.get("hexcasting:data")));
            itemStackData.setComponent("hexcasting:iota", component);
        }
    }
}
