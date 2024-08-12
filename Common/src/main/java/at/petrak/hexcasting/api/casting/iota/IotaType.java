package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import java.util.function.Function;
import java.util.function.Supplier;

// Take notes from ForgeRegistryEntry
public abstract class IotaType<T extends Iota> {

    public abstract Codec<T> getCodec();

    public Codec<T> getCodec(ServerLevel world) {
        Function<T, DataResult<T>> validate = iota -> {
            if (validate(iota, world)) {
                return DataResult.success(iota);
            } else {
                return DataResult.error(() -> "iota validation failed");
            }
        };

        return getCodec().flatXmap(validate, validate);
    }

    /**
     * Validates an iota using the world.
     * This can be used for iotas the reference things that may not currently be present in the world, like entities.
     *
     * @param iota  the iota to validate
     * @param world the world to use for validation
     * @return true if the iota is valid. false if it is not.
     */
    protected boolean validate(T iota, ServerLevel world) {
        return true;
    }

    /**
     * Spell datums are stored as such: {@code { "type": "modid:type", "datum": a_tag }}.
     * <p>
     * The {@code type} key is given when registering the spell datum type; this method
     * deserializes the tag associated with {@code "datum"}.
     * <p>
     * Returning {@code null} makes the resulting datum be {@link NullIota}.
     * Throwing an exception raises a mishap.
     *
     * @deprecated use {@code Codec} from {@link IotaType#getCodec()} instead.
     */
    @Deprecated
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

    /**
     * @deprecated Use {@link Iota#getCodec()} instead.
     */
    @Deprecated
    public static CompoundTag serialize(Iota iota) {
        return (CompoundTag) Iota.getCodec().encodeStart(NbtOps.INSTANCE, iota).resultOrPartial(HexAPI.LOGGER::error).orElseThrow();
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
     *
     * @deprecated Use {@link Iota#getCodec()} instead.
     */
    @Deprecated
    public static Iota deserialize(CompoundTag tag, ServerLevel world) {
        return Iota.getCodec(world).parse(NbtOps.INSTANCE, tag).resultOrPartial(HexAPI.LOGGER::error).orElseThrow();
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
