package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.player.HexPlayerDataHelper;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.common.misc.Brainsweeping;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

// Ah, java classloading, how I hate thee
public record ClientPacketHandler(Object rawMsg) {

	@OnlyIn(Dist.CLIENT)
	public void beep() {
		if (!(rawMsg instanceof MsgBeepAck msg))
			return;

		var minecraft = Minecraft.getInstance();
		var world = minecraft.level;
		if (world != null){
			float pitch = (float) Math.pow(2, (msg.note() - 12) / 12.0);
			world.playLocalSound(msg.target().x, msg.target().y, msg.target().z, msg.instrument().getSoundEvent(), SoundSource.PLAYERS, 3, pitch, false);
			world.addParticle(ParticleTypes.NOTE, msg.target().x, msg.target().y + 0.2, msg.target().z, msg.note() / 24.0, 0, 0);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void blink() {
		if (!(rawMsg instanceof MsgBlinkAck msg))
			return;

		var player = Minecraft.getInstance().player;
		player.setPos(player.position().add(msg.addedPosition()));
	}


	@OnlyIn(Dist.CLIENT)
	public void updateComparator() {
		if (!(rawMsg instanceof MsgUpdateComparatorVisualsAck msg))
			return;

		ScryingLensOverlayRegistry.receiveComparatorValue(msg.pos(), msg.value());
	}

	@OnlyIn(Dist.CLIENT)
	public void openSpellGui() {
		if (!(rawMsg instanceof MsgOpenSpellGuiAck msg))
			return;

		var mc = Minecraft.getInstance();
		mc.setScreen(new GuiSpellcasting(msg.hand(), msg.patterns(), msg.components()));
	}

	@OnlyIn(Dist.CLIENT)
	public void brainsweep() {
		if (!(rawMsg instanceof MsgBrainsweepAck msg))
			return;

		var level = Minecraft.getInstance().level;
		if (level != null) {
			Entity entity = level.getEntity(msg.target());
			if (entity instanceof LivingEntity living) {
				Brainsweeping.brainsweep(living);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void newPattern() {
		if (!(rawMsg instanceof MsgNewSpellPatternAck msg))
			return;

		var mc = Minecraft.getInstance();
		if (msg.info().isStackClear()) {
			// don't pay attention to the screen, so it also stops when we die
			mc.getSoundManager().stop(HexSounds.CASTING_AMBIANCE.getId(), null);
		}
		var screen = Minecraft.getInstance().screen;
		if (screen instanceof GuiSpellcasting spellGui) {
			if (msg.info().isStackClear()) {
				mc.setScreen(null);
			} else {
				spellGui.recvServerUpdate(msg.info());
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void updateColorizer() {
		if (!(rawMsg instanceof MsgColorizerUpdateAck msg))
			return;

		var player = Minecraft.getInstance().player;
		if (player != null) {
			HexPlayerDataHelper.setColorizer(player, msg.update());
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void updateSentinel() {
		if (!(rawMsg instanceof MsgSentinelStatusUpdateAck msg))
			return;

		var player = Minecraft.getInstance().player;
		if (player != null) {
			HexPlayerDataHelper.setSentinel(player, msg.update());
		}
	}

	private static final Random RANDOM = new Random();

	// https://math.stackexchange.com/questions/44689/how-to-find-a-random-axis-or-unit-vector-in-3d
	private static Vec3 randomInCircle(double maxTh) {
		var th = RANDOM.nextDouble(0.0, maxTh + 0.001);
		var z = RANDOM.nextDouble(-1.0, 1.0);
		return new Vec3(Math.sqrt(1.0 - z * z) * Math.cos(th), Math.sqrt(1.0 - z * z) * Math.sin(th), z);
	}

	@OnlyIn(Dist.CLIENT)
	public void particleSpray() {
		if (!(rawMsg instanceof MsgCastParticleAck msg))
			return;

		for (int i = 0; i < msg.spray().getCount(); i++) {
			// For the colors, pick any random time to get a mix of colors
			var color = msg.colorizer().getColor(RANDOM.nextFloat() * 256f, Vec3.ZERO);

			var offset = randomInCircle(Mth.TWO_PI).normalize()
				.scale(RANDOM.nextFloat() * msg.spray().getSpread() / 2);
			var pos = msg.spray().getPos().add(offset);

			var phi = Math.acos(1.0 - RANDOM.nextDouble() * (1.0 - Math.cos(msg.spray().getSpread())));
			var theta = Math.PI * 2.0 * RANDOM.nextDouble();
			var v = msg.spray().getVel().normalize();
			// pick any old vector to get a vector normal to v with
			Vec3 k;
			if (v.x == 0.0 && v.y == 0.0) {
				// oops, pick a *different* normal
				k = new Vec3(1.0, 0.0, 0.0);
			} else {
				k = v.cross(new Vec3(0.0, 0.0, 1.0));
			}
			var velUnlen = v.scale(Math.cos(phi))
				.add(k.scale(Math.sin(phi) * Math.cos(theta)))
				.add(v.cross(k).scale(Math.sin(phi) * Math.sin(theta)));
			var vel = velUnlen.scale(msg.spray().getVel().length() / 20);

			Minecraft.getInstance().level.addParticle(
				new ConjureParticleOptions(color, false),
				pos.x, pos.y, pos.z,
				vel.x, vel.y, vel.z
			);
		}
	}
}
