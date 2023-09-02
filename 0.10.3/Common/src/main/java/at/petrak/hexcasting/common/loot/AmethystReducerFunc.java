package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.common.lib.HexLootFunctions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AmethystReducerFunc extends LootItemConditionalFunction {
    public final double delta;

    public AmethystReducerFunc(LootItemCondition[] lootItemConditions, double delta) {
        super(lootItemConditions);
        this.delta = delta;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        if (stack.is(Items.AMETHYST_SHARD)) {
            stack.setCount((int) (stack.getCount() * (1 + delta)));
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return HexLootFunctions.AMETHYST_SHARD_REDUCER;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AmethystReducerFunc> {
        @Override
        public void serialize(JsonObject json, AmethystReducerFunc value, JsonSerializationContext ctx) {
            super.serialize(json, value, ctx);
            json.addProperty("delta", value.delta);
        }

        @Override
        public AmethystReducerFunc deserialize(JsonObject object, JsonDeserializationContext ctx,
            LootItemCondition[] conditions) {
            var delta = GsonHelper.getAsDouble(object, "delta");
            return new AmethystReducerFunc(conditions, delta);
        }
    }
}
