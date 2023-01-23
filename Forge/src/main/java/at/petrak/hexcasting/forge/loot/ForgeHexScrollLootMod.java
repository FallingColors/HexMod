package at.petrak.hexcasting.forge.loot;

import at.petrak.hexcasting.api.misc.ScrollQuantity;
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
                Codec.INT.fieldOf("quantity").forGetter(it -> it.quantity.ordinal())
            ).apply(inst, (cond, quant) -> new ForgeHexScrollLootMod(cond, ScrollQuantity.values()[quant]))
        ));

    public final ScrollQuantity quantity;

    public ForgeHexScrollLootMod(LootItemCondition[] conditionsIn, ScrollQuantity quantity) {
        super(conditionsIn);
        this.quantity = quantity;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
        LootContext context) {
        var injectedTable = context.getLootTable(this.quantity.getPool());
        injectedTable.getRandomItemsRaw(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return null;
    }
}
