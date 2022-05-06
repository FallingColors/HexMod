package at.petrak.hexcasting;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.client.HexAdditionalRenderers;
import at.petrak.hexcasting.client.RegisterClientStuff;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// This is Java because I can't kotlin-fu some of the consumers
public class ForgeHexClientInitializer {
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent evt) {
        RegisterClientStuff.init();

        var evBus = MinecraftForge.EVENT_BUS;

        evBus.addListener(
            (RenderLevelLastEvent e) -> HexAdditionalRenderers.overlayLevel(e.getPoseStack(), e.getPartialTick()));
        evBus.addListener((RenderGameOverlayEvent.PreLayer e) ->
            HexAdditionalRenderers.overlayGui(e.getMatrixStack(), e.getPartialTicks()));
        evBus.addListener(EventPriority.LOWEST, (ParticleFactoryRegisterEvent e) ->
            RegisterClientStuff.registerParticles());
        evBus.addListener((TickEvent.ClientTickEvent e) -> ClientTickCounter.onTick());
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        RegisterClientStuff.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
    }
}
