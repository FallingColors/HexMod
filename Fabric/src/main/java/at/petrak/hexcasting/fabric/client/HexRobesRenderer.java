package at.petrak.hexcasting.fabric.client;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.items.armor.ItemRobes;
import at.petrak.hexcasting.common.lib.HexItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.world.item.Item;

import java.util.List;

public class HexRobesRenderer {
    public static final List<Item> ROBE_ITEMS =
        List.of(
            HexItems.ROBES_HOOD.get(),
            HexItems.ROBES_TUNIC.get(),
            HexItems.ROBES_LEGS.get(),
            HexItems.ROBES_BOOTS.get()
        );

    public static void init() {
        ArmorRenderer renderer = (matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {

            ItemRobes armor = (ItemRobes) stack.getItem();
            var model = armor.getArmorModels()[armor.getVariant(stack)];
            var texture = armor.getArmorTexture(stack, entity, slot, HexAPI.instance().robesMaterial().layers().getFirst(), false);
            contextModel.copyPropertiesTo(model);

            ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, texture);

        };
        ArmorRenderer.register(renderer, ROBE_ITEMS.toArray(new Item[0]));
    }
}
