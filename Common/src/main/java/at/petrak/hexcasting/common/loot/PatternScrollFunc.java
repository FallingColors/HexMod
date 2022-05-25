package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.lib.HexLootFunctions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class PatternScrollFunc extends LootItemConditionalFunction {
    public PatternScrollFunc(LootItemCondition[] lootItemConditions) {
        super(lootItemConditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        var rand = ctx.getRandom();
        var worldLookup = PatternRegistry.getPerWorldPatterns(ctx.getLevel());

        var keys = worldLookup.keySet().stream().toList();
        var sig = keys.get(rand.nextInt(keys.size()));

        var entry = worldLookup.get(sig);
        var opId = entry.component1();
        var startDir = entry.component2();
        var tag = new CompoundTag();
        tag.putString(ItemScroll.TAG_OP_ID, opId.toString());
        tag.put(ItemScroll.TAG_PATTERN, HexPattern.fromAngles(sig, startDir).serializeToNBT());

        stack.getOrCreateTag().merge(tag);

        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return HexLootFunctions.PATTERN_SCROLL;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<PatternScrollFunc> {
        @Override
        public void serialize(JsonObject json, PatternScrollFunc value, JsonSerializationContext ctx) {
            super.serialize(json, value, ctx);
        }

        @Override
        public PatternScrollFunc deserialize(JsonObject object, JsonDeserializationContext ctx,
            LootItemCondition[] conditions) {
            return new PatternScrollFunc(conditions);
        }
    }
}
