package at.petrak.hexcasting.api.cap;

import at.petrak.hexcasting.api.spell.SpellDatum;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SpellHolder {

	boolean canDrawManaFromInventory();

	boolean hasSpell();
	
	@Nullable
	List<SpellDatum<?>> getPatterns(ServerLevel level);

	void writePatterns(List<SpellDatum<?>> patterns, int mana);

	void clearPatterns();
}
