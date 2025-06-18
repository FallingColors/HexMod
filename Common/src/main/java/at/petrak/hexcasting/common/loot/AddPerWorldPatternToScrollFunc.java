package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexLootFunctions;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Slap a random per-world pattern on the scroll.
 * <p>
 * The function itself is only used on Fabric but the behavior {@link AddPerWorldPatternToScrollFunc#doStatic}
 * is used on both sides
 */
public class AddPerWorldPatternToScrollFunc extends LootItemConditionalFunction {
    public static final MapCodec<AddPerWorldPatternToScrollFunc> CODEC = RecordCodecBuilder.mapCodec(
            p_344674_ -> commonFields(p_344674_)
                    .apply(p_344674_, AddPerWorldPatternToScrollFunc::new)
    );

    public AddPerWorldPatternToScrollFunc(List<LootItemCondition> lootItemConditions) {
        super(lootItemConditions);
    }

    /**
     * This doesn't actually have any params so extract behaviour out for the benefit of forge
     */
    public static ItemStack doStatic(ItemStack stack, RandomSource rand, ServerLevel overworld) {
        var perWorldKeys = new ArrayList<ResourceKey<ActionRegistryEntry>>();
        Registry<ActionRegistryEntry> regi = IXplatAbstractions.INSTANCE.getActionRegistry();
        for (var key : regi.registryKeySet()) {
            if (HexUtils.isOfTag(regi, key, HexTags.Actions.PER_WORLD_PATTERN)) {
                perWorldKeys.add(key);
            }
        }
        var patternKey = perWorldKeys.get(rand.nextInt(perWorldKeys.size()));
        var pat = PatternRegistryManifest.getCanonicalStrokesPerWorld(patternKey, overworld);
        stack.set(HexDataComponents.ACTION, patternKey);
        stack.set(HexDataComponents.PATTERN, pat);
        return stack;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        return doStatic(stack, ctx.getRandom(), ctx.getLevel().getServer().overworld());
    }

    @Override
    public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return HexLootFunctions.PATTERN_SCROLL;
    }
}
