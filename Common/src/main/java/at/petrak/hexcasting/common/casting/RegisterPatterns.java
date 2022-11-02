package at.petrak.hexcasting.common.casting;

import at.petrak.hexcasting.api.PatternRegistryBak;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.api.spell.Action;
import at.petrak.hexcasting.api.spell.iota.DoubleIota;
import at.petrak.hexcasting.api.spell.iota.NullIota;
import at.petrak.hexcasting.api.spell.iota.Vec3Iota;
import at.petrak.hexcasting.api.spell.math.HexAngle;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.casting.operators.*;
import at.petrak.hexcasting.common.casting.operators.akashic.OpAkashicRead;
import at.petrak.hexcasting.common.casting.operators.akashic.OpAkashicWrite;
import at.petrak.hexcasting.common.casting.operators.circles.OpCircleBounds;
import at.petrak.hexcasting.common.casting.operators.circles.OpImpetusDir;
import at.petrak.hexcasting.common.casting.operators.circles.OpImpetusPos;
import at.petrak.hexcasting.common.casting.operators.eval.OpEval;
import at.petrak.hexcasting.common.casting.operators.eval.OpForEach;
import at.petrak.hexcasting.common.casting.operators.eval.OpHalt;
import at.petrak.hexcasting.common.casting.operators.lists.*;
import at.petrak.hexcasting.common.casting.operators.local.OpPeekLocal;
import at.petrak.hexcasting.common.casting.operators.local.OpPushLocal;
import at.petrak.hexcasting.common.casting.operators.math.*;
import at.petrak.hexcasting.common.casting.operators.math.bit.*;
import at.petrak.hexcasting.common.casting.operators.math.logic.*;
import at.petrak.hexcasting.common.casting.operators.math.trig.*;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetCaster;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntitiesBy;
import at.petrak.hexcasting.common.casting.operators.selectors.OpGetEntityAt;
import at.petrak.hexcasting.common.casting.operators.spells.*;
import at.petrak.hexcasting.common.casting.operators.spells.great.*;
import at.petrak.hexcasting.common.casting.operators.spells.sentinel.OpCreateSentinel;
import at.petrak.hexcasting.common.casting.operators.spells.sentinel.OpDestroySentinel;
import at.petrak.hexcasting.common.casting.operators.spells.sentinel.OpGetSentinelPos;
import at.petrak.hexcasting.common.casting.operators.spells.sentinel.OpGetSentinelWayfind;
import at.petrak.hexcasting.common.casting.operators.stack.*;
import at.petrak.hexcasting.common.lib.HexItems;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class RegisterPatterns {
    // I guess this means the client will have a big empty map for patterns
    public static void registerPatterns() {
        try {
            // In general:
            // - CCW is the normal or construction version
            // - CW is the special or destruction version
            // == Getters ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qaq", HexDir.NORTH_EAST), modLoc("get_caster"),
                OpGetCaster.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aa", HexDir.EAST), modLoc("get_entity_pos"),
                OpEntityPos.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wa", HexDir.EAST), modLoc("get_entity_look"),
                OpEntityLook.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("awq", HexDir.NORTH_EAST), modLoc("get_entity_height"),
                OpEntityHeight.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wq", HexDir.EAST), modLoc("get_entity_velocity"),
                OpEntityVelocity.INSTANCE);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wqaawdd", HexDir.EAST), modLoc("raycast"),
                OpBlockRaycast.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("weddwaa", HexDir.EAST), modLoc("raycast/axis"),
                OpBlockAxisRaycast.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("weaqa", HexDir.EAST), modLoc("raycast/entity"),
                OpEntityRaycast.INSTANCE);

            // == spell circle getters ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eaqwqae", HexDir.SOUTH_WEST),
                modLoc("circle/impetus_pos"), OpImpetusPos.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eaqwqaewede", HexDir.SOUTH_WEST),
                modLoc("circle/impetus_dir"), OpImpetusDir.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eaqwqaewdd", HexDir.SOUTH_WEST),
                modLoc("circle/bounds/min"), new OpCircleBounds(false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aqwqawaaqa", HexDir.WEST),
                modLoc("circle/bounds/max"), new OpCircleBounds(true));

            // == Modify Stack ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aadaa", HexDir.EAST), modLoc("duplicate"),
                OpDuplicate.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aadaadaa", HexDir.EAST), modLoc("duplicate_n"),
                OpDuplicateN.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qwaeawqaeaqa", HexDir.NORTH_WEST), modLoc("stack_len"),
                OpStackSize.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aawdd", HexDir.EAST), modLoc("swap"), OpSwap.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ddad", HexDir.WEST), modLoc("fisherman"),
                OpFisherman.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qaawdde", HexDir.SOUTH_EAST), modLoc("swizzle"),
                OpAlwinfyHasAscendedToABeingOfPureMath.INSTANCE);

            // == Math ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waaw", HexDir.NORTH_EAST), modLoc("add"),
                OpAdd.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wddw", HexDir.NORTH_WEST), modLoc("sub"),
                OpSub.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waqaw", HexDir.SOUTH_EAST), modLoc("mul_dot"),
                OpMulDot.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wdedw", HexDir.NORTH_EAST), modLoc("div_cross"),
                OpDivCross.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wqaqw", HexDir.NORTH_EAST), modLoc("abs_len"),
                OpAbsLen.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wedew", HexDir.NORTH_WEST), modLoc("pow_proj"),
                OpPowProj.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ewq", HexDir.EAST), modLoc("floor"),
                OpFloor.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qwe", HexDir.EAST), modLoc("ceil"),
                OpCeil.INSTANCE);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eqqqqq", HexDir.EAST), modLoc("construct_vec"),
                OpConstructVec.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qeeeee", HexDir.EAST), modLoc("deconstruct_vec"),
                OpDeconstructVec.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqaww", HexDir.NORTH_WEST), modLoc("coerce_axial"),
                OpCoerceToAxial.INSTANCE);

            // == Logic ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wdw", HexDir.NORTH_EAST), modLoc("and"),
                OpBoolAnd.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waw", HexDir.SOUTH_EAST), modLoc("or"),
                OpBoolOr.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("dwa", HexDir.NORTH_WEST), modLoc("xor"),
                OpBoolXor.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("e", HexDir.SOUTH_EAST), modLoc("greater"),
                new OpCompare(false, (a, b) -> a > b));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("q", HexDir.SOUTH_WEST), modLoc("less"),
                new OpCompare(false, (a, b) -> a < b));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ee", HexDir.SOUTH_EAST), modLoc("greater_eq"),
                new OpCompare(true, (a, b) -> a >= b));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qq", HexDir.SOUTH_WEST), modLoc("less_eq"),
                new OpCompare(true, (a, b) -> a <= b));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ad", HexDir.EAST), modLoc("equals"),
                new OpEquality(false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("da", HexDir.EAST), modLoc("not_equals"),
                new OpEquality(true));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("dw", HexDir.NORTH_WEST), modLoc("not"),
                OpBoolNot.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aw", HexDir.NORTH_EAST), modLoc("identity"),
                OpBoolIdentityKindOf.INSTANCE);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eqqq", HexDir.NORTH_WEST), modLoc("random"),
                OpRandom.INSTANCE);

            // == Advanced Math ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqaa", HexDir.SOUTH_EAST), modLoc("sin"),
                OpSin.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqad", HexDir.SOUTH_EAST), modLoc("cos"),
                OpCos.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wqqqqqadq", HexDir.SOUTH_WEST), modLoc("tan"),
                OpTan.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ddeeeee", HexDir.SOUTH_EAST), modLoc("arcsin"),
                OpArcSin.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("adeeeee", HexDir.NORTH_EAST), modLoc("arccos"),
                OpArcCos.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eadeeeeew", HexDir.NORTH_EAST), modLoc("arctan"),
                OpArcTan.INSTANCE);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eqaqe", HexDir.NORTH_WEST), modLoc("logarithm"),
                OpLog.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("addwaad", HexDir.NORTH_EAST), modLoc("modulo"),
                OpModulo.INSTANCE);

            // == Sets ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wdweaqa", HexDir.NORTH_EAST), modLoc("and_bit"),
                OpAnd.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waweaqa", HexDir.SOUTH_EAST), modLoc("or_bit"),
                OpOr.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("dwaeaqa", HexDir.NORTH_WEST), modLoc("xor_bit"),
                OpXor.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("dweaqa", HexDir.NORTH_WEST), modLoc("not_bit"),
                OpNot.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aweaqa", HexDir.NORTH_EAST), modLoc("to_set"),
                OpToSet.INSTANCE);

            // == Spells ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("de", HexDir.NORTH_EAST), modLoc("print"),
                OpPrint.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aawaawaa", HexDir.EAST), modLoc("explode"),
                new OpExplode(false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ddwddwdd", HexDir.EAST), modLoc("explode/fire"),
                new OpExplode(true));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("awqqqwaqw", HexDir.SOUTH_WEST), modLoc("add_motion"),
                OpAddMotion.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("awqqqwaq", HexDir.SOUTH_WEST), modLoc("blink"),
                OpBlink.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qaqqqqq", HexDir.EAST), modLoc("break_block"),
                OpBreakBlock.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeede", HexDir.SOUTH_WEST), modLoc("place_block"),
                OpPlaceBlock.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("awddwqawqwawq", HexDir.EAST),
                modLoc("colorize"),
                OpColorize.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aqawqadaq", HexDir.SOUTH_EAST), modLoc("create_water"),
                OpCreateWater.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("dedwedade", HexDir.SOUTH_WEST),
                modLoc("destroy_water"),
                OpDestroyWater.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aaqawawa", HexDir.SOUTH_EAST), modLoc("ignite"),
                OpIgnite.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ddedwdwd", HexDir.SOUTH_WEST), modLoc("extinguish"),
                OpExtinguish.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqa", HexDir.NORTH_EAST), modLoc("conjure_block"),
                new OpConjureBlock(false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqd", HexDir.NORTH_EAST), modLoc("conjure_light"),
                new OpConjureBlock(true));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wqaqwawqaqw", HexDir.NORTH_EAST), modLoc("bonemeal"),
                OpTheOnlyReasonAnyoneDownloadedPsi.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqwaeaeaeaeaea", HexDir.NORTH_WEST),
                modLoc("recharge"),
                OpRecharge.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qdqawwaww", HexDir.EAST), modLoc("erase"),
                new OpErase());
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wqaqwd", HexDir.NORTH_EAST), modLoc("edify"),
                OpEdifySapling.INSTANCE);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("adaa", HexDir.WEST), modLoc("beep"),
                OpBeep.INSTANCE);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waqqqqq", HexDir.EAST), modLoc("craft/cypher"),
                new OpMakePackagedSpell<>(HexItems.CYPHER, ManaConstants.CRYSTAL_UNIT));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wwaqqqqqeaqeaeqqqeaeq", HexDir.EAST),
                modLoc("craft/trinket"),
                new OpMakePackagedSpell<>(HexItems.TRINKET, 5 * ManaConstants.CRYSTAL_UNIT));
            PatternRegistryBak.mapPattern(
                HexPattern.fromAngles("wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq", HexDir.EAST),
                modLoc("craft/artifact"),
                new OpMakePackagedSpell<>(HexItems.ARTIFACT, 10 * ManaConstants.CRYSTAL_UNIT));
            PatternRegistryBak.mapPattern(
                HexPattern.fromAngles("aqqqaqwwaqqqqqeqaqqqawwqwqwqwqwqw", HexDir.SOUTH_WEST),
                modLoc("craft/battery"),
                OpMakeBattery.INSTANCE,
                true);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqaqwawaw", HexDir.NORTH_WEST),
                modLoc("potion/weakness"),
                new OpPotionEffect(MobEffects.WEAKNESS, ManaConstants.DUST_UNIT / 10, true, false, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqawwawawd", HexDir.WEST),
                modLoc("potion/levitation"),
                new OpPotionEffect(MobEffects.LEVITATION, ManaConstants.DUST_UNIT / 5, false, false, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqaewawawe", HexDir.SOUTH_WEST),
                modLoc("potion/wither"),
                new OpPotionEffect(MobEffects.WITHER, ManaConstants.DUST_UNIT, true, false, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqadwawaww", HexDir.SOUTH_EAST),
                modLoc("potion/poison"),
                new OpPotionEffect(MobEffects.POISON, ManaConstants.DUST_UNIT / 3, true, false, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqadwawaw", HexDir.SOUTH_EAST),
                modLoc("potion/slowness"),
                new OpPotionEffect(MobEffects.MOVEMENT_SLOWDOWN, ManaConstants.DUST_UNIT / 3, true, false, false));

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqaawawaedd", HexDir.NORTH_WEST),
                modLoc("potion/regeneration"),
                new OpPotionEffect(MobEffects.REGENERATION, ManaConstants.DUST_UNIT, true, true, true), true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqaawawaeqdd", HexDir.WEST),
                modLoc("potion/night_vision"),
                new OpPotionEffect(MobEffects.NIGHT_VISION, ManaConstants.DUST_UNIT / 5, false, true, true), true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqaawawaeqqdd", HexDir.SOUTH_WEST),
                modLoc("potion/absorption"),
                new OpPotionEffect(MobEffects.ABSORPTION, ManaConstants.DUST_UNIT, true, true, true), true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qaawawaeqqqdd", HexDir.SOUTH_EAST),
                modLoc("potion/haste"),
                new OpPotionEffect(MobEffects.DIG_SPEED, ManaConstants.DUST_UNIT / 3, true, true, true), true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aawawaeqqqqdd", HexDir.EAST),
                modLoc("potion/strength"),
                new OpPotionEffect(MobEffects.DAMAGE_BOOST, ManaConstants.DUST_UNIT / 3, true, true, true), true);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waeawae", HexDir.EAST),
                modLoc("sentinel/create"),
                new OpCreateSentinel(false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qdwdqdw", HexDir.NORTH_EAST),
                modLoc("sentinel/destroy"),
                OpDestroySentinel.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waeawaede", HexDir.EAST),
                modLoc("sentinel/get_pos"),
                OpGetSentinelPos.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waeawaedwa", HexDir.EAST),
                modLoc("sentinel/wayfind"),
                OpGetSentinelWayfind.INSTANCE);


            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waadwawdaaweewq", HexDir.EAST),
                modLoc("lightning"), OpLightning.INSTANCE, true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eawwaeawawaa", HexDir.NORTH_WEST),
                modLoc("flight"), OpFlight.INSTANCE, true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eaqawqadaqd", HexDir.EAST),
                modLoc("create_lava"), OpCreateLava.INSTANCE, true);
            PatternRegistryBak.mapPattern(
                HexPattern.fromAngles("wwwqqqwwwqqeqqwwwqqwqqdqqqqqdqq", HexDir.EAST),
                modLoc("teleport"), OpTeleport.INSTANCE, true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("waeawaeqqqwqwqqwq", HexDir.EAST),
                modLoc("sentinel/create/great"),
                new OpCreateSentinel(true), true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeewwweeewwaqqddqdqd", HexDir.EAST),
                modLoc("dispel_rain"),
                new OpWeather(false), true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wwweeewwweewdawdwad", HexDir.WEST),
                modLoc("summon_rain"),
                new OpWeather(true), true);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qeqwqwqwqwqeqaeqeaqeqaeqaqded", HexDir.NORTH_EAST),
                modLoc("brainsweep"),
                OpBrainsweep.INSTANCE, true);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqwqqqqqaq", HexDir.WEST), modLoc("akashic/read"),
                OpAkashicRead.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeweeeeede", HexDir.EAST), modLoc("akashic/write"),
                OpAkashicWrite.INSTANCE);

            // == Meta stuff ==

            // Intro/Retro/Consideration are now special-form-likes and aren't even ops.
            // TODO should there be a registry for these too

            // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
            // eval being a space filling curve feels apt doesn't it
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("deaqq", HexDir.SOUTH_EAST), modLoc("eval"),
                OpEval.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aqdee", HexDir.SOUTH_WEST), modLoc("halt"),
                OpHalt.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aqqqqq", HexDir.EAST), modLoc("read"),
                OpRead.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("deeeee", HexDir.EAST), modLoc("write"),
                OpWrite.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aqqqqqe", HexDir.EAST), modLoc("readable"),
                OpReadable.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("deeeeeq", HexDir.EAST), modLoc("writable"),
                OpWritable.INSTANCE);

            // lorge boyes
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wawqwqwqwqwqw", HexDir.EAST),
                modLoc("read/entity"), OpTheCoolerRead.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wawqwqwqwqwqwew", HexDir.EAST),
                modLoc("readable/entity"), OpTheCoolerReadable.INSTANCE);

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qeewdweddw", HexDir.NORTH_EAST),
                modLoc("read/local"), OpPeekLocal.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eqqwawqaaw", HexDir.NORTH_WEST),
                modLoc("write/local"), OpPushLocal.INSTANCE);

            // == Consts ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("d", HexDir.EAST), modLoc("const/null"),
                Action.makeConstantOp(new NullIota()));

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqea", HexDir.NORTH_WEST), modLoc("const/vec/px"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(1.0, 0.0, 0.0))));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqew", HexDir.NORTH_WEST), modLoc("const/vec/py"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 1.0, 0.0))));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqed", HexDir.NORTH_WEST), modLoc("const/vec/pz"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 1.0))));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeeqa", HexDir.SOUTH_WEST), modLoc("const/vec/nx"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(-1.0, 0.0, 0.0))));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeeqw", HexDir.SOUTH_WEST), modLoc("const/vec/ny"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, -1.0, 0.0))));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeeqd", HexDir.SOUTH_WEST), modLoc("const/vec/nz"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, -1.0))));
            // Yep, this is what I spend the "plain hexagon" pattern on.
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqq", HexDir.NORTH_WEST), modLoc("const/vec/0"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 0.0))));

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qdwdq", HexDir.NORTH_EAST), modLoc("const/double/pi"),
                Action.makeConstantOp(new DoubleIota(Math.PI)));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eawae", HexDir.NORTH_WEST), modLoc("const/double/tau"),
                Action.makeConstantOp(new DoubleIota(HexUtils.TAU)));

            // e
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aaq", HexDir.EAST), modLoc("const/double/e"),
                Action.makeConstantOp(new DoubleIota(Math.E)));

            // == Entities ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqdaqa", HexDir.SOUTH_EAST), modLoc("get_entity"),
                new OpGetEntityAt(e -> true));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqdaqaawa", HexDir.SOUTH_EAST),
                modLoc("get_entity/animal"),
                new OpGetEntityAt(OpGetEntitiesBy::isAnimal));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqdaqaawq", HexDir.SOUTH_EAST),
                modLoc("get_entity/monster"),
                new OpGetEntityAt(OpGetEntitiesBy::isMonster));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqdaqaaww", HexDir.SOUTH_EAST),
                modLoc("get_entity/item"),
                new OpGetEntityAt(OpGetEntitiesBy::isItem));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqdaqaawe", HexDir.SOUTH_EAST),
                modLoc("get_entity/player"),
                new OpGetEntityAt(OpGetEntitiesBy::isPlayer));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqdaqaawd", HexDir.SOUTH_EAST),
                modLoc("get_entity/living"),
                new OpGetEntityAt(OpGetEntitiesBy::isLiving));

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqwded", HexDir.SOUTH_EAST), modLoc("zone_entity"),
                new OpGetEntitiesBy(e -> true, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqwdeddwa", HexDir.SOUTH_EAST),
                modLoc("zone_entity/animal"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeewaqaawa", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_animal"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, true));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqwdeddwq", HexDir.SOUTH_EAST),
                modLoc("zone_entity/monster"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeewaqaawq", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_monster"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, true));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqwdeddww", HexDir.SOUTH_EAST),
                modLoc("zone_entity/item"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isItem, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeewaqaaww", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_item"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isItem, true));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqwdeddwe", HexDir.SOUTH_EAST),
                modLoc("zone_entity/player"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeewaqaawe", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_player"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, true));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqqqwdeddwd", HexDir.SOUTH_EAST),
                modLoc("zone_entity/living"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, false));
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("eeeeewaqaawd", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_living"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, true));

            // == Lists ==

            PatternRegistryBak.mapPattern(HexPattern.fromAngles("edqde", HexDir.SOUTH_WEST), modLoc("append"),
                OpAppend.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST), modLoc("concat"),
                OpConcat.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("deeed", HexDir.NORTH_WEST), modLoc("index"),
                OpIndex.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("dadad", HexDir.NORTH_EAST), modLoc("for_each"),
                OpForEach.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aqaeaq", HexDir.EAST), modLoc("list_size"),
                OpListSize.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("adeeed", HexDir.EAST), modLoc("singleton"),
                OpSingleton.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqaeaae", HexDir.NORTH_EAST), modLoc("empty_list"),
                OpEmptyList.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qqqaede", HexDir.EAST), modLoc("reverse_list"),
                OpReverski.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ewdqdwe", HexDir.SOUTH_WEST), modLoc("last_n_list"),
                OpLastNToList.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qwaeawq", HexDir.NORTH_WEST), modLoc("splat"),
                OpSplat.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("dedqde", HexDir.EAST), modLoc("index_of"),
                OpIndexOf.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("edqdewaqa", HexDir.SOUTH_WEST), modLoc("list_remove"),
                OpRemove.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("qaeaqwded", HexDir.NORTH_WEST), modLoc("slice"),
                OpSlice.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("wqaeaqw", HexDir.NORTH_WEST),
                modLoc("modify_in_place"),
                OpModifyInPlace.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("ddewedd", HexDir.SOUTH_EAST), modLoc("construct"),
                OpCons.INSTANCE);
            PatternRegistryBak.mapPattern(HexPattern.fromAngles("aaqwqaa", HexDir.SOUTH_WEST), modLoc("deconstruct"),
                OpUnCons.INSTANCE);

        } catch (PatternRegistryBak.RegisterPatternException exn) {
            exn.printStackTrace();
        }

        // Add zilde->number
        PatternRegistryBak.addSpecialHandler(modLoc("number"), pat -> {
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
                return Action.makeConstantOp(new DoubleIota(accumulator));
            } else {
                return null;
            }
        });

        PatternRegistryBak.addSpecialHandler(modLoc("mask"), pat -> {
            var directions = pat.directions();

            HexDir flatDir = pat.getStartDir();
            if (!pat.getAngles().isEmpty() && pat.getAngles().get(0) == HexAngle.LEFT_BACK) {
                flatDir = directions.get(0).rotatedBy(HexAngle.LEFT);
            }

            var mask = new BooleanArrayList();
            for (int i = 0; i < directions.size(); i++) {
                // Angle with respect to the *start direction*
                var angle = directions.get(i).angleFrom(flatDir);
                if (angle == HexAngle.FORWARD) {
                    mask.add(true);
                    continue;
                }
                if (i >= directions.size() - 1) {
                    // then we're out of angles!
                    return null;
                }
                var angle2 = directions.get(i + 1).angleFrom(flatDir);
                if (angle == HexAngle.RIGHT && angle2 == HexAngle.LEFT) {
                    mask.add(false);
                    i++;
                    continue;
                }

                return null;
            }

            return new OpMask(mask);
        });
    }
}
