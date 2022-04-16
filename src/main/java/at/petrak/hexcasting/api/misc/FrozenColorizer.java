package at.petrak.hexcasting.api.misc;

import at.petrak.hexcasting.api.cap.Colorizer;
import at.petrak.hexcasting.api.cap.HexCapabilities;
import at.petrak.hexcasting.api.mod.HexApiItems;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * A colorizer item and the player who owned it at the time of making the color.
 */
public record FrozenColorizer(ItemStack item, UUID owner) {

    private static final int[] MINIMUM_LUMINANCE_COLOR_WHEEL = {
            0xFF200000, 0xFF202000, 0xFF002000, 0xFF002020, 0xFF000020, 0xFF200020
    };

    public static final String TAG_STACK = "stack";
    public static final String TAG_OWNER = "owner";

    public static final Supplier<FrozenColorizer> DEFAULT =
            () -> new FrozenColorizer(new ItemStack(HexApiItems.COLORIZER_WHITE), Util.NIL_UUID);

    public CompoundTag serialize() {
        var out = new CompoundTag();
        out.put(TAG_STACK, this.item.serializeNBT());
        out.putUUID(TAG_OWNER, this.owner);
        return out;
    }

    public static FrozenColorizer deserialize(CompoundTag tag) {
        if (tag.isEmpty())
            return FrozenColorizer.DEFAULT.get();
        try {
            ItemStack item;
            if (tag.contains("item", Tag.TAG_STRING) && !tag.contains(TAG_STACK, Tag.TAG_COMPOUND)) {
                item = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("item"))));
            } else {
                item = ItemStack.of(tag.getCompound(TAG_STACK));
            }
            var uuid = tag.getUUID(TAG_OWNER);
            return new FrozenColorizer(item, uuid);
        } catch (NullPointerException exn) {
            return FrozenColorizer.DEFAULT.get();
        }
    }

    public static boolean isColorizer(ItemStack stack) {
        return stack.getCapability(HexCapabilities.COLOR).isPresent();
    }

    /**
     * Gets a color with a minimum luminance applied.
     * @param time     absolute world time in ticks
     * @param position a position for the icosahedron, a randomish number for particles.
     * @return an AARRGGBB color.
     */
    public int getColor(float time, Vec3 position) {
        int raw = getRawColor(time, position);

        var r = FastColor.ARGB32.red(raw);
        var g = FastColor.ARGB32.green(raw);
        var b = FastColor.ARGB32.blue(raw);
        double luminance = 0.2126 * r / 0xFF + 0.7152 * g / 0xFF + 0.0722 * b / 0xFF; // Standard relative luminance calculation

        if (luminance < 0.1) {
            int rawMod = Colorizer.morphBetweenColors(MINIMUM_LUMINANCE_COLOR_WHEEL, new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);

            r += FastColor.ARGB32.red(rawMod);
            g += FastColor.ARGB32.green(rawMod);
            b += FastColor.ARGB32.blue(rawMod);
        }

        return 0xff_000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * @param time     absolute world time in ticks
     * @param position a position for the icosahedron, a randomish number for particles.
     * @return an AARRGGBB color.
     */
    public int getRawColor(float time, Vec3 position) {
        var maybeColorizer = item.getCapability(HexCapabilities.COLOR).resolve();
        if (maybeColorizer.isPresent()) {
            Colorizer colorizer = maybeColorizer.get();
            return colorizer.color(owner, time, position);
        }

        return 0xff_ff00dc; // missing color
    }
}
