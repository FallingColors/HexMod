package at.petrak.hexcasting.forge.interop.curios;

import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.forge.cap.ForgeCapabilityHandler;
import at.petrak.hexcasting.interop.HexInterop;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CuriosApiInterop {
    static class Wrapper implements ICurio {
        private final ItemStack stack;
        private final HexBaubleItem bauble;

        Wrapper(ItemStack stack) {
            this.stack = stack;
            if (stack.getItem() instanceof HexBaubleItem bauble) {
                this.bauble = bauble;
            } else {
                throw new IllegalArgumentException("Item stack " + stack + " wasn't a bauble item weewoo");
            }
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
            var map = ICurio.super.getAttributeModifiers(slotContext, uuid);
            map.putAll(this.bauble.getHexBaubleAttrs(this.stack));
            return map;
        }
    }

    public static ICapabilityProvider curioCap(ItemStack stack) {
        return ForgeCapabilityHandler.makeProvider(CuriosCapability.ITEM, new Wrapper(stack));
    }


    public static void init() {
        DiscoveryHandlers.addDebugItemDiscoverer((player, type) -> {
            AtomicReference<ItemStack> result = new AtomicReference<>(ItemStack.EMPTY);
            player.getCapability(CuriosCapability.INVENTORY).ifPresent(handler -> {
                for (var stacksHandler : handler.getCurios().values()) {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        var stack = stacks.getStackInSlot(i);
                        if (ItemCreativeUnlocker.isDebug(stack, type)) {
                            result.set(stack);
                            return;
                        }
                    }
                }
            });
            return result.get();
        });
    }

    public static void onInterModEnqueue(final InterModEnqueueEvent event) {
        InterModComms.sendTo(HexInterop.Forge.CURIOS_API_ID, SlotTypeMessage.REGISTER_TYPE,
            () -> SlotTypePreset.HEAD.getMessageBuilder().build());
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {
        CuriosRenderers.register();
    }
}
