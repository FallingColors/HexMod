package at.petrak.hexcasting.common.casting.env;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.misc.FrozenColorizer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class StaffCastEnv extends PlayerBasedCastEnv {
    public StaffCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster, castingHand);
    }

    @Override
    public void postExecution(CastResult result) {
        // TODO: send information to client
    }

    @Override
    public long extractMedia(long cost) {
        var canOvercast = this.canOvercast();
        var remaining = this.extractMediaFromInventory(cost, canOvercast);
        if (remaining > 0 && !canOvercast) {
            this.caster.sendSystemMessage(Component.translatable("hexcasting.message.cant_overcast"));
        }
        return remaining;
    }

    @Override
    public FrozenColorizer getColorizer() {
        return HexAPI.instance().getColorizer(this.caster);
    }
}
