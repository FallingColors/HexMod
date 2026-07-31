package at.petrak.hexcasting.mixin.datafixer;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.serialization.Dynamic;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix.ItemStackData;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackComponentizationFix.class)
public class MixinItemStackComponentizationFix {
    @Unique
    private static final String HEX_TYPE = "hexcasting:type";
    @Unique
    private static final String HEX_DATA = "hexcasting:data";

    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void fixHexItemStack(ItemStackData itemStackData, Dynamic<?> dynamic, CallbackInfo ci) {
        if (itemStackData.is(Set.of("hexcasting:focus", "hexcasting:thought_knot"))) {
            hexCasting$fixIotaHolder(itemStackData, dynamic);
        }
        if (itemStackData.is(Set.of("hexcasting:scroll_small", "hexcasting:scroll_medium", "hexcasting:scroll"))) {
            hexCasting$fixScroll(itemStackData);
        }
        if (itemStackData.is("hexcasting:ancient_cypher")) {
            itemStackData.moveTagToComponent("hex_name", "hexcasting:hex_name");
        }
        if (itemStackData.is("hexcasting:abacus")) {
            itemStackData.moveTagToComponent("value", "hexcasting:abacus_value");
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
    private static void hexCasting$fixIotaHolder(ItemStackData itemStackData, Dynamic<?> dynamic) {
        itemStackData.fixSubTag("data", true, MixinItemStackComponentizationFix::hexCasting$mapIotaData);
        itemStackData.moveTagToComponent("data", "hexcasting:iota");

        if (itemStackData.removeTag("sealed").asBoolean(false))
            itemStackData.setComponent("hexcasting:sealed", dynamic.createMap(Map.of()));
    }

    @Unique
    private static Dynamic<?> hexCasting$mapIotaData(Dynamic<?> iota) {
        String iotaType = iota.get(HEX_TYPE).asString("");
        Dynamic<?> iotaData = iota.get(HEX_DATA).orElseEmptyMap();
        Map<Dynamic<?>, Dynamic<?>> component = new HashMap<>();
        component.put(iota.createString("type"), iota.createString(iotaType));
        switch (iotaType) {
            case "hexcasting:entity":
                IntStream uuid = iotaData.get("uuid").asIntStream();
                // Unfortunately, it seems like we cannot easily fix InlineAPI components, so we will have to live with "an unknown entity"
                component.put(iota.createString("entityId"), iota.createIntList(uuid));
                component.put(iota.createString("isPlayer"), iota.createBoolean(true));
                break;
            case "hexcasting:boolean", "hexcasting:double":
                component.put(iota.createString("value"), iotaData);
                break;
            case "hexcasting:list":
                component.put(iota.createString("list"),
                        iota.createList(iotaData.asStream().map(MixinItemStackComponentizationFix::hexCasting$mapIotaData)));
                break;
            case "hexcasting:pattern":
                component.put(iota.createString("value"), hexCasting$mapPattern(iotaData));
                break;
            case "hexcasting:vec3":
                component.put(iota.createString("value"), hexCasting$mapVec3(iotaData));
                break;
            case "hexcasting:continuation":
                component.put(iota.createString("value"),
                        iota.createList(iotaData.asStream().map(MixinItemStackComponentizationFix::hexCasting$mapContinuationFrame)));
                break;
            case "hexcasting:null", "hexcasting:garbage":
            default:
                break;
        }
        return iota.createMap(component);
    }

    @Unique
    private static final String[] hexCasting$hexDir = {"NORTH_EAST", "EAST", "SOUTH_EAST", "SOUTH_WEST", "WEST", "NORTH_WEST"};
    @Unique
    private static final String[] hexCasting$hexAngle = {"w", "e", "d", "s", "a", "q"};
    @Unique
    private static Dynamic<?> hexCasting$mapPattern(Dynamic<?> pattern) {
        Map<Dynamic<?>, Dynamic<?>> patternComponent = new HashMap<>();
        byte startDir = pattern.get("start_dir").asByte((byte) 0);
        List<String> angles = pattern.get("angles").asStream()
                .map(a -> hexCasting$hexAngle[a.asByte((byte) 0)])
                .toList();
        patternComponent.put(pattern.createString(HexPattern.TAG_START_DIR), pattern.createString(hexCasting$hexDir[startDir]));
        patternComponent.put(pattern.createString(HexPattern.TAG_ANGLES), pattern.createString(String.join("", angles)));
        return pattern.createMap(patternComponent);
    }

    @Unique
    private static Dynamic<?> hexCasting$mapVec3(Dynamic<?> iotaData) {
        double x = iotaData.get("x").asDouble(0);
        double y = iotaData.get("y").asDouble(0);
        double z = iotaData.get("z").asDouble(0);
        return iotaData.createList(Stream.of(iotaData.createDouble(x), iotaData.createDouble(y), iotaData.createDouble(z)));
    }

    @Unique
    private static Dynamic<?> hexCasting$mapContinuationFrame(Dynamic<?> frame) {
        String type = frame.get(HEX_TYPE).asString("");
        Dynamic<?> frameData = frame.get(HEX_DATA).orElseEmptyMap();
        Map<Dynamic<?>, Dynamic<?>> component = new HashMap<>();
        component.put(frame.createString("type"), frame.createString(type));
        switch (type) {
            case "hexcasting:evaluate":
                component.put(frame.createString("patterns"),
                        frame.createList(frameData.get("patterns").asStream().map(MixinItemStackComponentizationFix::hexCasting$mapIotaData)));
                component.put(frame.createString("isMetacasting"),
                        frame.createBoolean(frameData.get("isMetacasting").asBoolean(false)));
                break;
            case "hexcasting:foreach":
                component.put(frame.createString("data"),
                        frame.createList(frameData.get("data").asStream().map(MixinItemStackComponentizationFix::hexCasting$mapIotaData)));
                component.put(frame.createString("code"),
                        frame.createList(frameData.get("code").asStream().map(MixinItemStackComponentizationFix::hexCasting$mapIotaData)));
                component.put(frame.createString("context"),
                        frame.createList(frameData.get("base").asStream().map(MixinItemStackComponentizationFix::hexCasting$mapIotaData)));
                // TODO: figure out whether stashed should be base (replicating otherwise unavailable 1.20 thoth behavior) or an empty list (equivalent to 1.21 thoth with whatever size the stack had before casting)
                component.put(frame.createString("stashed"),
                        frame.createList(frameData.get("base").asStream().map(MixinItemStackComponentizationFix::hexCasting$mapIotaData)));
                component.put(frame.createString("accumulator"),
                        frame.createList(frameData.get("accumulator").asStream().map(MixinItemStackComponentizationFix::hexCasting$mapIotaData)));
                break;
            case "hexcasting:end":
            default:
                break;
        }
        return frame.createMap(component);
    }

    @Unique
    private static void hexCasting$fixScroll(ItemStackData itemStackData) {
        itemStackData.fixSubTag("pattern", true, MixinItemStackComponentizationFix::hexCasting$mapPattern);

        itemStackData.moveTagToComponent("pattern", "hexcasting:pattern");
        itemStackData.moveTagToComponent("op_id", "hexcasting:op_id");
        itemStackData.moveTagToComponent("recalc_warning", "hexcasting:recalc_warning");
        itemStackData.moveTagToComponent("needs_purchase", "hexcasting:needs_purchase");
    }

    @Unique
    private static void hexCasting$fixSpellbook(ItemStackData itemStackData, Dynamic<?> dynamic) {
        // TODO: all spellbook components
    }
}
