package at.petrak.hexcasting.common.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Common/src/main/java/vazkii/botania/network/IPacket.java
// yoink
public interface IMessage {
    default FriendlyByteBuf toBuf() {
        var ret = new FriendlyByteBuf(Unpooled.buffer());
        serialize(ret);
        return ret;
    }

    void serialize(FriendlyByteBuf buf);

    /**
     * Forge auto-assigns incrementing integers, Fabric requires us to declare an ID
     * These are sent using vanilla's custom plugin channel system and thus are written to every single packet.
     * So this ID tends to be more terse.
     */
    ResourceLocation getFabricId();
}
