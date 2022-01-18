package at.petrak.hex.common.lib;

import at.petrak.hex.HexMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class HexStatistics {
    public static final ResourceLocation MANA_USED = makeCustomStat(HexMod.MOD_ID + ":mana_used",
            (manamount) -> String.valueOf(manamount / HexMod.CONFIG.dustManaAmount.get())
    );
    public static final ResourceLocation MANA_OVERCASTED = makeCustomStat(HexMod.MOD_ID + ":mana_overcasted",
            (manamount) -> String.valueOf(manamount / HexMod.CONFIG.dustManaAmount.get())
    );

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
