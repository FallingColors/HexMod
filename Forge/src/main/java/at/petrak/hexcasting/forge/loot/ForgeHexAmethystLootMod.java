package at.petrak.hexcasting.forge.loot;

import com.mojang.serialization.Codec;
import at.petrak.hexcasting.common.loot.AmethystReducerFunc;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.forge.lib.ForgeHexLootMods;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ForgeHexAmethystLootMod extends LootModifier {
    public static final Supplier<MapCodec<ForgeHexAmethystLootMod>> CODEC =
        Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(
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
        var injectPool = context.getResolver()
            .lookup(net.minecraft.world.level.storage.loot.LootDataType.TABLE.registryKey())
            .orElseThrow()
            .get(net.minecraft.resources.ResourceKey.create(
                net.minecraft.world.level.storage.loot.LootDataType.TABLE.registryKey(),
                HexLootHandler.TABLE_INJECT_AMETHYST_CLUSTER))
            .orElseThrow()
            .value();
        injectPool.getRandomItems(context, generatedLoot::add);

        for (var stack : generatedLoot) {
            AmethystReducerFunc.doStatic(stack, context, this.shardDelta);
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ForgeHexLootMods.AMETHYST.get();
    }
}
