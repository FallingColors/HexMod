package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.common.lib.HexCapabilities;
import at.petrak.hexcasting.mixin.AccessorLivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Brainsweeping {
    public static final String CAP_NAME = "brainsweeping";

    public static class Cap implements ICapabilitySerializable<CompoundTag> {
        public boolean brainswept = false;

        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return HexCapabilities.BRAINSWEPT.orEmpty(cap, LazyOptional.of(() -> this));
        }

        public CompoundTag serializeNBT() {
            var out = new CompoundTag();
            out.putBoolean("brainswept", this.brainswept);
            return out;
        }

        public void deserializeNBT(CompoundTag tag) {
            this.brainswept = tag.getBoolean("brainswept");
        }
    }

    public static boolean isBrainswept(Villager villager) {
        var maybeCap = villager.getCapability(HexCapabilities.BRAINSWEPT).resolve();
        return maybeCap.map(cap -> cap.brainswept).orElse(false);
    }

    public static void brainsweep(Villager villager) {
        var maybeCap = villager.getCapability(HexCapabilities.BRAINSWEPT).resolve();
        maybeCap.ifPresent(cap -> {
            cap.brainswept = true;

            var brain = villager.getBrain();
            if (villager.level instanceof ServerLevel slevel) {
                brain.stopAll(slevel, villager);
            }
            ((AccessorLivingEntity) villager).hex$SetBrain(brain.copyWithoutBehaviors());
        });
    }

    @SubscribeEvent
    public static void tradeWithVillager(PlayerInteractEvent.EntityInteract evt) {
        if (evt.getTarget() instanceof Villager v && isBrainswept(v)) {
            evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void copyBrainsweepToZombie(LivingConversionEvent evt) {
        
    }
}
