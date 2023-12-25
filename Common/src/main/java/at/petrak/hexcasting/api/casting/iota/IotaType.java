package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// Take notes from ForgeRegistryEntry
public abstract class IotaType<T extends Iota> {

    /**
     * Spell datums are stored as such: {@code { "type": "modid:type", "datum": a_tag }}.
     * <p>
     * The {@code type} key is given when registering the spell datum type; this method
     * deserializes the tag associated with {@code "datum"}.
     * <p>
     * Returning {@code null} makes the resulting datum be {@link NullIota}.
     * Throwing an exception raises a mishap.
     */
    @Nullable
    public abstract T deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException;

    /**
     * Get a display of this datum from the {@code data} tag, <i>without</i> the world.
     * This is for use on the client.
     */
    public abstract Component display(Tag tag);

    /**
     * Get the color associated with this datum type.
     */
    public abstract int color();

    /**
     * Get a display component that's the name of this iota type.
     */
    public Component typeName() {
        var key = HexIotaTypes.REGISTRY.getKey(this);
        return Component.translatable("hexcasting.iota." + key)
            .withStyle(style -> style.withColor(TextColor.fromRgb(color())));
    }

    public static CompoundTag serialize(Iota iota) {
        var type = iota.getType();
        var typeId = HexIotaTypes.REGISTRY.getKey(type);
        if (typeId == null) {
            throw new IllegalStateException(
                "Tried to serialize an unregistered iota type. Iota: " + iota
                    + " ; Type" + type.getClass().getTypeName());
        }

        // We check if it's too big on serialization; if it is we just return a garbage.
        if (isTooLargeToSerialize(List.of(iota), 0)) {
            // Garbage will never be too large so we just recurse
            return serialize(new GarbageIota());
        }
        var dataTag = iota.serialize();
        var out = new CompoundTag();
        out.putString(HexIotaTypes.KEY_TYPE, typeId.toString());
        out.put(HexIotaTypes.KEY_DATA, dataTag);
        return out;
    }

    public static boolean isTooLargeToSerialize(Iterable<Iota> examinee) {
        return isTooLargeToSerialize(examinee, 1);
    }

    private static boolean isTooLargeToSerialize(Iterable<Iota> examinee, int startingCount) {
        // We don't recurse here, just a work queue (or work stack, if we liked.)
        // Each element is a found sub-iota, and how deep it is.
        //
        // TODO: is it worth trying to cache the depth and size statically on a SpellList.
        var listsToExamine = new ArrayDeque<>(Collections.singleton(new Pair<>(examinee, 0)));
        int totalEltsFound = startingCount; // count the first list
        while (!listsToExamine.isEmpty()) {
            var iotaPair = listsToExamine.removeFirst();
            var sublist = iotaPair.getFirst();
            int depth = iotaPair.getSecond();
            for (var iota : sublist) {
                totalEltsFound += iota.size();
                if (totalEltsFound >= HexIotaTypes.MAX_SERIALIZATION_TOTAL) {
                    return true; // too bad
                }
                var subIotas = iota.subIotas();
                if (subIotas != null) {
                    if (depth + 1 >= HexIotaTypes.MAX_SERIALIZATION_DEPTH) {
                        return true;
                    }

                    listsToExamine.addLast(new Pair<>(subIotas, depth + 1));
                }
            }
        }
        // we made it!
        return false;
    }

    /**
     * This method attempts to find the type from the {@code type} key.
     * See {@link IotaType#serialize(Iota)} for the storage format.
     *
     * @return {@code null} if it cannot get the type.
     */
    @org.jetbrains.annotations.Nullable
    public static IotaType<?> getTypeFromTag(CompoundTag tag) {
        if (!tag.contains(HexIotaTypes.KEY_TYPE, Tag.TAG_STRING)) {
            return null;
        }
        var typeKey = tag.getString(HexIotaTypes.KEY_TYPE);
        if (!ResourceLocation.isValidResourceLocation(typeKey)) {
            return null;
        }
        var typeLoc = new ResourceLocation(typeKey);
        return HexIotaTypes.REGISTRY.get(typeLoc);
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
            return new GarbageIota();
        }
        var data = tag.get(HexIotaTypes.KEY_DATA);
        if (data == null) {
            return new GarbageIota();
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

    private static Component brokenIota() {
        return Component.translatable("hexcasting.spelldata.unknown")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    public static Component getDisplay(CompoundTag tag) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return brokenIota();
        }
        var data = tag.get(HexIotaTypes.KEY_DATA);
        if (data == null) {
            return brokenIota();
        }
        return type.display(data);
    }

    public static FormattedCharSequence getDisplayWithMaxWidth(CompoundTag tag, int maxWidth, Font font) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return brokenIota().getVisualOrderText();
        }
        var data = tag.get(HexIotaTypes.KEY_DATA);
        if (data == null) {
            return brokenIota().getVisualOrderText();
        }
        var display = type.display(data);
        var splitted = font.split(display, maxWidth - font.width("..."));
        if (splitted.isEmpty())
            return FormattedCharSequence.EMPTY;
        else if (splitted.size() == 1)
            return splitted.get(0);
        else {
            var first = splitted.get(0);
            return FormattedCharSequence.fromPair(first,
                Component.literal("...").withStyle(ChatFormatting.GRAY).getVisualOrderText());
        }
    }

    public static int getColor(CompoundTag tag) {
        var type = getTypeFromTag(tag);
        if (type == null) {
            return HexUtils.ERROR_COLOR;
        }
        return type.color();
    }
}
