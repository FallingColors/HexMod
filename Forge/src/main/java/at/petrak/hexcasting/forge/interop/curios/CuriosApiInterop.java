package at.petrak.hexcasting.forge.interop.curios;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CuriosApiInterop {

    public static void init() {
        DiscoveryHandlers.addLensPredicate(player -> {
            AtomicBoolean hasLens = new AtomicBoolean(false);
            player.getCapability(CuriosCapability.INVENTORY).ifPresent(handler -> {
                ICurioStacksHandler stacksHandler = handler.getCurios().get("head");
                if(stacksHandler != null) {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        if (stacks.getStackInSlot(i).is(HexItems.SCRYING_LENS)) {
                            hasLens.set(true);
                            break;
                        }
                    }
                }
            });
            return hasLens.get();
        });


        DiscoveryHandlers.addMediaHolderDiscoverer(harness -> {
            List<ADMediaHolder> holders = Lists.newArrayList();
            harness.getCtx().getCaster().getCapability(CuriosCapability.INVENTORY).ifPresent(handler -> {
                for (var stacksHandler : handler.getCurios().values()) {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        var stack = stacks.getStackInSlot(i);
                        if (MediaHelper.isMediaItem(stack)) {
                            var holder = IXplatAbstractions.INSTANCE.findMediaHolder(stack);
                            if (holder != null) {
                                holders.add(holder);
                            }
                        }
                    }
                }
            });

            return holders;
        });

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
        InterModComms.sendTo(HexInterop.Forge.CURIOS_API_ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.HEAD.getMessageBuilder().build());
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {
        CuriosRenderers.register();
    }
}
