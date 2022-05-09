package at.petrak.hexcasting.api.mod;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexStatistics {
    public static ResourceLocation MANA_USED;
    public static ResourceLocation MANA_OVERCASTED;
    public static ResourceLocation PATTERNS_DRAWN;
    public static ResourceLocation SPELLS_CAST;

    public static void register() {
        MANA_USED = makeCustomStat("mana_used",
            manamount -> StatFormatter.DEFAULT.format(manamount / HexConfig.common().dustManaAmount())
        );
        MANA_OVERCASTED = makeCustomStat("mana_overcasted",
            manamount -> StatFormatter.DEFAULT.format(manamount / HexConfig.common().dustManaAmount())
        );
        PATTERNS_DRAWN = makeCustomStat("patterns_drawn", StatFormatter.DEFAULT);
        SPELLS_CAST = makeCustomStat("spells_cast", StatFormatter.DEFAULT);
    }

    private static ResourceLocation makeCustomStat(String pKey, StatFormatter pFormatter) {
        ResourceLocation resourcelocation = modLoc(pKey);
        Registry.register(Registry.CUSTOM_STAT, pKey, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, pFormatter);
        return resourcelocation;
    }
}
