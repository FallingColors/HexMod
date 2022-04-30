package at.petrak.hexcasting.api.mod;

import at.petrak.hexcasting.api.HexAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public class HexConfig {
    public interface CommonConfigAccess {
        int dustManaAmount();

        int shardManaAmount();

        int chargedCrystalManaAmount();

        double manaToHealthRate();

        int DEFAULT_DUST_MANA_AMOUNT = 10_000;
        int DEFAULT_SHARD_MANA_AMOUNT = 50_000;
        int DEFAULT_CHARGED_MANA_AMOUNT = 100_000;
        double DEFAULT_MANA_TO_HEALTH_RATE = 200_000.0 / 20.0;

    }

    public interface ClientConfigAccess {
        double patternPointSpeedMultiplier();
        
        boolean ctrlTogglesOffStrokeOrder();

        double DEFAULT_PATTERN_POINT_SPEED_MULTIPLIER = 1;
        boolean DEFAULT_CTRL_TOGGLES_OFF_STROKE_ORDER = false;
    }

    public interface ServerConfigAccess {
        int opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort();

        int maxRecurseDepth();

        int maxSpellCircleLength();

        boolean isActionAllowed(ResourceLocation actionID);

        int DEFAULT_MAX_RECURSE_DEPTH = 64;
        int DEFAULT_MAX_SPELL_CIRCLE_LENGTH = 1024;
        int DEFAULT_OP_BREAK_HARVEST_LEVEL = 3;
        // We can't have default values for the break harvest level or if

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
