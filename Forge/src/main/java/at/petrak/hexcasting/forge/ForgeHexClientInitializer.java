package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.client.RegisterClientStuff;
import at.petrak.hexcasting.common.msgs.*;
import at.petrak.hexcasting.forge.network.*;
import at.petrak.hexcasting.client.ShiftScrollListener;
import at.petrak.hexcasting.client.gui.PatternTooltipComponent;
import at.petrak.hexcasting.client.model.AltioraLayer;
import at.petrak.hexcasting.client.model.HexModelLayers;
import at.petrak.hexcasting.client.render.HexAdditionalRenderers;
import at.petrak.hexcasting.client.render.shader.HexShaders;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.lib.HexParticles;
import at.petrak.hexcasting.common.misc.PatternTooltip;
import at.petrak.hexcasting.interop.HexInterop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;
import java.util.function.Function;

// This is Java because I can't kotlin-fu some of the consumers
public class ForgeHexClientInitializer {

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent evt) {
        evt.enqueueWork(RegisterClientStuff::init);

        var evBus = NeoForge.EVENT_BUS;

        evBus.addListener((ClientPlayerNetworkEvent.LoggingIn e) ->
            PatternRegistryManifest.processRegistry(null));

        evBus.addListener((RenderLevelStageEvent e) -> {
            if (e.getStage().equals(RenderLevelStageEvent.Stage.AFTER_PARTICLES)) {
                HexAdditionalRenderers.overlayLevel(e.getPoseStack(), e.getPartialTick().getGameTimeDeltaPartialTick(true));
            }
        });

        evBus.addListener((RenderGuiEvent.Post e) -> {
            HexAdditionalRenderers.overlayGui(e.getGuiGraphics(), e.getPartialTick().getGameTimeDeltaPartialTick(true));
        });


        evBus.addListener((RenderFrameEvent.Pre e) ->
            ClientTickCounter.renderTickStart(e.getPartialTick().getGameTimeDeltaPartialTick(true)));

        evBus.addListener((ClientTickEvent.Post e) -> {
            ClientTickCounter.clientTickEnd();
            ShiftScrollListener.clientTickEnd();
        });

        evBus.addListener((InputEvent.MouseScrollingEvent e) -> {
            var cancel = ShiftScrollListener.onScrollInGameplay((float) e.getScrollDeltaY());
            e.setCanceled(cancel);
        });

        HexInterop.clientInit();
    }

    @SubscribeEvent
    public static void registerColors(RegisterColorHandlersEvent.Item evt) {
        RegisterClientStuff.registerColorProviders(
            (colorizer, item) -> evt.getItemColors().register(colorizer, item),
            (colorizer, block) -> evt.getBlockColors().register(colorizer, block));
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent evt) throws IOException {
        HexShaders.init(evt.getResourceProvider(), p -> evt.registerShader(p.getFirst(), p.getSecond()));
    }

    // https://github.com/VazkiiMods/Botania/blob/1.19.x/Forge/src/main/java/vazkii/botania/forge/client/ForgeClientInitializer.java#L225
    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent evt) {
        HexParticles.FactoryHandler.registerFactories(new HexParticles.FactoryHandler.Consumer() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet,
                ParticleProvider<T>> constructor) {
                evt.registerSpriteSet(type, constructor::apply);
            }
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers evt) {
        RegisterClientStuff.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent evt) {
        evt.register(PatternTooltip.class, PatternTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional evt) {
        var recMan = Minecraft.getInstance().getResourceManager();
        RegisterClientStuff.onModelRegister(recMan,
            loc -> evt.register(ModelResourceLocation.standalone(loc)));
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult evt) {
        var models = evt.getModels();
        var modelMap = new java.util.HashMap<net.minecraft.resources.ResourceLocation, net.minecraft.client.resources.model.BakedModel>();
        for (var entry : models.entrySet()) {
            var key = entry.getKey();
            modelMap.put(key.id(), entry.getValue());
        }
        RegisterClientStuff.onModelBake(evt.getModelBakery(), modelMap);
    }

    @SubscribeEvent
    public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions evt) {
        HexModelLayers.init(evt::registerLayerDefinition);
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers evt) {
        evt.getSkins().forEach(skinName -> {
            var skin = (LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) evt.getSkin(skinName);

            skin.addLayer(new AltioraLayer<>(skin, evt.getEntityModels()));
        });
    }
}
