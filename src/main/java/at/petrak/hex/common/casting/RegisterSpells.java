package at.petrak.hex.common.casting;

import at.petrak.hex.HexMod;
import at.petrak.hex.api.PatternRegistry;
import at.petrak.hex.api.SpellOperator;
import at.petrak.hex.common.casting.operators.*;
import at.petrak.hex.common.casting.operators.math.*;
import at.petrak.hex.common.casting.operators.spells.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class RegisterSpells {
    // I guess this means the client will have a big empty map for patterns
    @SubscribeEvent
    public static void registerSpellPatterns(FMLCommonSetupEvent evt) {
        int count = 0;
        try {
            // In general:
            // - CCW is the normal or construction version
            // - CW is the special or destruction version
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
                    new Pair<>("eqqqqq", OpConstructVec.INSTANCE),
                    new Pair<>("qeeeee", OpDeconstructVec.INSTANCE),

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

                    // == Consts ==
                    new Pair<>("qqqqqqea", SpellOperator.makeConstantOp(SpellDatum.make(new Vec3(1.0, 0.0, 0.0)))),
                    new Pair<>("qqqqqqew", SpellOperator.makeConstantOp(SpellDatum.make(new Vec3(1.0, 1.0, 0.0)))),
                    new Pair<>("qqqqqqed", SpellOperator.makeConstantOp(SpellDatum.make(new Vec3(1.0, 0.0, 1.0)))),
                    new Pair<>("eeeeeeqa", SpellOperator.makeConstantOp(SpellDatum.make(new Vec3(-1.0, 0.0, 0.0)))),
                    new Pair<>("eeeeeeqw", SpellOperator.makeConstantOp(SpellDatum.make(new Vec3(1.0, -1.0, 0.0)))),
                    new Pair<>("eeeeeeqd", SpellOperator.makeConstantOp(SpellDatum.make(new Vec3(1.0, 0.0, -1.0)))),
                    
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
