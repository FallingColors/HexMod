package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LibCapabilities {
    public static final Capability<OpFlight.CapFlight> FLIGHT = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(OpFlight.CapFlight.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof ServerPlayer) {
            evt.addCapability(new ResourceLocation(HexMod.MOD_ID, OpFlight.CAP_NAME),
                // generate a new instance of the capability
                new OpFlight.CapFlight(false, 0, Vec3.ZERO, 0.0));
        }
    }
}
