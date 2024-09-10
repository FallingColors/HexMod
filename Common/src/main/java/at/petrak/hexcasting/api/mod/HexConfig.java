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

        int DEFAULT_MAX_OP_COUNT = 1_000_000;
        int DEFAULT_MAX_SPELL_CIRCLE_LENGTH = 1024;
        int DEFAULT_OP_BREAK_HARVEST_LEVEL = 3;

        boolean DEFAULT_VILLAGERS_DISLIKE_MIND_MURDER = true;

        List<String> DEFAULT_DIM_TP_DENYLIST = List.of("twilightforest:twilight_forest");

        boolean DEFAULT_TRUE_NAME_HAS_AMBIT = true;

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
