package at.petrak.hexcasting.forge.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.forge.ForgeHexInitializer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ForgeHexAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HexAPI.MOD_ID);

    // TODO port: maybe make client-side only?
    public static final Supplier<AttachmentType<ClientCastingStack>> CLIENT_CASTING_STACK = ATTACHMENT_TYPES.register(
            "casting_stack", () -> AttachmentType.builder(ClientCastingStack::new).build()
    );

    public static void register() {
        ATTACHMENT_TYPES.register(ForgeHexInitializer.getModEventBus());
    }
}
