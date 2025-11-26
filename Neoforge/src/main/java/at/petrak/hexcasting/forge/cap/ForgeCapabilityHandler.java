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
<<<<<<< HEAD
=======
import net.minecraft.core.registries.BuiltInRegistries;
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
<<<<<<< HEAD
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
=======
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
>>>>>>> refs/remotes/slava/devel/port-1.21

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeCapabilityHandler {

    public static void registerCaps(RegisterCapabilitiesEvent evt) {
<<<<<<< HEAD
        evt.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ,
                (impetus, context) -> provide(impetus, Capabilities.ItemHandler.BLOCK,
                        () -> new ForgeImpetusCapability((BlockEntityAbstractImpetus) impetus)
        );
    }

    public static void attachItemCaps(AttachCapabilitiesEvent<ItemStack> evt) {
        ItemStack stack = evt.getObject();

        if (stack.getItem() instanceof MediaHolderItem holder) {
            evt.addCapability(MEDIA_STORAGE_CAP,
                provide(stack, HexCapabilities.MEDIA, () -> new CapItemMediaHolder(holder, stack)));
        } else if (stack.is(HexItems.AMETHYST_DUST)) {
            evt.addCapability(MEDIA_STATIC_CAP, provide(stack, HexCapabilities.MEDIA, () ->
                new CapStaticMediaHolder(HexConfig.common()::dustMediaAmount, ADMediaHolder.AMETHYST_DUST_PRIORITY,
                    stack)));
        } else if (stack.is(Items.AMETHYST_SHARD)) {
            evt.addCapability(MEDIA_STATIC_CAP, provide(stack, HexCapabilities.MEDIA, () -> new CapStaticMediaHolder(
                HexConfig.common()::shardMediaAmount, ADMediaHolder.AMETHYST_SHARD_PRIORITY, stack)));
        } else if (stack.is(HexItems.CHARGED_AMETHYST)) {
            evt.addCapability(MEDIA_STATIC_CAP,
                provide(stack, HexCapabilities.MEDIA, () -> new CapStaticMediaHolder(
                    HexConfig.common()::chargedCrystalMediaAmount, ADMediaHolder.CHARGED_AMETHYST_PRIORITY, stack)));
        } else if (stack.is(HexItems.QUENCHED_SHARD)) {
            // no one uses the config
            evt.addCapability(MEDIA_STATIC_CAP,
                    provide(stack, HexCapabilities.MEDIA, () -> new CapStaticMediaHolder(
                            () -> MediaConstants.QUENCHED_SHARD_UNIT, ADMediaHolder.QUENCHED_SHARD_PRIORITY, stack)));
        } else if (stack.is(HexBlocks.QUENCHED_ALLAY.asItem())) {
            // no one uses the config
            evt.addCapability(MEDIA_STATIC_CAP,
                provide(stack, HexCapabilities.MEDIA, () -> new CapStaticMediaHolder(
                    () -> MediaConstants.QUENCHED_BLOCK_UNIT, ADMediaHolder.QUENCHED_ALLAY_PRIORITY, stack)));
=======
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
>>>>>>> refs/remotes/slava/devel/port-1.21
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

<<<<<<< HEAD
        if (IXplatAbstractions.INSTANCE.isModPresent(HexInterop.Forge.CURIOS_API_ID)
            && stack.getItem() instanceof HexBaubleItem) {
            evt.addCapability(CURIO_CAP, CuriosApiInterop.curioCap(stack));
        }
    }

    public static void attachEntityCaps(AttachCapabilitiesEvent<Entity> evt) {
        var entity = evt.getObject();
        if (entity instanceof ItemEntity item) {
            evt.addCapability(IOTA_STORAGE_CAP, wrapItemEntityDelegate(item,
                ItemDelegatingEntityIotaHolder.ToItemEntity::new));
        } else if (entity instanceof ItemFrame frame) {
            evt.addCapability(IOTA_STORAGE_CAP, wrapItemEntityDelegate(frame,
                ItemDelegatingEntityIotaHolder.ToItemFrame::new));
        } else if (entity instanceof EntityWallScroll scroll) {
            evt.addCapability(IOTA_STORAGE_CAP, wrapItemEntityDelegate(scroll,
                ItemDelegatingEntityIotaHolder.ToWallScroll::new));
        } else if (entity instanceof Player player) {
            evt.addCapability(PATTERN_SPIRAL, provide(player, HexCapabilities.CLIENT_CASTING_STACK,
            () -> new CapClientCastingStack(player, new ClientCastingStack())));
        }
    }

    // i do not know why we need super here
    private static <E extends Entity> SimpleProvider<? super CapEntityIotaHolder.Wrapper, Void> wrapItemEntityDelegate(E entity,
        Function<E, ItemDelegatingEntityIotaHolder> make) {
        return provide(entity, HexCapabilities.ITEM_IOTA,
            () -> new CapEntityIotaHolder.Wrapper(make.apply(entity)));
    }

    private static <CAP, CON> SimpleProvider<CAP, CON> provide(Entity entity, BaseCapability<CAP, CON> capability,
        Supplier<CAP> supplier) {
        return provide(entity::isRemoved, capability, supplier);
    }

    private static <CAP, CON> SimpleProvider<CAP, CON> provide(BlockEntity be, BaseCapability<CAP, CON> capability,
        Supplier<CAP> supplier) {
        return provide(be::isRemoved, capability, supplier);
    }

    public static <CAP, CON> SimpleProvider<CAP, CON> provide(ItemStack stack, BaseCapability<CAP, CON> capability,
        Supplier<CAP> supplier) {
        return provide(stack::isEmpty, capability, supplier);
    }

    private static <CAP, CON> SimpleProvider<CAP, CON> provide(BooleanSupplier invalidated, BaseCapability<CAP, CON> capability,
        Supplier<CAP> supplier) {
        return new SimpleProvider<>(invalidated, capability, Optional.of(supplier));
    }

    public static <CAP, CON> ICapabilityProvider makeProvider(BaseCapability<CAP, CON> cap, CAP instance) {
        Optional<Supplier<CAP>> lazyInstanceButNotReally = Optional.of(() -> instance);
        return new SimpleProvider<>(() -> false, cap, lazyInstanceButNotReally);
    }

    public record SimpleProvider<CAP, CON>(BooleanSupplier invalidated,
                                      BaseCapability<CAP,CON> capability,
                                      Optional<Supplier<CAP>> instance) implements ICapabilityProvider {

        @NotNull
        public Optional getCapability(@NotNull BaseCapability<CAP, CON> cap) {
            if (invalidated.getAsBoolean()) {
                return Optional.empty();
            }

            return cap == capability ? instance : Optional.empty();
        }

        @Override
        public @Nullable Object getCapability(Object object, Object object2) {
            return null;
=======
        for(Block block : BuiltInRegistries.BLOCK) {
            if(block instanceof BlockAbstractImpetus imBlock) {
                evt.registerBlockEntity(
                        Capabilities.ItemHandler.BLOCK,
                        imBlock.getBlockEntityType(),
                        (be, dir) -> new ForgeImpetusCapability(be)
                );
            }
>>>>>>> refs/remotes/slava/devel/port-1.21
        }
    }
}
