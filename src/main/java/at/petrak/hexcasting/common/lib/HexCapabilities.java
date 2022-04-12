package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.HexConfig;
import at.petrak.hexcasting.api.item.IManaReservoir;
import at.petrak.hexcasting.api.item.ManaHolder;
import at.petrak.hexcasting.common.items.HexItems;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HexCapabilities {

    private static final ResourceLocation MANA_HOLDER_CAPABILITY = new ResourceLocation("hexcasting", "mana_holder");
    private static final ResourceLocation MANA_ITEM_CAPABILITY = new ResourceLocation("hexcasting", "mana_item");

    public static final Capability<IManaReservoir> MANA = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent evt) {
        evt.register(IManaReservoir.class);
    }

    @SubscribeEvent
    public static void attachCaps(AttachCapabilitiesEvent<ItemStack> evt) {
        ItemStack stack = evt.getObject();
        if (stack.getItem() instanceof ManaHolder holder)
            evt.addCapability(MANA_HOLDER_CAPABILITY, new SimpleProvider<>(MANA, LazyOptional.of(() -> new ManaHolderReservoir(holder, stack))));
        else if (stack.is(HexItems.AMETHYST_DUST.get()))
            evt.addCapability(MANA_ITEM_CAPABILITY, new SimpleProvider<>(MANA, LazyOptional.of(() -> new ItemReservoir(HexConfig.dustManaAmount, 3, stack))));
        else if (stack.is(Items.AMETHYST_SHARD))
            evt.addCapability(MANA_ITEM_CAPABILITY, new SimpleProvider<>(MANA, LazyOptional.of(() -> new ItemReservoir(HexConfig.shardManaAmount, 2, stack))));
        else if (stack.is(HexItems.CHARGED_AMETHYST.get()))
            evt.addCapability(MANA_ITEM_CAPABILITY, new SimpleProvider<>(MANA, LazyOptional.of(() -> new ItemReservoir(HexConfig.chargedCrystalManaAmount, 1, stack))));
    }

    private record SimpleProvider<CAP>(Capability<CAP> capability, LazyOptional<CAP> instance) implements ICapabilityProvider {

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == capability ? instance.cast() : LazyOptional.empty();
        }
    }

    private record ItemReservoir(ForgeConfigSpec.IntValue baseWorth,
                                 int consumptionPriority,
                                 ItemStack stack) implements IManaReservoir {
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

    private record ManaHolderReservoir(ManaHolder holder,
                                       ItemStack stack) implements IManaReservoir {

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
}
