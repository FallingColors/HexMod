package at.petrak.hexcasting.api.mod;

import at.petrak.hexcasting.HexMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HexStatistics {
    public static ResourceLocation MANA_USED;
    public static ResourceLocation MANA_OVERCASTED;
    public static ResourceLocation PATTERNS_DRAWN;
    public static ResourceLocation SPELLS_CAST;

    // We need to listen to *something* so we don't fire too early
    @SubscribeEvent
    public static void register(RegistryEvent.Register<Block> evt) {
        MANA_USED = makeCustomStat("mana_used",
            manamount -> StatFormatter.DEFAULT.format(manamount / HexConfig.dustManaAmount.get())
        );
        MANA_OVERCASTED = makeCustomStat("mana_overcasted",
            manamount -> StatFormatter.DEFAULT.format(manamount / HexConfig.dustManaAmount.get())
        );
        PATTERNS_DRAWN = makeCustomStat("patterns_drawn", StatFormatter.DEFAULT);
        SPELLS_CAST = makeCustomStat("spells_cast", StatFormatter.DEFAULT);
    }

    private static ResourceLocation makeCustomStat(String pKey, StatFormatter pFormatter) {
        ResourceLocation resourcelocation = new ResourceLocation(HexMod.MOD_ID, pKey);
        Registry.register(Registry.CUSTOM_STAT, pKey, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, pFormatter);
        return resourcelocation;
    }
}
