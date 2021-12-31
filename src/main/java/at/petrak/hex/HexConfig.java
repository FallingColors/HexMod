package at.petrak.hex;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;

public class HexConfig {

    public final ForgeConfigSpec.DoubleValue healthToManaRate;

    public final ForgeConfigSpec.IntValue wandMaxMana;
    public final ForgeConfigSpec.IntValue wandRechargeRate;
    public final ForgeConfigSpec.IntValue cypherMaxMana;
    public final ForgeConfigSpec.IntValue trinketMaxMana;
    public final ForgeConfigSpec.IntValue artifactMaxMana;
    public final ForgeConfigSpec.IntValue artifactRechargeRate;

    public final ForgeConfigSpec.IntValue opBreakHarvestLevel;
    public final ForgeConfigSpec.IntValue maxRecurseDepth;

    public HexConfig(ForgeConfigSpec.Builder builder) {


        healthToManaRate = builder.comment("How many points of mana a half-heart is worth when casting from HP")
                .defineInRange("healthToManaRate", 1_000_000.0 / 20.0, 0.0, 1_000_000.0);

        builder.push("items");
        wandMaxMana = builder.comment("The maximum amount of mana a wand can store.")
                .defineInRange("wandMaxMana", 1_000_000, 0, Integer.MAX_VALUE);
        wandRechargeRate = builder.comment("How many mana points a wand recharges per tick")
                .defineInRange("wandRechargeRate", 2_000, 0, Integer.MAX_VALUE);
        cypherMaxMana = builder.comment("The maximum amount of mana a cypher can store.")
                .defineInRange("cypherMaxMana", 8_000_000, 0, Integer.MAX_VALUE);
        trinketMaxMana = builder.comment("The maximum amount of mana a trinket can store.")
                .defineInRange("trinketMaxMana", 4_000_000, 0, Integer.MAX_VALUE);
        artifactMaxMana = builder.comment("The maximum amount of mana an artifact can store.")
                .defineInRange("artifactMaxMana", 1_000_000, 0, Integer.MAX_VALUE);
        artifactRechargeRate = builder.comment("How many mana points an artifact recharges per tick")
                .defineInRange("artifactRechargeRate", 1_500, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("spells");
        maxRecurseDepth = builder.comment("How many times a spell can recursively cast other spells")
                .defineInRange("maxRecurseDepth", 15, 0, Integer.MAX_VALUE);
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
