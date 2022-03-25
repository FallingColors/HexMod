package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.HexMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static at.petrak.hexcasting.common.lib.RegisterHelper.prefix;

public class HexSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(
        ForgeRegistries.SOUND_EVENTS,
        HexMod.MOD_ID);

    public static final RegistryObject<SoundEvent> ADD_LINE = sound("casting.add_line");
    public static final RegistryObject<SoundEvent> START_PATTERN = sound("casting.start_pattern");
    public static final RegistryObject<SoundEvent> ADD_PATTERN = sound("casting.add_pattern");
    public static final RegistryObject<SoundEvent> FAIL_PATTERN = sound("casting.fail_pattern");
    public static final RegistryObject<SoundEvent> CASTING_AMBIANCE = sound("casting.ambiance");
    public static final RegistryObject<SoundEvent> ACTUALLY_CAST = sound("casting.cast");

    public static final RegistryObject<SoundEvent> ABACUS = sound("abacus");
    public static final RegistryObject<SoundEvent> ABACUS_SHAKE = sound("abacus.shake");

    public static final RegistryObject<SoundEvent> SPELL_CIRCLE_FIND_BLOCK = sound("spellcircle.find_block");
    public static final RegistryObject<SoundEvent> SPELL_CIRCLE_FAIL = sound("spellcircle.fail");
    public static final RegistryObject<SoundEvent> SPELL_CIRCLE_CAST = sound("spellcircle.cast");

    public static final RegistryObject<SoundEvent> DUST_SCROLL = sound("scroll.dust");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(prefix(name)));
    }
}
