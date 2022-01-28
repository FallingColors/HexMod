package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.HexMod;
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
        NETWORK.registerMessage(messageIdx++, MsgQuitSpellcasting.class, MsgQuitSpellcasting::serialize,
            MsgQuitSpellcasting::deserialize, MsgQuitSpellcasting::handle);
        NETWORK.registerMessage(messageIdx++, MsgShiftScrollSyn.class, MsgShiftScrollSyn::serialize,
            MsgShiftScrollSyn::deserialize, MsgShiftScrollSyn::handle);
        NETWORK.registerMessage(messageIdx++, MsgAddMotionAck.class, MsgAddMotionAck::serialize,
            MsgAddMotionAck::deserialize, MsgAddMotionAck::handle);
        NETWORK.registerMessage(messageIdx++, MsgBlinkAck.class, MsgBlinkAck::serialize,
            MsgBlinkAck::deserialize, MsgBlinkAck::handle);
        NETWORK.registerMessage(messageIdx++, MsgSentinelStatusUpdateAck.class, MsgSentinelStatusUpdateAck::serialize,
            MsgSentinelStatusUpdateAck::deserialize, MsgSentinelStatusUpdateAck::handle);
        NETWORK.registerMessage(messageIdx++, MsgColorizerUpdateAck.class, MsgColorizerUpdateAck::serialize,
            MsgColorizerUpdateAck::deserialize, MsgColorizerUpdateAck::handle);
    }
}
