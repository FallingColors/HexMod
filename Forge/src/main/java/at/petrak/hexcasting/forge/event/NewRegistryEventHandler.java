package at.petrak.hexcasting.forge.event;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.castables.SpecialHandler;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.common.lib.HexRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class NewRegistryEventHandler {
    public static Supplier<IForgeRegistry<ActionRegistryEntry>> ACTION;
    public static Supplier<IForgeRegistry<SpecialHandler.Factory<?>>> SPECIAL_HANDLER;
    public static Supplier<IForgeRegistry<IotaType<?>>> IOTA_TYPE;
    public static Supplier<IForgeRegistry<Arithmetic>> ARITHMETIC;
    public static Supplier<IForgeRegistry<EvalSound>> EVAL_SOUND;

    public static void newRegistry(NewRegistryEvent event) {
        ACTION = event.create(
            new RegistryBuilder<ActionRegistryEntry>()
                .setName(HexRegistries.ACTION.location())
        );
        SPECIAL_HANDLER = event.create(
            new RegistryBuilder<SpecialHandler.Factory<?>>()
                .setName(HexRegistries.SPECIAL_HANDLER.location())
        );
        IOTA_TYPE = event.create(
            new RegistryBuilder<IotaType<?>>()
                .setName(HexRegistries.IOTA_TYPE.location())
                .setDefaultKey(modLoc("null"))
        );
        ARITHMETIC = event.create(
            new RegistryBuilder<Arithmetic>()
                .setName(HexRegistries.ARITHMETIC.location())
                .disableSync()
        );
        EVAL_SOUND = event.create(
            new RegistryBuilder<EvalSound>()
                .setName(HexRegistries.EVAL_SOUND.location())
                .setDefaultKey(modLoc("nothing"))
        );
    }
}
