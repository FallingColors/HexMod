package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ForgeHexConfig implements HexConfig.CommonConfigAccess {
    private static ForgeConfigSpec.IntValue dustManaAmount;
    private static ForgeConfigSpec.IntValue shardManaAmount;
    private static ForgeConfigSpec.IntValue chargedCrystalManaAmount;
    private static ForgeConfigSpec.DoubleValue manaToHealthRate;

    public ForgeHexConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Mana Amounts");
        dustManaAmount = builder.comment("How much mana a single Amethyst Dust item is worth")
            .defineInRange("dustManaAmount", DEFAULT_DUST_MANA_AMOUNT, 0, Integer.MAX_VALUE);
        shardManaAmount = builder.comment("How much mana a single Amethyst Shard item is worth")
            .defineInRange("shardManaAmount", DEFAULT_SHARD_MANA_AMOUNT, 0, Integer.MAX_VALUE);
        chargedCrystalManaAmount = builder.comment("How much mana a single Charged Amethyst Crystal item is worth")
            .defineInRange("chargedCrystalManaAmount", DEFAULT_CHARGED_MANA_AMOUNT, 0, Integer.MAX_VALUE);
        manaToHealthRate = builder.comment("How many points of mana a half-heart is worth when casting from HP")
            .defineInRange("manaToHealthRate", DEFAULT_MANA_TO_HEALTH_RATE, 0.0, Double.POSITIVE_INFINITY);
        builder.pop();
    }

    @Override
    public int dustManaAmount() {
        return dustManaAmount.get();
    }

    @Override
    public int shardManaAmount() {
        return shardManaAmount.get();
    }

    @Override
    public int chargedCrystalManaAmount() {
        return chargedCrystalManaAmount.get();
    }

    @Override
    public double manaToHealthRate() {
        return manaToHealthRate.get();
    }

    public static class Client implements HexConfig.ClientConfigAccess {
        private static ForgeConfigSpec.DoubleValue patternPointSpeedMultiplier;
        private static ForgeConfigSpec.BooleanValue ctrlTogglesOffStrokeOrder;

        public Client(ForgeConfigSpec.Builder builder) {
            patternPointSpeedMultiplier = builder.comment(
                    "How fast the point showing you the stroke order on patterns moves")
                .defineInRange("manaToHealthRate", DEFAULT_PATTERN_POINT_SPEED_MULTIPLIER, 0.0,
                    Double.POSITIVE_INFINITY);
            ctrlTogglesOffStrokeOrder = builder.comment(
                    "Whether the ctrl key will instead turn *off* the color gradient on patterns")
                .define("ctrlTogglesOffStrokeOrder", DEFAULT_CTRL_TOGGLES_OFF_STROKE_ORDER);
        }

        @Override
        public double patternPointSpeedMultiplier() {
            return patternPointSpeedMultiplier.get();
        }

        @Override
        public boolean ctrlTogglesOffStrokeOrder() {
            return ctrlTogglesOffStrokeOrder.get();
        }
    }

    public static class Server implements HexConfig.ServerConfigAccess {
        private static ForgeConfigSpec.IntValue opBreakHarvestLevel;
        private static ForgeConfigSpec.IntValue maxRecurseDepth;

        private static ForgeConfigSpec.IntValue maxSpellCircleLength;

        private static ForgeConfigSpec.ConfigValue<List<? extends String>> actionDenyList;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> circleActionDenyList;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("Spells");
            maxRecurseDepth = builder.comment("How many times a spell can recursively cast other spells")
                .defineInRange("maxRecurseDepth", DEFAULT_MAX_RECURSE_DEPTH, 0, Integer.MAX_VALUE);
            opBreakHarvestLevel = builder.comment(
                "The harvest level of the Break Block spell.",
                "0 = wood, 1 = stone, 2 = iron, 3 = diamond, 4 = netherite."
            ).defineInRange("opBreakHarvestLevel", DEFAULT_OP_BREAK_HARVEST_LEVEL, 0, 4);
            builder.pop();

            builder.push("Spell Circles");
            maxSpellCircleLength = builder.comment("The maximum number of slates in a spell circle")
                .defineInRange("maxSpellCircleLength", DEFAULT_MAX_SPELL_CIRCLE_LENGTH, 4, Integer.MAX_VALUE);

            circleActionDenyList = builder.comment(
                            "Resource locations of disallowed actions within circles. Trying to cast one of these in a circle will result in a mishap.")
                    .defineList("circleActionDenyList", List.of(),
                            obj -> obj instanceof String s && ResourceLocation.isValidResourceLocation(s));
            builder.pop();

            actionDenyList = builder.comment(
                    "Resource locations of disallowed actions. Trying to cast one of these will result in a mishap.")
                .defineList("actionDenyList", List.of(),
                    obj -> obj instanceof String s && ResourceLocation.isValidResourceLocation(s));
        }

        @Override
        public int opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort() {
            return opBreakHarvestLevel.get();
        }

        @Override
        public int maxRecurseDepth() {
            return maxRecurseDepth.get();
        }

        @Override
        public int maxSpellCircleLength() {
            return maxSpellCircleLength.get();
        }

        @Override
        public boolean isActionAllowed(ResourceLocation actionID) {
            return !actionDenyList.get().contains(actionID.toString());
        }

        @Override
        public boolean isActionAllowedInCircles(ResourceLocation actionID) {
            return !circleActionDenyList.get().contains(actionID.toString());
        }
    }
}
