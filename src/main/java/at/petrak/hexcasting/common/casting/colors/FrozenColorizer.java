package at.petrak.hexcasting.common.casting.colors;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.colorizer.ItemDyeColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.paucal.api.contrib.Contributors;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * A colorizer item and the player who owned it at the time of making the color.
 */
public record FrozenColorizer(Item item, UUID owner) {
    private static final int[] MINIMUM_LUMINANCE_COLOR_WHEEL = {
            0xFF200000, 0xFF202000, 0xFF002000, 0xFF002020, 0xFF000020, 0xFF200020
    };

    public static final String TAG_ITEM = "item";
    public static final String TAG_OWNER = "owner";

    public static final FrozenColorizer DEFAULT =
        new FrozenColorizer(HexItems.DYE_COLORIZERS.get(DyeColor.WHITE).get(), Util.NIL_UUID);

    public CompoundTag serialize() {
        var out = new CompoundTag();
        out.putString(TAG_ITEM, ForgeRegistries.ITEMS.getKey(this.item).toString());
        out.putUUID(TAG_OWNER, this.owner);
        return out;
    }

    public static FrozenColorizer deserialize(CompoundTag tag) {
        if (tag.isEmpty())
            return FrozenColorizer.DEFAULT;
        try {
            var itemID = new ResourceLocation(tag.getString(TAG_ITEM));
            var item = ForgeRegistries.ITEMS.getValue(itemID);
            var uuid = tag.getUUID(TAG_OWNER);
            return new FrozenColorizer(item, uuid);
        } catch (NullPointerException exn) {
            return FrozenColorizer.DEFAULT;
        }
    }

    public static boolean isColorizer(Item item) {
        return item instanceof ItemDyeColorizer
            || item instanceof ItemPrideColorizer
            || item == HexItems.UUID_COLORIZER.get();
    }

    /**
     * Gets a color with a minimum luminance applied.
     * @param time     absolute world time in ticks
     * @param position a position for the icosahedron, a randomish number for particles.
     * @return an AARRGGBB color.
     */
    public int getColor(float time, Vec3 position) {
        int raw = getRawColor(time, position);

        int r = (raw & 0xFF0000) >> 16;
        int g = (raw & 0xFF00) >> 8;
        int b = (raw & 0xFF);
        double luminance = 0.2126 * r / 0xFF + 0.7152 * g / 0xFF + 0.0722 * b / 0xFF; // Standard relative luminance calculation

        if (luminance < 0.1) {
            int rawMod = morphBetweenColors(MINIMUM_LUMINANCE_COLOR_WHEEL, new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);

            r += (rawMod & 0xFF0000) >> 16;
            g += (rawMod & 0xFF00) >> 8;
            b += (rawMod & 0xFF);
        }

        return 0xff_000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * @param time     absolute world time in ticks
     * @param position a position for the icosahedron, a randomish number for particles.
     * @return an AARRGGBB color.
     */
    public int getRawColor(float time, Vec3 position) {
        if (this.item instanceof ItemDyeColorizer dye) {
            return dye.getDyeColor().getTextColor() | 0xff_000000;
        } else if (this.item instanceof ItemPrideColorizer politics) {
            var colors = politics.getColors();
            return morphBetweenColors(colors, new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);
        } else if (this.item == HexItems.UUID_COLORIZER.get()) {
            var contributor = Contributors.getContributor(this.owner);
            if (contributor != null) {
                Object colorObj = contributor.getRaw("hexcasting:colorizer");
                if (colorObj instanceof List<?> colorList) {
                    var colors = new int[colorList.size()];
                    var ok = true;
                    for (int i = 0; i < colorList.size(); i++) {
                        Object elt = colorList.get(i);
                        if (elt instanceof Number n) {
                            colors[i] = n.intValue() | 0xff_000000;
                        } else {
                            ok = false;
                            HexMod.getLogger().warn("Player {} had a bad colorizer", this.owner);
                            break;
                        }
                    }
                    if (ok) {
                        return morphBetweenColors(colors, new Vec3(0.1, 0.1, 0.1), time / 20 / 20, position);
                    }
                }
            }

            // randomly scrungle the bits
            var rand = new Random(this.owner.getLeastSignificantBits() ^ this.owner.getMostSignificantBits());
            var hue = rand.nextFloat();
            var saturation = rand.nextFloat(0.4f, 1.0f);
            var brightness = rand.nextFloat(0.5f, 1.0f);

            return Color.HSBtoRGB(hue, saturation, brightness);
        }

        return 0xff_ff00dc; // missing color
    }

    private static int morphBetweenColors(int[] colors, Vec3 gradientDir, float time, Vec3 position) {
        float fIdx = Mth.positiveModulo(time + (float) gradientDir.dot(position), 1f) * colors.length;

        int baseIdx = Mth.floor(fIdx);
        float tRaw = fIdx - baseIdx;
        float t = tRaw < 0.5 ? 4 * tRaw * tRaw * tRaw : (float) (1 - Math.pow(-2 * tRaw + 2, 3) / 2);
        int start = colors[baseIdx % colors.length];
        int end = colors[(baseIdx + 1) % colors.length];

        var r1 = FastColor.ARGB32.red(start);
        var g1 = FastColor.ARGB32.green(start);
        var b1 = FastColor.ARGB32.blue(start);
        var a1 = FastColor.ARGB32.alpha(start);
        var r2 = FastColor.ARGB32.red(end);
        var g2 = FastColor.ARGB32.green(end);
        var b2 = FastColor.ARGB32.blue(end);
        var a2 = FastColor.ARGB32.alpha(end);

        var r = Mth.lerp(t, r1, r2);
        var g = Mth.lerp(t, g1, g2);
        var b = Mth.lerp(t, b1, b2);
        var a = Mth.lerp(t, a1, a2);

        return FastColor.ARGB32.color((int) a, (int) r, (int) g, (int) b);
    }
}
