package at.petrak.hexcasting.forge.loot;

import at.petrak.hexcasting.common.loot.AmethystReducerFunc;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.forge.lib.ForgeHexLootMods;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ForgeHexAmethystLootMod extends LootModifier {
    public static final Supplier<Codec<ForgeHexAmethystLootMod>> CODEC =
        Suppliers.memoize(() -> RecordCodecBuilder.create(
            inst -> codecStart(inst).and(
                Codec.DOUBLE.fieldOf("shardDelta").forGetter(it -> it.shardDelta)
            ).apply(inst, ForgeHexAmethystLootMod::new)
        ));

    public final double shardDelta;

    public ForgeHexAmethystLootMod(LootItemCondition[] conditionsIn, double shardDelta) {
        super(conditionsIn);
        this.shardDelta = shardDelta;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
        LootContext context) {
        var injectPool = context.getResolver().getLootTable(HexLootHandler.TABLE_INJECT_AMETHYST_CLUSTER);
        injectPool.getRandomItemsRaw(context, generatedLoot::add);

        for (var stack : generatedLoot) {
            AmethystReducerFunc.doStatic(stack, context, this.shardDelta);
        }

        return generatedLoot;
    }

    @Override
    public Codec<ForgeHexAmethystLootMod> codec() {
        return ForgeHexLootMods.AMETHYST.get();
    }
}
