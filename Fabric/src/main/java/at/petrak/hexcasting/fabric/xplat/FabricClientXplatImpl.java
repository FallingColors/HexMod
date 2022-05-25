package at.petrak.hexcasting.fabric.xplat;

import at.petrak.hexcasting.common.network.IMessage;
import at.petrak.hexcasting.fabric.client.ExtendedTexture;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public <T extends ClientTooltipComponent & TooltipComponent> void registerIdentityTooltipMapping(Class<T> clazz) {
        TooltipComponentCallback.EVENT.register(data -> {
            if (clazz.isAssignableFrom(data.getClass())) {
                return clazz.cast(data);
            }
            return null;
        });
    }

    // suck it fabric trying to be "safe"
    private record UnclampedClampedItemPropFunc(ItemPropertyFunction inner) implements ClampedItemPropertyFunction {
        @Override
        public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity,
            int seed) {
            return inner.call(stack, level, entity, seed);
        }

        @Override
        public float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return this.unclampedCall(stack, level, entity, seed);
        }
    }

    @Override
    public void registerItemProperty(Item item, ResourceLocation id, ItemPropertyFunction func) {
        ItemProperties.register(item, id, new UnclampedClampedItemPropFunc(func));
    }

    @Override
    public void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap) {
        ((ExtendedTexture) texture).setFilterSave(filter, mipmap);
    }

    @Override
    public void restoreLastFilter(AbstractTexture texture) {
        ((ExtendedTexture) texture).restoreLastFilter();
    }
}
