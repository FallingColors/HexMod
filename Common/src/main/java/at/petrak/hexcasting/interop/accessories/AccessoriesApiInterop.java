package at.petrak.hexcasting.interop.accessories;

import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexItems;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import io.wispforest.accessories.api.slot.SlotReference;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import static io.wispforest.accessories.api.client.AccessoriesRendererRegistry.registerRenderer;

public class AccessoriesApiInterop {
    public static void init() {
        BuiltInRegistries.ITEM.stream().forEach(item -> {
            if (item instanceof HexBaubleItem bauble) {
                AccessoriesAPI.registerAccessory(item, new Accessory() {
                    @Override
                    public void getDynamicModifiers(ItemStack stack, SlotReference slot, AccessoryAttributeBuilder builder) {
                        bauble.getHexBaubleAttrs(stack).forEach(builder::addExclusive);
                    }
                });
            }
        });


        DiscoveryHandlers.addDebugItemDiscoverer((player, type) -> {
            AccessoriesCapability slots = AccessoriesCapability.get(player);
            if (slots != null) {
                var stack2 = slots.getEquipped(stack -> ItemCreativeUnlocker.isDebug(stack, type));
                if (stack2 != null) return stack2.getFirst().stack();
            }
            return ItemStack.EMPTY;
        });

        DiscoveryHandlers.addExtraEquipmentDiscoverer(player -> {
            Optional<AccessoriesCapability> optional = AccessoriesCapability.getOptionally(player);
            if (optional.isPresent()) {
                AccessoriesCapability component = optional.get();
                return component.getEquipped(i -> !i.isEmpty()).stream()
                        .map(SlotEntryReference::stack)
                        .toList();
            }
            return List.of();
        });
    }

    @Environment(EnvType.CLIENT)
    public static void clientInit() {
        registerRenderer(HexItems.SCRYING_LENS, LensAccessoryRenderer::new);
    }
}
