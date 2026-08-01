package at.petrak.hexcasting.mixin.datafixer;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import com.mojang.serialization.Dynamic;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix.ItemStackData;
import net.minecraft.world.item.Item;
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

    @Inject(method = "fixItemStack", at = @At("HEAD"))
    private static void preFixHexItemStack(ItemStackData itemStackData, Dynamic<?> dynamic, CallbackInfo ci) {
        if (itemStackData.is("hexcasting:slate")) {
            itemStackData.fixSubTag("BlockEntityTag", false, dynamic0 ->
                    hexCasting$mapPattern(dynamic0.get("pattern").orElseEmptyMap()));
            itemStackData.moveTagToComponent("BlockEntityTag", "hexcasting:pattern");
        }
    }

    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void fixHexItemStack(ItemStackData itemStackData, Dynamic<?> dynamic, CallbackInfo ci) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemStackData.item));

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
            hexCasting$fixSpellbook(itemStackData);
        }
        if (item instanceof IotaHolderItem) {
            itemStackData.moveTagToComponent("VisualOverride", "hexcasting:visual_override");
        }
        if (item instanceof VariantItem) {
            itemStackData.moveTagToComponent("variant", "hexcasting:variant");
        }
        if (item instanceof ItemPackagedHex) {
            hexCasting$fixPackagedHex(itemStackData, dynamic);
        }
        if (item instanceof ItemMediaHolder) {
            itemStackData.moveTagToComponent("hexcasting:media", "hexcasting:media");
            itemStackData.moveTagToComponent("hexcasting:start_media", "hexcasting:start_media");
        }
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
                component.put(frame.createString("stashed"),
                        frame.createList(Stream.of()));
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
    private static Dynamic<?> hexCasting$stringToPlainComponent(Dynamic<?> string) {
        return string.createMap(Map.of(string.createString("text"), string));
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
    private static void hexCasting$fixSpellbook(ItemStackData itemStackData) {
        itemStackData.moveTagToComponent("page_idx", "hexcasting:page_idx");

        itemStackData.fixSubTag("page_names", true, d0 ->
            d0.createMap(d0.asMap(Function.identity(), MixinItemStackComponentizationFix::hexCasting$stringToPlainComponent)));
        itemStackData.moveTagToComponent("page_names", "hexcasting:page_names");

        itemStackData.fixSubTag("pages", true, d0 ->
            d0.createMap(d0.asMap(Function.identity(), MixinItemStackComponentizationFix::hexCasting$mapIotaData)));
        itemStackData.moveTagToComponent("pages", "hexcasting:pages");

        itemStackData.moveTagToComponent("sealed_pages", "hexcasting:sealed_pages");
    }

    @Unique
    private static void hexCasting$fixPackagedHex(ItemStackData itemStackData, Dynamic<?> dynamic) {
        Stream<Dynamic<?>> patterns = itemStackData.removeTag("patterns").asStream()
            .map(MixinItemStackComponentizationFix::hexCasting$mapIotaData);
        Dynamic<?> pigment = itemStackData.removeTag("pigment").orElseEmptyMap();
        Dynamic<?> hexHolder = dynamic.createMap(Map.of(
            dynamic.createString("hex"), dynamic.createList(patterns),
            dynamic.createString("pigment"), pigment));
        itemStackData.setComponent("hexcasting:hex_holder", hexHolder);
    }
}
