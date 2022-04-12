package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.mixin.AccessorLivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Brainsweeping {

    public static final String TAG_BRAINSWEPT = "hexcasting:brainswept";

    public static boolean isBrainswept(LivingEntity entity) {
        return entity instanceof VillagerDataHolder && entity.getPersistentData().getBoolean(TAG_BRAINSWEPT);
    }

    public static void brainsweep(LivingEntity entity) {
        if (entity instanceof VillagerDataHolder) {
            entity.getPersistentData().putBoolean(TAG_BRAINSWEPT, true);

            if (entity instanceof Villager villager) {
                Brain<Villager> brain = villager.getBrain();
                if (entity.level instanceof ServerLevel slevel) {
                    brain.stopAll(slevel, villager);
                }
                ((AccessorLivingEntity) entity).hex$SetBrain(brain.copyWithoutBehaviors());
            }
        }
    }

    @SubscribeEvent
    public static void tradeWithVillager(PlayerInteractEvent.EntityInteract evt) {
        if (evt.getTarget() instanceof Villager v && isBrainswept(v)) {
            evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void copyBrainsweepBetweenZombieAndVillager(LivingConversionEvent.Post evt) {
        var outcome = evt.getOutcome();
        var original = evt.getEntityLiving();
        if (outcome instanceof VillagerDataHolder && original instanceof VillagerDataHolder) {
            if (isBrainswept(original)) brainsweep(outcome);
        }
    }
}
