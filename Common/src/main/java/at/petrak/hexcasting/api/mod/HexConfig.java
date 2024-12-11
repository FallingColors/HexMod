package at.petrak.hexcasting.api.mod;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.misc.MediaConstants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

import java.util.List;

public class HexConfig {
    public interface CommonConfigAccess {

        long dustMediaAmount();

        long shardMediaAmount();

        long chargedCrystalMediaAmount();

        double mediaToHealthRate();

        int cypherCooldown();

        int trinketCooldown();

        int artifactCooldown();

        long DEFAULT_DUST_MEDIA_AMOUNT = MediaConstants.DUST_UNIT;
        long DEFAULT_SHARD_MEDIA_AMOUNT = MediaConstants.SHARD_UNIT;
        long DEFAULT_CHARGED_MEDIA_AMOUNT = MediaConstants.CRYSTAL_UNIT;
        double DEFAULT_MEDIA_TO_HEALTH_RATE = 2 * MediaConstants.CRYSTAL_UNIT / 20.0;

        int DEFAULT_CYPHER_COOLDOWN = 8;
        int DEFAULT_TRINKET_COOLDOWN = 5;
        int DEFAULT_ARTIFACT_COOLDOWN = 3;

    }

    public interface ClientConfigAccess {
        boolean ctrlTogglesOffStrokeOrder();

        boolean invertSpellbookScrollDirection();

        boolean invertAbacusScrollDirection();

        double gridSnapThreshold();

        boolean clickingTogglesDrawing();

        boolean DEFAULT_CTRL_TOGGLES_OFF_STROKE_ORDER = false;
        boolean DEFAULT_INVERT_SPELLBOOK_SCROLL = false;
        boolean DEFAULT_INVERT_ABACUS_SCROLL = false;
        double DEFAULT_GRID_SNAP_THRESHOLD = 0.5;
        boolean DEFAULT_CLICKING_TOGGLES_DRAWING = false;
    }

    public interface ServerConfigAccess {
        int opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort();

        int maxOpCount();

        int maxSpellCircleLength();

        boolean isActionAllowed(ResourceLocation actionID);

        boolean isActionAllowedInCircles(ResourceLocation actionID);

        boolean doVillagersTakeOffenseAtMindMurder();

        // fun fact, although dimension keys are a RegistryHolder, they aren't a registry, so i can't do tags
        boolean canTeleportInThisDimension(ResourceKey<Level> dimension);

        boolean trueNameHasAmbit();

        // is passing the randint from LootContext.getRandom().nextInt() really necessary? or can it just use its own RNG
        List<String> getRandomLootHex(int randint);

        int DEFAULT_MAX_OP_COUNT = 100_000;
        int DEFAULT_MAX_SPELL_CIRCLE_LENGTH = 1024;
        int DEFAULT_OP_BREAK_HARVEST_LEVEL = 3;

        boolean DEFAULT_VILLAGERS_DISLIKE_MIND_MURDER = true;

        List<String> DEFAULT_DIM_TP_DENYLIST = List.of("twilightforest:twilight_forest");

        boolean DEFAULT_TRUE_NAME_HAS_AMBIT = true;

        List<List<String>> DEFAULT_LOOT_HEX_LIST = List.of(
            List.of("hexcasting.loot_hex.shatter","NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST wqaawdd","EAST qaqqqqq"),
            List.of("hexcasting.loot_hex.kindle","NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST wqaawdd","SOUTH_EAST aaqawawa"),
            List.of("hexcasting.loot_hex.illuminate","NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST aadadaaw","EAST wqaawdd","NORTH_EAST ddqdd","EAST weddwaa","NORTH_EAST waaw","NORTH_EAST qqd"),
            List.of("hexcasting.loot_hex.growth","NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST aadadaaw","EAST wqaawdd","NORTH_EAST ddqdd","EAST weddwaa","NORTH_EAST waaw","SOUTH_EAST aqaaedwd","EAST aadaadaa","NORTH_EAST wqaqwawqaqw","NORTH_EAST wqaqwawqaqw","NORTH_EAST wqaqwawqaqw"),
            List.of("hexcasting.loot_hex.lunge","NORTH_EAST qaq","EAST aadaa","NORTH_EAST wa","SOUTH_EAST aqaawa","SOUTH_EAST waqaw","SOUTH_WEST awqqqwaqw"),
            List.of("hexcasting.loot_hex.sidestep","NORTH_EAST qaq","EAST aadaa","NORTH_EAST wa","NORTH_WEST eqqq","SOUTH_EAST aqaawd","SOUTH_EAST e","NORTH_WEST qqqqqew","SOUTH_WEST eeeeeqw","SOUTH_EAST awdd","NORTH_EAST wdedw","SOUTH_WEST awqqqwaqw"),
            List.of("hexcasting.loot_hex.ascend","NORTH_EAST qaq","SOUTH_EAST aqaae","WEST qqqqqawwawawd"),
            List.of("hexcasting.loot_hex.blink","NORTH_EAST qaq","EAST aadaa","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST wqaawdd","NORTH_EAST qaq","EAST aa","NORTH_WEST wddw","NORTH_EAST wqaqw","SOUTH_EAST aqaaw","NORTH_WEST wddw","SOUTH_WEST awqqqwaq"),
            List.of("hexcasting.loot_hex.blastoff","NORTH_EAST qaq","NORTH_WEST qqqqqew","SOUTH_EAST aqaawaa","SOUTH_EAST waqaw","SOUTH_WEST awqqqwaqw"),
            List.of("hexcasting.loot_hex.radar","WEST qqq","EAST aadaa","EAST aa","SOUTH_EAST aqaawa","SOUTH_WEST ewdqdwe","NORTH_EAST de","EAST eee","NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaaeaqq","SOUTH_EAST qqqqqwdeddwd","NORTH_EAST dadad"),
            List.of("hexcasting.loot_hex.beckon","NORTH_EAST qaq","EAST aa","NORTH_EAST qaq","NORTH_EAST wa","EAST weaqa","EAST aadaa","EAST dd","NORTH_EAST qaq","EAST aa","EAST aawdd","NORTH_WEST wddw","EAST aadaa","NORTH_EAST wqaqw","NORTH_EAST wdedw","SOUTH_EAST aqaawa","SOUTH_EAST waqaw","SOUTH_WEST awqqqwaqw"),
            List.of("hexcasting.loot_hex.detonate","NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaaedwd","EAST ddwddwdd"),
            List.of("hexcasting.loot_hex.shockwave","NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaawaa","EAST aadaadaa","SOUTH_EAST aqawqadaq","SOUTH_EAST aqaaedwd","EAST aawaawaa","NORTH_EAST qqa","EAST qaqqqqq"),
            List.of("hexcasting.loot_hex.heat_wave","WEST qqq","SOUTH_EAST aaqawawa","EAST eee","NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaae","SOUTH_EAST qqqqqwded","SOUTH_WEST aaqwqaa","SOUTH_EAST a","NORTH_EAST dadad"),
            List.of("hexcasting.loot_hex.wither_wave","WEST qqq","SOUTH_EAST aqaae","SOUTH_EAST aqaaw","SOUTH_WEST qqqqqaewawawe","EAST eee","NORTH_EAST qaq","EAST aa","SOUTH_EAST aqaae","SOUTH_EAST qqqqqwdeddwd","SOUTH_WEST aaqwqaa","SOUTH_EAST a","NORTH_EAST dadad"),
            List.of("hexcasting.loot_hex.flight_zone","NORTH_EAST qaq","SOUTH_EAST aqaaq","SOUTH_WEST awawaawq")
        );

        default Tier opBreakHarvestLevel() {
            return switch (this.opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort()) {
                case 0 -> Tiers.WOOD;
                case 1 -> Tiers.STONE;
                case 2 -> Tiers.IRON;
                case 3 -> Tiers.DIAMOND;
                case 4 -> Tiers.NETHERITE;
                default -> throw new RuntimeException("please only return a value in 0<=x<=4");
            };
        }
    }

    // Simple extensions for resource location configs
    public static boolean anyMatch(List<? extends String> keys, ResourceLocation key) {
        for (String s : keys) {
            if (ResourceLocation.isValidResourceLocation(s)) {
                var rl = new ResourceLocation(s);
                if (rl.equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean noneMatch(List<? extends String> keys, ResourceLocation key) {
        return !anyMatch(keys, key);
    }

    public static boolean anyMatchResLoc(List<? extends ResourceLocation> keys, ResourceLocation key) {
        return keys.stream().anyMatch(key::equals);
    }

    // oh man this is aesthetically pleasing
    private static CommonConfigAccess common = null;
    private static ClientConfigAccess client = null;
    private static ServerConfigAccess server = null;

    public static CommonConfigAccess common() {
        return common;
    }

    public static ClientConfigAccess client() {
        return client;
    }

    public static ServerConfigAccess server() {
        return server;
    }

    public static void setCommon(CommonConfigAccess access) {
        if (common != null) {
            HexAPI.LOGGER.warn("CommonConfigAccess was replaced! Old {} New {}",
                common.getClass().getName(), access.getClass().getName());
        }
        common = access;
    }

    public static void setClient(ClientConfigAccess access) {
        if (client != null) {
            HexAPI.LOGGER.warn("ClientConfigAccess was replaced! Old {} New {}",
                client.getClass().getName(), access.getClass().getName());
        }
        client = access;
    }

    public static void setServer(ServerConfigAccess access) {
        if (server != null) {
            HexAPI.LOGGER.warn("ServerConfigAccess was replaced! Old {} New {}",
                server.getClass().getName(), access.getClass().getName());
        }
        server = access;
    }
}
