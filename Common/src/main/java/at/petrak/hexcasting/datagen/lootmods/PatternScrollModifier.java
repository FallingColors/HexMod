package at.petrak.hexcasting.datagen.lootmods;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.api.spell.math.HexPattern;
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

        var worldLookup = PatternRegistry.getPerWorldPatterns(context.getLevel());
        var allOpIds = worldLookup.keySet().stream().toList();
        // Don't generate more than one of the same pattern
        var seenPats = new HashSet<String>();
        var scrollAmt = rand.nextGaussian(0.0, this.countStddev);
        for (int i = 0; i < scrollAmt; i++) {
            if (seenPats.size() == allOpIds.size()) {
                break;
            }
            String pattern;
            do {
                pattern = allOpIds.get(rand.nextInt(allOpIds.size()));
            } while (seenPats.contains(pattern));

            seenPats.add(pattern);

            var entry = worldLookup.get(pattern);
            var opId = entry.component1();
            var startDir = entry.component2();
            var tag = new CompoundTag();
            tag.putString(ItemScroll.TAG_OP_ID, opId.toString());
            tag.put(ItemScroll.TAG_PATTERN,
                HexPattern.FromAnglesSig(pattern, startDir).serializeToNBT());

            var stack = new ItemStack(HexItems.SCROLL.get());
            stack.setTag(tag);
            generatedLoot.add(stack);

            seenPats.add(pattern);
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
