package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.common.network.HexMessages;
import at.petrak.hexcasting.common.network.MsgBrainsweepAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class Brainsweeping {
    // Keeping these functions in Brainsweeping just so we have to change less code
    public static void brainsweep(LivingEntity entity) {
        IXplatAbstractions.INSTANCE.brainsweep(entity);
    }

    public static boolean isBrainswept(LivingEntity entity) {
        return IXplatAbstractions.INSTANCE.isBrainswept(entity);
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking evt) {
        Entity target = evt.getTarget();
        if (evt.getPlayer() instanceof ServerPlayer serverPlayer &&
            target instanceof VillagerDataHolder && target instanceof LivingEntity living && isBrainswept(living)) {
            HexMessages.getNetwork()
                .send(PacketDistributor.PLAYER.with(() -> serverPlayer), MsgBrainsweepAck.of(living));
        }
    }

    public static InteractionResult tradeWithVillager(Player player, Level world, InteractionHand hand, Entity entity,
        @Nullable EntityHitResult hitResult) {
        if (entity instanceof Villager v && IXplatAbstractions.INSTANCE.isBrainswept(v)) {
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    @SubscribeEvent
    public static void copyBrainsweepBetweenZombieAndVillager(LivingConversionEvent.Post evt) {
        var outcome = evt.getOutcome();
        var original = evt.getEntityLiving();
        if (outcome instanceof VillagerDataHolder && original instanceof VillagerDataHolder) {
            if (isBrainswept(original)) {
                brainsweep(outcome);
            }
        }
    }
}
