package at.petrak.hexcasting.mixin.datafixer;

import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix.ItemStackData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackComponentizationFix.class)
public class MixinItemStackComponentizationFix {
    private static final String HEX_IOTA_COMPONENT = "hexcasting:iota";
    private static final String HEX_IOTA_TYPE = "hexcasting:type";
    private static final String HEX_STORAGE_SEALED = "hexcasting:sealed";
    private static final String HEX_DATA = "hexcasting:data";

    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void fixHexItemStack(ItemStackData itemStackData, Dynamic<?> dynamic, CallbackInfo ci) {
        if (itemStackData.is(Set.of("hexcasting:focus", "hexcasting:thought_knot"))) {
            Map<String, Dynamic<?>> data =
                itemStackData
                    .removeTag("data")
                    .asMap((dynamicx) -> dynamicx.asString(""), (dynamicx) -> dynamicx);
            if (data.containsKey(HEX_DATA) && data.containsKey(HEX_IOTA_TYPE)) {
              hexCasting$fixIotaHolder(
                  itemStackData,
                  dynamic,
                  data.remove(HEX_IOTA_TYPE).asString(""),
                  data.remove(HEX_DATA),
                  data.remove(HEX_STORAGE_SEALED).asBoolean(false));
            }
        }
    }


    @Unique
    private static void hexCasting$fixIotaHolder(ItemStackData itemStackData, Dynamic<?> dynamic, String iotaType, Dynamic<?> hexData, boolean sealed) {
        Map<Dynamic<?>, Dynamic<?>> component = new HashMap<>();
        component.put(dynamic.createString("type"), dynamic.createString(iotaType));
        component.put(dynamic.createString("value"), hexData);
        if (sealed) component.put(dynamic.createString("sealed"), dynamic.createBoolean(true));
        itemStackData.setComponent(HEX_IOTA_COMPONENT, dynamic.createMap(component));
    }
}
