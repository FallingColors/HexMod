package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.client.render.patternado.PatternadosTracker;
import at.petrak.hexcasting.common.casting.patternado.PatternadoPatInstance;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record MsgNewPatternadoPatS2C(UUID owner, PatternadoPatInstance newPat) implements IMessage {
    public static final ResourceLocation ID = modLoc("nado1");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgNewPatternadoPatS2C deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var owner = buf.readUUID();
        var pat = PatternadoPatInstance.loadFromWire(buf);
        return new MsgNewPatternadoPatS2C(owner, pat);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        this.newPat.saveToWire(buf);
        buf.writeUUID(this.owner);
    }

    public static void handle(MsgNewPatternadoPatS2C msg) {
        Minecraft.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                PatternadosTracker.getNewPat(msg.owner, msg.newPat);
            }
        });
    }
}
