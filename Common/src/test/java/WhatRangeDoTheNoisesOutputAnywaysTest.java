import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WhatRangeDoTheNoisesOutputAnywaysTest {
    @Test
    public void test() {
        var perlin = PerlinNoise.create(new SingleThreadedRandomSource(12345), List.of(0, 1, 2, 3, 4));
        var simplex = new SimplexNoise(new SingleThreadedRandomSource(12345));

        System.out.println("Perlin:");
        for (int i = 0; i < 20; i++) {
            System.out.printf("  %f%n", perlin.getValue(i / 10.0, 69420.0, 1337.0));
        }

        System.out.println("Simplex:");
        for (int i = 0; i < 20; i++) {
            System.out.printf("  %f%n", simplex.getValue(i / 10.0, 69420.0, 1337.0));
        }
    }

    @Test
    public void perlinBounds() {
        var perlin = PerlinNoise.create(new SingleThreadedRandomSource(12345), List.of(0, 1, 2, 3, 4));
        var min = Double.POSITIVE_INFINITY;
        var max = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < 10000; i++) {
            var it = perlin.getValue(i / 10.0, 12345.0, 7604.0);
            min = Math.min(min, it);
            max = Math.max(max, it);
        }

        System.out.printf("Min: %f\nMax: %f\n", min, max);
    }
}
