package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexLootFunctions;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;

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
    public static ItemStack doStatic(ItemStack stack, RandomSource rand, ServerLevel overworld) {
        ResourceKey patternKey;
        if (NBTHelper.hasString(stack, ItemScroll.TAG_OP_ID)) {
            patternKey = ResourceKey.create(
                IXplatAbstractions.INSTANCE.getActionRegistry().key(), 
                ResourceLocation.tryParse(NBTHelper.getString(stack, ItemScroll.TAG_OP_ID))
            );
        } else {
            var perWorldKeys = new ArrayList<ResourceKey<ActionRegistryEntry>>();
            Registry<ActionRegistryEntry> regi = IXplatAbstractions.INSTANCE.getActionRegistry();
            for (var key : regi.registryKeySet()) {
                if (HexUtils.isOfTag(regi, key, HexTags.Actions.PER_WORLD_PATTERN)) {
                    perWorldKeys.add(key);
                }
            }
            patternKey = perWorldKeys.get(rand.nextInt(perWorldKeys.size()));
            NBTHelper.putString(stack, ItemScroll.TAG_OP_ID, patternKey.location().toString());
        }
        var pat = PatternRegistryManifest.getCanonicalStrokesPerWorld(patternKey, overworld);
        NBTHelper.put(stack, ItemScroll.TAG_PATTERN, pat.serializeToNBT());
        return stack;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        return doStatic(stack, ctx.getRandom(), ctx.getLevel().getServer().overworld());
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
