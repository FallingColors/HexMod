package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.client.render.patternado.PatternadosTracker;
import at.petrak.hexcasting.common.casting.patternado.PatternadoPatInstance;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Set the patternado of a given player to this, overwrite the previous
 */
public record MsgOverridePatternadoS2C(UUID owner, List<PatternadoPatInstance> pats) implements IMessage {
    public static final ResourceLocation ID = modLoc("nados");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgOverridePatternadoS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var owner = buf.readUUID();
        var pats = buf.readCollection(ArrayList::new, PatternadoPatInstance::loadFromWire);
        return new MsgOverridePatternadoS2C(owner, pats);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeUUID(this.owner);
        buf.writeCollection(this.pats, (bf, it) -> it.saveToWire(bf));
    }

    public static void handle(MsgOverridePatternadoS2C msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                PatternadosTracker.clobberPatterns(msg.owner, msg.pats);
            }
        });
    }
}
