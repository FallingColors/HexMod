package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.client.HexAdditionalRenderers;
import at.petrak.hexcasting.client.RegisterClientStuff;
import at.petrak.hexcasting.client.ShiftScrollListener;
import at.petrak.hexcasting.client.gui.PatternTooltipComponent;
import at.petrak.hexcasting.client.shader.HexShaders;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.common.misc.PatternTooltip;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

// This is Java because I can't kotlin-fu some of the consumers
public class ForgeHexClientInitializer {
    // We copy Fabric's example; it mixes in on the return of the initializer and sticks it in a global variable.
    // So here's our global.
    public static ItemColors GLOBAL_ITEM_COLORS;
    public static BlockColors GLOBAL_BLOCK_COLORS;

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent evt) {
        evt.enqueueWork(() -> {
            RegisterClientStuff.init();
            RegisterClientStuff.registerColorProviders(
                (colorizer, item) -> GLOBAL_ITEM_COLORS.register(colorizer, item),
                (colorizer, block) -> GLOBAL_BLOCK_COLORS.register(colorizer, block));
        });

        var evBus = MinecraftForge.EVENT_BUS;

        evBus.addListener((RenderLevelStageEvent e) -> {
            if (e.getStage().equals(RenderLevelStageEvent.Stage.AFTER_PARTICLES)) {
                HexAdditionalRenderers.overlayLevel(e.getPoseStack(), e.getPartialTick());
            }
        });

        evBus.addListener((RenderGuiEvent.Post e) -> {
            HexAdditionalRenderers.overlayGui(e.getPoseStack(), e.getPartialTick());
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

        evBus.addListener((InputEvent.MouseScrollingEvent e) -> {
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
    public static void registerParticles(RegisterParticleProvidersEvent evt) {
        RegisterClientStuff.registerParticles();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        RegisterClientStuff.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent evt) {
        evt.register(PatternTooltip.class, PatternTooltipComponent::new);
    }
}
