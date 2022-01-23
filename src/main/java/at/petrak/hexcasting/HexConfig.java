package at.petrak.hexcasting;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;

public class HexConfig {

    public final ForgeConfigSpec.DoubleValue manaToHealthRate;
    public final ForgeConfigSpec.DoubleValue patternPointSpeedMultiplier;

    public final ForgeConfigSpec.IntValue batteryMaxMana;
    public final ForgeConfigSpec.IntValue dustManaAmount;
    public final ForgeConfigSpec.IntValue shardManaAmount;
    public final ForgeConfigSpec.IntValue chargedCrystalManaAmount;

    public final ForgeConfigSpec.IntValue opBreakHarvestLevel;
    public final ForgeConfigSpec.IntValue maxRecurseDepth;


    public HexConfig(ForgeConfigSpec.Builder builder) {
        manaToHealthRate = builder.comment("How many points of mana a half-heart is worth when casting from HP")
            .defineInRange("manaToHealthRate", 100_000.0 / 20.0, 0.0, Double.POSITIVE_INFINITY);
        patternPointSpeedMultiplier = builder.comment(
                "How fast the point showing you the stroke order on patterns moves")
            .defineInRange("manaToHealthRate", 1.0, 0.0, Double.POSITIVE_INFINITY);

        builder.push("items");
        batteryMaxMana = builder.comment("The maximum amount of mana a mana battery can store.")
            .defineInRange("batteryMaxMana", 1_000_000, 0, Integer.MAX_VALUE);
        dustManaAmount = builder.comment("How much mana a single Amethyst Dust item is worth")
            .defineInRange("dustManaAmount", 10_000, 0, Integer.MAX_VALUE);
        shardManaAmount = builder.comment("How much mana a single Amethyst Shard item is worth")
            .defineInRange("shardManaAmount", 50_000, 0, Integer.MAX_VALUE);
        chargedCrystalManaAmount = builder.comment("How much mana a single Charged Amethyst Crystal item is worth")
            .defineInRange("chargedCrystalManaAmount", 100_000, 0, Integer.MAX_VALUE);
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
