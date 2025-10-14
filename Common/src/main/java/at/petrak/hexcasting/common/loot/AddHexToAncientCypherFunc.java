package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.magic.ItemAncientCypher;
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

import java.util.List;
import com.mojang.datafixers.util.Pair;

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
        var hex = LOOT_HEXES.get(rand.nextInt(LOOT_HEXES.size()));
        var patsTag = new ListTag();
        for (var patString : hex.getSecond()){
            var pieces = patString.split(" ");
            var pat = HexPattern.fromAnglesUnchecked(pieces[1],HexDir.fromString(pieces[0]));
            patsTag.add(IotaType.serialize(new PatternIota(pat)));
        }
        
        var tag = new CompoundTag();
        tag.putString(ItemAncientCypher.TAG_HEX_NAME, hex.getFirst());
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

    // TODO: make this datapackable
    private static final List<Pair<String, String[]>> LOOT_HEXES = List.of(
        new Pair<>("hexcasting.loot_hex.shatter", new String[] {"NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST wqaawdd","EAST qaqqqqq"}),
        new Pair<>("hexcasting.loot_hex.kindle", new String[] {"NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST wqaawdd","SOUTH_EAST aaqawawa"}),
        new Pair<>("hexcasting.loot_hex.illuminate", new String[] {"NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST aadadaaw","EAST wqaawdd","NORTH_EAST ddqdd","EAST weddwaa","NORTH_EAST waaw","NORTH_EAST qqd"}),
        new Pair<>("hexcasting.loot_hex.growth", new String[] {"NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST aadadaaw","EAST wqaawdd","NORTH_EAST ddqdd","EAST weddwaa","NORTH_EAST waaw","SOUTH_EAST aqaaedwd","EAST aadaadaa","NORTH_EAST wqaqwawqaqw","NORTH_EAST wqaqwawqaqw","NORTH_EAST wqaqwawqaqw"}),
        new Pair<>("hexcasting.loot_hex.lunge", new String[] {"NORTH_EAST qaq","EAST aadaa","NORTH_EAST wa","SOUTH_EAST aqaawa","SOUTH_EAST waqaw","SOUTH_WEST awqqqwaqw"}),
        new Pair<>("hexcasting.loot_hex.sidestep", new String[] {"NORTH_EAST qaq","EAST aadaa","NORTH_EAST wa","NORTH_WEST eqqq","SOUTH_EAST aqaawd","SOUTH_EAST e","NORTH_WEST qqqqqew","SOUTH_WEST eeeeeqw","SOUTH_EAST awdd","NORTH_EAST wdedw","SOUTH_WEST awqqqwaqw"}),
        new Pair<>("hexcasting.loot_hex.ascend", new String[] {"NORTH_EAST qaq","SOUTH_EAST aqaae","WEST qqqqqawwawawd"}),
        new Pair<>("hexcasting.loot_hex.blink", new String[] {"NORTH_EAST qaq","EAST aadaa","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST wqaawdd","NORTH_EAST qaq","EAST aa","NORTH_WEST wddw","NORTH_EAST wqaqw","SOUTH_EAST aqaaw","NORTH_WEST wddw","SOUTH_WEST awqqqwaq"}),
        new Pair<>("hexcasting.loot_hex.blastoff", new String[] {"NORTH_EAST qaq","NORTH_WEST qqqqqew","SOUTH_EAST aqaawaa","SOUTH_EAST waqaw","SOUTH_WEST awqqqwaqw"}),
        new Pair<>("hexcasting.loot_hex.radar", new String[] {"WEST qqq","EAST aadaa","EAST aa","SOUTH_EAST aqaawa","SOUTH_WEST ewdqdwe","NORTH_EAST de","EAST eee","NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaaeaqq","SOUTH_EAST qqqqqwdeddwd","NORTH_EAST dadad"}),
        new Pair<>("hexcasting.loot_hex.beckon", new String[] {"NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST weaqa","EAST aadaa","EAST dd","NORTH_EAST qaq","EAST aa","EAST aawdd","NORTH_WEST wddw","EAST aadaa","NORTH_EAST wqaqw","NORTH_EAST wdedw","SOUTH_EAST aqaawa","SOUTH_EAST waqaw","SOUTH_WEST awqqqwaqw"}),
        new Pair<>("hexcasting.loot_hex.detonate", new String[] {"NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaaedwd","EAST ddwddwdd"}),
        new Pair<>("hexcasting.loot_hex.shockwave", new String[] {"NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaawaa","EAST aadaadaa","SOUTH_EAST aqawqadaq","SOUTH_EAST aqaaedwd","EAST aawaawaa","NORTH_EAST qqa","EAST qaqqqqq"}),
        new Pair<>("hexcasting.loot_hex.heat_wave", new String[] {"WEST qqq","SOUTH_EAST aaqawawa","EAST eee","NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaae","SOUTH_EAST qqqqqwded","SOUTH_WEST aaqwqaa","SOUTH_EAST a","NORTH_EAST dadad"}),
        new Pair<>("hexcasting.loot_hex.wither_wave", new String[] {"WEST qqq","SOUTH_EAST aqaae","SOUTH_EAST aqaaw","SOUTH_WEST qqqqqaewawawe","EAST eee","NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaae","SOUTH_EAST qqqqqwdeddwd","SOUTH_WEST aaqwqaa","SOUTH_EAST a","NORTH_EAST dadad"}),
        new Pair<>("hexcasting.loot_hex.flight_zone", new String[] {"NORTH_EAST qaq","SOUTH_EAST aqaaq","SOUTH_WEST awawaawq"})
    );
}
