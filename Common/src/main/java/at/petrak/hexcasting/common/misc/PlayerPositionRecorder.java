package at.petrak.hexcasting.common.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

public final class PlayerPositionRecorder {
	private static final Map<Player, Vec3> LAST_SECOND_POSITION_MAP = new WeakHashMap<>();
	private static final Map<Player, Vec3> LAST_POSITION_MAP = new WeakHashMap<>();

	public static void updateAllPlayers(ServerLevel world) {
		for (ServerPlayer player : world.players()) {
			var prev = LAST_POSITION_MAP.get(player);
			if (prev != null)
				LAST_SECOND_POSITION_MAP.put(player, prev);
			LAST_POSITION_MAP.put(player, player.position());
		}
	}

	public static Vec3 getMotion(ServerPlayer player) {
		Vec3 vec = LAST_POSITION_MAP.get(player);
		Vec3 prev = LAST_SECOND_POSITION_MAP.get(player);

		if (vec == null)
			return Vec3.ZERO;

		if (prev == null)
			return player.position().subtract(vec);

		return vec.subtract(prev);
	}
}
