package at.petrak.hexcasting.forge.interop.curios;

import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.interop.HexInterop;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

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
        public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
            var map = ICurio.super.getAttributeModifiers(slotContext, uuid);
            for (var entry : this.bauble.getHexBaubleAttrs(this.stack).entries()) {
                var key = BuiltInRegistries.ATTRIBUTE.getResourceKey(entry.getKey());
                key.ifPresent(k -> map.put(BuiltInRegistries.ATTRIBUTE.getHolderOrThrow(k), entry.getValue()));
            }
            return map;
        }
    }

    public static void registerCurioCapability(RegisterCapabilitiesEvent evt) {
        evt.registerItem(CuriosCapability.ITEM, (stack, ctx) ->
            stack.getItem() instanceof HexBaubleItem ? new Wrapper(stack) : null,
            HexItems.SCRYING_LENS);
    }

    public static void init() {
        DiscoveryHandlers.addDebugItemDiscoverer((player, type) -> {
            AtomicReference<ItemStack> result = new AtomicReference<>(ItemStack.EMPTY);
            var handler = player.getCapability(CuriosCapability.INVENTORY);
            if (handler != null) {
                search:
                for (var stacksHandler : handler.getCurios().values()) {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        var stack = stacks.getStackInSlot(i);
                        if (ItemCreativeUnlocker.isDebug(stack, type)) {
                            result.set(stack);
                            break search;
                        }
                    }
                }
            }
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
