package at.petrak.hexcasting.common.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.WeakHashMap;

public final class PlayerPositionRecorder {
	private static final Map<Player, Vec3> LAST_POSITION_MAP = new WeakHashMap<>();

	@SubscribeEvent
	public static void onEntityUpdate(LivingEvent.LivingUpdateEvent evt) {
		if (evt.getEntityLiving() instanceof ServerPlayer player) {
			LAST_POSITION_MAP.put(player, player.position());
		}
	}

	public static Vec3 getLastPosition(ServerPlayer player) {
		Vec3 vec = LAST_POSITION_MAP.get(player);
		return vec == null ? player.position() : vec;
	}
}
