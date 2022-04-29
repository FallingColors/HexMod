package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class Brainsweeping {
    // Keeping these functions in Brainsweeping just so we have to change less code
    public static void brainsweep(Mob entity) {
        if (isValidTarget(entity)) {
            IXplatAbstractions.INSTANCE.brainsweep(entity);
        }
    }

    public static boolean isBrainswept(Mob entity) {
        return isValidTarget(entity) && IXplatAbstractions.INSTANCE.isBrainswept(entity);
    }

    // TODO: make this a tag
    public static boolean isValidTarget(Mob mob) {
        return mob instanceof VillagerDataHolder || mob instanceof Raider;
    }

    public static InteractionResult tradeWithVillager(Player player, Level world, InteractionHand hand, Entity entity,
        @Nullable EntityHitResult hitResult) {
        if (entity instanceof Villager v && IXplatAbstractions.INSTANCE.isBrainswept(v)) {
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    public static InteractionResult copyBrainsweepFromVillager(LivingEntity original, LivingEntity outcome) {
        if (original instanceof Mob mOriginal && outcome instanceof Mob mOutcome
            && IXplatAbstractions.INSTANCE.isBrainswept(mOriginal) && Brainsweeping.isValidTarget(mOutcome)) {
            IXplatAbstractions.INSTANCE.brainsweep(mOutcome);
        }
        return InteractionResult.PASS;
    }
}
