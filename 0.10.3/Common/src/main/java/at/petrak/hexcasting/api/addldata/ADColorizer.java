package at.petrak.hexcasting.api.addldata;

import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface ADColorizer {
    int color(UUID owner, float time, Vec3 position);

    static int morphBetweenColors(int[] colors, Vec3 gradientDir, float time, Vec3 position) {
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
