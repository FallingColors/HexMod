package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.common.network.IMessage;
import at.petrak.hexcasting.forge.network.ForgePacketHandler;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ForgeClientXplatImpl implements IClientXplatAbstractions {
    @Override
    public void sendPacketToServer(IMessage packet) {
        ForgePacketHandler.getNetwork().sendToServer(packet);
    }

    @Override
    public void setRenderLayer(Block block, RenderType type) {
        // For forge, handled in block models
//        ItemBlockRenderTypes.setRenderLayer(block, type);
    }

    @Override
    public void initPlatformSpecific() {
        // NO-OP
    }

    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type,
                                                          EntityRendererProvider<T> renderer) {
        EntityRenderers.register(type, renderer);
    }

    @Override
    public void registerItemProperty(Item item, ResourceLocation id, ItemPropertyFunction func) {
        ItemProperties.register(item, id, func);
    }

    @Override
    public void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap) {
        texture.setBlurMipmap(filter, mipmap);
    }

    @Override
    public void restoreLastFilter(AbstractTexture texture) {
        texture.restoreLastBlurMipmap();
    }
}
