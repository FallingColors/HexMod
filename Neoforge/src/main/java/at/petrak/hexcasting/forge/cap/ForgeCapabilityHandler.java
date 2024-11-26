package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.*;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.client.ClientCastingStack;
import at.petrak.hexcasting.api.item.*;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.forge.cap.adimpl.*;
import at.petrak.hexcasting.forge.interop.curios.CuriosApiInterop;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeCapabilityHandler {
    /**
     * Items that store an iota to their tag.
     */
    public static final ResourceLocation IOTA_STORAGE_CAP = modLoc("iota_holder");
    /**
     * Items that intrinsically store an iota.
     */
    public static final ResourceLocation IOTA_STATIC_CAP = modLoc("iota_item");
    /**
     * Items that store a variable amount of media to their tag.
     */
    public static final ResourceLocation MEDIA_STORAGE_CAP = modLoc("media_holder");
    /**
     * Items that statically provide media.
     */
    public static final ResourceLocation MEDIA_STATIC_CAP = modLoc("media_item");
    /**
     * Items that store a packaged Hex.
     */
    public static final ResourceLocation HEX_HOLDER_CAP = modLoc("hex_item");
    /**
     * Items that have multiple visual variants.
     */
    public static final ResourceLocation VARIANT_ITEM_CAP = modLoc("variant_item");
    /**
     * Items that work as pigments.
     */
    public static final ResourceLocation PIGMENT_CAP = modLoc("pigment");
    public static final ResourceLocation CURIO_CAP = modLoc("curio");

    public static final ResourceLocation IMPETUS_HANDLER = modLoc("impetus_items");

    /**
     * Used to render the pattern spiral around players while casting.
     */
    public static final ResourceLocation PATTERN_SPIRAL = modLoc("pattern_spiral");

    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(ADMediaHolder.class);
        evt.register(ADIotaHolder.class);
        evt.register(ADHexHolder.class);
        evt.register(ADPigment.class);
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
        }

        if (stack.getItem() instanceof IotaHolderItem holder) {
            evt.addCapability(IOTA_STORAGE_CAP,
                provide(stack, HexCapabilities.IOTA, () -> new CapItemIotaHolder(holder, stack)));
        } else if (stack.is(Items.PUMPKIN_PIE)) {
            // haha yes
            evt.addCapability(IOTA_STATIC_CAP, provide(stack, HexCapabilities.IOTA, () ->
                new CapStaticIotaHolder((s) -> new DoubleIota(Math.PI * s.getCount()), stack)));
        }

        if (stack.getItem() instanceof HexHolderItem holder) {
            evt.addCapability(HEX_HOLDER_CAP,
                provide(stack, HexCapabilities.STORED_HEX, () -> new CapItemHexHolder(holder, stack)));
        }
        if (stack.getItem() instanceof VariantItem variantItem) {
            evt.addCapability(VARIANT_ITEM_CAP,
                    provide(stack, HexCapabilities.VARIANT_ITEM, () -> new CapItemVariantItem(variantItem, stack)));
        }

        if (stack.getItem() instanceof PigmentItem pigment) {
            evt.addCapability(PIGMENT_CAP,
                provide(stack, HexCapabilities.COLOR, () -> new CapItemPigment(pigment, stack)));
        }

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

    public static void attachBlockEntityCaps(AttachCapabilitiesEvent<BlockEntity> evt) {
        if (evt.getObject() instanceof BlockEntityAbstractImpetus impetus) {
            evt.addCapability(IMPETUS_HANDLER, provide(impetus, ForgeCapabilities.ITEM_HANDLER,
                () -> new ForgeImpetusCapability(impetus)));
        }
    }

    // i do not know why we need super here
    private static <E extends Entity> SimpleProvider<? super CapEntityIotaHolder.Wrapper> wrapItemEntityDelegate(E entity,
        Function<E, ItemDelegatingEntityIotaHolder> make) {
        return provide(entity, HexCapabilities.IOTA,
            () -> new CapEntityIotaHolder.Wrapper(make.apply(entity)));
    }

    private static <CAP> SimpleProvider<CAP> provide(Entity entity, Capability<CAP> capability,
        NonNullSupplier<CAP> supplier) {
        return provide(entity::isRemoved, capability, supplier);
    }

    private static <CAP> SimpleProvider<CAP> provide(BlockEntity be, Capability<CAP> capability,
        NonNullSupplier<CAP> supplier) {
        return provide(be::isRemoved, capability, supplier);
    }

    public static <CAP> SimpleProvider<CAP> provide(ItemStack stack, Capability<CAP> capability,
        NonNullSupplier<CAP> supplier) {
        return provide(stack::isEmpty, capability, supplier);
    }

    private static <CAP> SimpleProvider<CAP> provide(BooleanSupplier invalidated, Capability<CAP> capability,
        NonNullSupplier<CAP> supplier) {
        return new SimpleProvider<>(invalidated, capability, LazyOptional.of(supplier));
    }

    public static <T, U extends T> ICapabilityProvider makeProvider(Capability<T> cap, U instance) {
        LazyOptional<T> lazyInstanceButNotReally = LazyOptional.of(() -> instance);
        return new SimpleProvider<>(() -> false, cap, lazyInstanceButNotReally);
    }

    public record SimpleProvider<CAP>(BooleanSupplier invalidated,
                                      Capability<CAP> capability,
                                      LazyOptional<CAP> instance) implements ICapabilityProvider {

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (invalidated.getAsBoolean()) {
                return LazyOptional.empty();
            }

            return cap == capability ? instance.cast() : LazyOptional.empty();
        }
    }

}
