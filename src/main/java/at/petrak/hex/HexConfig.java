package at.petrak.hex;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;

public class HexConfig {
    public final ForgeConfigSpec.IntValue maxRecurseDepth;
    public final ForgeConfigSpec.IntValue wandRechargeRate;
    public final ForgeConfigSpec.DoubleValue healthToManaRate;
    public final ForgeConfigSpec.IntValue opBreakHarvestLevel;

    public HexConfig(ForgeConfigSpec.Builder builder) {
        maxRecurseDepth = builder.comment("How many times an Eval can recursively cast itself")
                .defineInRange("maxRecurseDepth", 15, 0, Integer.MAX_VALUE);
        wandRechargeRate = builder.comment("How many mana points a wand recharges per tick")
                .defineInRange("wandRechargeRate", 2, 0, Integer.MAX_VALUE);
        healthToManaRate = builder.comment("How many points of mana a half-heart is worth when casting from HP")
                .defineInRange("healthToManaRate", 10.0, 0.0, 1_000_000.0);

        builder.push("spells");
        opBreakHarvestLevel = builder.comment(
                "The harvest level of the Break Block spell.",
                "0 = wood, 1 = stone, 2 = iron, 3 = diamond, 4 = netherite."
        ).defineInRange("opBreakHarvestLevel", 3, 0, 4);
        builder.pop();
    }

    /**
     * i'm not kidding look upon net.minecraftforge.common.TierSortingRegistry and weep
     */
    public Tier getOpBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort() {
        return switch (this.opBreakHarvestLevel.get()) {
            case 0 -> Tiers.WOOD;
            case 1 -> Tiers.STONE;
            case 2 -> Tiers.IRON;
            case 3 -> Tiers.DIAMOND;
            case 4 -> Tiers.NETHERITE;
            default -> throw new RuntimeException("unreachable");
        };
    }
}
