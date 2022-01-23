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
    public static final RegistryObject<SoundEvent> CASTING_AMBIANCE = sound("casting.ambiance");
    public static final RegistryObject<SoundEvent> ACTUALLY_CAST = sound("casting.cast");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(prefix(name)));
    }
}
