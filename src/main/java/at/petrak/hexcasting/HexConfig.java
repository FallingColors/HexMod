package at.petrak.hexcasting;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;

public class HexConfig {

    public static ForgeConfigSpec.DoubleValue manaToHealthRate;


    public static ForgeConfigSpec.IntValue dustManaAmount;
    public static ForgeConfigSpec.IntValue shardManaAmount;
    public static ForgeConfigSpec.IntValue chargedCrystalManaAmount;

    public static ForgeConfigSpec.IntValue opBreakHarvestLevel;
    public static ForgeConfigSpec.IntValue maxRecurseDepth;


    public HexConfig(ForgeConfigSpec.Builder builder) {
        manaToHealthRate = builder.comment("How many points of mana a half-heart is worth when casting from HP")
            .defineInRange("manaToHealthRate", 200_000.0 / 20.0, 0.0, Double.POSITIVE_INFINITY);

        builder.push("items");
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
    public static Tier getOpBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort() {
        return switch (opBreakHarvestLevel.get()) {
            case 0 -> Tiers.WOOD;
            case 1 -> Tiers.STONE;
            case 2 -> Tiers.IRON;
            case 3 -> Tiers.DIAMOND;
            case 4 -> Tiers.NETHERITE;
            default -> throw new RuntimeException("unreachable");
        };
    }

    public static class Client {
        public static ForgeConfigSpec.DoubleValue patternPointSpeedMultiplier;
        public static ForgeConfigSpec.BooleanValue ctrlTogglesOffStrokeOrder;

        public Client(ForgeConfigSpec.Builder builder) {
            patternPointSpeedMultiplier = builder.comment(
                    "How fast the point showing you the stroke order on patterns moves")
                .defineInRange("manaToHealthRate", 1.0, 0.0, Double.POSITIVE_INFINITY);
            ctrlTogglesOffStrokeOrder = builder.comment(
                    "Whether the ctrl key will instead turn *off* the color gradient on patterns")
                .define("ctrlTogglesOffStrokeOrder", false);
        }

    }
}
