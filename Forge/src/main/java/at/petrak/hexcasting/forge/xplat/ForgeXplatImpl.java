package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.common.lib.HexStringKeys;
import at.petrak.hexcasting.common.network.IMessage;
import at.petrak.hexcasting.common.network.MsgBrainsweepAck;
import at.petrak.hexcasting.forge.network.ForgePacketHandler;
import at.petrak.hexcasting.mixin.AccessorLivingEntity;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.Platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;

public class ForgeXplatImpl implements IXplatAbstractions {
    @Override
    public Platform platform() {
        return Platform.FORGE;
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @Override
    public void brainsweep(LivingEntity entity) {
        if (entity instanceof VillagerDataHolder) {
            entity.getPersistentData().putBoolean(HexStringKeys.BRAINSWEPT, true);

            if (entity instanceof Mob mob) {
                mob.removeFreeWill();
            }

            if (entity instanceof Villager villager) {
                Brain<Villager> brain = villager.getBrain();
                if (entity.level instanceof ServerLevel slevel) {
                    brain.stopAll(slevel, villager);
                }
                ((AccessorLivingEntity) entity).hex$SetBrain(brain.copyWithoutBehaviors());
            }

            if (entity.level instanceof ServerLevel) {
                ForgePacketHandler.getNetwork()
                    .send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), MsgBrainsweepAck.of(entity));
            }
        }
    }

    @Override
    public boolean isBrainswept(LivingEntity e) {
        return e instanceof VillagerDataHolder && e.getPersistentData().getBoolean(HexStringKeys.BRAINSWEPT);
    }

    @Override
    public void sendPacketToPlayer(ServerPlayer target, IMessage packet) {
        ForgePacketHandler.getNetwork().send(PacketDistributor.PLAYER.with(() -> target), packet);
    }

    @Override
    public void sendPacketToServer(IMessage packet) {
        ForgePacketHandler.getNetwork().sendToServer(packet);
    }
}