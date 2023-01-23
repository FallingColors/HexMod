package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
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

/**
 * Slap a random per-world pattern on the scroll.
 * <p>
 * The function itself is only used on Fabric but the behavior {@link AddPerWorldPatternToScrollFunc#doStatic}
 * is used on both sides
 */
public class AddPerWorldPatternToScrollFunc extends LootItemConditionalFunction {
    public AddPerWorldPatternToScrollFunc(LootItemCondition[] lootItemConditions) {
        super(lootItemConditions);
    }

    /**
     * This doesn't actually have any params so extract behaviour out for the benefit of forge
     */
    public static ItemStack doStatic(ItemStack stack, LootContext ctx) {
        var rand = ctx.getRandom();
        var worldLookup = PatternRegistryManifest.getAllPerWorldActions();

        var keys = worldLookup.stream().toList();
        var key = keys.get(rand.nextInt(keys.size()));

        var pat = PatternRegistryManifest.getCanonicalStrokesPerWorld(key, ctx.getLevel().getServer().overworld());
        var tag = new CompoundTag();
        tag.putString(ItemScroll.TAG_OP_ID, key.location().toString());
        tag.put(ItemScroll.TAG_PATTERN, pat.serializeToNBT());

        stack.getOrCreateTag().merge(tag);

        return stack;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        return doStatic(stack, ctx);
    }

    @Override
    public LootItemFunctionType getType() {
        return HexLootFunctions.PATTERN_SCROLL;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AddPerWorldPatternToScrollFunc> {
        @Override
        public void serialize(JsonObject json, AddPerWorldPatternToScrollFunc value, JsonSerializationContext ctx) {
            super.serialize(json, value, ctx);
        }

        @Override
        public AddPerWorldPatternToScrollFunc deserialize(JsonObject object, JsonDeserializationContext ctx,
            LootItemCondition[] conditions) {
            return new AddPerWorldPatternToScrollFunc(conditions);
        }
    }
}
