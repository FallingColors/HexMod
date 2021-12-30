package at.petrak.hex;

import at.petrak.hex.api.PatternRegistry;
import at.petrak.hex.api.SpellOperator;
import at.petrak.hex.common.casting.SpellDatum;
import at.petrak.hex.common.casting.SpellWidget;
import at.petrak.hex.common.casting.operators.*;
import at.petrak.hex.common.casting.operators.math.*;
import at.petrak.hex.common.casting.operators.spells.*;
import at.petrak.hex.common.items.HexItems;
import at.petrak.hex.common.network.HexMessages;
import at.petrak.hex.server.TickScheduler;
import com.mojang.datafixers.util.Pair;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(HexMod.MOD_ID)
public class HexMod {
    // hmm today I will use a popular logging framework :clueless:
    public static final Logger LOGGER = LogManager.getLogger();
    public static final HexConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    static {
        final var specPair = new ForgeConfigSpec.Builder().configure(HexConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public static final String MOD_ID = "hex";

    public HexMod() {
        // Register ourselves for server and other game events we are interested in
        var evbus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        evbus.register(HexMod.class);

        HexItems.ITEMS.register(evbus);
        HexMessages.register();
        MinecraftForge.EVENT_BUS.register(TickScheduler.INSTANCE);
    }

    // I guess this means the client will have a big empty map for patterns
    @SubscribeEvent
    public static void registerSpellPatterns(FMLCommonSetupEvent evt) {
        int count = 0;
        try {
            for (Pair<String, SpellOperator> p : new Pair[]{
                    // == Getters ==

                    new Pair<>("qaq", OpGetCaster.INSTANCE),
                    new Pair<>("ede", OpGetCaster.INSTANCE),
                    new Pair<>("aa", OpEntityPos.INSTANCE),
                    new Pair<>("dd", OpEntityPos.INSTANCE),
                    new Pair<>("wa", OpEntityLook.INSTANCE),
                    new Pair<>("wd", OpEntityLook.INSTANCE),

                    new Pair<>("wqaawdd", OpBlockRaycast.INSTANCE),
                    new Pair<>("weddwaa", OpBlockAxisRaycast.INSTANCE),
                    new Pair<>("weaqa", OpEntityRaycast.INSTANCE),

                    // == Modify Stack ==

                    new Pair<>("a", OpUndo.INSTANCE),
                    new Pair<>("d", SpellWidget.NULL),
                    new Pair<>("aadaa", OpDuplicate.INSTANCE),
                    new Pair<>("aawdd", OpSwap.INSTANCE),

                    // == Math ==
                    new Pair<>("waaw", OpAdd.INSTANCE),
                    new Pair<>("wddw", OpSub.INSTANCE),
                    new Pair<>("waqaw", OpMulDot.INSTANCE),
                    new Pair<>("wdedw", OpDivCross.INSTANCE),
                    new Pair<>("wqaqw", OpAbsLen.INSTANCE),
                    new Pair<>("wedew", OpPowProj.INSTANCE),

                    // == Spells ==

                    new Pair<>("de", OpPrint.INSTANCE),
                    new Pair<>("aq", OpPrint.INSTANCE),
                    new Pair<>("aawaawaa", OpExplode.INSTANCE),
                    new Pair<>("weeewdq", OpAddMotion.INSTANCE),
                    new Pair<>("qaqqqqq", OpPlaceBlock.INSTANCE),
                    new Pair<>("eeeeede", OpBreakBlock.INSTANCE),
                    new Pair<>("waadwawdaaweewq", OpLightning.INSTANCE),

                    // == Meta stuff ==
                    new Pair<>("qqq", SpellWidget.OPEN_PAREN),
                    new Pair<>("eee", SpellWidget.CLOSE_PAREN),
                    new Pair<>("qqqaw", SpellWidget.ESCAPE),
                    // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
                    // eval being a space filling curve feels apt doesn't it
                    new Pair<>("deaqq", OpEval.INSTANCE),
                    new Pair<>("aqqqqq", OpRead.INSTANCE),
                    new Pair<>("deeeee", OpWrite.INSTANCE),
            }) {
                PatternRegistry.addRegularPattern(p.getFirst(), p.getSecond());
                count++;
            }
        } catch (PatternRegistry.RegisterPatternException exn) {
            exn.printStackTrace();
        }

        // Add zilde->number
        PatternRegistry.addSpecialHandler(pat -> {
            var sig = pat.anglesSignature();
            if (sig.startsWith("aqaa") || sig.startsWith("dedd")) {
                var negate = sig.startsWith("dedd");
                var accumulator = 0.0;
                for (char ch : sig.substring(4).toCharArray()) {
                    if (ch == 'w') {
                        accumulator += 1;
                    } else if (ch == 'q') {
                        accumulator += 5;
                    } else if (ch == 'e') {
                        accumulator += 10;
                    } else if (ch == 'a') {
                        accumulator *= 2;
                    } else if (ch == 'd') {
                        accumulator /= 2;
                    }
                }
                if (negate) {
                    accumulator = -accumulator;
                }
                return SpellOperator.makeConstantOp(SpellDatum.make(accumulator));
            } else {
                return null;
            }
        });

        HexMod.LOGGER.info("Registered {} patterns", count);
    }
}
