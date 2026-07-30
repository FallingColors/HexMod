package at.petrak.hexcasting.mixin.datafixer;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.serialization.Dynamic;

import java.util.*;
import java.util.function.Function;
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
    private static final String HEX_IOTA_TYPE = "hexcasting:type";
    @Unique
    private static final String HEX_STORAGE_SEALED = "hexcasting:sealed";
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
    private static void hexCasting$fixIotaHolder(ItemStackData itemStackData, Dynamic<?> dynamic) {
        itemStackData.fixSubTag("data", true, MixinItemStackComponentizationFix::hexCasting$mapIotaData);
        itemStackData.moveTagToComponent("data", "hexcasting:iota");

        if (itemStackData.removeTag(HEX_STORAGE_SEALED).asBoolean(false))
            itemStackData.setComponent("hexcasting:sealed", dynamic.createMap(Map.of()));
    }

    @Unique
    private static Dynamic<?> hexCasting$mapIotaData(Dynamic<?> iota) {
        String iotaType = iota.get(HEX_IOTA_TYPE).asString("");
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
            case "hexcasting:boolean":
            case "hexcasting:double":
                component.put(iota.createString("value"), iotaData);
                break;
            case "hexcasting:list":
                List<Dynamic<?>> listData = iotaData.asList(MixinItemStackComponentizationFix::hexCasting$mapIotaData);
                component.put(iota.createString("list"), iota.createList(listData.stream()));
                break;
            case "hexcasting:pattern":
                component.put(iota.createString("value"), hexCasting$mapPattern(iotaData));
                break;
            case "hexcasting:vec3":
                component.put(iota.createString("value"), hexCasting$mapVec3(iotaData));
                break;
            case "hexcasting:continuation":
                List<Dynamic<?>> frames = iotaData.asList(MixinItemStackComponentizationFix::hexCasting$mapContinuationFrame);
                component.put(iota.createString("value"), iota.createList(frames.stream()));
                break;
            case "hexcasting:null":
            case "hexcasting:garbage":
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
        List<Byte> angles = pattern.get("angles").asList(dynamic0 -> dynamic0.asByte((byte) 0));
        patternComponent.put(pattern.createString(HexPattern.TAG_START_DIR), pattern.createString(hexCasting$hexDir[startDir]));
        patternComponent.put(pattern.createString(HexPattern.TAG_ANGLES), pattern.createString(String.join("", angles.stream().map(a -> hexCasting$hexAngle[a]).toList())));
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
    private static Dynamic<?> hexCasting$mapContinuationFrame(Dynamic<?> dynamic) {
        throw new NotImplementedException();
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
