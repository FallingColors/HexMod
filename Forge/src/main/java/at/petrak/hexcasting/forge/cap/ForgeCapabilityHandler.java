package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.*;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.api.item.*;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.forge.cap.adimpl.*;
import at.petrak.hexcasting.forge.interop.curios.CuriosApiInterop;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeCapabilityHandler {

    public static void registerCapabilities(RegisterCapabilitiesEvent evt) {
        // Media holder - items
        // Register for ALL items (provider returns null for non-media) to avoid missing items
        // from explicit list due to registration order, matching Fabric's predicate-based approach.
        ICapabilityProvider<ItemStack, Void, ADMediaHolder> mediaProvider =
            (stack, ctx) -> {
                if (stack.getItem() instanceof MediaHolderItem holder) {
                    return new CapItemMediaHolder(holder, stack);
                }
                if (stack.is(HexItems.AMETHYST_DUST)) {
                    return new CapStaticMediaHolder(HexConfig.common()::dustMediaAmount, ADMediaHolder.AMETHYST_DUST_PRIORITY, stack);
                }
                if (stack.is(Items.AMETHYST_SHARD)) {
                    return new CapStaticMediaHolder(HexConfig.common()::shardMediaAmount, ADMediaHolder.AMETHYST_SHARD_PRIORITY, stack);
                }
                if (stack.is(HexItems.CHARGED_AMETHYST)) {
                    return new CapStaticMediaHolder(HexConfig.common()::chargedCrystalMediaAmount, ADMediaHolder.CHARGED_AMETHYST_PRIORITY, stack);
                }
                if (stack.is(HexItems.QUENCHED_SHARD)) {
                    return new CapStaticMediaHolder(() -> MediaConstants.QUENCHED_SHARD_UNIT, ADMediaHolder.QUENCHED_SHARD_PRIORITY, stack);
                }
                if (stack.is(HexBlocks.QUENCHED_ALLAY.asItem())) {
                    return new CapStaticMediaHolder(() -> MediaConstants.QUENCHED_BLOCK_UNIT, ADMediaHolder.QUENCHED_ALLAY_PRIORITY, stack);
                }
                return null;
            };
        for (Item item : BuiltInRegistries.ITEM) {
            evt.registerItem(HexCapabilities.MEDIA, mediaProvider, item);
        }

        // Iota holder - items
        evt.registerItem(HexCapabilities.IOTA, (stack, ctx) -> {
            if (stack.getItem() instanceof IotaHolderItem holder) {
                return new CapItemIotaHolder(holder, stack);
            }
            if (stack.is(Items.PUMPKIN_PIE)) {
                return new CapStaticIotaHolder((s) -> new DoubleIota(Math.PI * s.getCount()), stack);
            }
            return null;
        }, getIotaHolderItems());

        // Hex holder - items
        evt.registerItem(HexCapabilities.STORED_HEX, (stack, ctx) ->
            stack.getItem() instanceof HexHolderItem holder ? new CapItemHexHolder(holder, stack) : null,
            getHexHolderItems());

        // Variant item
        evt.registerItem(HexCapabilities.VARIANT_ITEM, (stack, ctx) ->
            stack.getItem() instanceof VariantItem v ? new CapItemVariantItem(v, stack) : null,
            getVariantItems());

        // Pigment
        evt.registerItem(HexCapabilities.COLOR, (stack, ctx) ->
            stack.getItem() instanceof PigmentItem p ? new CapItemPigment(p, stack) : null,
            getPigmentItems());

        // Entity iota holders
        evt.registerEntity(HexCapabilities.IOTA_ENTITY, EntityType.ITEM, (entity, ctx) ->
            new CapEntityIotaHolder.Wrapper(new ItemDelegatingEntityIotaHolder.ToItemEntity((ItemEntity) entity)));
        evt.registerEntity(HexCapabilities.IOTA_ENTITY, EntityType.GLOW_ITEM_FRAME, (entity, ctx) ->
            new CapEntityIotaHolder.Wrapper(new ItemDelegatingEntityIotaHolder.ToItemFrame((ItemFrame) entity)));
        evt.registerEntity(HexCapabilities.IOTA_ENTITY, EntityType.ITEM_FRAME, (entity, ctx) ->
            new CapEntityIotaHolder.Wrapper(new ItemDelegatingEntityIotaHolder.ToItemFrame((ItemFrame) entity)));
        evt.registerEntity(HexCapabilities.IOTA_ENTITY, HexEntities.WALL_SCROLL, (entity, ctx) ->
            new CapEntityIotaHolder.Wrapper(new ItemDelegatingEntityIotaHolder.ToWallScroll((EntityWallScroll) entity)));

        // Client casting stack - players only (client-side rendering)
        evt.registerEntity(HexCapabilities.CLIENT_CASTING_STACK, EntityType.PLAYER, (entity, ctx) ->
            new CapClientCastingStack((Player) entity, new ClientCastingStack()));

        // Block entity - impetus item handler
        evt.registerBlockEntity(Capabilities.ItemHandler.BLOCK, HexBlockEntities.IMPETUS_REDSTONE_TILE,
            (be, ctx) -> new ForgeImpetusCapability((BlockEntityAbstractImpetus) be));
        evt.registerBlockEntity(Capabilities.ItemHandler.BLOCK, HexBlockEntities.IMPETUS_LOOK_TILE,
            (be, ctx) -> new ForgeImpetusCapability((BlockEntityAbstractImpetus) be));
        evt.registerBlockEntity(Capabilities.ItemHandler.BLOCK, HexBlockEntities.IMPETUS_RIGHTCLICK_TILE,
            (be, ctx) -> new ForgeImpetusCapability((BlockEntityAbstractImpetus) be));

        // Curios - bauble items
        if (IXplatAbstractions.INSTANCE.isModPresent(HexInterop.Forge.CURIOS_API_ID)) {
            CuriosApiInterop.registerCurioCapability(evt);
        }
    }

    private static ItemLike[] getIotaHolderItems() {
        List<ItemLike> items = new ArrayList<>();
        items.add(Items.PUMPKIN_PIE);
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IotaHolderItem) {
                items.add(item);
            }
        }
        return items.toArray(ItemLike[]::new);
    }

    private static ItemLike[] getHexHolderItems() {
        List<ItemLike> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof HexHolderItem) {
                items.add(item);
            }
        }
        return items.toArray(ItemLike[]::new);
    }

    private static ItemLike[] getVariantItems() {
        List<ItemLike> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof VariantItem) {
                items.add(item);
            }
        }
        return items.toArray(ItemLike[]::new);
    }

    private static ItemLike[] getPigmentItems() {
        List<ItemLike> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof PigmentItem) {
                items.add(item);
            }
        }
        return items.toArray(ItemLike[]::new);
    }
}
