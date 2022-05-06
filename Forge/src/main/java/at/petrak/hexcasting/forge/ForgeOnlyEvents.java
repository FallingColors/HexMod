package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.common.blocks.behavior.HexStrippables;
import at.petrak.hexcasting.forge.network.ForgePacketHandler;
import at.petrak.hexcasting.forge.network.MsgBrainsweepAck;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

// Events we don't need on fabric for whatever reasons
public class ForgeOnlyEvents {
    // On Fabric this should be auto-synced
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking evt) {
        Entity target = evt.getTarget();
        if (evt.getPlayer() instanceof ServerPlayer serverPlayer &&
            target instanceof Mob mob && IXplatAbstractions.INSTANCE.isBrainswept(mob)) {
            ForgePacketHandler.getNetwork()
                .send(PacketDistributor.PLAYER.with(() -> serverPlayer), MsgBrainsweepAck.of(mob));
        }
    }

    // Implemented with a mixin
    @SubscribeEvent
    public static void stripBlock(BlockEvent.BlockToolModificationEvent evt) {
        if (!evt.isSimulated() && evt.getToolAction() == ToolActions.AXE_STRIP) {
            BlockState bs = evt.getState();
            var output = HexStrippables.STRIPPABLES.get(bs.getBlock());
            if (output != null) {
                evt.setFinalState(output.withPropertiesOf(bs));
            }
        }
    }
}
