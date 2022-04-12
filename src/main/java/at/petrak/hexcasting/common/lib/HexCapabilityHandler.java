package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.cap.Colorizer;
import at.petrak.hexcasting.api.cap.DataHolder;
import at.petrak.hexcasting.api.cap.ManaHolder;
import at.petrak.hexcasting.api.cap.SpellHolder;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.item.*;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.cap.HexCapabilities;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class HexCapabilityHandler {

    private static final ResourceLocation DATA_HOLDER_CAPABILITY = new ResourceLocation("hexcasting", "data_holder");
    private static final ResourceLocation DATA_ITEM_CAPABILITY = new ResourceLocation("hexcasting", "data_item");
    private static final ResourceLocation MANA_HOLDER_CAPABILITY = new ResourceLocation("hexcasting", "mana_holder");
    private static final ResourceLocation MANA_ITEM_CAPABILITY = new ResourceLocation("hexcasting", "mana_item");
    private static final ResourceLocation SPELL_HOLDER_CAPABILITY = new ResourceLocation("hexcasting", "spell_item");
    private static final ResourceLocation COLORIZER_CAPABILITY = new ResourceLocation("hexcasting", "colorizer");

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(ManaHolder.class);
        evt.register(DataHolder.class);
        evt.register(SpellHolder.class);
        evt.register(Colorizer.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<ItemStack> evt) {
        ItemStack stack = evt.getObject();
        if (stack.getItem() instanceof ManaHolderItem holder)
            evt.addCapability(MANA_HOLDER_CAPABILITY, provide(HexCapabilities.MANA,
                    () -> new ItemBasedManaHolder(holder, stack)));
        else if (stack.is(HexItems.AMETHYST_DUST.get()))
            evt.addCapability(MANA_ITEM_CAPABILITY, provide(HexCapabilities.MANA,
                    () -> new StaticManaHolder(HexConfig.dustManaAmount, 3, stack)));
        else if (stack.is(Items.AMETHYST_SHARD))
            evt.addCapability(MANA_ITEM_CAPABILITY, provide(HexCapabilities.MANA,
                    () -> new StaticManaHolder(HexConfig.shardManaAmount, 2, stack)));
        else if (stack.is(HexItems.CHARGED_AMETHYST.get()))
            evt.addCapability(MANA_ITEM_CAPABILITY, provide(HexCapabilities.MANA,
                    () -> new StaticManaHolder(HexConfig.chargedCrystalManaAmount, 1, stack)));

        if (stack.getItem() instanceof DataHolderItem holder)
            evt.addCapability(DATA_HOLDER_CAPABILITY, provide(HexCapabilities.DATUM,
                    () -> new ItemBasedDataHolder(holder, stack)));
        else if (stack.is(Items.PUMPKIN_PIE)) // haha yes
            evt.addCapability(DATA_ITEM_CAPABILITY, provide(HexCapabilities.DATUM,
                    () -> new StaticDatumHolder((s) -> SpellDatum.make(Math.PI * s.getCount()), stack)));

        if (stack.getItem() instanceof SpellHolderItem holder)
            evt.addCapability(SPELL_HOLDER_CAPABILITY, provide(HexCapabilities.SPELL,
                    () -> new ItemBasedSpellHolder(holder, stack)));

        if (stack.getItem() instanceof ColorizerItem colorizer)
            evt.addCapability(COLORIZER_CAPABILITY, provide(HexCapabilities.COLOR,
                    () -> new ItemBasedColorizer(colorizer, stack)));
    }

    private static <CAP> SimpleProvider<CAP> provide(Capability<CAP> capability, NonNullSupplier<CAP> supplier) {
        return new SimpleProvider<>(capability, LazyOptional.of(supplier));
    }

    private record SimpleProvider<CAP>(Capability<CAP> capability,
                                       LazyOptional<CAP> instance) implements ICapabilityProvider {

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == capability ? instance.cast() : LazyOptional.empty();
        }
    }

    private record StaticManaHolder(ForgeConfigSpec.IntValue baseWorth,
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
            if (cost < 0)
                cost = worth * stack.getCount();
            double itemsRequired = cost / (double) worth;
            int itemsUsed = Math.min((int) Math.ceil(itemsRequired), stack.getCount());
            if (!simulate)
                stack.shrink(itemsUsed);
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
            return true;
        }

        @Override
        public boolean canProvide() {
            return holder.manaProvider(stack);
        }

        @Override
        public int getConsumptionPriority() {
            return 4;
        }

        @Override
        public boolean canConstructBattery() {
            return false;
        }

        @Override
        public int withdrawMana(int cost, boolean simulate) {
            return holder.withdrawMana(stack, cost, simulate);
        }
    }

    private record StaticDatumHolder(Function<ItemStack, SpellDatum<?>> provider,
                                        ItemStack stack) implements DataHolder {

        @Override
        public @Nullable CompoundTag readRawDatum() {
            SpellDatum<?> datum = provider.apply(stack);
            return datum == null ? null : datum.serializeToNBT();
        }

        @Override
        public @Nullable SpellDatum<?> readDatum(ServerLevel world) {
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
        public @Nullable CompoundTag readRawDatum() {
            return holder.readDatumTag(stack);
        }

        @Override
        public @Nullable SpellDatum<?> readDatum(ServerLevel world) {
            return holder.readDatum(stack, world);
        }

        @Override
        public @Nullable SpellDatum<?> emptyDatum() {
            return holder.emptyDatum(stack);
        }

        @Override
        public boolean writeDatum(@Nullable SpellDatum<?> datum, boolean simulate) {
            if (!holder.canWrite(stack, datum))
                return false;
            if (!simulate)
                holder.writeDatum(stack, datum);
            return true;
        }
    }

    private record ItemBasedSpellHolder(SpellHolderItem holder,
                                        ItemStack stack) implements SpellHolder {

        @Override
        public boolean canDrawManaFromInventory() {
            return holder.canDrawManaFromInventory(stack);
        }

        @Override
        public @Nullable List<HexPattern> getPatterns() {
            return holder.getPatterns(stack);
        }

        @Override
        public void writePatterns(List<HexPattern> patterns, int mana) {
            holder.writePatterns(stack, patterns, mana);
        }

        @Override
        public void clearPatterns() {
            holder.clearPatterns(stack);
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
