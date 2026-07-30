package at.petrak.hexcasting.mixin.datafixer;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.serialization.Dynamic;

import java.util.HashMap;
import java.util.List;
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
    @Unique
    private static final String HEX_IOTA_TYPE = "hexcasting:type";
    @Unique
    private static final String HEX_STORAGE_SEALED = "hexcasting:sealed";
    @Unique
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
                        data.get(HEX_IOTA_TYPE).asString(""),
                        data.get(HEX_DATA),
                        data.containsKey(HEX_STORAGE_SEALED) && data.get(HEX_STORAGE_SEALED).asBoolean(false));
            }
        }
        if (itemStackData.is(Set.of("hexcasting:scroll_small", "hexcasting:scroll_medium", "hexcasting:scroll"))) {
            hexCasting$fixScroll(itemStackData, dynamic);
        }
        if (itemStackData.is("hexcasting:ancient_cypher")) {
            itemStackData.moveTagToComponent("hex_name", "hexcasting:hex_name");
        }
        if (itemStackData.is("hexcasting:abacus")) {
            // TODO: abacus value component
        }
        if (itemStackData.is("hexcasting:spellbook")) {
            hexCasting$fixSpellbook(itemStackData, dynamic);
        }

        // TODO: visual override component
        // TODO: item variant component
        // TODO: wait for #1220, then hex holder component
        // TODO: media component
        // TODO: media_max component
    }

    @Unique
    private static void hexCasting$fixIotaHolder(ItemStackData itemStackData, Dynamic<?> dynamic, String iotaType, Dynamic<?> hexData, boolean sealed) {
        Map<Dynamic<?>, Dynamic<?>> component = new HashMap<>();
        component.put(dynamic.createString("type"), dynamic.createString(iotaType));
        component.put(dynamic.createString("value"), hexData);
        itemStackData.setComponent("hexcasting:iota", dynamic.createMap(component));
        if (sealed) itemStackData.setComponent("hexcasting:sealed", dynamic.createMap(Map.of()));
    }

    @Unique
    private static final String[] hexCasting$hexDir = {"NORTH_EAST", "EAST", "SOUTH_EAST", "SOUTH_WEST", "WEST", "NORTH_WEST"};
    @Unique
    private static final String[] hexCasting$hexAngle = {"w", "e", "d", "s", "a", "q"};

    @Unique
    private static void hexCasting$fixScroll(ItemStackData itemStackData, Dynamic<?> dynamic) {
        Dynamic<?> pattern = itemStackData.removeTag("pattern").orElseEmptyMap();
        byte startDir = pattern.get("start_dir").asByte((byte) 0);
        List<Byte> angles = pattern.get("angles").asList(dynamic0 -> dynamic0.asByte((byte) 0));
        Map<Dynamic<?>, Dynamic<?>> patternComponent = new HashMap<>();
        patternComponent.put(dynamic.createString(HexPattern.TAG_START_DIR), dynamic.createString(hexCasting$hexDir[startDir]));
        patternComponent.put(dynamic.createString(HexPattern.TAG_ANGLES), dynamic.createString(String.join("", angles.stream().map(a -> hexCasting$hexAngle[a]).toList())));
        itemStackData.setComponent("hexcasting:pattern", dynamic.createMap(patternComponent));

        itemStackData.moveTagToComponent("op_id", "hexcasting:op_id");
        itemStackData.moveTagToComponent("recalc_warning", "hexcasting:recalc_warning");
        itemStackData.moveTagToComponent("needs_purchase", "hexcasting:needs_purchase");
    }

    @Unique
    private static void hexCasting$fixSpellbook(ItemStackData itemStackData, Dynamic<?> dynamic) {
        // TODO: all spellbook components
    }
}
