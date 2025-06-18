package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.addldata.ItemDelegatingEntityIotaHolder;
import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.api.item.*;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.forge.cap.adimpl.*;
import at.petrak.hexcasting.forge.interop.curios.CuriosApiInterop;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeCapabilityHandler {

    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        for(Item item : BuiltInRegistries.ITEM) {
            if(item instanceof MediaHolderItem holder)
                evt.registerItem(HexCapabilities.Item.MEDIA, (stack, ctx) -> new CapItemMediaHolder(holder, stack), item);
            if(item instanceof IotaHolderItem holder)
                evt.registerItem(HexCapabilities.Item.IOTA, (stack, ctx) -> new CapItemIotaHolder(holder, stack), item);
            if(item instanceof HexHolderItem holder)
                evt.registerItem(HexCapabilities.Item.STORED_HEX, (stack, ctx) -> new CapItemHexHolder(holder, stack), item);
            if(item instanceof VariantItem holder)
                evt.registerItem(HexCapabilities.Item.VARIANT_ITEM, (stack, ctx) -> new CapItemVariantItem(holder, stack), item);
            if(item instanceof PigmentItem holder)
                evt.registerItem(HexCapabilities.Item.COLOR, (stack, ctx) -> new CapItemPigment(holder, stack), item);
            if(item instanceof HexBaubleItem && IXplatAbstractions.INSTANCE.isModPresent(HexInterop.Forge.CURIOS_API_ID))
                CuriosApiInterop.registerCap(evt, item);
        }

        evt.registerItem(
                HexCapabilities.Item.MEDIA,
                (stack, ctx) -> new CapStaticMediaHolder(HexConfig.common()::dustMediaAmount, ADMediaHolder.AMETHYST_DUST_PRIORITY, stack),
                HexItems.AMETHYST_DUST
        );
        evt.registerItem(
                HexCapabilities.Item.MEDIA,
                (stack, ctx) -> new CapStaticMediaHolder(HexConfig.common()::shardMediaAmount, ADMediaHolder.AMETHYST_SHARD_PRIORITY, stack),
                Items.AMETHYST_SHARD
        );
        evt.registerItem(
                HexCapabilities.Item.MEDIA,
                (stack, ctx) -> new CapStaticMediaHolder(HexConfig.common()::chargedCrystalMediaAmount, ADMediaHolder.CHARGED_AMETHYST_PRIORITY, stack),
                HexItems.CHARGED_AMETHYST
        );
        evt.registerItem(
                HexCapabilities.Item.MEDIA,
                (stack, ctx) -> new CapStaticMediaHolder(() -> MediaConstants.QUENCHED_SHARD_UNIT, ADMediaHolder.QUENCHED_SHARD_PRIORITY, stack),
                HexItems.QUENCHED_SHARD
        );
        evt.registerItem(
                HexCapabilities.Item.MEDIA,
                (stack, ctx) -> new CapStaticMediaHolder(() -> MediaConstants.QUENCHED_BLOCK_UNIT, ADMediaHolder.QUENCHED_ALLAY_PRIORITY, stack),
                HexBlocks.QUENCHED_ALLAY.asItem()
        );

        // haha yes
        evt.registerItem(
                HexCapabilities.Item.IOTA,
                (stack, ctx) -> new CapStaticIotaHolder((s) -> new DoubleIota(Math.PI * s.getCount()), stack),
                Items.PUMPKIN_PIE
        );

        evt.registerEntity(
                HexCapabilities.Entity.IOTA,
                EntityType.ITEM,
                (ent, ctx) -> new ItemDelegatingEntityIotaHolder.ToItemEntity(ent)
        );
        evt.registerEntity(
                HexCapabilities.Entity.IOTA,
                EntityType.ITEM_FRAME,
                (ent, ctx) -> new ItemDelegatingEntityIotaHolder.ToItemFrame(ent)
        );
        evt.registerEntity(
                HexCapabilities.Entity.IOTA,
                HexEntities.WALL_SCROLL,
                (ent, ctx) -> new ItemDelegatingEntityIotaHolder.ToWallScroll(ent)
        );

        for(Block block : BuiltInRegistries.BLOCK) {
            if(block instanceof BlockAbstractImpetus imBlock) {
                evt.registerBlockEntity(
                        Capabilities.ItemHandler.BLOCK,
                        imBlock.getBlockEntityType(),
                        (be, dir) -> new ForgeImpetusCapability(be)
                );
            }
        }
    }
}
