package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.magic.ItemAncientCypher;
import at.petrak.hexcasting.common.items.magic.ItemPackagedHex;
import at.petrak.hexcasting.common.lib.HexLootFunctions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Add a random preset hex to the ancient cypher, and select a random variant.
 * <p>
 * The function itself is only used on Fabric but the behavior {@link AddHexToAncientCypherFunc#doStatic}
 * is used on both sides
 */
public class AddHexToAncientCypherFunc extends LootItemConditionalFunction {
    public AddHexToAncientCypherFunc(LootItemCondition[] lootItemConditions) {
        super(lootItemConditions);
    }

    /**
     * This doesn't actually have any params so extract behaviour out for the benefit of forge
     */
    public static ItemStack doStatic(ItemStack stack, RandomSource rand) {
        var hex = AncientCypherManager.INSTANCE.randomHex(rand);
        var patsTag = new ListTag();
        for (var pat : hex.getSecond()){
            patsTag.add(IotaType.serialize(pat));
        }
        
        var tag = new CompoundTag();
        tag.putString(ItemAncientCypher.TAG_HEX_NAME, "hexcasting.loot_hex." + hex.getFirst());
        tag.putLong(ItemAncientCypher.TAG_MEDIA, 32*MediaConstants.SHARD_UNIT);
        tag.putLong(ItemAncientCypher.TAG_MAX_MEDIA, 32*MediaConstants.SHARD_UNIT);
        tag.putInt(VariantItem.TAG_VARIANT, rand.nextInt(8));
        tag.put(ItemPackagedHex.TAG_PROGRAM, patsTag);
        stack.getOrCreateTag().merge(tag);

        return stack;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        return doStatic(stack, ctx.getRandom());
    }

    @Override
    public LootItemFunctionType getType() {
        return HexLootFunctions.HEX_CYPHER;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<AddHexToAncientCypherFunc> {
        @Override
        public void serialize(JsonObject json, AddHexToAncientCypherFunc value, JsonSerializationContext ctx) {
            super.serialize(json, value, ctx);
        }

        @Override
        public AddHexToAncientCypherFunc deserialize(JsonObject object, JsonDeserializationContext ctx,
            LootItemCondition[] conditions) {
            return new AddHexToAncientCypherFunc(conditions);
        }
    }
}
