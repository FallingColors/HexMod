package at.petrak.hexcasting.datagen.lootmods;

import at.petrak.hexcasting.common.items.HexItems;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class AmethystClusterModifier extends LootModifier {
    private final float chargedChance;

    public AmethystClusterModifier(LootItemCondition[] conditions, float chargedChance) {
        super(conditions);
        this.chargedChance = chargedChance;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        var rand = context.getRandom();
        var tool = context.getParamOrNull(LootContextParams.TOOL);
        var fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);

        var hasCharged = (rand.nextFloat() * (fortuneLevel / 2f + 1)) > this.chargedChance;
        var dustCount = hasCharged ? 0 : 1 + Math.round(rand.nextFloat() * fortuneLevel);

        generatedLoot.add(new ItemStack(HexItems.AMETHYST_DUST.get(), dustCount));
        generatedLoot.add(new ItemStack(HexItems.CHARGED_AMETHYST.get(), hasCharged ? 1 : 0));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AmethystClusterModifier> {
        @Override
        public AmethystClusterModifier read(ResourceLocation location, JsonObject object,
                LootItemCondition[] conditions) {
            var chargedChance = GsonHelper.getAsFloat(object, "chargedChance");
            return new AmethystClusterModifier(conditions, chargedChance);
        }

        @Override
        public JsonObject write(AmethystClusterModifier instance) {
            var obj = makeConditions(instance.conditions);
            obj.addProperty("chargedChance", instance.chargedChance);
            return obj;
        }
    }
}