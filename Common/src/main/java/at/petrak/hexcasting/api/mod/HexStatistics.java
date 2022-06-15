package at.petrak.hexcasting.api.mod;

import at.petrak.hexcasting.api.misc.ManaConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexStatistics {
    public static final ResourceLocation MANA_USED = makeCustomStat("mana_used",
        manamount -> StatFormatter.DEFAULT.format(manamount / ManaConstants.DUST_UNIT));
    public static final ResourceLocation MANA_OVERCASTED = makeCustomStat("mana_overcasted",
        manamount -> StatFormatter.DEFAULT.format(manamount / ManaConstants.DUST_UNIT));
    public static final ResourceLocation PATTERNS_DRAWN = makeCustomStat("patterns_drawn", StatFormatter.DEFAULT);
    public static final ResourceLocation SPELLS_CAST = makeCustomStat("spells_cast", StatFormatter.DEFAULT);

    public static void register() {
        // wake up java
    }

    private static ResourceLocation makeCustomStat(String key, StatFormatter formatter) {
        ResourceLocation resourcelocation = modLoc(key);
        Registry.register(Registry.CUSTOM_STAT, key, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, formatter);
        return resourcelocation;
    }
}
