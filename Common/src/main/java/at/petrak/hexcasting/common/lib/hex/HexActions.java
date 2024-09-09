package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.castables.OperationAction;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.casting.actions.*;
import at.petrak.hexcasting.common.casting.actions.akashic.*;
import at.petrak.hexcasting.common.casting.actions.circles.*;
import at.petrak.hexcasting.common.casting.actions.eval.*;
import at.petrak.hexcasting.common.casting.actions.lists.*;
import at.petrak.hexcasting.common.casting.actions.local.*;
import at.petrak.hexcasting.common.casting.actions.math.*;
import at.petrak.hexcasting.common.casting.actions.math.logic.*;
import at.petrak.hexcasting.common.casting.actions.raycast.*;
import at.petrak.hexcasting.common.casting.actions.rw.*;
import at.petrak.hexcasting.common.casting.actions.selectors.*;
import at.petrak.hexcasting.common.casting.actions.spells.*;
import at.petrak.hexcasting.common.casting.actions.spells.great.*;
import at.petrak.hexcasting.common.casting.actions.spells.sentinel.*;
import at.petrak.hexcasting.common.casting.actions.stack.*;
import at.petrak.hexcasting.common.casting.actions.eval.OpEvalBreakable;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.interop.pehkui.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
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
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

@SuppressWarnings("unused")
public class HexActions {
    public static final Registry<ActionRegistryEntry> REGISTRY = IXplatAbstractions.INSTANCE.getActionRegistry();

    private static final Map<ResourceLocation, ActionRegistryEntry> ACTIONS = new LinkedHashMap<>();

    // In general:
    // - CCW is the normal or construction version
    // - CW is the special or destruction version

    public static final ActionRegistryEntry GET_CASTER = make("get_caster",
        new ActionRegistryEntry(HexPattern.fromAngles("qaq", HexDir.NORTH_EAST), OpGetCaster.INSTANCE));
    public static final ActionRegistryEntry ENTITY_POS$EYE = make("entity_pos/eye",
        new ActionRegistryEntry(HexPattern.fromAngles("aa", HexDir.EAST), new OpEntityPos(false)));
    public static final ActionRegistryEntry ENTITY_POS$FOOT = make("entity_pos/foot",
        new ActionRegistryEntry(HexPattern.fromAngles("dd", HexDir.NORTH_EAST), new OpEntityPos(true)));
    public static final ActionRegistryEntry ENTITY_LOOK = make("get_entity_look",
        new ActionRegistryEntry(HexPattern.fromAngles("wa", HexDir.EAST), OpEntityLook.INSTANCE));
    public static final ActionRegistryEntry ENTITY_HEIGHT = make("get_entity_height",
        new ActionRegistryEntry(HexPattern.fromAngles("awq", HexDir.NORTH_EAST), OpEntityHeight.INSTANCE));
    public static final ActionRegistryEntry ENTITY_VELOCITY = make("get_entity_velocity",
        new ActionRegistryEntry(HexPattern.fromAngles("wq", HexDir.EAST), OpEntityVelocity.INSTANCE));

    // == Getters ==

    public static final ActionRegistryEntry RAYCAST = make("raycast",
        new ActionRegistryEntry(HexPattern.fromAngles("wqaawdd", HexDir.EAST), OpBlockRaycast.INSTANCE));
    public static final ActionRegistryEntry RAYCAST_AXIS = make("raycast/axis",
        new ActionRegistryEntry(HexPattern.fromAngles("weddwaa", HexDir.EAST), OpBlockAxisRaycast.INSTANCE));
    public static final ActionRegistryEntry RAYCAST_ENTITY = make("raycast/entity",
        new ActionRegistryEntry(HexPattern.fromAngles("weaqa", HexDir.EAST), OpEntityRaycast.INSTANCE));

    // == spell circle getters ==

    public static final ActionRegistryEntry CIRCLE$IMPETUS_POST = make("circle/impetus_pos",
        new ActionRegistryEntry(HexPattern.fromAngles("eaqwqae", HexDir.SOUTH_WEST), OpImpetusPos.INSTANCE));
    public static final ActionRegistryEntry CIRCLE$IMPETUS_DIR = make("circle/impetus_dir",
        new ActionRegistryEntry(HexPattern.fromAngles("eaqwqaewede", HexDir.SOUTH_WEST), OpImpetusDir.INSTANCE));
    public static final ActionRegistryEntry CIRCLE$BOUNDS$MIN = make("circle/bounds/min",
        new ActionRegistryEntry(HexPattern.fromAngles("eaqwqaewdd", HexDir.SOUTH_WEST), new OpCircleBounds(false)));
    public static final ActionRegistryEntry CIRCLE$BOUNDS$MAX = make("circle/bounds/max",
        new ActionRegistryEntry(HexPattern.fromAngles("aqwqawaaqa", HexDir.WEST), new OpCircleBounds(true)));

    // == Modify Stack ==

    public static final ActionRegistryEntry SWAP = make("swap",
        new ActionRegistryEntry(HexPattern.fromAngles("aawdd", HexDir.EAST), new OpTwiddling(2, new int[]{1, 0})));
    public static final ActionRegistryEntry ROTATE = make("rotate",
        new ActionRegistryEntry(HexPattern.fromAngles("aaeaa", HexDir.EAST), new OpTwiddling(3, new int[]{1, 2, 0})));
    public static final ActionRegistryEntry ROTATE_REVERSE = make("rotate_reverse",
        new ActionRegistryEntry(HexPattern.fromAngles("ddqdd",
            HexDir.NORTH_EAST), new OpTwiddling(3, new int[]{2, 0, 1})));
    public static final ActionRegistryEntry DUPLICATE = make("duplicate",
        new ActionRegistryEntry(HexPattern.fromAngles("aadaa", HexDir.EAST), new OpTwiddling(1, new int[]{0, 0})));
    public static final ActionRegistryEntry OVER = make("over",
        new ActionRegistryEntry(HexPattern.fromAngles("aaedd", HexDir.EAST), new OpTwiddling(2, new int[]{0, 1, 0})));
    public static final ActionRegistryEntry TUCK = make("tuck",
        new ActionRegistryEntry(HexPattern.fromAngles("ddqaa", HexDir.EAST), new OpTwiddling(2, new int[]{1, 0, 1})));
    public static final ActionRegistryEntry TWO_DUP = make("2dup",
        new ActionRegistryEntry(HexPattern.fromAngles("aadadaaw",
            HexDir.EAST), new OpTwiddling(2, new int[]{0, 1, 0, 1})));

    public static final ActionRegistryEntry STACK_LEN = make("stack_len",
        new ActionRegistryEntry(HexPattern.fromAngles("qwaeawqaeaqa", HexDir.NORTH_WEST), OpStackSize.INSTANCE));
    public static final ActionRegistryEntry DUPLICATE_N = make("duplicate_n",
        new ActionRegistryEntry(HexPattern.fromAngles("aadaadaa", HexDir.EAST), OpDuplicateN.INSTANCE));
    public static final ActionRegistryEntry FISHERMAN = make("fisherman",
        new ActionRegistryEntry(HexPattern.fromAngles("ddad", HexDir.WEST), OpFisherman.INSTANCE));
    public static final ActionRegistryEntry FISHERMAN$COPY = make("fisherman/copy",
        new ActionRegistryEntry(HexPattern.fromAngles("aada", HexDir.EAST), OpFishermanButItCopies.INSTANCE));
    public static final ActionRegistryEntry SWIZZLE = make("swizzle",
        new ActionRegistryEntry(HexPattern.fromAngles("qaawdde",
            HexDir.SOUTH_EAST), OpAlwinfyHasAscendedToABeingOfPureMath.INSTANCE));

    // == Math ==

    public static final ActionRegistryEntry ADD = make("add",
        new OperationAction(HexPattern.fromAngles("waaw", HexDir.NORTH_EAST)));
    public static final ActionRegistryEntry SUB = make("sub",
        new OperationAction(HexPattern.fromAngles("wddw", HexDir.NORTH_WEST)));
    public static final ActionRegistryEntry MUL_DOT = make("mul",
        new OperationAction(HexPattern.fromAngles("waqaw", HexDir.SOUTH_EAST)));
    public static final ActionRegistryEntry DIV_CROSS = make("div",
        new OperationAction(HexPattern.fromAngles("wdedw", HexDir.NORTH_EAST)));
    public static final ActionRegistryEntry ABS = make("abs",
        new OperationAction(HexPattern.fromAngles("wqaqw", HexDir.NORTH_EAST)));
    public static final ActionRegistryEntry POW_PROJ = make("pow",
        new OperationAction(HexPattern.fromAngles("wedew", HexDir.NORTH_WEST)));
    public static final ActionRegistryEntry FLOOR = make("floor",
        new OperationAction(HexPattern.fromAngles("ewq", HexDir.EAST)));
    public static final ActionRegistryEntry CEIL = make("ceil",
        new OperationAction(HexPattern.fromAngles("qwe", HexDir.EAST)));

    public static final ActionRegistryEntry CONSTRUCT_VEC = make("construct_vec",
        new OperationAction(HexPattern.fromAngles("eqqqqq", HexDir.EAST)));
    public static final ActionRegistryEntry DECONSTRUCT_VEC = make("deconstruct_vec",
        new OperationAction(HexPattern.fromAngles("qeeeee", HexDir.EAST)));
    public static final ActionRegistryEntry COERCE_AXIAL = make("coerce_axial",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqaww", HexDir.NORTH_WEST), OpCoerceToAxial.INSTANCE));

    // == Logic ==

    public static final ActionRegistryEntry AND = make("and",
        new OperationAction(HexPattern.fromAngles("wdw", HexDir.NORTH_EAST)));
    public static final ActionRegistryEntry OR = make("or",
        new OperationAction(HexPattern.fromAngles("waw", HexDir.SOUTH_EAST)));
    public static final ActionRegistryEntry XOR = make("xor",
        new OperationAction(HexPattern.fromAngles("dwa", HexDir.NORTH_WEST)));
    public static final ActionRegistryEntry GREATER = make("greater", new OperationAction(
        HexPattern.fromAngles("e", HexDir.SOUTH_EAST))
    );
    public static final ActionRegistryEntry LESS = make("less", new OperationAction(
        HexPattern.fromAngles("q", HexDir.SOUTH_WEST))
    );
    public static final ActionRegistryEntry GREATER_EQ = make("greater_eq", new OperationAction(
        HexPattern.fromAngles("ee", HexDir.SOUTH_EAST))
    );
    public static final ActionRegistryEntry LESS_EQ = make("less_eq", new OperationAction(
        HexPattern.fromAngles("qq", HexDir.SOUTH_WEST))
    );
    public static final ActionRegistryEntry EQUALS = make("equals",
        new ActionRegistryEntry(HexPattern.fromAngles("ad", HexDir.EAST), new OpEquality(false)));
    public static final ActionRegistryEntry NOT_EQUALS = make("not_equals",
        new ActionRegistryEntry(HexPattern.fromAngles("da", HexDir.EAST), new OpEquality(true)));
    public static final ActionRegistryEntry NOT = make("not",
        new ActionRegistryEntry(HexPattern.fromAngles("dw", HexDir.NORTH_WEST), OpBoolNot.INSTANCE));
    public static final ActionRegistryEntry BOOL_COERCE = make("bool_coerce",
        new ActionRegistryEntry(HexPattern.fromAngles("aw", HexDir.NORTH_EAST), OpCoerceToBool.INSTANCE));
    public static final ActionRegistryEntry IF = make("if",
        new ActionRegistryEntry(HexPattern.fromAngles("awdd", HexDir.SOUTH_EAST), OpBoolIf.INSTANCE));

    public static final ActionRegistryEntry RANDOM = make("random",
        new ActionRegistryEntry(HexPattern.fromAngles("eqqq", HexDir.NORTH_WEST), OpRandom.INSTANCE));

    // == Advanced Math ==

    public static final ActionRegistryEntry SIN = make("sin",
        new OperationAction(HexPattern.fromAngles("qqqqqaa", HexDir.SOUTH_EAST)));
    public static final ActionRegistryEntry COS = make("cos",
        new OperationAction(HexPattern.fromAngles("qqqqqad", HexDir.SOUTH_EAST)));
    public static final ActionRegistryEntry TAN = make("tan",
        new OperationAction(HexPattern.fromAngles("wqqqqqadq", HexDir.SOUTH_WEST)));
    public static final ActionRegistryEntry ARCSIN = make("arcsin",
        new OperationAction(HexPattern.fromAngles("ddeeeee", HexDir.SOUTH_EAST)));
    public static final ActionRegistryEntry ARCCOS = make("arccos",
        new OperationAction(HexPattern.fromAngles("adeeeee", HexDir.NORTH_EAST)));
    public static final ActionRegistryEntry ARCTAN = make("arctan",
        new OperationAction(HexPattern.fromAngles("eadeeeeew", HexDir.NORTH_EAST)));
    public static final ActionRegistryEntry ARCTAN2 = make("arctan2",
        new OperationAction(HexPattern.fromAngles("deadeeeeewd", HexDir.WEST)));
    public static final ActionRegistryEntry LOGARITHM = make("logarithm",
        new OperationAction(HexPattern.fromAngles("eqaqe", HexDir.NORTH_WEST)));
    public static final ActionRegistryEntry MODULO = make("modulo",
        new OperationAction(HexPattern.fromAngles("addwaad", HexDir.NORTH_EAST)));

    // == Sets ==

    public static final ActionRegistryEntry UNIQUE = make("unique",
        new OperationAction(HexPattern.fromAngles("aweaqa", HexDir.NORTH_EAST)));

    // == Spells ==

    public static final ActionRegistryEntry PRINT = make("print",
        new ActionRegistryEntry(HexPattern.fromAngles("de", HexDir.NORTH_EAST), OpPrint.INSTANCE));
    public static final ActionRegistryEntry EXPLODE = make("explode",
        new ActionRegistryEntry(HexPattern.fromAngles("aawaawaa", HexDir.EAST), new OpExplode(false)));
    public static final ActionRegistryEntry EXPLODE$FIRE = make("explode/fire",
        new ActionRegistryEntry(HexPattern.fromAngles("ddwddwdd", HexDir.EAST), new OpExplode(true)));
    public static final ActionRegistryEntry ADD_MOTION = make("add_motion",
        new ActionRegistryEntry(HexPattern.fromAngles("awqqqwaqw", HexDir.SOUTH_WEST), OpAddMotion.INSTANCE));
    public static final ActionRegistryEntry BLINK = make("blink",
        new ActionRegistryEntry(HexPattern.fromAngles("awqqqwaq", HexDir.SOUTH_WEST), OpBlink.INSTANCE));
    public static final ActionRegistryEntry BREAK_BLOCK = make("break_block",
        new ActionRegistryEntry(HexPattern.fromAngles("qaqqqqq", HexDir.EAST), OpBreakBlock.INSTANCE));
    public static final ActionRegistryEntry PLACE_BLOCK = make("place_block",
        new ActionRegistryEntry(HexPattern.fromAngles("eeeeede", HexDir.SOUTH_WEST), OpPlaceBlock.INSTANCE));
    public static final ActionRegistryEntry COLORIZE = make("colorize",
        new ActionRegistryEntry(HexPattern.fromAngles("awddwqawqwawq", HexDir.EAST), OpColorize.INSTANCE));
    public static final ActionRegistryEntry CYCLE_VARIANT = make("cycle_variant",
            new ActionRegistryEntry(HexPattern.fromAngles("dwaawedwewdwe", HexDir.WEST), OpCycleVariant.INSTANCE));
    public static final ActionRegistryEntry CREATE_WATER = make("create_water",
        new ActionRegistryEntry(HexPattern.fromAngles("aqawqadaq", HexDir.SOUTH_EAST), new OpCreateFluid(
            MediaConstants.DUST_UNIT,
            Items.WATER_BUCKET,
            Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL),
            Fluids.WATER)));
    public static final ActionRegistryEntry DESTROY_WATER = make("destroy_water",
        new ActionRegistryEntry(HexPattern.fromAngles("dedwedade", HexDir.SOUTH_WEST), OpDestroyFluid.INSTANCE));
    public static final ActionRegistryEntry IGNITE = make("ignite",
        new ActionRegistryEntry(HexPattern.fromAngles("aaqawawa", HexDir.SOUTH_EAST), OpIgnite.INSTANCE));
    public static final ActionRegistryEntry EXTINGUISH = make("extinguish",
        new ActionRegistryEntry(HexPattern.fromAngles("ddedwdwd", HexDir.SOUTH_WEST), OpExtinguish.INSTANCE));
    public static final ActionRegistryEntry CONJURE_BLOCK = make("conjure_block",
        new ActionRegistryEntry(HexPattern.fromAngles("qqa", HexDir.NORTH_EAST), new OpConjureBlock(false)));
    public static final ActionRegistryEntry CONJURE_LIGHT = make("conjure_light",
        new ActionRegistryEntry(HexPattern.fromAngles("qqd", HexDir.NORTH_EAST), new OpConjureBlock(true)));
    public static final ActionRegistryEntry BONEMEAL = make("bonemeal",
        new ActionRegistryEntry(HexPattern.fromAngles("wqaqwawqaqw",
            HexDir.NORTH_EAST), OpTheOnlyReasonAnyoneDownloadedPsi.INSTANCE));
    public static final ActionRegistryEntry RECHARGE = make("recharge",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqwaeaeaeaeaea", HexDir.NORTH_WEST), OpRecharge.INSTANCE));
    public static final ActionRegistryEntry ERASE = make("erase",
        new ActionRegistryEntry(HexPattern.fromAngles("qdqawwaww", HexDir.EAST), OpErase.INSTANCE));
    public static final ActionRegistryEntry EDIFY = make("edify",
        new ActionRegistryEntry(HexPattern.fromAngles("wqaqwd", HexDir.NORTH_EAST), OpEdifySapling.INSTANCE));

    public static final ActionRegistryEntry BEEP = make("beep",
        new ActionRegistryEntry(HexPattern.fromAngles("adaa", HexDir.WEST), OpBeep.INSTANCE));

    public static final ActionRegistryEntry CRAFT$CYPHER = make("craft/cypher", new ActionRegistryEntry(
        HexPattern.fromAngles("waqqqqq", HexDir.EAST), new OpMakePackagedSpell<>(HexItems.CYPHER,
        MediaConstants.CRYSTAL_UNIT)
    ));
    public static final ActionRegistryEntry CRAFT$TRINKET = make("craft/trinket", new ActionRegistryEntry(
        HexPattern.fromAngles(
            "wwaqqqqqeaqeaeqqqeaeq", HexDir.EAST), new OpMakePackagedSpell<>(HexItems.TRINKET,
        5 * MediaConstants.CRYSTAL_UNIT)));
    public static final ActionRegistryEntry CRAFT$ARTIFACT = make("craft/artifact", new ActionRegistryEntry(
        HexPattern.fromAngles("wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq", HexDir.EAST),
        new OpMakePackagedSpell<>(HexItems.ARTIFACT, 10 * MediaConstants.CRYSTAL_UNIT)
    ));
    public static final ActionRegistryEntry CRAFT$BATTERY = make("craft/battery", new ActionRegistryEntry(
        HexPattern.fromAngles("aqqqaqwwaqqqqqeqaqqqawwqwqwqwqwqw", HexDir.SOUTH_WEST), OpMakeBattery.INSTANCE));

    public static final ActionRegistryEntry POTION$WEAKNESS = make("potion/weakness", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqaqwawaw", HexDir.NORTH_WEST), new OpPotionEffect(MobEffects.WEAKNESS,
        MediaConstants.DUST_UNIT / 10, true, false)
    ));
    public static final ActionRegistryEntry POTION$LEVITATION = make("potion/levitation", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqawwawawd", HexDir.WEST), new OpPotionEffect(MobEffects.LEVITATION,
        MediaConstants.DUST_UNIT / 5, false, false)
    ));
    public static final ActionRegistryEntry POTION$WITHER = make("potion/wither", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqaewawawe", HexDir.SOUTH_WEST), new OpPotionEffect(MobEffects.WITHER,
        MediaConstants.DUST_UNIT, true, false)
    ));
    public static final ActionRegistryEntry POTION$POISON = make("potion/poison", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqadwawaww", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.POISON,
        MediaConstants.DUST_UNIT / 3, true, false)
    ));
    public static final ActionRegistryEntry POTION$SLOWNESS = make("potion/slowness", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqadwawaw", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.MOVEMENT_SLOWDOWN,
        MediaConstants.DUST_UNIT / 3, true, false)
    ));

    public static final ActionRegistryEntry POTION$REGENERATION = make("potion/regeneration", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqaawawaedd", HexDir.NORTH_WEST), new OpPotionEffect(MobEffects.REGENERATION,
        MediaConstants.DUST_UNIT, true, true)
    ));
    public static final ActionRegistryEntry POTION$NIGHT_VISION = make("potion/night_vision", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqaawawaeqdd", HexDir.WEST), new OpPotionEffect(MobEffects.NIGHT_VISION,
        MediaConstants.DUST_UNIT / 5, false, true)
    ));
    public static final ActionRegistryEntry POTION$ABSORPTION = make("potion/absorption", new ActionRegistryEntry(
        HexPattern.fromAngles("qqaawawaeqqdd", HexDir.SOUTH_WEST), new OpPotionEffect(MobEffects.ABSORPTION,
        MediaConstants.DUST_UNIT, true, true)
    ));
    public static final ActionRegistryEntry POTION$HASTE = make("potion/haste", new ActionRegistryEntry(
        HexPattern.fromAngles("qaawawaeqqqdd", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.DIG_SPEED,
        MediaConstants.DUST_UNIT / 3, true, true)
    ));
    public static final ActionRegistryEntry POTION$STRENGTH = make("potion/strength", new ActionRegistryEntry(
        HexPattern.fromAngles("aawawaeqqqqdd", HexDir.EAST), new OpPotionEffect(MobEffects.DAMAGE_BOOST,
        MediaConstants.DUST_UNIT / 3, true, true)
    ));

    public static final ActionRegistryEntry FLIGHT$RANGE = make("flight/range",
        new ActionRegistryEntry(HexPattern.fromAngles("awawaawq", HexDir.SOUTH_WEST),
            new OpFlight(OpFlight.Type.LimitRange)));
    public static final ActionRegistryEntry FLIGHT$TIME = make("flight/time",
        new ActionRegistryEntry(HexPattern.fromAngles("dwdwdewq", HexDir.NORTH_EAST),
            new OpFlight(OpFlight.Type.LimitTime)));

    public static final ActionRegistryEntry SENTINEL$CREATE = make("sentinel/create",
        new ActionRegistryEntry(HexPattern.fromAngles("waeawae", HexDir.EAST), new OpCreateSentinel(false)));
    public static final ActionRegistryEntry SENTINEL$DESTROY = make("sentinel/destroy",
        new ActionRegistryEntry(HexPattern.fromAngles("qdwdqdw", HexDir.NORTH_EAST), OpDestroySentinel.INSTANCE));
    public static final ActionRegistryEntry SENTINEL$GET_POS = make("sentinel/get_pos",
        new ActionRegistryEntry(HexPattern.fromAngles("waeawaede", HexDir.EAST), OpGetSentinelPos.INSTANCE));
    public static final ActionRegistryEntry SENTINEL$WAYFIND = make("sentinel/wayfind",
        new ActionRegistryEntry(HexPattern.fromAngles("waeawaedwa", HexDir.EAST), OpGetSentinelWayfind.INSTANCE));

    public static final ActionRegistryEntry LIGHTNING = make("lightning",
        new ActionRegistryEntry(HexPattern.fromAngles("waadwawdaaweewq", HexDir.EAST), OpLightning.INSTANCE));

    public static final ActionRegistryEntry ALTIORA = make("flight",
        new ActionRegistryEntry(HexPattern.fromAngles("eawwaeawawaa", HexDir.NORTH_WEST), OpAltiora.INSTANCE));


    public static final ActionRegistryEntry CREATE_LAVA = make("create_lava",
        new ActionRegistryEntry(HexPattern.fromAngles("eaqawqadaqd", HexDir.EAST), new OpCreateFluid(
            MediaConstants.CRYSTAL_UNIT,
            Items.LAVA_BUCKET,
            Blocks.LAVA_CAULDRON.defaultBlockState(),
            Fluids.LAVA)));
    public static final ActionRegistryEntry TELEPORT = make("teleport/great",
        new ActionRegistryEntry(HexPattern.fromAngles("wwwqqqwwwqqeqqwwwqqwqqdqqqqqdqq",
            HexDir.EAST), OpTeleport.INSTANCE));
    public static final ActionRegistryEntry SENTINEL$GREAT = make("sentinel/create/great",
        new ActionRegistryEntry(HexPattern.fromAngles("waeawaeqqqwqwqqwq", HexDir.EAST), new OpCreateSentinel(true)));
    public static final ActionRegistryEntry DISPEL_RAIN = make("dispel_rain",
        new ActionRegistryEntry(HexPattern.fromAngles("eeewwweeewwaqqddqdqd", HexDir.EAST), new OpWeather(false)));
    public static final ActionRegistryEntry SUMMON_RAIN = make("summon_rain",
        new ActionRegistryEntry(HexPattern.fromAngles("wwweeewwweewdawdwad", HexDir.WEST), new OpWeather(true)));
    public static final ActionRegistryEntry BRAINSWEEP = make("brainsweep",
        new ActionRegistryEntry(HexPattern.fromAngles("qeqwqwqwqwqeqaeqeaqeqaeqaqded",
            HexDir.NORTH_EAST), OpBrainsweep.INSTANCE));

    public static final ActionRegistryEntry AKASHIC$READ = make("akashic/read",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqwqqqqqaq", HexDir.WEST), OpAkashicRead.INSTANCE));
    public static final ActionRegistryEntry AKASHIC$WRITE = make("akashic/write",
        new ActionRegistryEntry(HexPattern.fromAngles("eeeweeeeede", HexDir.EAST), OpAkashicWrite.INSTANCE));

    // == Meta stuff ==

    // Intro/Retro/Consideration are now special-form-likes and aren't even ops.
    // TODO should there be a registry for these too

    // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
    // eval being a space filling curve feels apt doesn't it
    public static final ActionRegistryEntry EVAL = make("eval",
        new ActionRegistryEntry(HexPattern.fromAngles("deaqq", HexDir.SOUTH_EAST), OpEval.INSTANCE));
    public static final ActionRegistryEntry EVAL$CC = make("eval/cc",
        new ActionRegistryEntry(HexPattern.fromAngles("qwaqde", HexDir.NORTH_WEST), OpEvalBreakable.INSTANCE));
    public static final ActionRegistryEntry HALT = make("halt",
        new ActionRegistryEntry(HexPattern.fromAngles("aqdee", HexDir.SOUTH_WEST), OpHalt.INSTANCE));

    public static final ActionRegistryEntry READ = make("read",
        new ActionRegistryEntry(HexPattern.fromAngles("aqqqqq", HexDir.EAST), OpRead.INSTANCE));
    public static final ActionRegistryEntry READ$ENTITY = make("read/entity",
        new ActionRegistryEntry(HexPattern.fromAngles("wawqwqwqwqwqw", HexDir.EAST), OpTheCoolerRead.INSTANCE));
    public static final ActionRegistryEntry WRITE = make("write",
        new ActionRegistryEntry(HexPattern.fromAngles("deeeee", HexDir.EAST), OpWrite.INSTANCE));
    public static final ActionRegistryEntry WRITE$ENTITY = make("write/entity",
        new ActionRegistryEntry(HexPattern.fromAngles("wdwewewewewew", HexDir.EAST), OpTheCoolerWrite.INSTANCE));
    public static final ActionRegistryEntry READABLE = make("readable",
        new ActionRegistryEntry(HexPattern.fromAngles("aqqqqqe", HexDir.EAST), OpReadable.INSTANCE));
    public static final ActionRegistryEntry READABLE$ENTITY = make("readable/entity",
        new ActionRegistryEntry(HexPattern.fromAngles("wawqwqwqwqwqwew", HexDir.EAST), OpTheCoolerReadable.INSTANCE));
    public static final ActionRegistryEntry WRITABLE = make("writable",
        new ActionRegistryEntry(HexPattern.fromAngles("deeeeeq", HexDir.EAST), OpWritable.INSTANCE));
    public static final ActionRegistryEntry WRITABLE$ENTITY = make("writable/entity",
        new ActionRegistryEntry(HexPattern.fromAngles("wdwewewewewewqw", HexDir.EAST), OpTheCoolerWritable.INSTANCE));

    public static final ActionRegistryEntry READ$LOCAL = make("read/local",
        new ActionRegistryEntry(HexPattern.fromAngles("qeewdweddw", HexDir.NORTH_EAST), OpPeekLocal.INSTANCE));
    public static final ActionRegistryEntry WRITE$LOCAL = make("write/local",
        new ActionRegistryEntry(HexPattern.fromAngles("eqqwawqaaw", HexDir.NORTH_WEST), OpPushLocal.INSTANCE));

    public static final ActionRegistryEntry THANATOS = make("thanatos",
        new ActionRegistryEntry(HexPattern.fromAngles("qqaed", HexDir.SOUTH_EAST), OpThanos.INSTANCE));

    // == Consts ==

    public static final ActionRegistryEntry CONST$NULL = make("const/null",
        new ActionRegistryEntry(HexPattern.fromAngles("d", HexDir.EAST), Action.makeConstantOp(new NullIota())));

    public static final ActionRegistryEntry CONST$TRUE = make("const/true",
        new ActionRegistryEntry(HexPattern.fromAngles("aqae",
            HexDir.SOUTH_EAST), Action.makeConstantOp(new BooleanIota(true))));
    public static final ActionRegistryEntry CONST$FALSE = make("const/false",
        new ActionRegistryEntry(HexPattern.fromAngles("dedq",
            HexDir.NORTH_EAST), Action.makeConstantOp(new BooleanIota(false))));

    public static final ActionRegistryEntry CONST$VEC$PX = make("const/vec/px",
        new ActionRegistryEntry(HexPattern.fromAngles(
            "qqqqqea", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(1.0, 0.0, 0.0)))));
    public static final ActionRegistryEntry CONST$VEC$PY = make("const/vec/py",
        new ActionRegistryEntry(HexPattern.fromAngles(
            "qqqqqew", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 1.0, 0.0)))));
    public static final ActionRegistryEntry CONST$VEC$PZ = make("const/vec/pz",
        new ActionRegistryEntry(HexPattern.fromAngles(
            "qqqqqed", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 1.0)))));
    public static final ActionRegistryEntry CONST$VEC$NX = make("const/vec/nx",
        new ActionRegistryEntry(HexPattern.fromAngles(
            "eeeeeqa", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(-1.0, 0.0, 0.0)))));
    public static final ActionRegistryEntry CONST$VEC$NY = make("const/vec/ny",
        new ActionRegistryEntry(HexPattern.fromAngles(
            "eeeeeqw", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, -1.0, 0.0)))));
    public static final ActionRegistryEntry CONST$VEC$NZ = make("const/vec/nz",
        new ActionRegistryEntry(HexPattern.fromAngles(
            "eeeeeqd", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, -1.0)))));
    // Yep, this is what I spend the "plain hexagon" pattern on.
    public static final ActionRegistryEntry CONST$VEC$0 = make("const/vec/0",
        new ActionRegistryEntry(HexPattern.fromAngles(
            "qqqqq", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 0.0)))));

    public static final ActionRegistryEntry CONST$DOUBLE$PI = make("const/double/pi",
        new ActionRegistryEntry(HexPattern.fromAngles("qdwdq",
            HexDir.NORTH_EAST), Action.makeConstantOp(new DoubleIota(Math.PI))));
    public static final ActionRegistryEntry CONST$DOUBLE$TAU = make("const/double/tau",
        new ActionRegistryEntry(HexPattern.fromAngles("eawae",
            HexDir.NORTH_WEST), Action.makeConstantOp(new DoubleIota(HexUtils.TAU))));
    public static final ActionRegistryEntry CONST$E = make("const/double/e",
        new ActionRegistryEntry(HexPattern.fromAngles("aaq",
            HexDir.EAST), Action.makeConstantOp(new DoubleIota(Math.E))));

    // == Entities ==

    public static final ActionRegistryEntry GET_ENTITY = make("get_entity",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqa", HexDir.SOUTH_EAST), new OpGetEntityAt(e -> true)));
    public static final ActionRegistryEntry GET_ENTITY$ANIMAL = make("get_entity/animal",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaawa",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isAnimal)));
    public static final ActionRegistryEntry GET_ENTITY$MONSTER = make("get_entity/monster",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaawq",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isMonster)));
    public static final ActionRegistryEntry GET_ENTITY$ITEM = make("get_entity/item",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaaww",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isItem)));
    public static final ActionRegistryEntry GET_ENTITY$PLAYER = make("get_entity/player",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaawe",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isPlayer)));
    public static final ActionRegistryEntry GET_ENTITY$LIVING = make("get_entity/living",
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaawd",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isLiving)));

    public static final ActionRegistryEntry ZONE_ENTITY = make("zone_entity", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwded", HexDir.SOUTH_EAST), new OpGetEntitiesBy(e -> true, false)
    ));
    public static final ActionRegistryEntry ZONE_ENTITY$ANIMAL = make("zone_entity/animal", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddwa", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, false)
    ));
    public static final ActionRegistryEntry ZONE_ENTITY$NOT_ANIMAL = make("zone_entity/not_animal",
        new ActionRegistryEntry(
            HexPattern.fromAngles("eeeeewaqaawa", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal,
            true)
        ));
    public static final ActionRegistryEntry ZONE_ENTITY$MONSTER = make("zone_entity/monster", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddwq", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, false)
    ));
    public static final ActionRegistryEntry ZONE_ENTITY$NOT_MONSTER = make("zone_entity/not_monster",
        new ActionRegistryEntry(
            HexPattern.fromAngles("eeeeewaqaawq", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isMonster,
            true)
        ));
    public static final ActionRegistryEntry ZONE_ENTITY$ITEM = make("zone_entity/item", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddww", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isItem, false)
    ));
    public static final ActionRegistryEntry ZONE_ENTITY$NOT_ITEM = make("zone_entity/not_item", new ActionRegistryEntry(
        HexPattern.fromAngles("eeeeewaqaaww", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isItem, true)
    ));
    public static final ActionRegistryEntry ZONE_ENTITY$PLAYER = make("zone_entity/player", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddwe", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, false)
    ));
    public static final ActionRegistryEntry ZONE_ENTITY$NOT_PLAYER = make("zone_entity/not_player",
        new ActionRegistryEntry(
            HexPattern.fromAngles("eeeeewaqaawe", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer,
            true)
        ));
    public static final ActionRegistryEntry ZONE_ENTITY$LIVING = make("zone_entity/living", new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddwd", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, false)
    ));
    public static final ActionRegistryEntry ZONE_ENTITY$NOT_LIVING = make("zone_entity/not_living",
        new ActionRegistryEntry(
            HexPattern.fromAngles("eeeeewaqaawd", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isLiving,
            true)
        ));

    // == Lists ==

    public static final ActionRegistryEntry APPEND = make("append",
        new OperationAction(HexPattern.fromAngles("edqde", HexDir.SOUTH_WEST)));
    public static final ActionRegistryEntry UNAPPEND = make("unappend",
            new OperationAction(HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST)));
//    public static final ActionRegistryEntry CONCAT = make("concat",
//        new ActionRegistryEntry(HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST), OpConcat.INSTANCE));
    public static final ActionRegistryEntry INDEX = make("index",
        new OperationAction(HexPattern.fromAngles("deeed", HexDir.NORTH_WEST)));
    public static final ActionRegistryEntry FOR_EACH = make("for_each",
        new ActionRegistryEntry(HexPattern.fromAngles("dadad", HexDir.NORTH_EAST), OpForEach.INSTANCE));
//    public static final ActionRegistryEntry LIST_SIZE = make("list_size",
//        new ActionRegistryEntry(HexPattern.fromAngles("aqaeaq", HexDir.EAST), OpListSize.INSTANCE));
    public static final ActionRegistryEntry SINGLETON = make("singleton",
        new ActionRegistryEntry(HexPattern.fromAngles("adeeed", HexDir.EAST), OpSingleton.INSTANCE));
    public static final ActionRegistryEntry EMPTY_LIST = make("empty_list",
        new ActionRegistryEntry(HexPattern.fromAngles("qqaeaae", HexDir.NORTH_EAST), OpEmptyList.INSTANCE));
    public static final ActionRegistryEntry REVERSE = make("reverse",
        new OperationAction(HexPattern.fromAngles("qqqaede", HexDir.EAST)));
    public static final ActionRegistryEntry LAST_N_LIST = make("last_n_list",
        new ActionRegistryEntry(HexPattern.fromAngles("ewdqdwe", HexDir.SOUTH_WEST), OpLastNToList.INSTANCE));
    public static final ActionRegistryEntry SPLAT = make("splat",
        new ActionRegistryEntry(HexPattern.fromAngles("qwaeawq", HexDir.NORTH_WEST), OpSplat.INSTANCE));
    public static final ActionRegistryEntry INDEX_OF = make("index_of",
        new OperationAction(HexPattern.fromAngles("dedqde", HexDir.EAST)));
    public static final ActionRegistryEntry REMOVE_FROM = make("remove_from",
        new OperationAction(HexPattern.fromAngles("edqdewaqa", HexDir.SOUTH_WEST)));
    public static final ActionRegistryEntry SLICE = make("slice",
        new OperationAction(HexPattern.fromAngles("qaeaqwded", HexDir.NORTH_WEST)));
    public static final ActionRegistryEntry REPLACE = make("replace",
        new OperationAction(HexPattern.fromAngles("wqaeaqw", HexDir.NORTH_WEST)));
    public static final ActionRegistryEntry CONSTRUCT = make("construct",
        new OperationAction(HexPattern.fromAngles("ddewedd", HexDir.SOUTH_EAST)));
    public static final ActionRegistryEntry DECONSTRUCT = make("deconstruct",
        new OperationAction(HexPattern.fromAngles("aaqwqaa", HexDir.SOUTH_WEST)));

    // Xplat interops
    static {
        if (PehkuiInterop.isActive()) {
            make("interop/pehkui/get",
                new ActionRegistryEntry(HexPattern.fromAngles("aawawwawwa", HexDir.NORTH_WEST), OpGetScale.INSTANCE));
            make("interop/pehkui/set",
                new ActionRegistryEntry(HexPattern.fromAngles("ddwdwwdwwd", HexDir.NORTH_EAST), OpSetScale.INSTANCE));
        }
    }

    public static ActionRegistryEntry make(String name, ActionRegistryEntry are) {
        var old = ACTIONS.put(modLoc(name), are);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return are;
    }

    public static ActionRegistryEntry make(String name, OperationAction oa) {
        var are = new ActionRegistryEntry(oa.getPattern(), oa);
        var old = ACTIONS.put(modLoc(name), are);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return are;
    }

    public static void register(BiConsumer<ActionRegistryEntry, ResourceLocation> r) {
        for (var e : ACTIONS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }
}
