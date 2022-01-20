package at.petrak.hex.datagen;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.items.HexItems;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LootModifiers extends GlobalLootModifierProvider {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODS = DeferredRegister.create(
            ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, HexMod.MOD_ID);
    private static final RegistryObject<AmethystClusterModifier.Serializer> AMETHYST_CLUSTER = LOOT_MODS.register(
            "amethyst_cluster", AmethystClusterModifier.Serializer::new);

    public LootModifiers(DataGenerator gen) {
        super(gen, HexMod.MOD_ID);
    }

    @Override
    protected void start() {
        add("amethyst_cluster", AMETHYST_CLUSTER.get(), new AmethystClusterModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(new ResourceLocation("minecraft:blocks/amethyst_cluster")).build(),
                MatchTool.toolMatches(
                                ItemPredicate.Builder.item().hasEnchantment(
                                        new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.ANY)))
                        .invert().build(),
        }, 0.95f));
    }

    public static final class AmethystClusterModifier extends LootModifier {
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

        private static class Serializer extends GlobalLootModifierSerializer<AmethystClusterModifier> {
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
}