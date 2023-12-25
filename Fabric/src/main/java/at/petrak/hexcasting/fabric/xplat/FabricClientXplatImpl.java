package at.petrak.hexcasting.fabric.xplat;

import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.common.msgs.IMessage;
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents;
import at.petrak.hexcasting.fabric.client.ExtendedTexture;
import at.petrak.hexcasting.fabric.interop.trinkets.TrinketsApiInterop;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

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
    public void initPlatformSpecific() {
        if (IXplatAbstractions.INSTANCE.isModPresent(HexInterop.Fabric.TRINKETS_API_ID)) {
            TrinketsApiInterop.clientInit();
        }
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type,
        EntityRendererProvider<T> renderer) {
        EntityRendererRegistry.register(type, renderer);
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
    public ClientCastingStack getClientCastingStack(Player player) {
        return HexCardinalComponents.CLIENT_CASTING_STACK.get(player).getClientCastingStack();
    }

    @Override
    public void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap) {
        ((ExtendedTexture) texture).setFilterSave(filter, mipmap);
    }

    @Override
    public void restoreLastFilter(AbstractTexture texture) {
        ((ExtendedTexture) texture).restoreLastFilter();
    }

    // Set by FabricLevelRendererMixin
    public static Frustum LEVEL_RENDERER_FRUSTUM = null;

    @Override
    public boolean fabricAdditionalQuenchFrustumCheck(AABB aabb) {
        if (LEVEL_RENDERER_FRUSTUM == null) {
            return true; // fail safe
        }
        return LEVEL_RENDERER_FRUSTUM.isVisible(aabb);
    }
}
