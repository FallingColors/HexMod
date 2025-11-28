package at.petrak.hexcasting.forge.interop.curios;

import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

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
        public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id) {
            var map = ICurio.super.getAttributeModifiers(slotContext, id);
            map.putAll(this.bauble.getHexBaubleAttrs(this.stack));
            return map;
        }
    }


    public static void init() {
        DiscoveryHandlers.addDebugItemDiscoverer((player, type) -> {
            var inv = player.getCapability(CuriosCapability.INVENTORY);

            if(inv != null) {
                for (var stacksHandler : inv.getCurios().values()) {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        var stack = stacks.getStackInSlot(i);
                        if (ItemCreativeUnlocker.isDebug(stack, type)) {
                            return stack;
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        });
    }

    public static void registerCap(RegisterCapabilitiesEvent evt, Item item) {
        evt.registerItem(CuriosCapability.ITEM, (stack, ctx) -> new Wrapper(stack), item);
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {
        CuriosRenderers.register();
    }
}
