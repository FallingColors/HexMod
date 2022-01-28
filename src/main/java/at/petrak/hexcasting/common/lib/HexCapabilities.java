package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.casting.colors.CapPreferredColorizer;
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight;
import at.petrak.hexcasting.common.casting.operators.spells.sentinel.CapSentinel;
import at.petrak.hexcasting.common.items.HexItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HexCapabilities {
    public static final Capability<OpFlight.CapFlight> FLIGHT = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<CapSentinel> SENTINEL = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<CapPreferredColorizer> PREFERRED_COLORIZER =
        CapabilityManager.get(new CapabilityToken<>() {
        });

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(OpFlight.CapFlight.class);
        evt.register(CapSentinel.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof Player) {
            evt.addCapability(new ResourceLocation(HexMod.MOD_ID, OpFlight.CAP_NAME),
                // generate a new instance of the capability
                new OpFlight.CapFlight(false, 0, Vec3.ZERO, 0.0));
            evt.addCapability(new ResourceLocation(HexMod.MOD_ID, CapSentinel.CAP_NAME),
                new CapSentinel(false, false, Vec3.ZERO));
            evt.addCapability(new ResourceLocation(HexMod.MOD_ID, CapPreferredColorizer.CAP_NAME),
                new CapPreferredColorizer(new ItemStack(HexItems.DYE_COLORIZERS[0].get())));
        }
    }
}
