package at.petrak.hexcasting.api.cap;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpellHolder {

	boolean canDrawManaFromInventory();

	@Nullable
	List<HexPattern> getPatterns();

	void writePatterns(List<HexPattern> patterns, int mana);

	void clearPatterns();
}
