package at.petrak.hex.datagen.lootmods;

import at.petrak.hex.api.PatternRegistry;
import at.petrak.hex.common.items.HexItems;
import at.petrak.hex.common.items.ItemScroll;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

public class PatternScrollModifier extends LootModifier {
    private final double countStddev;

    public PatternScrollModifier(LootItemCondition[] conditions, double countStddev) {
        super(conditions);
        this.countStddev = countStddev;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        var rand = context.getRandom();

        var lookup = PatternRegistry.getPerWorldPatterns(context.getLevel());
        var allOpIds = lookup.values().stream().toList();
        // Don't generate more than one of the same pattern
        var seenPats = new HashSet<ResourceLocation>();
        var scrollAmt = rand.nextGaussian(0.0, this.countStddev);
        for (int i = 0; i < scrollAmt; i++) {
            ResourceLocation opId;
            do {
                opId = allOpIds.get(rand.nextInt(allOpIds.size()));
            } while (seenPats.contains(opId));

            seenPats.add(opId);

            var tag = new CompoundTag();
            tag.putString(ItemScroll.TAG_OP_ID, opId.toString());
            var stack = new ItemStack(HexItems.SCROLL.get());
            stack.setTag(tag);
            generatedLoot.add(stack);

            seenPats.add(opId);
        }

        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<PatternScrollModifier> {

        @Override
        public PatternScrollModifier read(ResourceLocation location, JsonObject json,
                LootItemCondition[] conditions) {
            var stddev = GsonHelper.getAsFloat(json, "stddev");
            return new PatternScrollModifier(conditions, stddev);
        }

        @Override
        public JsonObject write(PatternScrollModifier instance) {
            var obj = this.makeConditions(instance.conditions);
            obj.addProperty("stddev", instance.countStddev);
            return obj;
        }
    }
}
