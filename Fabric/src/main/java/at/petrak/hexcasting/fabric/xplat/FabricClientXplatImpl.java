package at.petrak.hexcasting.fabric.xplat;

import at.petrak.hexcasting.common.network.IMessage;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

public class FabricClientXplatImpl implements IClientXplatAbstractions {
    @Override
    public void sendPacketToServer(IMessage packet) {
        ClientPlayNetworking.send(packet.getFabricId(), packet.toBuf());
    }

    @Override
    public void setRenderLayer(Block block, RenderType type) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, type);
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type,
        EntityRendererProvider<T> renderer) {
        EntityRendererRegistry.register(type, renderer);
    }

    @Override
    public <T extends ParticleOptions> void registerParticleType(ParticleType<T> type,
        Function<SpriteSet, ParticleProvider<T>> factory) {
        ParticleFactoryRegistry.getInstance().register(type, factory::apply);
    }
}
