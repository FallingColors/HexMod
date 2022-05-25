package at.petrak.hexcasting.api.spell.datum;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.spell.SpellList;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpellDatums {
    private static final Map<ResourceLocation, SpellDatum.Type<?>> controllers = new ConcurrentHashMap<>();

    public static final String
        TAG_TYPE = HexAPI.MOD_ID + ":type",
        TAG_DATA = HexAPI.MOD_ID + ":data";

    public static SpellDatum deserializeFromRootTag(CompoundTag tag,
        ServerLevel world) throws IllegalArgumentException {
        if (tag.contains(TAG_TYPE, Tag.TAG_STRING) && tag.contains(TAG_DATA)) {
            var typeKey = new ResourceLocation(tag.getString(TAG_TYPE));
            var overseer = controllers.get(typeKey);
            if (overseer == null) {
                throw new IllegalArgumentException("Unknown type " + typeKey + "(did you remember to register it?)");
            }

            var datumTag = tag.get(TAG_DATA);
            return overseer.deserialize(datumTag, world);
        }

        // For legacy reasons we need to check if it's the old serialization method
        var legacyKeys = List.of(
            "entity", "double", "vec3", "list", "widget", "pattern"
        );
        for (var legacyKey : legacyKeys) {
            if (tag.contains(legacyKey)) {
                return legacyDeserialize(legacyKey, tag.get(legacyKey), world);
            }
        }

        throw new IllegalArgumentException("could not deserialize this tag: " + tag);
    }

    public static boolean equalsWithTolerance(SpellDatum a, SpellDatum b) {
        return a == b || a.equalsOther(b) || b.equalsOther(a);
    }

    private static SpellDatum legacyDeserialize(String key, Tag inner,
        ServerLevel world) throws IllegalArgumentException {
        return switch (key) {
            case "entity" -> {
                var subtag = (CompoundTag) inner;
                var uuid = subtag.getUUID("uuid"); // throw away the name
                var entity = world.getEntity(uuid);
                yield (entity == null)
                    ? new DatumWidget(Widget.NULL)
                    : new DatumEntity(entity);
            }
            case "double" -> new DatumDouble(((DoubleTag) inner).getAsDouble());
            case "vec3" -> new DatumVec3(HexUtils.vecFromNBT(((LongArrayTag) inner).getAsLongArray()));
            case "list" -> {
                var listTag = (ListTag) inner;
                var out = new ArrayList<SpellDatum>();
                for (var subtag : listTag) {
                    var subdatum = deserializeFromRootTag((CompoundTag) subtag, world);
                    out.add(subdatum);
                }
                yield new DatumList(new SpellList.LList(out));
            }
            case "widget" -> {
                var str = (StringTag) inner;
                yield new DatumWidget(Widget.valueOf(str.getAsString()));
            }
            case "pattern" -> {
                var ctag = (CompoundTag) inner;
                yield new DatumPattern(HexPattern.fromNBT(ctag));
            }
            default -> throw new IllegalArgumentException("bruh this should literally be impossible");
        };
    }
}
