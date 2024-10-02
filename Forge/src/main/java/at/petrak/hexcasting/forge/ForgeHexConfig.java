package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.api.mod.HexConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

import static at.petrak.hexcasting.api.mod.HexConfig.noneMatch;

public class ForgeHexConfig implements HexConfig.CommonConfigAccess {
    private static ForgeConfigSpec.LongValue dustMediaAmount;
    private static ForgeConfigSpec.LongValue shardMediaAmount;
    private static ForgeConfigSpec.LongValue chargedCrystalMediaAmount;
    private static ForgeConfigSpec.DoubleValue mediaToHealthRate;

    private static ForgeConfigSpec.IntValue cypherCooldown;
    private static ForgeConfigSpec.IntValue trinketCooldown;
    private static ForgeConfigSpec.IntValue artifactCooldown;

    public ForgeHexConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Media Amounts");
        dustMediaAmount = builder.comment("How much media a single Amethyst Dust item is worth")
            .defineInRange("dustMediaAmount", DEFAULT_DUST_MEDIA_AMOUNT, 0, Integer.MAX_VALUE);
        shardMediaAmount = builder.comment("How much media a single Amethyst Shard item is worth")
            .defineInRange("shardMediaAmount", DEFAULT_SHARD_MEDIA_AMOUNT, 0, Integer.MAX_VALUE);
        chargedCrystalMediaAmount = builder.comment("How much media a single Charged Amethyst Crystal item is worth")
            .defineInRange("chargedCrystalMediaAmount", DEFAULT_CHARGED_MEDIA_AMOUNT, 0, Integer.MAX_VALUE);
        mediaToHealthRate = builder.comment("How many points of media a half-heart is worth when casting from HP")
            .defineInRange("mediaToHealthRate", DEFAULT_MEDIA_TO_HEALTH_RATE, 0.0, Double.POSITIVE_INFINITY);
        builder.pop();

        builder.push("Cooldowns");
        cypherCooldown = builder.comment("Cooldown in ticks of a cypher")
            .defineInRange("cypherCooldown", DEFAULT_CYPHER_COOLDOWN, 0, Integer.MAX_VALUE);
        trinketCooldown = builder.comment("Cooldown in ticks of a trinket")
            .defineInRange("trinketCooldown", DEFAULT_TRINKET_COOLDOWN, 0, Integer.MAX_VALUE);
        artifactCooldown = builder.comment("Cooldown in ticks of a artifact")
            .defineInRange("artifactCooldown", DEFAULT_ARTIFACT_COOLDOWN, 0, Integer.MAX_VALUE);
        builder.pop();
    }

    @Override
    public long dustMediaAmount() {
        return dustMediaAmount.get();
    }

    @Override
    public long shardMediaAmount() {
        return shardMediaAmount.get();
    }

    @Override
    public long chargedCrystalMediaAmount() {
        return chargedCrystalMediaAmount.get();
    }

    @Override
    public double mediaToHealthRate() {
        return mediaToHealthRate.get();
    }

    @Override
    public int cypherCooldown() {
        return cypherCooldown.get();
    }

    @Override
    public int trinketCooldown() {
        return trinketCooldown.get();
    }

    @Override
    public int artifactCooldown() {
        return artifactCooldown.get();
    }

    public static class Client implements HexConfig.ClientConfigAccess {
        private static ForgeConfigSpec.BooleanValue ctrlTogglesOffStrokeOrder;
        private static ForgeConfigSpec.BooleanValue invertSpellbookScrollDirection;
        private static ForgeConfigSpec.BooleanValue invertAbacusScrollDirection;
        private static ForgeConfigSpec.DoubleValue gridSnapThreshold;
        private static ForgeConfigSpec.BooleanValue clickingTogglesDrawing;

        public Client(ForgeConfigSpec.Builder builder) {
            ctrlTogglesOffStrokeOrder = builder.comment(
                    "Whether the ctrl key will instead turn *off* the color gradient on patterns")
                .define("ctrlTogglesOffStrokeOrder", DEFAULT_CTRL_TOGGLES_OFF_STROKE_ORDER);
            invertSpellbookScrollDirection = builder.comment(
                    "Whether scrolling up (as opposed to down) will increase the page index of the spellbook, and " +
                        "vice versa")
                .define("invertSpellbookScrollDirection", DEFAULT_INVERT_SPELLBOOK_SCROLL);
            invertAbacusScrollDirection = builder.comment(
                    "Whether scrolling up (as opposed to down) will increase the value of the abacus, and vice versa")
                .define("invertAbacusScrollDirection", DEFAULT_INVERT_ABACUS_SCROLL);
            gridSnapThreshold = builder.comment(
                    "When using a staff, the distance from one dot you have to go to snap to the next dot, where 0.5 " +
                        "means 50% of the way.")
                .defineInRange("gridSnapThreshold", DEFAULT_GRID_SNAP_THRESHOLD, 0.5, 1.0);
            clickingTogglesDrawing = builder.comment(
                            "Whether you click to start and stop drawing instead of clicking and dragging")
                    .define("clickingTogglesDrawing", DEFAULT_CLICKING_TOGGLES_DRAWING);
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
        public boolean ctrlTogglesOffStrokeOrder() {
            return ctrlTogglesOffStrokeOrder.get();
        }

        @Override
        public double gridSnapThreshold() {
            return gridSnapThreshold.get();
        }

        @Override
        public boolean clickingTogglesDrawing() {
            return clickingTogglesDrawing.get();
        }
    }

    public static class Server implements HexConfig.ServerConfigAccess {
        private static ForgeConfigSpec.IntValue opBreakHarvestLevel;
        private static ForgeConfigSpec.IntValue maxOpCount;

        private static ForgeConfigSpec.IntValue maxSpellCircleLength;

        private static ForgeConfigSpec.ConfigValue<List<? extends String>> actionDenyList;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> circleActionDenyList;

        private static ForgeConfigSpec.BooleanValue villagersOffendedByMindMurder;

        private static ForgeConfigSpec.ConfigValue<List<? extends String>> tpDimDenyList;

        private static ForgeConfigSpec.BooleanValue doesTrueNameHaveAmbit;

        private static ForgeConfigSpec.ConfigValue<List<? extends String>> fewScrollTables;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> someScrollTables;
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> manyScrollTables;


        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("Spells");
            maxOpCount = builder.comment("The maximum number of actions that can be executed in one tick, to avoid " +
                    "hanging the server.")
                .defineInRange("maxOpCount", DEFAULT_MAX_OP_COUNT, 0, Integer.MAX_VALUE);
            opBreakHarvestLevel = builder.comment(
                "The harvest level of the Break Block spell.",
                "0 = wood, 1 = stone, 2 = iron, 3 = diamond, 4 = netherite."
            ).defineInRange("opBreakHarvestLevel", DEFAULT_OP_BREAK_HARVEST_LEVEL, 0, 4);
            builder.pop();

            builder.push("Spell Circles");
            maxSpellCircleLength = builder.comment("The maximum number of slates in a spell circle")
                .defineInRange("maxSpellCircleLength", DEFAULT_MAX_SPELL_CIRCLE_LENGTH, 4, Integer.MAX_VALUE);

            circleActionDenyList = builder.comment(
                    "Resource locations of disallowed actions within circles. Trying to cast one of these in a circle" +
                        " will result in a mishap. For example: hexcasting:get_caster will prevent Mind's Reflection.")
                .defineList("circleActionDenyList", List.of(), Server::isValidReslocArg);
            builder.pop();

            actionDenyList = builder.comment(
                    "Resource locations of disallowed actions. Trying to cast one of these will result in a mishap.")
                .defineList("actionDenyList", List.of(), Server::isValidReslocArg);

            villagersOffendedByMindMurder = builder.comment(
                    "Should villagers take offense when you flay the mind of their fellow villagers?")
                .define("villagersOffendedByMindMurder", true);

            tpDimDenyList = builder.comment("Resource locations of dimensions you can't Blink or Greater Teleport in.")
                .defineList("tpDimDenyList", DEFAULT_DIM_TP_DENYLIST, Server::isValidReslocArg);

            doesTrueNameHaveAmbit = builder.comment(
                    "when false makes player reference iotas behave as normal entity reference iotas")
                .define("doesTrueNameHaveAmbit", DEFAULT_TRUE_NAME_HAS_AMBIT);
        }

        @Override
        public int opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort() {
            return opBreakHarvestLevel.get();
        }

        @Override
        public int maxOpCount() {
            return maxOpCount.get();
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
        public boolean canTeleportInThisDimension(ResourceKey<Level> dimension) {
            return noneMatch(tpDimDenyList.get(), dimension.location());
        }

        @Override
        public boolean trueNameHasAmbit() {
            return doesTrueNameHaveAmbit.get();
        }

        private static boolean isValidReslocArg(Object o) {
            return o instanceof String s && ResourceLocation.isValidResourceLocation(s);
        }
    }
}
