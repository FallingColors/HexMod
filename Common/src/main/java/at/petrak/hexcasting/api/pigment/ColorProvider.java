package at.petrak.hexcasting.api.pigment;

import at.petrak.hexcasting.api.addldata.ADPigment;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;

public abstract class ColorProvider {
    /**
     * Implers, impl this function
     */
    protected abstract int getRawColor(float time, Vec3 position);

    private static final int[] MINIMUM_LUMINANCE_COLOR_WHEEL = {
        0xFF200000, 0xFF202000, 0xFF002000, 0xFF002020, 0xFF000020, 0xFF200020
    };

    /**
     * Gets a color with a minimum luminance applied.
     *
     * @param time     absolute world time in ticks
     * @param position a position for the icosahedron, a randomish number for particles.
     * @return an AARRGGBB color.
     */
    public final int getColor(float time, Vec3 position) {
        int raw = this.getRawColor(time, position);

        var r = FastColor.ARGB32.red(raw);
        var g = FastColor.ARGB32.green(raw);
        var b = FastColor.ARGB32.blue(raw);
        double luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 0xFF; // Standard relative luminance calculation

        if (luminance < 0.05) {
            int rawMod = ADPigment.morphBetweenColors(MINIMUM_LUMINANCE_COLOR_WHEEL, new Vec3(0.1, 0.1, 0.1),
                time / 20 / 20, position);

            r += FastColor.ARGB32.red(rawMod);
            g += FastColor.ARGB32.green(rawMod);
            b += FastColor.ARGB32.blue(rawMod);
        }

        return 0xff_000000 | (r << 16) | (g << 8) | b;
    }

    public static final ColorProvider MISSING = new ColorProvider() {
        @Override
        protected int getRawColor(float time, Vec3 position) {
            return 0xFF_ff00dc;
        }
    };
}
