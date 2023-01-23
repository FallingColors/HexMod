package at.petrak.hexcasting.forge.loot;

import at.petrak.hexcasting.common.lib.HexItems;
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

public class ForgeHexLoreLootMod extends LootModifier {
    public static final Supplier<Codec<ForgeHexLoreLootMod>> CODEC =
        Suppliers.memoize(() -> RecordCodecBuilder.create(
            inst -> codecStart(inst).and(
                Codec.DOUBLE.fieldOf("chance").forGetter(it -> it.chance)
            ).apply(inst, ForgeHexLoreLootMod::new)
        ));

    public final double chance;

    public ForgeHexLoreLootMod(LootItemCondition[] conditionsIn, double chance) {
        super(conditionsIn);
        this.chance = chance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
        LootContext context) {
        if (context.getRandom().nextDouble() < this.chance) {
            generatedLoot.add(new ItemStack(HexItems.LORE_FRAGMENT));
        }
        return generatedLoot;
    }

    @Override
    public Codec<ForgeHexLoreLootMod> codec() {
        return ForgeHexLootMods.INJECT_LORE.get();
    }
}
