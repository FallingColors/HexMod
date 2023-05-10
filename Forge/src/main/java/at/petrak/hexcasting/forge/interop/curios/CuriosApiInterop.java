package at.petrak.hexcasting.forge.interop.curios;

import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexAttributes;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.interop.HexInterop;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

import java.util.concurrent.atomic.AtomicReference;

public class CuriosApiInterop {

    public static void init() {

        addAttr(HexItems.SCRYING_LENS, SlotTypePreset.HEAD.getIdentifier(),
            HexAttributes.GRID_ZOOM, ItemLens.GRID_ZOOM);
        addAttr(HexItems.SCRYING_LENS, SlotTypePreset.HEAD.getIdentifier(),
            HexAttributes.SCRY_SIGHT, ItemLens.SCRY_SIGHT);

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

    private static void addAttr(Item item, String slot, Attribute attr, AttributeModifier mod) {
        CuriosApi.getCuriosHelper().addModifier(new ItemStack(item), attr,
            mod.getName(), mod.getId(), mod.getAmount(), mod.getOperation(), slot);
    }
}
