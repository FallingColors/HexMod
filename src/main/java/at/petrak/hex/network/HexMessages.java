package at.petrak.hex.network;

import at.petrak.hex.HexMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class HexMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HexMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static SimpleChannel getNetwork() {
        return NETWORK;
    }

    public static void register() {
        int messageIdx = 0;

        NETWORK.registerMessage(messageIdx++, MsgNewSpellPatternSyn.class, MsgNewSpellPatternSyn::serialize,
                MsgNewSpellPatternSyn::deserialize, MsgNewSpellPatternSyn::handle);
        NETWORK.registerMessage(messageIdx++, MsgNewSpellPatternAck.class, MsgNewSpellPatternAck::serialize,
                MsgNewSpellPatternAck::deserialize, MsgNewSpellPatternAck::handle);
    }
}
