package at.petrak.hexcasting.api.player;

import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.api.mod.HexApiMessages;
import at.petrak.hexcasting.api.spell.casting.CastingContext;
import at.petrak.hexcasting.api.spell.casting.CastingHarness;
import at.petrak.hexcasting.api.spell.casting.ResolvedPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class HexPlayerDataHelper {
	public static final String TAG_SENTINEL_EXISTS = "hexcasting:sentinel_exists";
	public static final String TAG_SENTINEL_GREATER = "hexcasting:sentinel_extends_range";
	public static final String TAG_SENTINEL_POSITION = "hexcasting:sentinel_position";
	public static final String TAG_SENTINEL_DIMENSION = "hexcasting:sentinel_dimension";

	public static final String TAG_COLOR = "hexcasting:colorizer";

	public static final String TAG_FLIGHT_ALLOWED = "hexcasting:flight_allowed";
	public static final String TAG_FLIGHT_TIME = "hexcasting:flight_time";
	public static final String TAG_FLIGHT_ORIGIN = "hexcasting:flight_origin";
	public static final String TAG_FLIGHT_DIMENSION = "hexcasting:flight_origin";
	public static final String TAG_FLIGHT_RADIUS = "hexcasting:flight_radius";

	public static final String TAG_HARNESS = "hexcasting:spell_harness";
	public static final String TAG_PATTERNS = "hexcasting:spell_patterns";


	@SubscribeEvent
	public static void copyDataOnDeath(PlayerEvent.Clone evt) {
		var eitherSidePlayer = evt.getPlayer();
		// this apparently defines it in outside scope. the more you know.
		if (!(eitherSidePlayer instanceof ServerPlayer player)) {
			return;
		}

		var eitherSideProto = evt.getOriginal();
		if (!(eitherSideProto instanceof ServerPlayer proto)) {
			return;
		}

		// Copy data from this to new player
		setFlight(player, getFlight(proto));
		setSentinel(player, getSentinel(proto));
		setColorizer(player, getColorizer(proto));
		setHarness(player, getHarness(proto, InteractionHand.MAIN_HAND));
		setPatterns(player, getPatterns(proto));
	}

	@SubscribeEvent
	public static void syncDataOnLogin(PlayerEvent.PlayerLoggedInEvent evt) {
		if (!(evt.getPlayer() instanceof ServerPlayer player)) {
			return;
		}

		syncSentinel(player);
		syncColorizer(player);
	}

	@SubscribeEvent
	public static void syncDataOnRejoin(PlayerEvent.PlayerRespawnEvent evt) {
		if (!(evt.getPlayer() instanceof ServerPlayer player)) {
			return;
		}

		syncSentinel(player);
		syncColorizer(player);
	}

	private static void syncSentinel(ServerPlayer player) {
		HexApiMessages.getChannel().send(PacketDistributor.PLAYER.with(() -> player), HexApiMessages.getSentinelMessage(getSentinel(player)));
	}

	private static void syncColorizer(ServerPlayer player) {
		HexApiMessages.getChannel().send(PacketDistributor.PLAYER.with(() -> player), HexApiMessages.getColorizerMessage(getColorizer(player)));
	}

	public static void setFlight(ServerPlayer player, FlightAbility flight) {
		CompoundTag tag = player.getPersistentData();
		tag.putBoolean(TAG_FLIGHT_ALLOWED, flight.allowed());
		if (flight.allowed()) {
			tag.putInt(TAG_FLIGHT_TIME, flight.timeLeft());
			tag.put(TAG_FLIGHT_ORIGIN, HexUtils.serializeToNBT(flight.origin()));
			tag.putString(TAG_FLIGHT_DIMENSION, flight.dimension().location().toString());
			tag.putDouble(TAG_FLIGHT_RADIUS, flight.radius());
		} else {
			tag.remove(TAG_FLIGHT_TIME);
			tag.remove(TAG_FLIGHT_ORIGIN);
			tag.remove(TAG_FLIGHT_RADIUS);
		}
	}

	public static void setColorizer(Player player, FrozenColorizer colorizer) {
		CompoundTag tag = player.getPersistentData();
		tag.put(TAG_COLOR, colorizer.serialize());

		if (player instanceof ServerPlayer serverPlayer)
			syncColorizer(serverPlayer);
	}

	public static void setSentinel(Player player, Sentinel sentinel) {
		CompoundTag tag = player.getPersistentData();
		tag.putBoolean(TAG_SENTINEL_EXISTS, sentinel.hasSentinel());
		if (sentinel.hasSentinel()) {
			tag.putBoolean(TAG_SENTINEL_GREATER, sentinel.extendsRange());
			tag.put(TAG_SENTINEL_POSITION, HexUtils.serializeToNBT(sentinel.position()));
			tag.putString(TAG_SENTINEL_DIMENSION, sentinel.dimension().location().toString());
		} else {
			tag.remove(TAG_SENTINEL_GREATER);
			tag.remove(TAG_SENTINEL_POSITION);
			tag.remove(TAG_SENTINEL_DIMENSION);
		}

		if (player instanceof ServerPlayer serverPlayer)
			syncSentinel(serverPlayer);
	}

	public static void setHarness(ServerPlayer player, CastingHarness harness) {
		player.getPersistentData().put(TAG_HARNESS, harness == null ? new CompoundTag() : harness.serializeToNBT());
	}

	public static void setPatterns(ServerPlayer player, List<ResolvedPattern> patterns) {
		var listTag = new ListTag();
		for (ResolvedPattern pattern : patterns) {
			listTag.add(pattern.serializeToNBT());
		}
		player.getPersistentData().put(TAG_PATTERNS, listTag);
	}

	public static FlightAbility getFlight(ServerPlayer player) {
		CompoundTag tag = player.getPersistentData();
		boolean allowed = tag.getBoolean(TAG_FLIGHT_ALLOWED);
		if (allowed) {
			var timeLeft = tag.getInt(TAG_FLIGHT_TIME);
			var origin = HexUtils.DeserializeVec3FromNBT(tag.getLongArray(TAG_FLIGHT_ORIGIN));
			var radius = tag.getDouble(TAG_FLIGHT_RADIUS);
			var dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString(TAG_SENTINEL_DIMENSION)));
			return new FlightAbility(true, timeLeft, dimension, origin, radius);
		}
		return FlightAbility.deny();
	}

	public static FrozenColorizer getColorizer(Player player) {
		return FrozenColorizer.deserialize(player.getPersistentData().getCompound(TAG_COLOR));
	}

	public static Sentinel getSentinel(Player player) {
		CompoundTag tag = player.getPersistentData();
		var exists = tag.getBoolean(TAG_SENTINEL_EXISTS);
		if (!exists)
			return Sentinel.none();
		var extendsRange = tag.getBoolean(TAG_SENTINEL_GREATER);
		var position = HexUtils.DeserializeVec3FromNBT(tag.getLongArray(TAG_SENTINEL_POSITION));
		var dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString(TAG_SENTINEL_DIMENSION)));

		return new Sentinel(true, extendsRange, position, dimension);
	}

	public static CastingHarness getHarness(ServerPlayer player, InteractionHand hand) {
		var ctx = new CastingContext(player, hand);
		return CastingHarness.DeserializeFromNBT(player.getPersistentData().getCompound(TAG_HARNESS), ctx);
	}

	public static List<ResolvedPattern> getPatterns(ServerPlayer player) {
		ListTag patternsTag = player.getPersistentData().getList(TAG_PATTERNS, Tag.TAG_COMPOUND);

		List<ResolvedPattern> patterns = new ArrayList<>(patternsTag.size());

		for (int i = 0; i < patternsTag.size(); i++) {
			patterns.add(ResolvedPattern.DeserializeFromNBT(patternsTag.getCompound(i)));
		}
		return patterns;
	}

	public static void clearCastingData(ServerPlayer player) {
		player.getPersistentData().remove(TAG_HARNESS);
		player.getPersistentData().remove(TAG_PATTERNS);
	}
}
