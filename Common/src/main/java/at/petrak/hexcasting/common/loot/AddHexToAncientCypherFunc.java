package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.items.magic.ItemAncientCypher;
import at.petrak.hexcasting.common.lib.HexLootFunctions;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Registry;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;

/**
 * Add a random preset hex to the ancient cypher, and change the
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
        var fullHex = HexConfig.server().getRandomLootHex(rand.nextInt());
        var patsTag = new ListTag();
        Registry<ActionRegistryEntry> regi = IXplatAbstractions.INSTANCE.getActionRegistry();
        // skip first element since it's the name, not a pattern
        for (var patString : fullHex.subList(1,fullHex.size())){
            var pieces = patString.split(" ");
            var pat = HexPattern.fromAngles(pieces[1],HexDir.fromString(pieces[0]));
            patsTag.add(IotaType.serialize(new PatternIota(pat)));
        }
        
        var tag = new CompoundTag();
        tag.putString(ItemAncientCypher.TAG_HEX_NAME, fullHex.get(0));
        tag.putLong(ItemAncientCypher.TAG_MEDIA, 32*MediaConstants.SHARD_UNIT);
        tag.putLong(ItemAncientCypher.TAG_MAX_MEDIA, 32*MediaConstants.SHARD_UNIT);
        tag.putInt(VariantItem.TAG_VARIANT, rand.nextInt(8));
        tag.put(ItemAncientCypher.TAG_PATTERNS, patsTag);
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
