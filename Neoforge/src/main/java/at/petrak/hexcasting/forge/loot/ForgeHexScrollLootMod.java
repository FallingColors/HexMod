package at.petrak.hexcasting.forge.loot;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.loot.AddPerWorldPatternToScrollFunc;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.forge.lib.ForgeHexLootMods;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ForgeHexScrollLootMod extends LootModifier {
    public static final Supplier<Codec<ForgeHexScrollLootMod>> CODEC =
        Suppliers.memoize(() -> RecordCodecBuilder.create(
            inst -> codecStart(inst).and(
                Codec.INT.fieldOf("countRange").forGetter(it -> it.countRange)
            ).apply(inst, ForgeHexScrollLootMod::new)
        ));

    public final int countRange;

    public ForgeHexScrollLootMod(LootItemCondition[] conditionsIn, int countRange) {
        super(conditionsIn);
        this.countRange = countRange;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
        LootContext context) {
        int count = HexLootHandler.getScrollCount(this.countRange, context.getRandom());
        for (int i = 0; i < count; i++) {
            var newStack = new ItemStack(HexItems.SCROLL_LARGE);
            AddPerWorldPatternToScrollFunc.doStatic(newStack, context);
            generatedLoot.add(newStack);
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ForgeHexLootMods.INJECT_SCROLLS.get();
    }
}
