package at.petrak.hexcasting.api.mod;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ConstantConditions")
@ApiStatus.Internal
@ObjectHolder("hexcasting")
public class HexApiSounds {
	@NotNull
	@ObjectHolder("spellcircle.cast")
	public static final SoundEvent SPELL_CIRCLE_CAST = null;

	@NotNull
	@ObjectHolder("spellcircle.fail")
	public static final SoundEvent SPELL_CIRCLE_FAIL = null;

	@NotNull
	@ObjectHolder("spellcircle.find_block")
	public static final SoundEvent SPELL_CIRCLE_FIND_BLOCK = null;

	@NotNull
	@ObjectHolder("casting.fail_pattern")
	public static final SoundEvent FAIL_PATTERN = null;
}
