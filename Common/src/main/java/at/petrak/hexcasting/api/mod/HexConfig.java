package at.petrak.hexcasting.api.mod;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.misc.ScrollQuantity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import java.util.List;

public class HexConfig {
    public interface CommonConfigAccess {

        int dustManaAmount();

        int shardManaAmount();

        int chargedCrystalManaAmount();

        double manaToHealthRate();

        int DEFAULT_DUST_MANA_AMOUNT = ManaConstants.DUST_UNIT;
        int DEFAULT_SHARD_MANA_AMOUNT = ManaConstants.SHARD_UNIT;
        int DEFAULT_CHARGED_MANA_AMOUNT = ManaConstants.CRYSTAL_UNIT;
        double DEFAULT_MANA_TO_HEALTH_RATE = 2 * ManaConstants.CRYSTAL_UNIT / 20.0;

    }

    public interface ClientConfigAccess {
        double patternPointSpeedMultiplier();

        boolean ctrlTogglesOffStrokeOrder();

        boolean invertSpellbookScrollDirection();

        boolean invertAbacusScrollDirection();

        double gridSnapThreshold();

        double DEFAULT_PATTERN_POINT_SPEED_MULTIPLIER = 1;
        boolean DEFAULT_CTRL_TOGGLES_OFF_STROKE_ORDER = false;
        boolean DEFAULT_INVERT_SPELLBOOK_SCROLL = false;
        boolean DEFAULT_INVERT_ABACUS_SCROLL = false;
        double DEFAULT_GRID_SNAP_THRESHOLD = 0.5;
    }

    public interface ServerConfigAccess {
        int opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort();

        int maxRecurseDepth();

        int maxSpellCircleLength();

        boolean isActionAllowed(ResourceLocation actionID);

        boolean isActionAllowedInCircles(ResourceLocation actionID);

        boolean doVillagersTakeOffenseAtMindMurder();

        ScrollQuantity scrollsForLootTable(ResourceLocation lootTable);

        int DEFAULT_MAX_RECURSE_DEPTH = 64;
        int DEFAULT_MAX_SPELL_CIRCLE_LENGTH = 1024;
        int DEFAULT_OP_BREAK_HARVEST_LEVEL = 3;
        List<String> DEFAULT_FEW_SCROLL_TABLES = List.of("minecraft:chests/jungle_temple",
            "minecraft:chests/simple_dungeon", "minecraft:chests/village/village_cartographer");
        List<String> DEFAULT_SOME_SCROLL_TABLES = List.of("minecraft:chests/bastion_treasure",
            "minecraft:chests/shipwreck_map");
        List<String> DEFAULT_MANY_SCROLL_TABLES = List.of("minecraft:chests/stronghold_library");
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

    // Simple extensions for resource location configs
    public static boolean anyMatch(List<? extends String> keys, ResourceLocation key) {
        return keys.stream().map(ResourceLocation::new).anyMatch(key::equals);
    }

    public static boolean noneMatch(List<? extends String> keys, ResourceLocation key) {
        return keys.stream().map(ResourceLocation::new).noneMatch(key::equals);
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
