package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.spell.iota.*;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Stores the registry for iota types, some utility methods, and all the types Hexcasting itself defines.
 */
@ParametersAreNonnullByDefault
public class HexIotaTypes {
    public static final Registry<IotaType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getIotaTypeRegistry();
    public static final String
        KEY_TYPE = HexAPI.MOD_ID + ":type",
        KEY_DATA = HexAPI.MOD_ID + ":data";
    public static final int MAX_SERIALIZATION_DEPTH = 256;
    public static final int MAX_SERIALIZATION_TOTAL = 1024;

    public static CompoundTag serialize(Iota iota) {
        var type = iota.getType();
        var typeId = REGISTRY.getKey(type);
        if (typeId == null) {
            throw new IllegalStateException(
                "Tried to serialize an unregistered iota type. Iota: " + iota
                    + " ; Type" + type.getClass().getTypeName());
        }

        // We check if it's too big on serialization; if it is we just return a garbage.
        if (iota instanceof ListIota listIota && isTooLargeToSerialize(listIota.getList())) {
            // Garbage will never be too large so we just recurse
            return serialize(new GarbageIota());
        }
        var dataTag = iota.serialize();
        var out = new CompoundTag();
        out.putString(KEY_TYPE, typeId.toString());
        out.put(KEY_DATA, dataTag);
        return out;
    }

    public static boolean isTooLargeToSerialize(Iterable<Iota> examinee) {
        // We don't recurse here, just a work queue (or work stack, if we liked.)
        // Each element is a found sub-iota, and how deep it is.
        //
        // TODO: is it worth trying to cache the depth and size statically on a SpellList.
        var listsToExamine = new ArrayDeque<>(Collections.singleton(new Pair<>(examinee, 0)));
        int totalEltsFound = 1; // count the first list
        while (!listsToExamine.isEmpty()) {
            var iotaPair = listsToExamine.removeFirst();
            var sublist = iotaPair.getFirst();
            int depth = iotaPair.getSecond();
            for (var iota : sublist) {
                totalEltsFound++;
                if (totalEltsFound >= MAX_SERIALIZATION_TOTAL) {
                    return true; // too bad
                }
                if (iota instanceof ListIota subsublist) {
                    if (depth + 1 >= MAX_SERIALIZATION_DEPTH) {
                        return true;
                    }
                    listsToExamine.addLast(new Pair<>(subsublist.getList(), depth + 1));
                }
            }
        }
        // we made it!
        return false;
    }

    /**
     * This method attempts to find the type from the {@code type} key.
     * See {@link HexIotaTypes#getTypeFromTag} for the storage format.
     *
     * @return {@code null} if it cannot get the type.
     */
    @Nullable
    public static IotaType<?> getTypeFromTag(CompoundTag tag) {
        if (!tag.contains(KEY_TYPE, Tag.TAG_STRING)) {
            return null;
        }
        var typeKey = tag.getString(KEY_TYPE);
        if (!ResourceLocation.isValidResourceLocation(typeKey)) {
            return null;
        }
        var typeLoc = new ResourceLocation(typeKey);
        return REGISTRY.get(typeLoc);
    }

    /**
     * Attempt to deserialize an iota from a tag.
     * <br>
     * Iotas are saved as such:
     * <code>
     * {
     * "type": "hexcasting:atype",
     * "data": {...}
     * }
     * </code>
     */
    public static Iota deserialize(CompoundTag tag, ServerLevel world) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return null;
        }
        var data = tag.get(KEY_DATA);
        if (data == null) {
            return null;
        }
        Iota deserialized;
        try {
            deserialized = Objects.requireNonNullElse(type.deserialize(data, world), new NullIota());
        } catch (IllegalArgumentException exn) {
            HexAPI.LOGGER.warn("Caught an exception deserializing an iota", exn);
            deserialized = new GarbageIota();
        }
        return deserialized;
    }

    public static Component getDisplay(CompoundTag tag) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return TextComponent.EMPTY;
        }
        var data = tag.get(KEY_DATA);
        if (data == null) {
            return TextComponent.EMPTY;
        }
        return type.display(data);
    }

    public static List<FormattedCharSequence> getDisplayWithMaxWidth(CompoundTag tag, int maxWidth, Font font) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return List.of();
        }
        var data = tag.get(KEY_DATA);
        if (data == null) {
            return List.of();
        }
        return type.displayWithWidth(data, maxWidth, font);
    }

    public static int getColor(CompoundTag tag) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return HexUtils.ERROR_COLOR;
        }
        return type.color();
    }

    @ApiStatus.Internal
    public static void registerTypes() {
        BiConsumer<IotaType<?>, ResourceLocation> r = (type, id) -> Registry.register(REGISTRY, id, type);
        for (var e : TYPES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, IotaType<?>> TYPES = new LinkedHashMap<>();

    public static final IotaType<NullIota> NULL = type("null", NullIota.TYPE);
    public static final IotaType<DoubleIota> DOUBLE = type("double", DoubleIota.TYPE);
    public static final IotaType<EntityIota> ENTITY = type("entity", EntityIota.TYPE);
    public static final IotaType<ListIota> LIST = type("list", ListIota.TYPE);
    public static final IotaType<PatternIota> PATTERN = type("pattern", PatternIota.TYPE);
    public static final IotaType<GarbageIota> GARBAGE = type("garbage", GarbageIota.TYPE);
    public static final IotaType<Vec3Iota> VEC3 = type("vec3", Vec3Iota.TYPE);


    private static <U extends Iota, T extends IotaType<U>> T type(String name, T type) {
        var old = TYPES.put(modLoc(name), type);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return type;
    }
}
