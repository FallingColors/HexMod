package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.spell.Action;
import at.petrak.hexcasting.api.spell.ActionRegistryEntry;
import at.petrak.hexcasting.api.spell.iota.BooleanIota;
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
import at.petrak.hexcasting.common.casting.operators.rw.*;
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
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexActionsAndHandlers {
    public static final Registry<ActionRegistryEntry> ACTIONS = IXplatAbstractions.INSTANCE.getActionRegistry();

    private static final Map<ResourceLocation, ActionRegistryEntry> ACTION_MAP = new LinkedHashMap<>();

    // In general:
    // - CCW is the normal or construction version
    // - CW is the special or destruction version

    public static final ActionRegistryEntry GET_CASTER = make("get_caster",
        new ActionRegistryEntry(OpGetCaster.INSTANCE, HexPattern.fromAngles("qaq", HexDir.NORTH_EAST)));
    public static final ActionRegistryEntry OpEntityPos = make("entity_pos/eye", 
        new ActionRegistryEntry(new OpEntityPos(false), HexPattern.fromAngles("aa", HexDir.EAST));
    public static final ActionRegistryEntry OpEntityPos = make("entity_pos/foot", 
        new ActionRegistryEntry(new OpEntityPos(true), HexPattern.fromAngles("dd", HexDir.NORTH_EAST));
    public static final ActionRegistryEntry OpEntityLook = make("get_entity_look", 
        new ActionRegistryEntry(OpEntityLook.INSTANCE, HexPattern.fromAngles("wa", HexDir.EAST));
    public static final ActionRegistryEntry OpEntityHeight = make("get_entity_height", 
        new ActionRegistryEntry(OpEntityHeight.INSTANCE, HexPattern.fromAngles("awq", HexDir.NORTH_EAST));
    public static final ActionRegistryEntry OpEntityVelocity = make("get_entity_velocity", 
 
            // == Getters ==

            public static final ActionRegistryEntry RAYCAST = make("raycast",  
                new ActionRegistryEntry(OpBlockRaycast.INSTANCE, HexPattern.fromAngles("wqaawdd", HexDir.EAST)));
            public static final ActionRegistryEntry RAYCAST_AXIS = make("raycast/axis",
                new ActionRegistryEntry(OpBlockAxisRaycast.INSTANCE, HexPattern.fromAngles("weddwaa", HexDir.EAST)));
            public static final ActionRegistryEntry = RAYCAST_ENTITY = make("raycast/entity",
                new ActionRegistryEntry(OpEntityRaycast.INSTANCE, HexPattern.fromAngles("weaqa", HexDir.EAST)));

            // == spell circle getters ==

            public static final ActionRegistryEntry CIRCLE$IMPETUS_POST = make("circle/impetus_pos",
                new ActionRegistryEntry(OpImpetusPos.INSTANCE, HexPattern.fromAngles("eaqwqae", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry CIRCLE$IMPETUS_DIR = make("circle/impetus_dir",
                new ActionRegistryEntry(OpImpetusDir.INSTANCE, HexPattern.fromAngles("eaqwqaewede", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry CIRCLE$BOUNDS$MIN = make ("circle/bounds/min",
                new ActionRegistryEntry(new OpCircleBounds(false), HexPattern.fromAngles("eaqwqaewdd", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry CIRCLE$BOUNDS$MAX = make("circle/bounds/max",
                new ActionRegistryEntry(new OpCircleBounds(true), HexPattern.fromAngles("aqwqawaaqa", HexDir.WEST)));

            // == Modify Stack ==

            public static final ActionRegistryEntry SWAP = make("swap", 
                new ActionRegistryEntry(new OpTwiddling(2, new int[]{1, 0}), HexPattern.fromAngles("aawdd", HexDir.EAST)));
            public static final ActionRegistryEntry ROTATE = make("rotate", 
                new ActionRegistryEntry(new OpTwiddling(3, new int[]{1, 2, 0}), HexPattern.fromAngles("aaeaa", HexDir.EAST)));
            public static final ActionRegistryEntry ROTATE_REVERSE = make("rotate_reverse", 
                new ActionRegistryEntry(new OpTwiddling(3, new int[]{2, 0, 1}), HexPattern.fromAngles("ddqdd", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry DUPLICATE = make("duplicate", 
                new ActionRegistryEntry(new OpTwiddling(1, new int[]{0, 0}), HexPattern.fromAngles("aadaa", HexDir.EAST)));
            public static final ActionRegistryEntry OVER = make("over", 
                new ActionRegistryEntry(new OpTwiddling(2, new int[]{0, 1, 0}), HexPattern.fromAngles("aaedd", HexDir.EAST)));
            public static final ActionRegistryEntry TUCK = make("tuck", 
                new ActionRegistryEntry(new OpTwiddling(2, new int[]{1, 0, 1}), HexPattern.fromAngles("ddqaa", HexDir.EAST)));
            public static final ActionRegistryEntry 2DUP = make("2dup", 
                new ActionRegistryEntry(new OpTwiddling(2, new int[]{0, 1, 0, 1}), HexPattern.fromAngles("aadadaaw", HexDir.EAST)));

            public static final ActionRegistryEntry STACK_LEN = make("stack_len", 
                new ActionRegistryEntry(OpStackSize.INSTANCE, HexPattern.fromAngles("qwaeawqaeaqa", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry DUPLICATE_N = make("duplicate_n", 
                new ActionRegistryEntry(OpDuplicateN.INSTANCE, HexPattern.fromAngles("aadaadaa", HexDir.EAST)));
            public static final ActionRegistryEntry FISHERMAN = make("fisherman", 
                new ActionRegistryEntry(OpFisherman.INSTANCE, HexPattern.fromAngles("ddad", HexDir.WEST)));
            public static final ActionRegistryEntry FISHERMAN$COPY = make("fisherman/copy", 
                new ActionRegistryEntry(OpFishermanButItCopies.INSTANCE, HexPattern.fromAngles("aada", HexDir.EAST)));
            public static final ActionRegistryEntry SWIZZLE = make("swizzle", 
                new ActionRegistryEntry(OpAlwinfyHasAscendedToABeingOfPureMath.INSTANCE, HexPattern.fromAngles("qaawdde", HexDir.SOUTH_EAST)));

            // == Math ==

            public static final ActionRegistryEntry ADD = make("add", 
                new ActionRegistryEntry(OpAdd.INSTANCE, HexPattern.fromAngles("waaw", HexDir.NORTH_EAST))); 
            public static final ActionRegistryEntry SUB = make("sub", 
                new ActionRegistryEntry(OpSub.INSTANCE, HexPattern.fromAngles("wddw", HexDir.NORTH_WEST))); 
            public static final ActionRegistryEntry MUL_DOT = make("mul_dot", 
                new ActionRegistryEntry(OpMulDot.INSTANCE, HexPattern.fromAngles("waqaw", HexDir.SOUTH_EAST))); 
            public static final ActionRegistryEntry DIV_CROSS = make("div_cross", 
                new ActionRegistryEntry(OpDivCross.INSTANCE, HexPattern.fromAngles("wdedw", HexDir.NORTH_EAST))); 
            public static final ActionRegistryEntry ABS_LEN = make("abs_len", 
                new ActionRegistryEntry(OpAbsLen.INSTANCE, HexPattern.fromAngles("wqaqw", HexDir.NORTH_EAST))); 
            public static final ActionRegistryEntry POW_PROJ = make("pow_proj", 
                new ActionRegistryEntry(OpPowProj.INSTANCE, HexPattern.fromAngles("wedew", HexDir.NORTH_WEST))); 
            public static final ActionRegistryEntry FLOOR = make("floor", 
                new ActionRegistryEntry(OpFloor.INSTANCE, HexPattern.fromAngles("ewq", HexDir.EAST))); 
            public static final ActionRegistryEntry CEIL = make("ceil", 
                new ActionRegistryEntry(OpCeil.INSTANCE, HexPattern.fromAngles("qwe", HexDir.EAST))); 

            public static final ActionRegistryEntry CONSTRUCT_VEC = make("construct_vec", 
                new ActionRegistryEntry(OpConstructVec.INSTANCE, HexPattern.fromAngles("eqqqqq", HexDir.EAST)));
            public static final ActionRegistryEntry DECONSTRUCT_VEC = make("deconstruct_vec", 
                new ActionRegistryEntry(OpDeconstructVec.INSTANCE, HexPattern.fromAngles("qeeeee", HexDir.EAST)));
            public static final ActionRegistryEntry COERCE_AXIAL = make("coerce_axial", 
                new ActionRegistryEntry(OpCoerceToAxial.INSTANCE, HexPattern.fromAngles("qqqqqaww", HexDir.NORTH_WEST)));

            // == Logic ==

            public static final ActionRegistryEntry AND = make("and", 
                new ActionRegistryEntry(OpBoolAnd.INSTANCE, HexPattern.fromAngles("wdw", HexDir.NORTH_EAST));
            public static final ActionRegistryEntry OR = make("or", 
                new ActionRegistryEntry(OpBoolOr.INSTANCE, HexPattern.fromAngles("waw", HexDir.SOUTH_EAST));
            public static final ActionRegistryEntry XOR = make("xor", 
                new ActionRegistryEntry(OpBoolXor.INSTANCE, HexPattern.fromAngles("dwa", HexDir.NORTH_WEST));
            public static final ActionRegistryEntry GREATER = make("greater", 
                new ActionRegistryEntry(new OpCompare(false, (a, b) -> a > b), HexPattern.fromAngles("e", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry LESS = make("less", 
                new ActionRegistryEntry(new OpCompare(false, (a, b) -> a < b), HexPattern.fromAngles("q", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry GREATER_EQ = make("greater_eq", 
                new ActionRegistryEntry(new OpCompare(true, (a, b) -> a >= b), HexPattern.fromAngles("ee", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry LESS_EQ = make("less_eq", 
                new ActionRegistryEntry(new OpCompare(true, (a, b) -> a <= b), HexPattern.fromAngles("qq", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry EQUALS = make("equals", 
                new ActionRegistryEntry(new OpEquality(false), HexPattern.fromAngles("ad", HexDir.EAST)));
            public static final ActionRegistryEntry NOT_EQUALS = make("not_equals", 
                new ActionRegistryEntry(new OpEquality(true), HexPattern.fromAngles("da", HexDir.EAST)));
            public static final ActionRegistryEntry NOT = make("not", 
                new ActionRegistryEntry(OpBoolNot.INSTANCE, HexPattern.fromAngles("dw", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry BOOL_COERCE = make("bool_coerce", 
                new ActionRegistryEntry(OpCoerceToBool.INSTANCE, HexPattern.fromAngles("aw", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry IF = make("if", 
                new ActionRegistryEntry(OpBoolIf.INSTANCE, HexPattern.fromAngles("awdd", HexDir.SOUTH_EAST)));

            public static final ActionRegistryEntry RANDOM = make("random", 
                new ActionRegistryEntry(OpRandom.INSTANCE, HexPattern.fromAngles("eqqq", HexDir.NORTH_WEST)));

            // == Advanced Math ==

            public static final ActionRegistryEntry SIN = make("sin",
                new ActionRegistryEntry(OpSin.INSTANCE, HexPattern.fromAngles("qqqqqaa", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry COS = make("cos",
                new ActionRegistryEntry(OpCos.INSTANCE, HexPattern.fromAngles("qqqqqad", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry TAN = make("tan",
                new ActionRegistryEntry(OpTan.INSTANCE, HexPattern.fromAngles("wqqqqqadq", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry ARCSIN = make("arcsin",
                new ActionRegistryEntry(OpArcSin.INSTANCE, HexPattern.fromAngles("ddeeeee", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry ARCCOS = make("arccos",
                new ActionRegistryEntry(OpArcCos.INSTANCE, HexPattern.fromAngles("adeeeee", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry ARCTAN = make("arctan",
                new ActionRegistryEntry(OpArcTan.INSTANCE, HexPattern.fromAngles("eadeeeeew", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry LOGARITHM = make("logarithm",
                new ActionRegistryEntry(OpLog.INSTANCE, HexPattern.fromAngles("eqaqe", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry MODULO = make("modulo",
                new ActionRegistryEntry(OpModulo.INSTANCE, HexPattern.fromAngles("addwaad", HexDir.NORTH_EAST)));

            // == Sets ==

            public static final ActionRegistryEntry BIT$AND = make("bit/and", 
                new ActionRegistryEntry(OpAnd.INSTANCE, HexPattern.fromAngles("wdweaqa", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry BIT$OR = make("bit/or",
                new ActionRegistryEntry(OpOr.INSTANCE, HexPattern.fromAngles("waweaqa", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry BIT$XOR = make("bit/xor", 
                new ActionRegistryEntry(OpXor.INSTANCE, HexPattern.fromAngles("dwaeaqa", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry BIT$NOT = make("bit/not",
                new ActionRegistryEntry(OpNot.INSTANCE, HexPattern.fromAngles("dweaqa", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry BIT$TO_SET  = make("bit/to_set",
                new ActionRegistryEntry(OpToSet.INSTANCE, HexPattern.fromAngles("aweaqa", HexDir.NORTH_EAST)));

            // == Spells ==

            public static final ActionRegistryEntry PRINT = make("print",
                new ActionRegistryObject(OpPrint.INSTANCE, HexPattern.fromAngles("de", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry EXPLODE = make("explode",
                new ActionRegistryObject(new OpExplode(false), HexPattern.fromAngles("aawaawaa", HexDir.EAST)));
            public static final ActionRegistryEntry EXPLODE/FIRE = make("explode/fire",
                new ActionRegistryObject(new OpExplode(true), HexPattern.fromAngles("ddwddwdd", HexDir.EAST)));
            public static final ActionRegistryEntry ADD_MOTION = make("add_motion",
                new ActionRegistryObject(OpAddMotion.INSTANCE, HexPattern.fromAngles("awqqqwaqw", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry BLINK = make("blink",
                new ActionRegistryObject(OpBlink.INSTANCE, HexPattern.fromAngles("awqqqwaq", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry BREAK_BLOCK = make("break_block",
                new ActionRegistryObject(OpBreakBlock.INSTANCE, HexPattern.fromAngles("qaqqqqq", HexDir.EAST)));
            public static final ActionRegistryEntry PLACE_BLOCK = make("place_block",
                new ActionRegistryObject(OpPlaceBlock.INSTANCE, HexPattern.fromAngles("eeeeede", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry COLORIZE = make("colorize",
                new ActionRegistryObject(OpColorize.INSTANCE, HexPattern.fromAngles("awddwqawqwawq", HexDir.EAST)));
            public static final ActionRegistryEntry CREATE_WATER = make("create_water",
                new ActionRegistryObject(new OpCreateFluid(false, MediaConstants.DUST_UNIT,
                    Items.WATER_BUCKET,
                    Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(Lnew OpCreateFluid(false, MediaConstants.DUST_UNIT,
                    Items.WATER_BUCKET,
                    Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL),
                    Fluids.WATER), ayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL),
                    Fluids.WATER)), HexPattern.fromAngles("aqawqadaq", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry DESTROY_WATER = make("destroy_water",
                new ActionRegistryObject(OpDestroyFluid.INSTANCE, HexPattern.fromAngles("dedwedade", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry IGNITE = make("ignite",
                new ActionRegistryObject(OpIgnite.INSTANCE, HexPattern.fromAngles("aaqawawa", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry EXTINGUISH = make("extinguish",
                new ActionRegistryObject(OpExtinguish.INSTANCE, HexPattern.fromAngles("ddedwdwd", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry CONJURE_BLOCK = make("conjure_block",
                new ActionRegistryObject(OpConjureBlock(false), HexPattern.fromAngles("qqa", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry CONJURE_LIGHT = make("conjure_light",
                new ActionRegistryObject(new OpConjureBlock(true), HexPattern.fromAngles("qqd", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry BONEMEAL = make("bonemeal",
                new ActionRegistryObject(OpTneOnlyReasonAnyoneDownloadedPsi.INSTANCE, HexPattern.fromAngles("wqaqwawqaqw", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry RECHARGE = make("recharge",
                new ActionRegistryObject(OpRecharge.INSTANCE, HexPattern.fromAngles("qqqqqwaeaeaeaeaea", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry ERASE = make("erase",
                new ActionRegistryObject(new OpErase(), HexPattern.fromAngles("qdqawwaww", HexDir.EAST), modLoc("erase")));
            public static final ActionRegistryEntry EDIFY = make("edify",
                new ActionRegistryObject(OpEdifySapling.INSTANCE, HexPattern.fromAngles("wqaqwd", HexDir.NORTH_EAST), modLoc("edify")));

            public static final ActionRegistryEntry BEEP = make("beep",
                new ActionRegistryObject(OpBeep.INSTANCE, HexPattern.fromAngles("adaa", HexDir.WEST)));

            public static final ActionRegistryEntry CRAFT$CYPHER = make("craft/cypher", new ActionRegistryObject(
                new OpMakePackagedSpell<>(HexItems.CYPHER, MediaConstants.CRYSTAL_UNIT), 
                HexPattern.fromAngles("waqqqqq", HexDir.EAST)));
            public static final ActionRegistryEntry CRAFT$TRINKET = make("craft/trinket", new ActionRegistryObject(
                new OpMakePackagedSpell<>(HexItems.TRINKET, 5 * MediaConstants.CRYSTAL_UNIT), HexPattern.fromAngles("wwaqqqqqeaqeaeqqqeaeq", HexDir.EAST)));
            public static final ActionRegistryEntry CRAFT$ARTIFACT = make("craft/artifact", new ActionRegistryObject(
                new OpMakePackagedSpell<>(HexItems.ARTIFACT, 10 * MediaConstants.CRYSTAL_UNIT),
                HexPattern.fromAngles("wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq", HexDir.EAST)));
            public static final ActionRegistryEntry CRAFT$BATTERY = make("craft/battery", new ActionRegistryObject(
                OpMakeBattery.INSTANCE, HexPattern.fromAngles("aqqqaqwwaqqqqqeqaqqqawwqwqwqwqwqw", HexDir.SOUTH_WEST)));

            public static final ActionRegistryEntry POTION$WEAKNESS = make("potion/weakness", new ActionRegistryEntry(
                new OpPotionEffect(MobEffects.WEAKNESS, MediaConstants.DUST_UNIT / 10, true, false, false), 
                HexPattern.fromAngles("qqqqqaqwawaw", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry POTION$LEVITATION = make("potion/levitation", new ActionRegistryEntry(
                new OpPotionEffect(MobEffects.LEVITATION, MediaConstants.DUST_UNIT / 5, false, false, false), 
                HexPattern.fromAngles("qqqqqawwawawd", HexDir.WEST)));
            public static final ActionRegistryEntry POTION$WITHER = make("potion/wither", new ActionRegistryEntry(
                new OpPotionEffect(MobEffects.WITHER, MediaConstants.DUST_UNIT, true, false, false), 
                HexPattern.fromAngles("qqqqqaewawawe", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry POTION$POISON = make("potion/poison", new ActionRegistryEntry(
                new OpPotionEffect(MobEffects.POISON, MediaConstants.DUST_UNIT / 3, true, false, false), 
                HexPattern.fromAngles("qqqqqadwawaww", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry POTION$SLOWNESS = make("potion/slowness", new ActionRegistryEntry(
                new OpPotionEffect(MobEffects.MOVEMENT_SLOWDOWN, MediaConstants.DUST_UNIT / 3, true, false, false), 
                HexPattern.fromAngles("qqqqqadwawaw", HexDir.SOUTH_EAST)));

            public static final ActionRegistryEntry POTION$REGENERATION = make("potion/regeneration", new ActionRegistryEntry(
                new OpPotionEffect(new OpPotionEffect(MobEffects.REGENERATION, MediaConstants.DUST_UNIT, true, true, true),
                HexPattern.fromAngles("qqqqaawawaedd", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry POTION$NIGHT_VISION = make("potion/night_vision", new ActionRegistryEntry(
                new OpPotionEffect(new OpPotionEffect(MobEffects.NIGHT_VISION, MediaConstants.DUST_UNIT / 5, false, true, true),
                HexPattern.fromAngles("qqqaawawaeqdd", HexDir.WEST)));
            public static final ActionRegistryEntry POTION$ABSORPTION = make("potion/absorption", new ActionRegistryEntry(
                new OpPotionEffect(new OpPotionEffect(MobEffects.ABSORPTION, MediaConstants.DUST_UNIT, true, true, true),
                HexPattern.fromAngles("qqaawawaeqqdd", HexDir.SOUTH_WEST)));
            public static final ActionRegistryEntry POTION$HASTE = make("potion/haste", new ActionRegistryEntry(
                new OpPotionEffect(new OpPotionEffect(MobEffects.DIG_SPEED, MediaConstants.DUST_UNIT / 3, true, true, true),
                HexPattern.fromAngles("qaawawaeqqqdd", HexDir.SOUTH_EAST)));
            public static final ActionRegistryEntry POTION$STRENGTH = make("potion/strength", new ActionRegistryEntry(
                new OpPotionEffect(new OpPotionEffect(MobEffects.DAMAGE_BOOST, MediaConstants.DUST_UNIT / 3, true, true, true),
                HexPattern.fromAngles("aawawaeqqqqdd", HexDir.EAST)));

            public static final ActionRegistryEntry SENTINEL$CREATE = make("sentinel/create",
                new ActionRegistryEntry(new OpCreateSentinel(false), HexPattern.fromAngles("waeawae", HexDir.EAST)));
            public static final ActionRegistryEntry SENTINEL$DESTROY = make("sentinel/destroy",
                new ActionRegistryEntry(OpDestroySentinel.INSTANCE, HexPattern.fromAngles("qdwdqdw", HexDir.NORTH_EAST)));
            public static final ActionRegistryEntry SENTINEL$GET_POS = make("sentinel/get_pos",
                new ActionRegistryEntry(OpGetSentinelPos.INSTANCE, HexPattern.fromAngles("waeawaede", HexDir.EAST)));
            public static final ActionRegistryEntry SENTINEL$WAYFIND = make("sentinel/wayfind",
                new ActionRegistryEntry(OpGetSentinelWayfind.INSTANCE, HexPattern.fromAngles("waeawaedwa", HexDir.EAST)));

            public static final ActionRegistryEntry LIGHTNING = make("lightning",
                new ActionRegistryEntry(OpLightning.INSTANCE, HexPattern.fromAngles("waadwawdaaweewq", HexDir.EAST)));
            public static final ActionRegistryEntry FLIGHT = make("flight",
                new ActionRegistryEntry(OpFlight.INSTANCE, HexPattern.fromAngles("eawwaeawawaa", HexDir.NORTH_WEST)));
            public static final ActionRegistryEntry CREATE_LAVA = make("create_lava",
                new ActionRegistryEntry(new OpCreateFluid(true, MediaConstants.CRYSTAL_UNIT,
                    Items.LAVA_BUCKET,
                    Blocks.LAVA_CAULDRON.defaultBlockState(),
                    Fluids.LAVA), HexPattern.fromAngles("eaqawqadaqd", HexDir.EAST)));
            public static final ActionRegistryEntry TELEPORT = make("teleport",
                new ActionRegistryEntry(OpTeleport.INSTANCE, HexPattern.fromAngles("wwwqqqwwwqqeqqwwwqqwqqdqqqqqdqq", HexDir.EAST)));
            public static final ActionRegistryEntry SENTINEL$GREAT = make("sentinel/create/great",
                new ActionRegistryEntry(new OpCreateSentinel(true), HexPattern.fromAngles("waeawaeqqqwqwqqwq", HexDir.EAST)));
            public static final ActionRegistryEntry DISPEL_RAIN = make("dispel_rain",
                new ActionRegistryEntry(new OpWeather(false), HexPattern.fromAngles("eeewwweeewwaqqddqdqd", HexDir.EAST)));
            public static final ActionRegistryEntry SUMMON_RAIN = make("summon_rain",
                new ActionRegistryEntry(OpWeather(true),  HexPattern.fromAngles("wwweeewwweewdawdwad", HexDir.WEST)));
            public static final ActionRegistryEntry BRAINSWEEP = make("brainsweep",
                new ActionRegistryEntry(OpBrainsweep.INSTANCE, HexPattern.fromAngles("qeqwqwqwqwqeqaeqeaqeqaeqaqded", HexDir.NORTH_EAST)));

            public static final ActionRegistryEntry AKASHIC$READ= make("akashic/read", 
                new ActionRegistryEntry(OpAkashicRead.INSTANCE, HexPattern.fromAngles("qqqwqqqqqaq", HexDir.WEST));
            public static final ActionRegistryEntry AKASHIC$WRITE= make("akashic/write", 
                new ActionRegistryEntry(OpAkashicWrite.INSTANCE, HexPattern.fromAngles("eeeweeeeede", HexDir.EAST));

            // == Meta stuff ==

            // Intro/Retro/Consideration are now special-form-likes and aren't even ops.
            // TODO should there be a registry for these too

            // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
            // eval being a space filling curve feels apt doesn't it
            public static final ActionRegistryEntry (HexPattern.fromAngles("deaqq", HexDir.SOUTH_EAST), modLoc("eval"),
                OpEval.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("aqdee", HexDir.SOUTH_WEST), modLoc("halt"),
                OpHalt.INSTANCE);

            public static final ActionRegistryEntry (HexPattern.fromAngles("aqqqqq", HexDir.EAST), modLoc("read"),
                OpRead.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("wawqwqwqwqwqw", HexDir.EAST),
                modLoc("read/entity"), OpTheCoolerRead.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("deeeee", HexDir.EAST), modLoc("write"),
                OpWrite.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("wdwewewewewew", HexDir.EAST),
                modLoc("write/entity"), OpTheCoolerWrite.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("aqqqqqe", HexDir.EAST), modLoc("readable"),
                OpReadable.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("wawqwqwqwqwqwew", HexDir.EAST),
                modLoc("readable/entity"), OpTheCoolerReadable.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("deeeeeq", HexDir.EAST), modLoc("writable"),
                OpWritable.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("wdwewewewewewqw", HexDir.EAST),
                modLoc("writable/entity"), OpTheCoolerWritable.INSTANCE);

            // lorge boyes


            public static final ActionRegistryEntry (HexPattern.fromAngles("qeewdweddw", HexDir.NORTH_EAST),
                modLoc("read/local"), OpPeekLocal.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("eqqwawqaaw", HexDir.NORTH_WEST),
                modLoc("write/local"), OpPushLocal.INSTANCE);

            // == Consts ==

            public static final ActionRegistryEntry (HexPattern.fromAngles("d", HexDir.EAST), modLoc("const/null"),
                Action.makeConstantOp(new NullIota()));

            public static final ActionRegistryEntry (HexPattern.fromAngles("aqae", HexDir.SOUTH_EAST), modLoc("const/true"),
                Action.makeConstantOp(new BooleanIota(true)));
            public static final ActionRegistryEntry (HexPattern.fromAngles("dedq", HexDir.NORTH_EAST), modLoc("const/false"),
                Action.makeConstantOp(new BooleanIota(false)));

            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqea", HexDir.NORTH_WEST), modLoc("const/vec/px"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(1.0, 0.0, 0.0))));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqew", HexDir.NORTH_WEST), modLoc("const/vec/py"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 1.0, 0.0))));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqed", HexDir.NORTH_WEST), modLoc("const/vec/pz"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 1.0))));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eeeeeqa", HexDir.SOUTH_WEST), modLoc("const/vec/nx"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(-1.0, 0.0, 0.0))));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eeeeeqw", HexDir.SOUTH_WEST), modLoc("const/vec/ny"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, -1.0, 0.0))));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eeeeeqd", HexDir.SOUTH_WEST), modLoc("const/vec/nz"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, -1.0))));
            // Yep, this is what I spend the "plain hexagon" pattern on.
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqq", HexDir.NORTH_WEST), modLoc("const/vec/0"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 0.0))));

            public static final ActionRegistryEntry (HexPattern.fromAngles("qdwdq", HexDir.NORTH_EAST), modLoc("const/double/pi"),
                Action.makeConstantOp(new DoubleIota(Math.PI)));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eawae", HexDir.NORTH_WEST), modLoc("const/double/tau"),
                Action.makeConstantOp(new DoubleIota(HexUtils.TAU)));

            // e
            public static final ActionRegistryEntry (HexPattern.fromAngles("aaq", HexDir.EAST), modLoc("const/double/e"),
                Action.makeConstantOp(new DoubleIota(Math.E)));

            // == Entities ==

            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqdaqa", HexDir.SOUTH_EAST), modLoc("get_entity"),
                new OpGetEntityAt(e -> true));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqdaqaawa", HexDir.SOUTH_EAST),
                modLoc("get_entity/animal"),
                new OpGetEntityAt(OpGetEntitiesBy::isAnimal));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqdaqaawq", HexDir.SOUTH_EAST),
                modLoc("get_entity/monster"),
                new OpGetEntityAt(OpGetEntitiesBy::isMonster));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqdaqaaww", HexDir.SOUTH_EAST),
                modLoc("get_entity/item"),
                new OpGetEntityAt(OpGetEntitiesBy::isItem));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqdaqaawe", HexDir.SOUTH_EAST),
                modLoc("get_entity/player"),
                new OpGetEntityAt(OpGetEntitiesBy::isPlayer));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqdaqaawd", HexDir.SOUTH_EAST),
                modLoc("get_entity/living"),
                new OpGetEntityAt(OpGetEntitiesBy::isLiving));

            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqwded", HexDir.SOUTH_EAST), modLoc("zone_entity"),
                new OpGetEntitiesBy(e -> true, false));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqwdeddwa", HexDir.SOUTH_EAST),
                modLoc("zone_entity/animal"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, false));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eeeeewaqaawa", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_animal"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, true));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqwdeddwq", HexDir.SOUTH_EAST),
                modLoc("zone_entity/monster"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, false));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eeeeewaqaawq", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_monster"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, true));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqwdeddww", HexDir.SOUTH_EAST),
                modLoc("zone_entity/item"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isItem, false));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eeeeewaqaaww", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_item"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isItem, true));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqwdeddwe", HexDir.SOUTH_EAST),
                modLoc("zone_entity/player"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, false));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eeeeewaqaawe", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_player"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, true));
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqqqwdeddwd", HexDir.SOUTH_EAST),
                modLoc("zone_entity/living"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, false));
            public static final ActionRegistryEntry (HexPattern.fromAngles("eeeeewaqaawd", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_living"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, true));

            // == Lists ==

            public static final ActionRegistryEntry (HexPattern.fromAngles("edqde", HexDir.SOUTH_WEST), modLoc("append"),
                OpAppend.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST), modLoc("concat"),
                OpConcat.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("deeed", HexDir.NORTH_WEST), modLoc("index"),
                OpIndex.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("dadad", HexDir.NORTH_EAST), modLoc("for_each"),
                OpForEach.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("aqaeaq", HexDir.EAST), modLoc("list_size"),
                OpListSize.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("adeeed", HexDir.EAST), modLoc("singleton"),
                OpSingleton.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqaeaae", HexDir.NORTH_EAST), modLoc("empty_list"),
                OpEmptyList.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("qqqaede", HexDir.EAST), modLoc("reverse_list"),
                OpReverski.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("ewdqdwe", HexDir.SOUTH_WEST), modLoc("last_n_list"),
                OpLastNToList.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("qwaeawq", HexDir.NORTH_WEST), modLoc("splat"),
                OpSplat.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("dedqde", HexDir.EAST), modLoc("index_of"),
                OpIndexOf.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("edqdewaqa", HexDir.SOUTH_WEST), modLoc("list_remove"),
                OpRemove.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("qaeaqwded", HexDir.NORTH_WEST), modLoc("slice"),
                OpSlice.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("wqaeaqw", HexDir.NORTH_WEST),
                modLoc("modify_in_place"),
                OpModifyInPlace.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("ddewedd", HexDir.SOUTH_EAST), modLoc("construct"),
                OpCons.INSTANCE);
            public static final ActionRegistryEntry (HexPattern.fromAngles("aaqwqaa", HexDir.SOUTH_WEST), modLoc("deconstruct"),
                OpUnCons.INSTANCE);

    private static ActionRegistryEntry make(String name, ActionRegistryEntry are) {
        var old = ACTION_MAP.put(modLoc(name), are);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return are;
    }

    // I guess this means the client will have a big empty map for patterns
    public static void registerPatterns() {
        try {
            // == Getters ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("wqaawdd", HexDir.EAST), modLoc("raycast"),
                OpBlockRaycast.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("weddwaa", HexDir.EAST), modLoc("raycast/axis"),
                OpBlockAxisRaycast.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("weaqa", HexDir.EAST), modLoc("raycast/entity"),
                OpEntityRaycast.INSTANCE);

            // == spell circle getters ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("eaqwqae", HexDir.SOUTH_WEST),
                modLoc("circle/impetus_pos"), OpImpetusPos.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eaqwqaewede", HexDir.SOUTH_WEST),
                modLoc("circle/impetus_dir"), OpImpetusDir.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eaqwqaewdd", HexDir.SOUTH_WEST),
                modLoc("circle/bounds/min"), new OpCircleBounds(false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("aqwqawaaqa", HexDir.WEST),
                modLoc("circle/bounds/max"), new OpCircleBounds(true));

            // == Modify Stack ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("aawdd", HexDir.EAST), modLoc("swap"),
                new OpTwiddling(2, new int[]{1, 0}));
            PatternRegistry.mapPattern(HexPattern.fromAngles("aaeaa", HexDir.EAST), modLoc("rotate"),
                new OpTwiddling(3, new int[]{1, 2, 0}));
            PatternRegistry.mapPattern(HexPattern.fromAngles("ddqdd", HexDir.NORTH_EAST), modLoc("rotate_reverse"),
                new OpTwiddling(3, new int[]{2, 0, 1}));
            PatternRegistry.mapPattern(HexPattern.fromAngles("aadaa", HexDir.EAST), modLoc("duplicate"),
                new OpTwiddling(1, new int[]{0, 0}));
            PatternRegistry.mapPattern(HexPattern.fromAngles("aaedd", HexDir.EAST), modLoc("over"),
                new OpTwiddling(2, new int[]{0, 1, 0}));
            PatternRegistry.mapPattern(HexPattern.fromAngles("ddqaa", HexDir.EAST), modLoc("tuck"),
                new OpTwiddling(2, new int[]{1, 0, 1}));
            PatternRegistry.mapPattern(HexPattern.fromAngles("aadadaaw", HexDir.EAST), modLoc("2dup"),
                new OpTwiddling(2, new int[]{0, 1, 0, 1}));

            PatternRegistry.mapPattern(HexPattern.fromAngles("qwaeawqaeaqa", HexDir.NORTH_WEST), modLoc("stack_len"),
                OpStackSize.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aadaadaa", HexDir.EAST), modLoc("duplicate_n"),
                OpDuplicateN.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("ddad", HexDir.WEST), modLoc("fisherman"),
                OpFisherman.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aada", HexDir.EAST), modLoc("fisherman/copy"),
                OpFishermanButItCopies.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qaawdde", HexDir.SOUTH_EAST), modLoc("swizzle"),
                OpAlwinfyHasAscendedToABeingOfPureMath.INSTANCE);

            // == Math ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("waaw", HexDir.NORTH_EAST), modLoc("add"),
                OpAdd.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wddw", HexDir.NORTH_WEST), modLoc("sub"),
                OpSub.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("waqaw", HexDir.SOUTH_EAST), modLoc("mul_dot"),
                OpMulDot.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wdedw", HexDir.NORTH_EAST), modLoc("div_cross"),
                OpDivCross.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wqaqw", HexDir.NORTH_EAST), modLoc("abs_len"),
                OpAbsLen.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wedew", HexDir.NORTH_WEST), modLoc("pow_proj"),
                OpPowProj.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("ewq", HexDir.EAST), modLoc("floor"),
                OpFloor.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qwe", HexDir.EAST), modLoc("ceil"),
                OpCeil.INSTANCE);

            PatternRegistry.mapPattern(HexPattern.fromAngles("eqqqqq", HexDir.EAST), modLoc("construct_vec"),
                OpConstructVec.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qeeeee", HexDir.EAST), modLoc("deconstruct_vec"),
                OpDeconstructVec.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqaww", HexDir.NORTH_WEST), modLoc("coerce_axial"),
                OpCoerceToAxial.INSTANCE);

            // == Logic ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("wdw", HexDir.NORTH_EAST), modLoc("and"),
                OpBoolAnd.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("waw", HexDir.SOUTH_EAST), modLoc("or"),
                OpBoolOr.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("dwa", HexDir.NORTH_WEST), modLoc("xor"),
                OpBoolXor.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("e", HexDir.SOUTH_EAST), modLoc("greater"),
                new OpCompare(false, (a, b) -> a > b));
            PatternRegistry.mapPattern(HexPattern.fromAngles("q", HexDir.SOUTH_WEST), modLoc("less"),
                new OpCompare(false, (a, b) -> a < b));
            PatternRegistry.mapPattern(HexPattern.fromAngles("ee", HexDir.SOUTH_EAST), modLoc("greater_eq"),
                new OpCompare(true, (a, b) -> a >= b));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qq", HexDir.SOUTH_WEST), modLoc("less_eq"),
                new OpCompare(true, (a, b) -> a <= b));
            PatternRegistry.mapPattern(HexPattern.fromAngles("ad", HexDir.EAST), modLoc("equals"),
                new OpEquality(false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("da", HexDir.EAST), modLoc("not_equals"),
                new OpEquality(true));
            PatternRegistry.mapPattern(HexPattern.fromAngles("dw", HexDir.NORTH_WEST), modLoc("not"),
                OpBoolNot.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aw", HexDir.NORTH_EAST), modLoc("bool_coerce"),
                OpCoerceToBool.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("awdd", HexDir.SOUTH_EAST), modLoc("if"),
                OpBoolIf.INSTANCE);

            PatternRegistry.mapPattern(HexPattern.fromAngles("eqqq", HexDir.NORTH_WEST), modLoc("random"),
                OpRandom.INSTANCE);

            // == Advanced Math ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqaa", HexDir.SOUTH_EAST), modLoc("sin"),
                OpSin.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqad", HexDir.SOUTH_EAST), modLoc("cos"),
                OpCos.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wqqqqqadq", HexDir.SOUTH_WEST), modLoc("tan"),
                OpTan.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("ddeeeee", HexDir.SOUTH_EAST), modLoc("arcsin"),
                OpArcSin.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("adeeeee", HexDir.NORTH_EAST), modLoc("arccos"),
                OpArcCos.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eadeeeeew", HexDir.NORTH_EAST), modLoc("arctan"),
                OpArcTan.INSTANCE);

            PatternRegistry.mapPattern(HexPattern.fromAngles("eqaqe", HexDir.NORTH_WEST), modLoc("logarithm"),
                OpLog.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("addwaad", HexDir.NORTH_EAST), modLoc("modulo"),
                OpModulo.INSTANCE);

            // == Sets ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("wdweaqa", HexDir.NORTH_EAST), modLoc("and_bit"),
                OpAnd.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("waweaqa", HexDir.SOUTH_EAST), modLoc("or_bit"),
                OpOr.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("dwaeaqa", HexDir.NORTH_WEST), modLoc("xor_bit"),
                OpXor.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("dweaqa", HexDir.NORTH_WEST), modLoc("not_bit"),
                OpNot.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aweaqa", HexDir.NORTH_EAST), modLoc("to_set"),
                OpToSet.INSTANCE);

            // == Spells ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("de", HexDir.NORTH_EAST), modLoc("print"),
                OpPrint.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aawaawaa", HexDir.EAST), modLoc("explode"),
                new OpExplode(false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("ddwddwdd", HexDir.EAST), modLoc("explode/fire"),
                new OpExplode(true));
            PatternRegistry.mapPattern(HexPattern.fromAngles("awqqqwaqw", HexDir.SOUTH_WEST), modLoc("add_motion"),
                OpAddMotion.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("awqqqwaq", HexDir.SOUTH_WEST), modLoc("blink"),
                OpBlink.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qaqqqqq", HexDir.EAST), modLoc("break_block"),
                OpBreakBlock.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeede", HexDir.SOUTH_WEST), modLoc("place_block"),
                OpPlaceBlock.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("awddwqawqwawq", HexDir.EAST),
                modLoc("colorize"),
                OpColorize.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aqawqadaq", HexDir.SOUTH_EAST), modLoc("create_water"),
                new OpCreateFluid(false, MediaConstants.DUST_UNIT,
                    Items.WATER_BUCKET,
                    Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL),
                    Fluids.WATER));
            PatternRegistry.mapPattern(HexPattern.fromAngles("dedwedade", HexDir.SOUTH_WEST),
                modLoc("destroy_water"),
                OpDestroyFluid.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aaqawawa", HexDir.SOUTH_EAST), modLoc("ignite"),
                OpIgnite.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("ddedwdwd", HexDir.SOUTH_WEST), modLoc("extinguish"),
                OpExtinguish.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqa", HexDir.NORTH_EAST), modLoc("conjure_block"),
                new OpConjureBlock(false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqd", HexDir.NORTH_EAST), modLoc("conjure_light"),
                new OpConjureBlock(true));
            PatternRegistry.mapPattern(HexPattern.fromAngles("wqaqwawqaqw", HexDir.NORTH_EAST), modLoc("bonemeal"),
                OpTheOnlyReasonAnyoneDownloadedPsi.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqwaeaeaeaeaea", HexDir.NORTH_WEST),
                modLoc("recharge"),
                OpRecharge.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qdqawwaww", HexDir.EAST), modLoc("erase"),
                new OpErase());
            PatternRegistry.mapPattern(HexPattern.fromAngles("wqaqwd", HexDir.NORTH_EAST), modLoc("edify"),
                OpEdifySapling.INSTANCE);

            PatternRegistry.mapPattern(HexPattern.fromAngles("adaa", HexDir.WEST), modLoc("beep"),
                OpBeep.INSTANCE);

            PatternRegistry.mapPattern(HexPattern.fromAngles("waqqqqq", HexDir.EAST), modLoc("craft/cypher"),
                new OpMakePackagedSpell<>(HexItems.CYPHER, MediaConstants.CRYSTAL_UNIT));
            PatternRegistry.mapPattern(HexPattern.fromAngles("wwaqqqqqeaqeaeqqqeaeq", HexDir.EAST),
                modLoc("craft/trinket"),
                new OpMakePackagedSpell<>(HexItems.TRINKET, 5 * MediaConstants.CRYSTAL_UNIT));
            PatternRegistry.mapPattern(
                HexPattern.fromAngles("wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq", HexDir.EAST),
                modLoc("craft/artifact"),
                new OpMakePackagedSpell<>(HexItems.ARTIFACT, 10 * MediaConstants.CRYSTAL_UNIT));
            PatternRegistry.mapPattern(
                HexPattern.fromAngles("aqqqaqwwaqqqqqeqaqqqawwqwqwqwqwqw", HexDir.SOUTH_WEST),
                modLoc("craft/battery"),
                OpMakeBattery.INSTANCE,
                true);

            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqaqwawaw", HexDir.NORTH_WEST),
                modLoc("potion/weakness"),
                new OpPotionEffect(MobEffects.WEAKNESS, MediaConstants.DUST_UNIT / 10, true, false, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqawwawawd", HexDir.WEST),
                modLoc("potion/levitation"),
                new OpPotionEffect(MobEffects.LEVITATION, MediaConstants.DUST_UNIT / 5, false, false, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqaewawawe", HexDir.SOUTH_WEST),
                modLoc("potion/wither"),
                new OpPotionEffect(MobEffects.WITHER, MediaConstants.DUST_UNIT, true, false, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqadwawaww", HexDir.SOUTH_EAST),
                modLoc("potion/poison"),
                new OpPotionEffect(MobEffects.POISON, MediaConstants.DUST_UNIT / 3, true, false, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqadwawaw", HexDir.SOUTH_EAST),
                modLoc("potion/slowness"),
                new OpPotionEffect(MobEffects.MOVEMENT_SLOWDOWN, MediaConstants.DUST_UNIT / 3, true, false, false));

            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqaawawaedd", HexDir.NORTH_WEST),
                modLoc("potion/regeneration"),
                new OpPotionEffect(MobEffects.REGENERATION, MediaConstants.DUST_UNIT, true, true, true), true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqaawawaeqdd", HexDir.WEST),
                modLoc("potion/night_vision"),
                new OpPotionEffect(MobEffects.NIGHT_VISION, MediaConstants.DUST_UNIT / 5, false, true, true), true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqaawawaeqqdd", HexDir.SOUTH_WEST),
                modLoc("potion/absorption"),
                new OpPotionEffect(MobEffects.ABSORPTION, MediaConstants.DUST_UNIT, true, true, true), true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qaawawaeqqqdd", HexDir.SOUTH_EAST),
                modLoc("potion/haste"),
                new OpPotionEffect(MobEffects.DIG_SPEED, MediaConstants.DUST_UNIT / 3, true, true, true), true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aawawaeqqqqdd", HexDir.EAST),
                modLoc("potion/strength"),
                new OpPotionEffect(MobEffects.DAMAGE_BOOST, MediaConstants.DUST_UNIT / 3, true, true, true), true);

            PatternRegistry.mapPattern(HexPattern.fromAngles("waeawae", HexDir.EAST),
                modLoc("sentinel/create"),
                new OpCreateSentinel(false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qdwdqdw", HexDir.NORTH_EAST),
                modLoc("sentinel/destroy"),
                OpDestroySentinel.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("waeawaede", HexDir.EAST),
                modLoc("sentinel/get_pos"),
                OpGetSentinelPos.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("waeawaedwa", HexDir.EAST),
                modLoc("sentinel/wayfind"),
                OpGetSentinelWayfind.INSTANCE);

            PatternRegistry.mapPattern(HexPattern.fromAngles("waadwawdaaweewq", HexDir.EAST),
                modLoc("lightning"), OpLightning.INSTANCE, true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eawwaeawawaa", HexDir.NORTH_WEST),
                modLoc("flight"), OpFlight.INSTANCE, true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eaqawqadaqd", HexDir.EAST),
                modLoc("create_lava"), new OpCreateFluid(true, MediaConstants.CRYSTAL_UNIT,
                    Items.LAVA_BUCKET,
                    Blocks.LAVA_CAULDRON.defaultBlockState(),
                    Fluids.LAVA), true);
            PatternRegistry.mapPattern(
                HexPattern.fromAngles("wwwqqqwwwqqeqqwwwqqwqqdqqqqqdqq", HexDir.EAST),
                modLoc("teleport"), OpTeleport.INSTANCE, true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("waeawaeqqqwqwqqwq", HexDir.EAST),
                modLoc("sentinel/create/great"),
                new OpCreateSentinel(true), true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeewwweeewwaqqddqdqd", HexDir.EAST),
                modLoc("dispel_rain"),
                new OpWeather(false), true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wwweeewwweewdawdwad", HexDir.WEST),
                modLoc("summon_rain"),
                new OpWeather(true), true);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qeqwqwqwqwqeqaeqeaqeqaeqaqded", HexDir.NORTH_EAST),
                modLoc("brainsweep"),
                OpBrainsweep.INSTANCE, true);

            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqwqqqqqaq", HexDir.WEST), modLoc("akashic/read"),
                OpAkashicRead.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeweeeeede", HexDir.EAST), modLoc("akashic/write"),
                OpAkashicWrite.INSTANCE);

            // == Meta stuff ==

            // Intro/Retro/Consideration are now special-form-likes and aren't even ops.
            // TODO should there be a registry for these too

            // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
            // eval being a space filling curve feels apt doesn't it
            PatternRegistry.mapPattern(HexPattern.fromAngles("deaqq", HexDir.SOUTH_EAST), modLoc("eval"),
                OpEval.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aqdee", HexDir.SOUTH_WEST), modLoc("halt"),
                OpHalt.INSTANCE);

            PatternRegistry.mapPattern(HexPattern.fromAngles("aqqqqq", HexDir.EAST), modLoc("read"),
                OpRead.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wawqwqwqwqwqw", HexDir.EAST),
                modLoc("read/entity"), OpTheCoolerRead.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("deeeee", HexDir.EAST), modLoc("write"),
                OpWrite.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wdwewewewewew", HexDir.EAST),
                modLoc("write/entity"), OpTheCoolerWrite.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aqqqqqe", HexDir.EAST), modLoc("readable"),
                OpReadable.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wawqwqwqwqwqwew", HexDir.EAST),
                modLoc("readable/entity"), OpTheCoolerReadable.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("deeeeeq", HexDir.EAST), modLoc("writable"),
                OpWritable.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wdwewewewewewqw", HexDir.EAST),
                modLoc("writable/entity"), OpTheCoolerWritable.INSTANCE);

            // lorge boyes


            PatternRegistry.mapPattern(HexPattern.fromAngles("qeewdweddw", HexDir.NORTH_EAST),
                modLoc("read/local"), OpPeekLocal.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("eqqwawqaaw", HexDir.NORTH_WEST),
                modLoc("write/local"), OpPushLocal.INSTANCE);

            // == Consts ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("d", HexDir.EAST), modLoc("const/null"),
                Action.makeConstantOp(new NullIota()));

            PatternRegistry.mapPattern(HexPattern.fromAngles("aqae", HexDir.SOUTH_EAST), modLoc("const/true"),
                Action.makeConstantOp(new BooleanIota(true)));
            PatternRegistry.mapPattern(HexPattern.fromAngles("dedq", HexDir.NORTH_EAST), modLoc("const/false"),
                Action.makeConstantOp(new BooleanIota(false)));

            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqea", HexDir.NORTH_WEST), modLoc("const/vec/px"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(1.0, 0.0, 0.0))));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqew", HexDir.NORTH_WEST), modLoc("const/vec/py"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 1.0, 0.0))));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqed", HexDir.NORTH_WEST), modLoc("const/vec/pz"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 1.0))));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeeqa", HexDir.SOUTH_WEST), modLoc("const/vec/nx"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(-1.0, 0.0, 0.0))));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeeqw", HexDir.SOUTH_WEST), modLoc("const/vec/ny"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, -1.0, 0.0))));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeeqd", HexDir.SOUTH_WEST), modLoc("const/vec/nz"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, -1.0))));
            // Yep, this is what I spend the "plain hexagon" pattern on.
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqq", HexDir.NORTH_WEST), modLoc("const/vec/0"),
                Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 0.0))));

            PatternRegistry.mapPattern(HexPattern.fromAngles("qdwdq", HexDir.NORTH_EAST), modLoc("const/double/pi"),
                Action.makeConstantOp(new DoubleIota(Math.PI)));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eawae", HexDir.NORTH_WEST), modLoc("const/double/tau"),
                Action.makeConstantOp(new DoubleIota(HexUtils.TAU)));

            // e
            PatternRegistry.mapPattern(HexPattern.fromAngles("aaq", HexDir.EAST), modLoc("const/double/e"),
                Action.makeConstantOp(new DoubleIota(Math.E)));

            // == Entities ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqdaqa", HexDir.SOUTH_EAST), modLoc("get_entity"),
                new OpGetEntityAt(e -> true));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqdaqaawa", HexDir.SOUTH_EAST),
                modLoc("get_entity/animal"),
                new OpGetEntityAt(OpGetEntitiesBy::isAnimal));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqdaqaawq", HexDir.SOUTH_EAST),
                modLoc("get_entity/monster"),
                new OpGetEntityAt(OpGetEntitiesBy::isMonster));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqdaqaaww", HexDir.SOUTH_EAST),
                modLoc("get_entity/item"),
                new OpGetEntityAt(OpGetEntitiesBy::isItem));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqdaqaawe", HexDir.SOUTH_EAST),
                modLoc("get_entity/player"),
                new OpGetEntityAt(OpGetEntitiesBy::isPlayer));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqdaqaawd", HexDir.SOUTH_EAST),
                modLoc("get_entity/living"),
                new OpGetEntityAt(OpGetEntitiesBy::isLiving));

            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqwded", HexDir.SOUTH_EAST), modLoc("zone_entity"),
                new OpGetEntitiesBy(e -> true, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqwdeddwa", HexDir.SOUTH_EAST),
                modLoc("zone_entity/animal"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeewaqaawa", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_animal"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, true));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqwdeddwq", HexDir.SOUTH_EAST),
                modLoc("zone_entity/monster"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeewaqaawq", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_monster"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, true));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqwdeddww", HexDir.SOUTH_EAST),
                modLoc("zone_entity/item"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isItem, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeewaqaaww", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_item"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isItem, true));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqwdeddwe", HexDir.SOUTH_EAST),
                modLoc("zone_entity/player"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeewaqaawe", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_player"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, true));
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqqqwdeddwd", HexDir.SOUTH_EAST),
                modLoc("zone_entity/living"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, false));
            PatternRegistry.mapPattern(HexPattern.fromAngles("eeeeewaqaawd", HexDir.NORTH_EAST),
                modLoc("zone_entity/not_living"),
                new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, true));

            // == Lists ==

            PatternRegistry.mapPattern(HexPattern.fromAngles("edqde", HexDir.SOUTH_WEST), modLoc("append"),
                OpAppend.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST), modLoc("concat"),
                OpConcat.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("deeed", HexDir.NORTH_WEST), modLoc("index"),
                OpIndex.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("dadad", HexDir.NORTH_EAST), modLoc("for_each"),
                OpForEach.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aqaeaq", HexDir.EAST), modLoc("list_size"),
                OpListSize.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("adeeed", HexDir.EAST), modLoc("singleton"),
                OpSingleton.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqaeaae", HexDir.NORTH_EAST), modLoc("empty_list"),
                OpEmptyList.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qqqaede", HexDir.EAST), modLoc("reverse_list"),
                OpReverski.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("ewdqdwe", HexDir.SOUTH_WEST), modLoc("last_n_list"),
                OpLastNToList.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qwaeawq", HexDir.NORTH_WEST), modLoc("splat"),
                OpSplat.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("dedqde", HexDir.EAST), modLoc("index_of"),
                OpIndexOf.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("edqdewaqa", HexDir.SOUTH_WEST), modLoc("list_remove"),
                OpRemove.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("qaeaqwded", HexDir.NORTH_WEST), modLoc("slice"),
                OpSlice.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("wqaeaqw", HexDir.NORTH_WEST),
                modLoc("modify_in_place"),
                OpModifyInPlace.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("ddewedd", HexDir.SOUTH_EAST), modLoc("construct"),
                OpCons.INSTANCE);
            PatternRegistry.mapPattern(HexPattern.fromAngles("aaqwqaa", HexDir.SOUTH_WEST), modLoc("deconstruct"),
                OpUnCons.INSTANCE);

        } catch (PatternRegistry.RegisterPatternException exn) {
            exn.printStackTrace();
        }

        // Add zilde->number
        PatternRegistry.addSpecialHandler(modLoc("number"), pat -> {
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
                return Action.makeConstantOp(accumulator, modLoc("number"));
            } else {
                return null;
            }
        });

        PatternRegistry.addSpecialHandler(modLoc("mask"), pat -> {
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

            return new OpMask(mask, modLoc("mask"));
        });
    }
}
