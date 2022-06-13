package at.petrak.hexcasting.api.misc;

import at.petrak.hexcasting.api.addldata.ADColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * A colorizer item and the player who owned it at the time of making the color.
 */
public record FrozenColorizer(ItemStack item, UUID owner) {

    private static final int[] MINIMUM_LUMINANCE_COLOR_WHEEL = {
        0xFF200000, 0xFF202000, 0xFF002000, 0xFF002020, 0xFF000020, 0xFF200020
    };

    private static final Map<String, Supplier<Item>> OLD_PRIDE_COLORIZERS = Arrays.stream(
            ItemPrideColorizer.Type.values())
        .collect(Collectors.<ItemPrideColorizer.Type, String, Supplier<Item>>toMap(
            (type) -> modLoc("pride_colorizer_" + type.ordinal()).toString(),
            (type) -> (() -> HexItems.PRIDE_COLORIZERS.get(type))));

    public static final String TAG_STACK = "stack";
    public static final String TAG_OWNER = "owner";

    public static final Supplier<FrozenColorizer> DEFAULT =
        () -> new FrozenColorizer(new ItemStack(HexItems.DYE_COLORIZERS.get(DyeColor.WHITE)), Util.NIL_UUID);

    public CompoundTag serializeToNBT() {
        var out = new CompoundTag();
        out.put(TAG_STACK, this.item.save(new CompoundTag()));
        out.putUUID(TAG_OWNER, this.owner);
        return out;
    }

    public static FrozenColorizer fromNBT(CompoundTag tag) {
        if (tag.isEmpty()) {
            return FrozenColorizer.DEFAULT.get();
        }
        try {
            CompoundTag stackTag = tag.getCompound(TAG_STACK);
            if (stackTag.contains("id", Tag.TAG_STRING)) {
                String id = stackTag.getString("id");
                if (OLD_PRIDE_COLORIZERS.containsKey(id)) {
                    stackTag.putString("id", Registry.ITEM.getKey(OLD_PRIDE_COLORIZERS.get(id).get()).toString());
                }
            }
            var stack = ItemStack.of(stackTag);
            var uuid = tag.getUUID(TAG_OWNER);
            return new FrozenColorizer(stack, uuid);
        } catch (NullPointerException exn) {
            return FrozenColorizer.DEFAULT.get();
        }
    }

    /**
     * Gets a color with a minimum luminance applied.
     *
     * @param time     absolute world time in ticks
     * @param position a position for the icosahedron, a randomish number for particles.
     * @return an AARRGGBB color.
     */
    public int getColor(float time, Vec3 position) {
        int raw = IXplatAbstractions.INSTANCE.getRawColor(this, time, position);

        var r = FastColor.ARGB32.red(raw);
        var g = FastColor.ARGB32.green(raw);
        var b = FastColor.ARGB32.blue(raw);
        double luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 0xFF; // Standard relative luminance calculation

        if (luminance < 0.05) {
            int rawMod = ADColorizer.morphBetweenColors(MINIMUM_LUMINANCE_COLOR_WHEEL, new Vec3(0.1, 0.1, 0.1),
                time / 20 / 20, position);

            r += FastColor.ARGB32.red(rawMod);
            g += FastColor.ARGB32.green(rawMod);
            b += FastColor.ARGB32.blue(rawMod);
        }

        return 0xff_000000 | (r << 16) | (g << 8) | b;
    }
}
