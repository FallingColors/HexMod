package at.petrak.hexcasting.api.misc;

import at.petrak.hexcasting.api.addldata.ManaHolder;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import com.google.common.collect.Lists;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class DiscoveryHandlers {
	private static final List<Predicate<Player>> HAS_LENS_PREDICATE = new ArrayList<>();
	private static final List<Function<CastingHarness, List<ManaHolder>>> MANA_HOLDER_DISCOVERY = new ArrayList<>();

	public static boolean hasLens(Player player) {
		for (var predicate : HAS_LENS_PREDICATE) {
			if (predicate.test(player)) {
				return true;
			}
		}
		return false;
	}

	public static List<ManaHolder> collectManaHolders(CastingHarness harness) {
		List<ManaHolder> holders = Lists.newArrayList();
		for (var discoverer : MANA_HOLDER_DISCOVERY) {
			holders.addAll(discoverer.apply(harness));
		}
		return holders;
	}


	public static void addLensPredicate(Predicate<Player> predicate) {
		HAS_LENS_PREDICATE.add(predicate);
	}

	public static void addManaHolderDiscoverer(Function<CastingHarness, List<ManaHolder>> discoverer) {
		MANA_HOLDER_DISCOVERY.add(discoverer);
	}
}
