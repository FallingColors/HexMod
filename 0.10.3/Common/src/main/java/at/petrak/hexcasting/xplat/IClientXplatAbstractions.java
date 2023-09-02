package at.petrak.hexcasting.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.network.IMessage;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface IClientXplatAbstractions {
    void sendPacketToServer(IMessage packet);

    void setRenderLayer(Block block, RenderType type);

    void initPlatformSpecific();

    <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> renderer);

    void registerItemProperty(Item item, ResourceLocation id, ItemPropertyFunction func);

    // On Forge, these are already exposed; on Farbc we do a mixin
    void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap);

    void restoreLastFilter(AbstractTexture texture);

    IClientXplatAbstractions INSTANCE = find();

    private static IClientXplatAbstractions find() {
        var providers = ServiceLoader.load(IClientXplatAbstractions.class).stream().toList();
        if (providers.size() != 1) {
            var names = providers.stream().map(p -> p.type().getName()).collect(Collectors.joining(",", "[", "]"));
            throw new IllegalStateException(
                    "There should be exactly one IClientXplatAbstractions implementation on the classpath. Found: " + names);
        } else {
            var provider = providers.get(0);
            HexAPI.LOGGER.debug("Instantiating client xplat impl: " + provider.type().getName());
            return provider.get();
        }
    }
}
