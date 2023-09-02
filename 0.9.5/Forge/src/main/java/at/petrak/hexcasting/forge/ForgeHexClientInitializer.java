package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.client.HexAdditionalRenderers;
import at.petrak.hexcasting.client.RegisterClientStuff;
import at.petrak.hexcasting.client.ShiftScrollListener;
import at.petrak.hexcasting.client.shader.HexShaders;
import at.petrak.hexcasting.interop.HexInterop;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

// This is Java because I can't kotlin-fu some of the consumers
public class ForgeHexClientInitializer {
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent evt) {
        evt.enqueueWork(RegisterClientStuff::init);

        var evBus = MinecraftForge.EVENT_BUS;

        evBus.addListener((RenderLevelLastEvent e) ->
            HexAdditionalRenderers.overlayLevel(e.getPoseStack(), e.getPartialTick()));

        evBus.addListener((RenderGameOverlayEvent.Post e) -> {
            if (e.getType() == RenderGameOverlayEvent.ElementType.ALL)
                HexAdditionalRenderers.overlayGui(e.getMatrixStack(), e.getPartialTicks());
        });


        evBus.addListener((TickEvent.RenderTickEvent e) -> {
            if (e.phase == TickEvent.Phase.START) {
                ClientTickCounter.renderTickStart(e.renderTickTime);
            }
        });

        evBus.addListener((TickEvent.ClientTickEvent e) -> {
            if (e.phase == TickEvent.Phase.END) {
                ClientTickCounter.clientTickEnd();
                ShiftScrollListener.clientTickEnd();
            }
        });

        evBus.addListener((InputEvent.MouseScrollEvent e) -> {
            var cancel = ShiftScrollListener.onScrollInGameplay(e.getScrollDelta());
            e.setCanceled(cancel);
        });

        HexInterop.clientInit();
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent evt) throws IOException {
        HexShaders.init(evt.getResourceManager(), p -> evt.registerShader(p.getFirst(), p.getSecond()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent evt) {
        RegisterClientStuff.registerParticles();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        RegisterClientStuff.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
    }
}
