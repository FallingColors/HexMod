package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.Colorizer;
import at.petrak.hexcasting.api.addldata.DataHolder;
import at.petrak.hexcasting.api.addldata.HexHolder;
import at.petrak.hexcasting.api.addldata.ManaHolder;
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.item.ColorizerItem;
import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.item.HexHolderItem;
import at.petrak.hexcasting.api.item.ManaHolderItem;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeCapabilityHandler {
    private static final ResourceLocation DATA_HOLDER_CAPABILITY = new ResourceLocation("hexcasting", "data_holder");
    private static final ResourceLocation DATA_ITEM_CAPABILITY = new ResourceLocation("hexcasting", "data_item");
    private static final ResourceLocation MANA_HOLDER_CAPABILITY = new ResourceLocation("hexcasting", "mana_holder");
    private static final ResourceLocation MANA_ITEM_CAPABILITY = new ResourceLocation("hexcasting", "mana_item");
    private static final ResourceLocation SPELL_HOLDER_CAPABILITY = new ResourceLocation("hexcasting", "spell_item");
    private static final ResourceLocation COLORIZER_CAPABILITY = new ResourceLocation("hexcasting", "colorizer");

    private static final ResourceLocation IMPETUS_HANDLER = new ResourceLocation("hexcasting", "impetus_items");

    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(ManaHolder.class);
        evt.register(DataHolder.class);
        evt.register(HexHolder.class);
        evt.register(Colorizer.class);
    }

    public static void attachItemCaps(AttachCapabilitiesEvent<ItemStack> evt) {
        ItemStack stack = evt.getObject();
        if (stack.getItem() instanceof ManaHolderItem holder)
            evt.addCapability(MANA_HOLDER_CAPABILITY, provide(stack, HexCapabilities.MANA,
                    () -> new ItemBasedManaHolder(holder, stack)));
        else if (stack.is(HexItems.AMETHYST_DUST))
            evt.addCapability(MANA_ITEM_CAPABILITY, provide(stack, HexCapabilities.MANA,
                    () -> new StaticManaHolder(HexConfig.common()::dustManaAmount, 3, stack)));
        else if (stack.is(Items.AMETHYST_SHARD))
            evt.addCapability(MANA_ITEM_CAPABILITY, provide(stack, HexCapabilities.MANA,
                    () -> new StaticManaHolder(HexConfig.common()::shardManaAmount, 2, stack)));
        else if (stack.is(HexItems.CHARGED_AMETHYST))
            evt.addCapability(MANA_ITEM_CAPABILITY, provide(stack, HexCapabilities.MANA,
                    () -> new StaticManaHolder(HexConfig.common()::chargedCrystalManaAmount, 1, stack)));

        if (stack.getItem() instanceof DataHolderItem holder)
            evt.addCapability(DATA_HOLDER_CAPABILITY, provide(stack, HexCapabilities.DATUM,
                    () -> new ItemBasedDataHolder(holder, stack)));
        else if (stack.is(Items.PUMPKIN_PIE)) // haha yes
            evt.addCapability(DATA_ITEM_CAPABILITY, provide(stack, HexCapabilities.DATUM,
                    () -> new StaticDatumHolder((s) -> SpellDatum.make(Math.PI * s.getCount()), stack)));

        if (stack.getItem() instanceof HexHolderItem holder)
            evt.addCapability(SPELL_HOLDER_CAPABILITY, provide(stack, HexCapabilities.STORED_HEX,
                    () -> new ItemBasedHexHolder(holder, stack)));

        if (stack.getItem() instanceof ColorizerItem colorizer)
            evt.addCapability(COLORIZER_CAPABILITY, provide(stack, HexCapabilities.COLOR,
                    () -> new ItemBasedColorizer(colorizer, stack)));
    }

    public static void attachEntityCaps(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof ItemEntity item) {
            evt.addCapability(DATA_HOLDER_CAPABILITY, delegateTo(item::getItem)); // Delegate to the item
        } else if (evt.getObject() instanceof ItemFrame frame) {
            evt.addCapability(DATA_HOLDER_CAPABILITY, delegateTo(frame::getItem));
        } else if (evt.getObject() instanceof EntityWallScroll scroll) {
            evt.addCapability(DATA_HOLDER_CAPABILITY, delegateTo(() -> scroll.scroll));
        }
    }

    public static void attachBlockEntityCaps(AttachCapabilitiesEvent<BlockEntity> evt) {
        if (evt.getObject() instanceof BlockEntityAbstractImpetus impetus)
            evt.addCapability(IMPETUS_HANDLER, provide(impetus, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    () -> new ForgeImpetusCapability(impetus)));
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

    private static <CAP> SimpleProvider<CAP> provide(Entity entity, Capability<CAP> capability, NonNullSupplier<CAP> supplier) {
        return provide(entity::isRemoved, capability, supplier);
    }

    private static <CAP> SimpleProvider<CAP> provide(BlockEntity be, Capability<CAP> capability, NonNullSupplier<CAP> supplier) {
        return provide(be::isRemoved, capability, supplier);
    }

    private static <CAP> SimpleProvider<CAP> provide(ItemStack stack, Capability<CAP> capability, NonNullSupplier<CAP> supplier) {
        return provide(stack::isEmpty, capability, supplier);
    }

    private static <CAP> SimpleProvider<CAP> provide(BooleanSupplier invalidated, Capability<CAP> capability, NonNullSupplier<CAP> supplier) {
        return new SimpleProvider<>(invalidated, capability, LazyOptional.of(supplier));
    }

    private record SimpleProvider<CAP>(BooleanSupplier invalidated,
                                       Capability<CAP> capability,
                                       LazyOptional<CAP> instance) implements ICapabilityProvider {

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (invalidated.getAsBoolean())
                return LazyOptional.empty();

            return cap == capability ? instance.cast() : LazyOptional.empty();
        }
    }

    private record StaticManaHolder(Supplier<Integer> baseWorth,
                                    int consumptionPriority,
                                    ItemStack stack) implements ManaHolder {
        @Override
        public int getMana() {
            return baseWorth.get() * stack.getCount();
        }

        @Override
        public int getMaxMana() {
            return getMana();
        }

        @Override
        public void setMana(int mana) {
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
        public int withdrawMana(int cost, boolean simulate) {
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

    private record ItemBasedManaHolder(ManaHolderItem holder,
                                       ItemStack stack) implements ManaHolder {

        @Override
        public int getMana() {
            return holder.getMana(stack);
        }

        @Override
        public int getMaxMana() {
            return holder.getMaxMana(stack);
        }

        @Override
        public void setMana(int mana) {
            holder.setMana(stack, mana);
        }

        @Override
        public boolean canRecharge() {
            return holder.canRecharge(stack);
        }

        @Override
        public boolean canProvide() {
            return holder.manaProvider(stack);
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
        public int withdrawMana(int cost, boolean simulate) {
            return holder.withdrawMana(stack, cost, simulate);
        }

        @Override
        public int insertMana(int amount, boolean simulate) {
            return holder.insertMana(stack, amount, simulate);
        }
    }

    private record StaticDatumHolder(Function<ItemStack, SpellDatum<?>> provider,
                                     ItemStack stack) implements DataHolder {

        @Override
        public @Nullable
        CompoundTag readRawDatum() {
            SpellDatum<?> datum = provider.apply(stack);
            return datum == null ? null : datum.serializeToNBT();
        }

        @Override
        public @Nullable
        SpellDatum<?> readDatum(ServerLevel world) {
            return provider.apply(stack);
        }

        @Override
        public boolean writeDatum(@Nullable SpellDatum<?> datum, boolean simulate) {
            return false;
        }
    }

    private record ItemBasedDataHolder(DataHolderItem holder,
                                       ItemStack stack) implements DataHolder {

        @Override
        public @Nullable
        CompoundTag readRawDatum() {
            return holder.readDatumTag(stack);
        }

        @Override
        public @Nullable
        SpellDatum<?> readDatum(ServerLevel world) {
            return holder.readDatum(stack, world);
        }

        @Override
        public @Nullable
        SpellDatum<?> emptyDatum() {
            return holder.emptyDatum(stack);
        }

        @Override
        public boolean writeDatum(@Nullable SpellDatum<?> datum, boolean simulate) {
            if (!holder.canWrite(stack, datum)) {
                return false;
            }
            if (!simulate) {
                holder.writeDatum(stack, datum);
            }
            return true;
        }
    }

    private record ItemBasedHexHolder(HexHolderItem holder,
                                      ItemStack stack) implements HexHolder {

        @Override
        public boolean canDrawManaFromInventory() {
            return holder.canDrawManaFromInventory(stack);
        }

        @Override
        public boolean hasHex() {
            return holder.hasHex(stack);
        }

        @Override
        public @Nullable List<SpellDatum<?>> getHex(ServerLevel level) {
            return holder.getHex(stack, level);
        }

        @Override
        public void writeHex(List<SpellDatum<?>> patterns, int mana) {
            holder.writeHex(stack, patterns, mana);
        }

        @Override
        public void clearHex() {
            holder.clearHex(stack);
        }
    }

    private record ItemBasedColorizer(ColorizerItem holder,
                                      ItemStack stack) implements Colorizer {
        @Override
        public int color(UUID owner, float time, Vec3 position) {
            return holder.color(stack, owner, time, position);
        }
    }
}
