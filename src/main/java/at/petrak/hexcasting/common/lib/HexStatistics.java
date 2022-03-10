package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.HexConfig;
import at.petrak.hexcasting.HexMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class HexStatistics {
    public static final ResourceLocation MANA_USED = makeCustomStat(HexMod.MOD_ID + ":mana_used",
        manamount -> StatFormatter.DEFAULT.format(manamount / HexConfig.dustManaAmount.get())
    );
    public static final ResourceLocation MANA_OVERCASTED = makeCustomStat(HexMod.MOD_ID + ":mana_overcasted",
        manamount -> StatFormatter.DEFAULT.format(manamount / HexConfig.dustManaAmount.get())
    );
    public static final ResourceLocation PATTERNS_DRAWN = makeCustomStat(HexMod.MOD_ID + ":patterns_drawn",
        StatFormatter.DEFAULT);
    public static final ResourceLocation SPELLS_CAST = makeCustomStat(HexMod.MOD_ID + ":spells_cast",
        StatFormatter.DEFAULT);

    public static void register() {
        // No-op! Just to un-lazy this class.
    }

    private static ResourceLocation makeCustomStat(String pKey, StatFormatter pFormatter) {
        ResourceLocation resourcelocation = new ResourceLocation(pKey);
        Registry.register(Registry.CUSTOM_STAT, pKey, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, pFormatter);
        return resourcelocation;
    }
}
