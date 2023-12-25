package at.petrak.hexcasting.datagen.recipe.builders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ItemProcessingOutput(ItemStack stack, float chance) implements ProcessingOutput {
    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(stack.getItem());
        json.addProperty("item", resourceLocation.toString());
        int count = stack.getCount();
        if (count != 1) {
            json.addProperty("count", count);
        }
        if (stack.hasTag()) {
            json.add("nbt", JsonParser.parseString(stack.getTag().toString()));
        }
        if (chance != 1) {
            json.addProperty("chance", chance);
        }
        return json;
    }
}
