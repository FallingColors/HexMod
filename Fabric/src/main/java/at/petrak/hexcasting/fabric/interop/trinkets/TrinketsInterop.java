package at.petrak.hexcasting.fabric.interop.trinkets;

import at.petrak.hexcasting.api.misc.DiscoveryHandlers;
import at.petrak.hexcasting.common.items.HexBaubleItem;
import at.petrak.hexcasting.common.items.magic.ItemCreativeUnlocker;
import at.petrak.hexcasting.common.lib.HexItems;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import static dev.emi.trinkets.api.client.TrinketRendererRegistry.registerRenderer;

public class TrinketsInterop {
    public static void init() {
        BuiltInRegistries.ITEM.stream().forEach(item -> {
            if (item instanceof HexBaubleItem bauble) {
                TrinketsApi.registerTrinket(item, new Trinket() {
                    @Override
                    public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, ResourceLocation slotIdentifier) {
                        return bauble.getHexBaubleAttrs(stack);
                    }
                });
            }
        });


        DiscoveryHandlers.addDebugItemDiscoverer((player, type) -> {
            Optional<TrinketComponent> slots = TrinketsApi.getTrinketComponent(player);
            if (slots.isPresent()) {
                var stack2 = slots.get().getEquipped(stack -> ItemCreativeUnlocker.isDebug(stack, type));
                if (stack2 != null) return stack2.getFirst().getB();
            }
            return ItemStack.EMPTY;
        });

        DiscoveryHandlers.addExtraEquipmentDiscoverer(player -> {
            Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
            if (optional.isPresent()) {
                TrinketComponent component = optional.get();
                return component.getEquipped(i -> !i.isEmpty()).stream()
                        .map(Tuple::getB)
                        .toList();
            }
            return List.of();
        });
    }

    @Environment(EnvType.CLIENT)
    public static void clientInit() {
        registerRenderer(HexItems.SCRYING_LENS, new LensTrinketRenderer());
    }
}
