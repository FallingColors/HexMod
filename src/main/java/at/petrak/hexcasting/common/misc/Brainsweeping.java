package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.common.network.HexMessages;
import at.petrak.hexcasting.common.network.MsgBrainsweepAck;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.raid.Raider;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class Brainsweeping {

    public static final String TAG_BRAINSWEPT = "hexcasting:brainswept";

    private static boolean isValidTarget(LivingEntity entity) {
        return entity instanceof VillagerDataHolder || entity instanceof Raider;
    }

    public static boolean isBrainswept(LivingEntity entity) {
        return isValidTarget(entity) && entity.getPersistentData().getBoolean(TAG_BRAINSWEPT);
    }

    public static void brainsweep(LivingEntity entity) {
        if (entity instanceof Mob mob && isValidTarget(entity)) {
            entity.getPersistentData().putBoolean(TAG_BRAINSWEPT, true);

            mob.removeFreeWill();

            if (entity.level instanceof ServerLevel)
                HexMessages.getNetwork().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), MsgBrainsweepAck.of(entity));
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking evt) {
        Entity target = evt.getTarget();
        if (evt.getPlayer() instanceof ServerPlayer serverPlayer &&
                target instanceof LivingEntity living && isBrainswept(living)) {
            HexMessages.getNetwork().send(PacketDistributor.PLAYER.with(() -> serverPlayer), MsgBrainsweepAck.of(living));
        }
    }

    @SubscribeEvent
    public static void tradeWithVillager(PlayerInteractEvent.EntityInteract evt) {
        if (evt.getTarget() instanceof Villager v && isBrainswept(v)) {
            evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void copyBrainsweepBetweenZombieVillagerAndWitch(LivingConversionEvent.Post evt) {
        var outcome = evt.getOutcome();
        var original = evt.getEntityLiving();
        if (isValidTarget(outcome) && isBrainswept(original))
            brainsweep(outcome);
    }
}
