package at.petrak.hexcasting.common.casting;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.api.Operator;
import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.SpellDatum;
import at.petrak.hexcasting.common.casting.operators.*;
import at.petrak.hexcasting.common.casting.operators.lists.OpAppend;
import at.petrak.hexcasting.common.casting.operators.lists.OpConcat;
import at.petrak.hexcasting.common.casting.operators.lists.OpForEach;
import at.petrak.hexcasting.common.casting.operators.lists.OpIndex;
import at.petrak.hexcasting.common.casting.operators.math.*;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetCaster;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntitiesBy;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntityAt;
import at.petrak.hexcasting.common.casting.operators.spells.*;
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight;
import at.petrak.hexcasting.common.casting.operators.spells.great.OpLightning;
import at.petrak.hexcasting.common.items.magic.ItemArtifact;
import at.petrak.hexcasting.common.items.magic.ItemCypher;
import at.petrak.hexcasting.common.items.magic.ItemTrinket;
import at.petrak.hexcasting.hexmath.HexDir;
import at.petrak.hexcasting.hexmath.HexPattern;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static at.petrak.hexcasting.common.lib.RegisterHelper.prefix;

public class RegisterPatterns {
    // I guess this means the client will have a big empty map for patterns
    @SubscribeEvent
    public static void registerSpellPatterns(FMLCommonSetupEvent evt) {
        int count = 0;
        try {
            // In general:
            // - CCW is the normal or construction version
            // - CW is the special or destruction version
            // == Getters ==

            PatternRegistry.addRegularPatternAndMirror("qaq", OpGetCaster.INSTANCE);
            PatternRegistry.addRegularPatternAndMirror("aa", OpEntityPos.INSTANCE);
            PatternRegistry.addRegularPatternAndMirror("wa", OpEntityLook.INSTANCE);

            PatternRegistry.addRegularPattern("wqaawdd", OpBlockRaycast.INSTANCE);
            PatternRegistry.addRegularPattern("weddwaa", OpBlockAxisRaycast.INSTANCE);
            PatternRegistry.addRegularPattern("weaqa", OpEntityRaycast.INSTANCE);

            PatternRegistry.addRegularPattern("edqde", OpAppend.INSTANCE);
            PatternRegistry.addRegularPattern("qaeaq", OpConcat.INSTANCE);
            PatternRegistry.addRegularPattern("deeed", OpIndex.INSTANCE);
            PatternRegistry.addRegularPattern("dadad", OpForEach.INSTANCE);

            PatternRegistry.addRegularPattern("qqqqqdaqa", new OpGetEntityAt(e -> true));
            PatternRegistry.addRegularPattern("qqqqqdaqaqwa", new OpGetEntityAt(OpGetEntitiesBy::isAnimal));
            PatternRegistry.addRegularPattern("qqqqqdaqaqwq", new OpGetEntityAt(OpGetEntitiesBy::isMonster));
            PatternRegistry.addRegularPattern("qqqqqdaqaqww", new OpGetEntityAt(OpGetEntitiesBy::isItem));
            PatternRegistry.addRegularPattern("qqqqqdaqaqwe", new OpGetEntityAt(OpGetEntitiesBy::isPlayer));
            PatternRegistry.addRegularPattern("qqqqqdaqaqwd", new OpGetEntityAt(OpGetEntitiesBy::isLiving));
            PatternRegistry.addRegularPattern("qqqqqwded", new OpGetEntitiesBy(e -> true, false));
            PatternRegistry.addRegularPattern("qqqqqwdeddwa", new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, false));
            PatternRegistry.addRegularPattern("eeeeewaqaawa", new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, true));
            PatternRegistry.addRegularPattern("qqqqqwdeddwq", new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, false));
            PatternRegistry.addRegularPattern("eeeeewaqaawq", new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, true));
            PatternRegistry.addRegularPattern("qqqqqwdeddww", new OpGetEntitiesBy(OpGetEntitiesBy::isItem, false));
            PatternRegistry.addRegularPattern("eeeeewaqaaww", new OpGetEntitiesBy(OpGetEntitiesBy::isItem, true));
            PatternRegistry.addRegularPattern("qqqqqwdeddwe", new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, false));
            PatternRegistry.addRegularPattern("eeeeewaqaawe", new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, true));
            PatternRegistry.addRegularPattern("qqqqqwdeddwd", new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, false));
            PatternRegistry.addRegularPattern("eeeeewaqqawd", new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, true));

            // == Modify Stack ==

            PatternRegistry.addRegularPattern("a", OpUndo.INSTANCE);
            PatternRegistry.addRegularPattern("d", Widget.NULL);
            PatternRegistry.addRegularPattern("aadaa", OpDuplicate.INSTANCE);
            PatternRegistry.addRegularPattern("aawdd", OpSwap.INSTANCE);

            // == Math ==
            PatternRegistry.addRegularPattern("waaw", OpAdd.INSTANCE);
            PatternRegistry.addRegularPattern("wddw", OpSub.INSTANCE);
            PatternRegistry.addRegularPattern("waqaw", OpMulDot.INSTANCE);
            PatternRegistry.addRegularPattern("wdedw", OpDivCross.INSTANCE);
            PatternRegistry.addRegularPattern("wqaqw", OpAbsLen.INSTANCE);
            PatternRegistry.addRegularPattern("wedew", OpPowProj.INSTANCE);
            PatternRegistry.addRegularPattern("eqqqqq", OpConstructVec.INSTANCE);
            PatternRegistry.addRegularPattern("qeeeee", OpDeconstructVec.INSTANCE);

            // == Spells ==

            PatternRegistry.addRegularPatternAndMirror("de", OpPrint.INSTANCE);
            PatternRegistry.addRegularPatternPerWorld(HexPattern.FromAnglesSig("aawaawaa", HexDir.EAST),
                    prefix("explode"), new OpExplode(false));
            PatternRegistry.addRegularPatternPerWorld(HexPattern.FromAnglesSig("aawqaqwaa", HexDir.EAST),
                    prefix("explode/fireball"), new OpExplode(true));

            PatternRegistry.addRegularPatternPerWorld(HexPattern.FromAnglesSig("waqwaeawq", HexDir.EAST),
                    prefix("add_motion"), OpAddMotion.INSTANCE);
            PatternRegistry.addRegularPatternPerWorld(HexPattern.FromAnglesSig("aqwaeawq", HexDir.EAST),
                    prefix("blink"), OpBlink.INSTANCE);
            PatternRegistry.addRegularPatternPerWorld(HexPattern.FromAnglesSig("qaqqqqq", HexDir.EAST),
                    prefix("break_block"), OpBreakBlock.INSTANCE);
            PatternRegistry.addRegularPatternPerWorld(HexPattern.FromAnglesSig("weeeeedqe", HexDir.NORTH_EAST),
                    prefix("place_block"), OpPlaceBlock.INSTANCE);
            PatternRegistry.addRegularPatternPerWorld(HexPattern.FromAnglesSig("waqqqqq", HexDir.EAST),
                    prefix("make_cypher"), new OpMakePackagedSpell<>(ItemCypher.class, 100_000));
            PatternRegistry.addRegularPatternPerWorld(HexPattern.FromAnglesSig("wwaqqqqqeaqeaeqqqeaeq", HexDir.EAST),
                    prefix("make_trinket"), new OpMakePackagedSpell<>(ItemTrinket.class, 500_000));
            PatternRegistry.addRegularPatternPerWorld(
                    HexPattern.FromAnglesSig("wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq", HexDir.EAST),
                    prefix("make_artifact"), new OpMakePackagedSpell<>(ItemArtifact.class, 1_000_000));
            // great spells are revealed to you automatically ... for now
            PatternRegistry.addRegularPattern("waadwawdaaweewq", OpLightning.INSTANCE);
            PatternRegistry.addRegularPattern("eawwaeawawaa", OpFlight.INSTANCE);

            // == Meta stuff ==
            PatternRegistry.addRegularPattern("qqq", Widget.OPEN_PAREN);
            PatternRegistry.addRegularPattern("eee", Widget.CLOSE_PAREN);
            PatternRegistry.addRegularPattern("qqqaw", Widget.ESCAPE);
            // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
            // eval being a space filling curve feels apt doesn't it
            PatternRegistry.addRegularPattern("deaqq", OpEval.INSTANCE);
            PatternRegistry.addRegularPattern("aqdee", OpEvalDelay.INSTANCE);
            PatternRegistry.addRegularPattern("aqqqqq", OpRead.INSTANCE);
            PatternRegistry.addRegularPattern("deeeee", OpWrite.INSTANCE);

            // == Consts ==
            PatternRegistry.addRegularPattern("qqqqqea",
                    Operator.makeConstantOp(SpellDatum.make(new Vec3(1.0, 0.0, 0.0))));
            PatternRegistry.addRegularPattern("qqqqqew",
                    Operator.makeConstantOp(SpellDatum.make(new Vec3(0.0, 1.0, 0.0))));
            PatternRegistry.addRegularPattern("qqqqqed",
                    Operator.makeConstantOp(SpellDatum.make(new Vec3(0.0, 0.0, 1.0))));
            PatternRegistry.addRegularPattern("eeeeeqa",
                    Operator.makeConstantOp(SpellDatum.make(new Vec3(-1.0, 0.0, 0.0))));
            PatternRegistry.addRegularPattern("eeeeeqw",
                    Operator.makeConstantOp(SpellDatum.make(new Vec3(0.0, -1.0, 0.0))));
            PatternRegistry.addRegularPattern("eeeeeqd",
                    Operator.makeConstantOp(SpellDatum.make(new Vec3(0.0, 0.0, -1.0))));

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

        HexMod.getLogger().info("Registered {} patterns", count);
    }
}
