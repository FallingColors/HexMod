package at.petrak.hex.common.lib;

import at.petrak.hex.HexMod;
import at.petrak.hex.api.Operator;
import at.petrak.hex.api.PatternRegistry;
import at.petrak.hex.api.SpellDatum;
import at.petrak.hex.common.casting.Widget;
import at.petrak.hex.common.casting.operators.*;
import at.petrak.hex.common.casting.operators.lists.OpAppend;
import at.petrak.hex.common.casting.operators.lists.OpConcat;
import at.petrak.hex.common.casting.operators.lists.OpForEach;
import at.petrak.hex.common.casting.operators.lists.OpIndex;
import at.petrak.hex.common.casting.operators.math.*;
import at.petrak.hex.common.casting.operators.selectors.OpGetCaster;
import at.petrak.hex.common.casting.operators.selectors.OpGetEntitiesBy;
import at.petrak.hex.common.casting.operators.selectors.OpGetEntityAt;
import at.petrak.hex.common.casting.operators.spells.*;
import at.petrak.hex.common.casting.operators.spells.great.OpFlight;
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
                    new Pair<>("wqded", OpGetEntityAt.INSTANCE),

                    new Pair<>("edqde", OpAppend.INSTANCE),
                    new Pair<>("qaeaq", OpConcat.INSTANCE),
                    new Pair<>("deeed", OpIndex.INSTANCE),
                    new Pair<>("dadad", OpForEach.INSTANCE),

                    new Pair<>("qqqqqwded", new OpGetEntitiesBy(e -> true, false)),
                    new Pair<>("qqqqqwdeddwa", new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, false)),
                    new Pair<>("eeeeewaqaawa", new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, true)),
                    new Pair<>("qqqqqwdeddwq", new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, false)),
                    new Pair<>("eeeeewaqaawq", new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, true)),
                    new Pair<>("qqqqqwdeddww", new OpGetEntitiesBy(OpGetEntitiesBy::isItem, false)),
                    new Pair<>("eeeeewaqaaww", new OpGetEntitiesBy(OpGetEntitiesBy::isItem, true)),
                    new Pair<>("qqqqqwdeddwe", new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, false)),
                    new Pair<>("eeeeewaqaawe", new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, true)),
                    new Pair<>("qqqqqwdeddwd", new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, false)),
                    new Pair<>("eeeeewaqqawd", new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, true)),

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
                    new Pair<>("wqqqwae", OpBlink.INSTANCE),
                    new Pair<>("qaqqqqq", OpPlaceBlock.INSTANCE),
                    new Pair<>("eeeeede", OpBreakBlock.INSTANCE),
                    new Pair<>("waadwawdaaweewq", OpLightning.INSTANCE),
                    new Pair<>("waqqqqq", new OpMakePackagedSpell<>(ItemCypher.class, 500_000)),
                    new Pair<>("wwaqqqqqeaqeaeqqqeaeq", new OpMakePackagedSpell<>(ItemTrinket.class, 1_000_000)),
                    new Pair<>(
                            "wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq",
                            new OpMakePackagedSpell<>(ItemArtifact.class, 4_000_000)
                    ),
                    new Pair<>("eawwaeawawaa", OpFlight.INSTANCE),

                    // == Meta stuff ==
                    new Pair<>("qqq", Widget.OPEN_PAREN),
                    new Pair<>("eee", Widget.CLOSE_PAREN),
                    new Pair<>("qqqaw", Widget.ESCAPE),
                    // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
                    // eval being a space filling curve feels apt doesn't it
                    new Pair<>("deaqq", OpEval.INSTANCE),
                    new Pair<>("aqdee", OpEvalDelay.INSTANCE),
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
