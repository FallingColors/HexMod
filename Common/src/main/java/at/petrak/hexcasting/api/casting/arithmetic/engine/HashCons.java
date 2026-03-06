package at.petrak.hexcasting.api.casting.arithmetic.engine;


import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public sealed interface HashCons {
	public record Pattern(HexPattern operator) implements HashCons {}

	public record Pair(IotaType<?> head, HashCons tail) implements HashCons {}
}
