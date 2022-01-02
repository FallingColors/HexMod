package at.petrak.hex.common.lib;

import at.petrak.hex.HexMod;
import at.petrak.hex.api.Operator;
import at.petrak.hex.api.PatternRegistry;
import at.petrak.hex.common.casting.SpellDatum;
import at.petrak.hex.common.casting.Widget;
import at.petrak.hex.common.casting.operators.*;
import at.petrak.hex.common.casting.operators.math.*;
import at.petrak.hex.common.casting.operators.spells.*;
import at.petrak.hex.common.items.magic.ItemArtifact;
import at.petrak.hex.common.items.magic.ItemCypher;
import at.petrak.hex.common.items.magic.ItemTrinket;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class RegisterPatterns {
    // I guess this means the client will have a big empty map for patterns
    @SubscribeEvent
    public static void registerSpellPatterns(FMLCommonSetupEvent evt) {
        int count = 0;
        try {
            // In general:
            // - CCW is the normal or construction version
            // - CW is the special or destruction version
            for (Pair<String, Operator> p : new Pair[]{
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
                    new Pair<>("d", Widget.NULL),
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
                    new Pair<>("waqqqqq", new OpMakePackagedSpell<>(ItemCypher.class, 500_000)),
                    new Pair<>("wwaqqqqqeaqeaeqqqeaeq", new OpMakePackagedSpell<>(ItemTrinket.class, 1_000_000)),
                    new Pair<>(
                            "wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq",
                            new OpMakePackagedSpell<>(ItemArtifact.class, 4_000_000)
                    ),

                    // == Meta stuff ==
                    new Pair<>("qqq", Widget.OPEN_PAREN),
                    new Pair<>("eee", Widget.CLOSE_PAREN),
                    new Pair<>("qqqaw", Widget.ESCAPE),
                    // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
                    // eval being a space filling curve feels apt doesn't it
                    new Pair<>("deaqq", OpEval.INSTANCE),
                    new Pair<>("aqqqqq", OpRead.INSTANCE),
                    new Pair<>("deeeee", OpWrite.INSTANCE),

                    // == Consts ==
                    new Pair<>("qqqqqea", Operator.makeConstantOp(SpellDatum.make(new Vec3(1.0, 0.0, 0.0)))),
                    new Pair<>("qqqqqew", Operator.makeConstantOp(SpellDatum.make(new Vec3(0.0, 1.0, 0.0)))),
                    new Pair<>("qqqqqed", Operator.makeConstantOp(SpellDatum.make(new Vec3(0.0, 0.0, 1.0)))),
                    new Pair<>("eeeeeqa", Operator.makeConstantOp(SpellDatum.make(new Vec3(-1.0, 0.0, 0.0)))),
                    new Pair<>("eeeeeqw", Operator.makeConstantOp(SpellDatum.make(new Vec3(0.0, -1.0, 0.0)))),
                    new Pair<>("eeeeeqd", Operator.makeConstantOp(SpellDatum.make(new Vec3(0.0, 0.0, -1.0)))),

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
                return Operator.makeConstantOp(SpellDatum.make(accumulator));
            } else {
                return null;
            }
        });

        HexMod.LOGGER.info("Registered {} patterns", count);
    }
}
