package at.petrak.hexcasting.forge.datagen.lootmods;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AmethystShardReducerModifier extends LootModifier {
    private final double modifier;

    public AmethystShardReducerModifier(double modifier, LootItemCondition[] conditions) {
        super(conditions);
        this.modifier = modifier;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        for (var stack : generatedLoot) {
            if (stack.is(Items.AMETHYST_SHARD)) {
                stack.shrink((int) (stack.getCount() * modifier));
            }
        }
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AmethystShardReducerModifier> {
        @Override
        public AmethystShardReducerModifier read(ResourceLocation location, JsonObject json,
            LootItemCondition[] conditions) {
            var modifier = GsonHelper.getAsDouble(json, "modifier");
            return new AmethystShardReducerModifier(modifier, conditions);
        }

        @Override
        public JsonObject write(AmethystShardReducerModifier instance) {
            var obj = this.makeConditions(instance.conditions);
            obj.addProperty("modifier", instance.modifier);
            return obj;
        }
    }
}
