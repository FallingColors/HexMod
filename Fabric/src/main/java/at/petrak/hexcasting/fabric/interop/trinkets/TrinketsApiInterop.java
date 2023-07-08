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
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class TrinketsApiInterop {
    public static void init() {
        BuiltInRegistries.ITEM.stream().forEach(item -> {
            if (item instanceof HexBaubleItem bauble) {
                TrinketsApi.registerTrinket(item, new Trinket() {
                    @Override
                    public Multimap<Attribute, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot,
                        LivingEntity entity, UUID uuid) {
                        var map = Trinket.super.getModifiers(stack, slot, entity, uuid);
                        map.putAll(bauble.getHexBaubleAttrs(stack));
                        return map;
                    }
                });
            }
        });


        DiscoveryHandlers.addDebugItemDiscoverer((player, type) -> {
            Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
            if (optional.isPresent()) {
                TrinketComponent component = optional.get();
                var equipped = component.getEquipped(stack -> ItemCreativeUnlocker.isDebug(stack, type));
                if (!equipped.isEmpty()) {
                    return equipped.get(0).getB();
                }
            }
            return ItemStack.EMPTY;
        });
    }

    @Environment(EnvType.CLIENT)
    public static void clientInit() {
        TrinketRendererRegistry.registerRenderer(HexItems.SCRYING_LENS, new LensTrinketRenderer());
    }
}
