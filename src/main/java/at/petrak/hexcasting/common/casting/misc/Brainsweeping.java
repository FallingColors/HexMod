package at.petrak.hexcasting.common.casting.misc;

import at.petrak.hexcasting.common.lib.HexCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
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

    @SubscribeEvent
    public static void tradeWithVillager(PlayerInteractEvent.EntityInteract evt) {
        if (evt.getTarget() instanceof Villager v) {
            var maybeCap = v.getCapability(HexCapabilities.BRAINSWEPT).resolve();
            if (maybeCap.isPresent() && maybeCap.get().brainswept) {
                evt.setCanceled(true);
            }
        }
    }
}
