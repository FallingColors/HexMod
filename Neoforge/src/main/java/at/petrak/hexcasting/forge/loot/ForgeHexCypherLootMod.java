package at.petrak.hexcasting.forge.loot;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.loot.AddHexToAncientCypherFunc;
import at.petrak.hexcasting.forge.lib.ForgeHexLootMods;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ForgeHexCypherLootMod extends LootModifier {
    public static final Supplier<MapCodec<ForgeHexCypherLootMod>> CODEC =
        Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(
            inst -> codecStart(inst).and(
                Codec.DOUBLE.fieldOf("chance").forGetter(it -> it.chance)
            ).apply(inst, ForgeHexCypherLootMod::new)
        ));

    public final double chance;

    public ForgeHexCypherLootMod(LootItemCondition[] conditionsIn, double chance) {
        super(conditionsIn);
        this.chance = chance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
        LootContext context) {
        if (context.getRandom().nextDouble() < this.chance) {
            var newStack = new ItemStack(HexItems.ANCIENT_CYPHER);
            AddHexToAncientCypherFunc.doStatic(newStack, context.getRandom());
            generatedLoot.add(newStack);
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<ForgeHexCypherLootMod> codec() {
        return ForgeHexLootMods.INJECT_CYPHERS.get();
    }
}
