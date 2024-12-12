package at.petrak.hexcasting.common.loot;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/common/loot/LootHandler.java
// We need to inject dungeon loot (scrolls and lore), make amethyst drop fewer shards, and the extra dust stuff.
// On forge:
// - Scrolls and lore are done with a loot mod
// - Amethyst drop fiddling is done with another loot mod; the shard delta is in the loot mod data and the rest of
//   the stuff is loaded from TABLE_INJECT_AMETHYST_CLUSTER
// On fabric:
// - Scrolls and lore are done with a lootLoad listener and the amounts are loaded from config
// - Amethyst shard reduction is done with a loot function mixed in to always be on amethyst clusters, god, cause I
//   don't think it's facile to use the loot pool api to try to figure out which pool is for the amethyst and reduce it
// - Amethyst dust and crystals are done by adding the loot table Forge uses in directly via listener
public class HexLootHandler {
    public static final ImmutableList<ScrollInjection> DEFAULT_SCROLL_INJECTS = ImmutableList.of(
        // TODO: not sure what the lore implications of scrolls and the nether/end are. adding scrolls
        // there for now just to be nice to players.

        // In places where it doesn't really make sense to have them lore-wise just put them rarely anyways
        // to make it less of a PITA for new players
        new ScrollInjection(new ResourceLocation("minecraft", "chests/simple_dungeon"), 1),
        new ScrollInjection(new ResourceLocation("minecraft", "chests/abandoned_mineshaft"), 1),
        new ScrollInjection(new ResourceLocation("minecraft", "chests/bastion_other"), 1),
        new ScrollInjection(new ResourceLocation("minecraft", "chests/nether_bridge"), 1),

        new ScrollInjection(new ResourceLocation("minecraft", "chests/jungle_temple"), 2),
        new ScrollInjection(new ResourceLocation("minecraft", "chests/desert_pyramid"), 2),
        new ScrollInjection(new ResourceLocation("minecraft", "chests/village/village_cartographer"), 2),

        new ScrollInjection(new ResourceLocation("minecraft", "chests/shipwreck_map"), 3),
        new ScrollInjection(new ResourceLocation("minecraft", "chests/bastion_treasure"), 3),
        new ScrollInjection(new ResourceLocation("minecraft", "chests/end_city_treasure"), 3),

        // ancient city chests have amethyst in them, thinking emoji
        new ScrollInjection(new ResourceLocation("minecraft", "chests/ancient_city"), 4),
        // wonder what those pillagers are up to with those scrolls
        new ScrollInjection(new ResourceLocation("minecraft", "chests/pillager_outpost"), 4),

        // if you manage to find one of these things you deserve a lot of scrolls
        new ScrollInjection(new ResourceLocation("minecraft", "chests/woodland_mansion"), 5),
        new ScrollInjection(new ResourceLocation("minecraft", "chests/stronghold_library"), 5),

        // inject into our own (otherwise empty) table to spawn a guaranteed scroll
        new ScrollInjection(new ResourceLocation("hexcasting","random_scroll"), -1)
    );

    public static final ImmutableList<ResourceLocation> DEFAULT_LORE_INJECTS = ImmutableList.of(
        new ResourceLocation("minecraft", "chests/simple_dungeon"),
        new ResourceLocation("minecraft", "chests/abandoned_mineshaft"),
        new ResourceLocation("minecraft", "chests/pillager_outpost"),
        new ResourceLocation("minecraft", "chests/woodland_mansion"),
        new ResourceLocation("minecraft", "chests/stronghold_library"),
        // >:)
        new ResourceLocation("minecraft", "chests/village/village_desert_house"),
        new ResourceLocation("minecraft", "chests/village/village_plains_house"),
        new ResourceLocation("minecraft", "chests/village/village_savanna_house"),
        new ResourceLocation("minecraft", "chests/village/village_snowy_house"),
        new ResourceLocation("minecraft", "chests/village/village_taiga_house")
    );

    public static final ImmutableList<ResourceLocation> DEFAULT_CYPHER_INJECTS = ImmutableList.of(
        new ResourceLocation("minecraft", "chests/simple_dungeon"),
        new ResourceLocation("minecraft", "chests/abandoned_mineshaft"),
        new ResourceLocation("minecraft", "chests/stronghold_corridor"),
        new ResourceLocation("minecraft", "chests/jungle_temple"),
        new ResourceLocation("minecraft", "chests/desert_pyramid"),
        new ResourceLocation("minecraft", "chests/ancient_city"),
        new ResourceLocation("minecraft", "chests/nether_bridge"),
        // this one is hardcoded to always give a cypher, just like random_scroll
        new ResourceLocation("hexcasting", "random_cypher")
    );

    public static final ImmutableList<List<String>> DEFAULT_LOOT_HEXES = ImmutableList.of(
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

    public static int getScrollCount(int range, RandomSource random) {
        return Math.max(random.nextIntBetweenInclusive(-range, range), 0);
    }

    public static final double DEFAULT_SHARD_MODIFICATION = -0.5;
    public static final double DEFAULT_LORE_CHANCE = 0.4;
    public static final double DEFAULT_CYPHER_CHANCE = 0.4;

    public static final ResourceLocation TABLE_INJECT_AMETHYST_CLUSTER = modLoc("inject/amethyst_cluster");

    public record ScrollInjection(ResourceLocation injectee, int countRange) {
    }
}
