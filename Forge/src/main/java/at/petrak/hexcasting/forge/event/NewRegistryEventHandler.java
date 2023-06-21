package at.petrak.hexcasting.forge.event;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.common.lib.HexRegistries;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class NewRegistryEventHandler {
    public static void newRegistry(NewRegistryEvent event) {
        event.create(
            new RegistryBuilder<ActionRegistryEntry>()
                .setName(HexRegistries.ACTION.location())
        );
        event.create(
            new RegistryBuilder<>()
                .setName(HexRegistries.SPECIAL_HANDLER.location())
        );
        event.create(
            new RegistryBuilder<>()
                .setName(HexRegistries.IOTA_TYPE.location())
                .setDefaultKey(modLoc("null"))
        );
        event.create(
            new RegistryBuilder<>()
                .setName(HexRegistries.ARITHMETIC.location())
                .disableSync()
        );
        event.create(
            new RegistryBuilder<>()
                .setName(HexRegistries.EVAL_SOUND.location())
                .setDefaultKey(modLoc("nothing"))
        );
    }
}
