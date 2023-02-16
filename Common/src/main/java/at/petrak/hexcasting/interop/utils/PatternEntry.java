package at.petrak.hexcasting.interop.utils;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public record PatternEntry(HexPattern pattern, HexCoord origin, List<Vec2> zappyPoints) {
    // NO-OP
}
