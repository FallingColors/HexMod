package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.ADColorizer;
import at.petrak.hexcasting.api.addldata.ADHexHolder;
import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.item.ColorizerItem;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.iota.DoubleIota;
import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * Items that work as pigments.
     */
    public static final ResourceLocation PIGMENT_CAP = modLoc("pigment");

    private static final ResourceLocation IMPETUS_HANDLER = modLoc("impetus_items");

    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(ADMediaHolder.class);
        evt.register(ADIotaHolder.class);
        evt.register(ADHexHolder.class);
        evt.register(ADColorizer.class);
    }

    public static void attachItemCaps(AttachCapabilitiesEvent<ItemStack> evt) {
        ItemStack stack = evt.getObject();

        if (stack.getItem() instanceof MediaHolderItem holder) {
            evt.addCapability(MEDIA_STORAGE_CAP,
                provide(stack, HexCapabilities.MEDIA, () -> new ItemBasedMediaHolder(holder, stack)));
        } else if (stack.is(HexItems.AMETHYST_DUST)) {
            evt.addCapability(MEDIA_STATIC_CAP, provide(stack, HexCapabilities.MEDIA, () ->
                new StaticMediaHolder(HexConfig.common()::dustMediaAmount, ADMediaHolder.AMETHYST_DUST_PRIORITY,
                    stack)));
        } else if (stack.is(Items.AMETHYST_SHARD)) {
            evt.addCapability(MEDIA_STATIC_CAP, provide(stack, HexCapabilities.MEDIA, () -> new StaticMediaHolder(
                HexConfig.common()::shardMediaAmount, ADMediaHolder.AMETHYST_SHARD_PRIORITY, stack)));
        } else if (stack.is(HexItems.CHARGED_AMETHYST)) {
            evt.addCapability(MEDIA_STATIC_CAP,
                provide(stack, HexCapabilities.MEDIA, () -> new StaticMediaHolder(
                    HexConfig.common()::chargedCrystalMediaAmount, ADMediaHolder.CHARGED_AMETHYST_PRIORITY, stack)));
        }

        if (stack.getItem() instanceof IotaHolderItem holder) {
            evt.addCapability(IOTA_STORAGE_CAP,
                provide(stack, HexCapabilities.IOTA, () -> new ItemBasedIotaHolder(holder, stack)));
        } else if (stack.is(Items.PUMPKIN_PIE)) {
            // haha yes
            evt.addCapability(IOTA_STATIC_CAP, provide(stack, HexCapabilities.IOTA, () ->
                new StaticIotaHolder((s) -> new DoubleIota(Math.PI * s.getCount()), stack)));
        }

        if (stack.getItem() instanceof HexHolderItem holder) {
            evt.addCapability(HEX_HOLDER_CAP,
                provide(stack, HexCapabilities.STORED_HEX, () -> new ItemBasedHexHolder(holder, stack)));
        }

        if (stack.getItem() instanceof ColorizerItem colorizer) {
            evt.addCapability(PIGMENT_CAP,
                provide(stack, HexCapabilities.COLOR, () -> new ItemBasedColorizer(colorizer, stack)));
        }
    }

    public static void attachEntityCaps(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof ItemEntity item) {
            evt.addCapability(IOTA_STORAGE_CAP, delegateTo(item::getItem)); // Delegate to the item
        } else if (evt.getObject() instanceof ItemFrame frame) {
            evt.addCapability(IOTA_STORAGE_CAP, delegateTo(frame::getItem));
        } else if (evt.getObject() instanceof EntityWallScroll scroll) {
            evt.addCapability(IOTA_STORAGE_CAP, delegateTo(() -> scroll.scroll));
        }
    }

    public static void attachBlockEntityCaps(AttachCapabilitiesEvent<BlockEntity> evt) {
        if (evt.getObject() instanceof BlockEntityAbstractImpetus impetus) {
            evt.addCapability(IMPETUS_HANDLER, provide(impetus, ForgeCapabilities.ITEM_HANDLER,
                () -> new ForgeImpetusCapability(impetus)));
        }
    }

    private static ICapabilityProvider delegateTo(Supplier<ICapabilityProvider> provider) {
        return new ICapabilityProvider() {
            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                var providerInst = provider.get();
                return providerInst == null ? LazyOptional.empty() : providerInst.getCapability(cap, side);
            }
        };
    }

    private static <CAP> SimpleProvider<CAP> provide(Entity entity, Capability<CAP> capability,
                                                     NonNullSupplier<CAP> supplier) {
        return provide(entity::isRemoved, capability, supplier);
    }

    private static <CAP> SimpleProvider<CAP> provide(BlockEntity be, Capability<CAP> capability,
                                                     NonNullSupplier<CAP> supplier) {
        return provide(be::isRemoved, capability, supplier);
    }

    private static <CAP> SimpleProvider<CAP> provide(ItemStack stack, Capability<CAP> capability,
                                                     NonNullSupplier<CAP> supplier) {
        return provide(stack::isEmpty, capability, supplier);
    }

    private static <CAP> SimpleProvider<CAP> provide(BooleanSupplier invalidated, Capability<CAP> capability,
                                                     NonNullSupplier<CAP> supplier) {
        return new SimpleProvider<>(invalidated, capability, LazyOptional.of(supplier));
    }

    private record SimpleProvider<CAP>(BooleanSupplier invalidated,
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

    private record StaticMediaHolder(Supplier<Integer> baseWorth,
                                     int consumptionPriority,
                                     ItemStack stack) implements ADMediaHolder {
        @Override
        public int getMedia() {
            return baseWorth.get() * stack.getCount();
        }

        @Override
        public int getMaxMedia() {
            return getMedia();
        }

        @Override
        public void setMedia(int media) {
            // NO-OP
        }

        @Override
        public boolean canRecharge() {
            return false;
        }

        @Override
        public boolean canProvide() {
            return true;
        }

        @Override
        public int getConsumptionPriority() {
            return consumptionPriority;
        }

        @Override
        public boolean canConstructBattery() {
            return true;
        }

        @Override
        public int withdrawMedia(int cost, boolean simulate) {
            int worth = baseWorth.get();
            if (cost < 0) {
                cost = worth * stack.getCount();
            }
            double itemsRequired = cost / (double) worth;
            int itemsUsed = Math.min((int) Math.ceil(itemsRequired), stack.getCount());
            if (!simulate) {
                stack.shrink(itemsUsed);
            }
            return itemsUsed * worth;
        }
    }

    private record ItemBasedMediaHolder(MediaHolderItem holder,
                                        ItemStack stack) implements ADMediaHolder {

        @Override
        public int getMedia() {
            return holder.getMedia(stack);
        }

        @Override
        public int getMaxMedia() {
            return holder.getMaxMedia(stack);
        }

        @Override
        public void setMedia(int media) {
            holder.setMedia(stack, media);
        }

        @Override
        public boolean canRecharge() {
            return holder.canRecharge(stack);
        }

        @Override
        public boolean canProvide() {
            return holder.canProvideMedia(stack);
        }

        @Override
        public int getConsumptionPriority() {
            return 40;
        }

        @Override
        public boolean canConstructBattery() {
            return false;
        }

        @Override
        public int withdrawMedia(int cost, boolean simulate) {
            return holder.withdrawMedia(stack, cost, simulate);
        }

        @Override
        public int insertMedia(int amount, boolean simulate) {
            return holder.insertMedia(stack, amount, simulate);
        }
    }

    private record StaticIotaHolder(Function<ItemStack, Iota> provider,
                                    ItemStack stack) implements ADIotaHolder {

        @Override
        public @Nullable
        CompoundTag readIotaTag() {
            var iota = provider.apply(stack);
            return iota == null ? null : HexIotaTypes.serialize(iota);
        }

        @Override
        public @Nullable
        Iota readIota(ServerLevel world) {
            return provider.apply(stack);
        }

        @Override
        public boolean writeIota(@Nullable Iota iota, boolean simulate) {
            return false;
        }
    }

    private record ItemBasedIotaHolder(IotaHolderItem holder,
                                       ItemStack stack) implements ADIotaHolder {

        @Override
        public @Nullable
        CompoundTag readIotaTag() {
            return holder.readIotaTag(stack);
        }

        @Override
        public @Nullable
        Iota readIota(ServerLevel world) {
            return holder.readIota(stack, world);
        }

        @Override
        public @Nullable
        Iota emptyIota() {
            return holder.emptyIota(stack);
        }

        @Override
        public boolean writeIota(@Nullable Iota iota, boolean simulate) {
            if (!holder.canWrite(stack, iota)) {
                return false;
            }
            if (!simulate) {
                holder.writeDatum(stack, iota);
            }
            return true;
        }
    }

    private record ItemBasedHexHolder(HexHolderItem holder,
                                      ItemStack stack) implements ADHexHolder {

        @Override
        public boolean canDrawMediaFromInventory() {
            return holder.canDrawMediaFromInventory(stack);
        }

        @Override
        public boolean hasHex() {
            return holder.hasHex(stack);
        }

        @Override
        public @Nullable List<Iota> getHex(ServerLevel level) {
            return holder.getHex(stack, level);
        }

        @Override
        public void writeHex(List<Iota> patterns, int media) {
            holder.writeHex(stack, patterns, media);
        }

        @Override
        public void clearHex() {
            holder.clearHex(stack);
        }
    }

    private record ItemBasedColorizer(ColorizerItem holder,
                                      ItemStack stack) implements ADColorizer {
        @Override
        public int color(UUID owner, float time, Vec3 position) {
            return holder.color(stack, owner, time, position);
        }
    }
}
