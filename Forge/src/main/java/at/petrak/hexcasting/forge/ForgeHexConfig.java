package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.api.misc.ScrollQuantity;
import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

import static at.petrak.hexcasting.api.mod.HexConfig.anyMatch;
import static at.petrak.hexcasting.api.mod.HexConfig.noneMatch;

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
        private static ForgeConfigSpec.BooleanValue invertSpellbookScrollDirection;
        private static ForgeConfigSpec.BooleanValue invertAbacusScrollDirection;
        private static ForgeConfigSpec.DoubleValue gridSnapThreshold;

        public Client(ForgeConfigSpec.Builder builder) {
            patternPointSpeedMultiplier = builder.comment(
                    "How fast the point showing you the stroke order on patterns moves")
                .defineInRange("manaToHealthRate", DEFAULT_PATTERN_POINT_SPEED_MULTIPLIER, 0.0,
                    Double.POSITIVE_INFINITY);
            ctrlTogglesOffStrokeOrder = builder.comment(
                    "Whether the ctrl key will instead turn *off* the color gradient on patterns")
                .define("ctrlTogglesOffStrokeOrder", DEFAULT_CTRL_TOGGLES_OFF_STROKE_ORDER);
            invertSpellbookScrollDirection = builder.comment(
                    "Whether scrolling up (as opposed to down) will increase the page index of the spellbook, and vice versa")
                .define("invertSpellbookScrollDirection", DEFAULT_INVERT_SPELLBOOK_SCROLL);
            invertAbacusScrollDirection = builder.comment(
                    "Whether scrolling up (as opposed to down) will increase the value of the abacus, and vice versa")
                .define("invertAbacusScrollDirection", DEFAULT_INVERT_ABACUS_SCROLL);
            gridSnapThreshold = builder.comment(
                    "When using a staff, the distance from one dot you have to go to snap to the next dot, where 0.5 means 50% of the way.")
                .defineInRange("gridSnapThreshold", DEFAULT_GRID_SNAP_THRESHOLD, 0.5, 1.0);
        }

        @Override
        public double patternPointSpeedMultiplier() {
            return patternPointSpeedMultiplier.get();
        }

        @Override
        public boolean ctrlTogglesOffStrokeOrder() {
            return ctrlTogglesOffStrokeOrder.get();
        }

        @Override
        public boolean invertSpellbookScrollDirection() {
            return invertSpellbookScrollDirection.get();
        }

        @Override
        public boolean invertAbacusScrollDirection() {
            return invertAbacusScrollDirection.get();
        }

        @Override
        public double gridSnapThreshold() {
            return gridSnapThreshold.get();
        }
    }

    public static class Server implements HexConfig.ServerConfigAccess {
        private static ForgeConfigSpec.IntValue opBreakHarvestLevel;
        private static ForgeConfigSpec.IntValue maxRecurseDepth;

        private static ForgeConfigSpec.IntValue maxSpellCircleLength;

        private static ForgeConfigSpec.ConfigValue<List<? extends String>> actionDenyList;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> circleActionDenyList;

        private static ForgeConfigSpec.BooleanValue villagersOffendedByMindMurder;

        private static ForgeConfigSpec.ConfigValue<List<? extends String>> fewScrollTables;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> someScrollTables;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> manyScrollTables;

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

            villagersOffendedByMindMurder = builder.comment(
                    "Should villagers take offense when you flay the mind of their fellow villagers?")
                .define("villagersOffendedByMindMurder", true);

            builder.push("Scrolls in Loot");

            fewScrollTables = builder.comment(
                    "Which loot tables should a small number of Ancient Scrolls be injected into?")
                .defineList("fewScrollTables", DEFAULT_FEW_SCROLL_TABLES,
                    obj -> obj instanceof String s && ResourceLocation.isValidResourceLocation(s));
            someScrollTables = builder.comment(
                    "Which loot tables should a decent number of Ancient Scrolls be injected into?")
                .defineList("someScrollTables", DEFAULT_SOME_SCROLL_TABLES,
                    obj -> obj instanceof String s && ResourceLocation.isValidResourceLocation(s));
            manyScrollTables = builder.comment(
                    "Which loot tables should a huge number of Ancient Scrolls be injected into?")
                .defineList("manyScrollTables", DEFAULT_MANY_SCROLL_TABLES,
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
            return noneMatch(actionDenyList.get(), actionID);
        }

        @Override
        public boolean isActionAllowedInCircles(ResourceLocation actionID) {
            return noneMatch(circleActionDenyList.get(), actionID);
        }

        @Override
        public boolean doVillagersTakeOffenseAtMindMurder() {
            return villagersOffendedByMindMurder.get();
        }

        @Override
        public ScrollQuantity scrollsForLootTable(ResourceLocation lootTable) {
            if (anyMatch(fewScrollTables.get(), lootTable)) {
                return ScrollQuantity.FEW;
            } else if (anyMatch(someScrollTables.get(), lootTable)) {
                return ScrollQuantity.SOME;
            } else if (anyMatch(manyScrollTables.get(), lootTable)) {
                return ScrollQuantity.MANY;
            }
            return ScrollQuantity.NONE;
        }
    }
}
