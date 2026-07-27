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
import at.petrak.hexcasting.common.casting.actions.akashic.OpAkashicRead;
import at.petrak.hexcasting.common.casting.actions.akashic.OpAkashicWrite;
import at.petrak.hexcasting.common.casting.actions.circles.OpCircleBounds;
import at.petrak.hexcasting.common.casting.actions.circles.OpImpetusDir;
import at.petrak.hexcasting.common.casting.actions.circles.OpImpetusPos;
import at.petrak.hexcasting.common.casting.actions.escaping.*;
import at.petrak.hexcasting.common.casting.actions.eval.*;
import at.petrak.hexcasting.common.casting.actions.lists.OpEmptyList;
import at.petrak.hexcasting.common.casting.actions.lists.OpLastNToList;
import at.petrak.hexcasting.common.casting.actions.lists.OpSingleton;
import at.petrak.hexcasting.common.casting.actions.lists.OpSplat;
import at.petrak.hexcasting.common.casting.actions.local.OpPeekLocal;
import at.petrak.hexcasting.common.casting.actions.local.OpPushLocal;
import at.petrak.hexcasting.common.casting.actions.math.OpCoerceToAxial;
import at.petrak.hexcasting.common.casting.actions.math.OpRandom;
import at.petrak.hexcasting.common.casting.actions.math.logic.OpBoolIf;
import at.petrak.hexcasting.common.casting.actions.math.logic.OpCoerceToBool;
import at.petrak.hexcasting.common.casting.actions.math.logic.OpEquality;
import at.petrak.hexcasting.common.casting.actions.math.logic.OpTypeEquality;
import at.petrak.hexcasting.common.casting.actions.queryentity.*;
import at.petrak.hexcasting.common.casting.actions.raycast.OpBlockAxisRaycast;
import at.petrak.hexcasting.common.casting.actions.raycast.OpBlockRaycast;
import at.petrak.hexcasting.common.casting.actions.raycast.OpEntityRaycast;
import at.petrak.hexcasting.common.casting.actions.rw.*;
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetCaster;
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntitiesBy;
import at.petrak.hexcasting.common.casting.actions.selectors.OpGetEntityAt;
import at.petrak.hexcasting.common.casting.actions.spells.*;
import at.petrak.hexcasting.common.casting.actions.spells.great.*;
import at.petrak.hexcasting.common.casting.actions.spells.sentinel.OpCreateSentinel;
import at.petrak.hexcasting.common.casting.actions.spells.sentinel.OpDestroySentinel;
import at.petrak.hexcasting.common.casting.actions.spells.sentinel.OpGetSentinelPos;
import at.petrak.hexcasting.common.casting.actions.spells.sentinel.OpGetSentinelWayfind;
import at.petrak.hexcasting.common.casting.actions.stack.*;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.interop.pehkui.OpGetScale;
import at.petrak.hexcasting.interop.pehkui.OpSetScale;
import at.petrak.hexcasting.interop.pehkui.PehkuiInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
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
    private static final IXplatRegister<ActionRegistryEntry> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.ACTION);

    public static final Registry<ActionRegistryEntry> REGISTRY = IXplatAbstractions.INSTANCE.getActionRegistry();

    public static void register() {
        REGISTER.registerAll();
    }

    // In general:
    // - CCW is the normal or construction version
    // - CW is the special or destruction version

    public static final Holder<ActionRegistryEntry> GET_CASTER = REGISTER.registerHolder("get_caster", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qaq", HexDir.NORTH_EAST), OpGetCaster.INSTANCE));
    public static final Holder<ActionRegistryEntry> ENTITY_POS$EYE = REGISTER.registerHolder("entity_pos/eye", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aa", HexDir.EAST), new OpEntityPos(false)));
    public static final Holder<ActionRegistryEntry> ENTITY_POS$FOOT = REGISTER.registerHolder("entity_pos/foot", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("dd", HexDir.NORTH_EAST), new OpEntityPos(true)));
    public static final Holder<ActionRegistryEntry> ENTITY_LOOK = REGISTER.registerHolder("get_entity_look", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wa", HexDir.EAST), OpEntityLook.INSTANCE));
    public static final Holder<ActionRegistryEntry> ENTITY_HEIGHT = REGISTER.registerHolder("get_entity_height", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("awq", HexDir.NORTH_EAST), OpEntityHeight.INSTANCE));
    public static final Holder<ActionRegistryEntry> ENTITY_VELOCITY = REGISTER.registerHolder("get_entity_velocity", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wq", HexDir.EAST), OpEntityVelocity.INSTANCE));

    // == Getters ==

    public static final Holder<ActionRegistryEntry> RAYCAST = REGISTER.registerHolder("raycast", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wqaawdd", HexDir.EAST), OpBlockRaycast.INSTANCE));
    public static final Holder<ActionRegistryEntry> RAYCAST_AXIS = REGISTER.registerHolder("raycast/axis", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("weddwaa", HexDir.EAST), OpBlockAxisRaycast.INSTANCE));
    public static final Holder<ActionRegistryEntry> RAYCAST_ENTITY = REGISTER.registerHolder("raycast/entity", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("weaqa", HexDir.EAST), OpEntityRaycast.INSTANCE));

    // == spell circle getters ==

    public static final Holder<ActionRegistryEntry> CIRCLE$IMPETUS_POST = REGISTER.registerHolder("circle/impetus_pos", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eaqwqae", HexDir.SOUTH_WEST), OpImpetusPos.INSTANCE));
    public static final Holder<ActionRegistryEntry> CIRCLE$IMPETUS_DIR = REGISTER.registerHolder("circle/impetus_dir", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eaqwqaewede", HexDir.SOUTH_WEST), OpImpetusDir.INSTANCE));
    public static final Holder<ActionRegistryEntry> CIRCLE$BOUNDS$MIN = REGISTER.registerHolder("circle/bounds/min", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eaqwqaewdd", HexDir.SOUTH_WEST), new OpCircleBounds(false)));
    public static final Holder<ActionRegistryEntry> CIRCLE$BOUNDS$MAX = REGISTER.registerHolder("circle/bounds/max", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aqwqawaaqa", HexDir.WEST), new OpCircleBounds(true)));

    // == Modify Stack ==

    public static final Holder<ActionRegistryEntry> SWAP = REGISTER.registerHolder("swap", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aawdd", HexDir.EAST), new OpTwiddling(2, new int[]{1, 0})));
    public static final Holder<ActionRegistryEntry> ROTATE = REGISTER.registerHolder("rotate", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aaeaa", HexDir.EAST), new OpTwiddling(3, new int[]{1, 2, 0})));
    public static final Holder<ActionRegistryEntry> ROTATE_REVERSE = REGISTER.registerHolder("rotate_reverse", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("ddqdd",
            HexDir.NORTH_EAST), new OpTwiddling(3, new int[]{2, 0, 1})));
    public static final Holder<ActionRegistryEntry> DUPLICATE = REGISTER.registerHolder("duplicate", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aadaa", HexDir.EAST), new OpTwiddling(1, new int[]{0, 0})));
    public static final Holder<ActionRegistryEntry> OVER = REGISTER.registerHolder("over", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aaedd", HexDir.EAST), new OpTwiddling(2, new int[]{0, 1, 0})));
    public static final Holder<ActionRegistryEntry> TUCK = REGISTER.registerHolder("tuck", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("ddqaa", HexDir.EAST), new OpTwiddling(2, new int[]{1, 0, 1})));
    public static final Holder<ActionRegistryEntry> TWO_DUP = REGISTER.registerHolder("2dup", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aadadaaw",
            HexDir.EAST), new OpTwiddling(2, new int[]{0, 1, 0, 1})));

    public static final Holder<ActionRegistryEntry> STACK_LEN = REGISTER.registerHolder("stack_len", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qwaeawqaeaqa", HexDir.NORTH_WEST), OpStackSize.INSTANCE));
    public static final Holder<ActionRegistryEntry> DUPLICATE_N = REGISTER.registerHolder("duplicate_n", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aadaadaa", HexDir.EAST), OpDuplicateN.INSTANCE));
    public static final Holder<ActionRegistryEntry> FISHERMAN = REGISTER.registerHolder("fisherman", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("ddad", HexDir.WEST), OpFisherman.INSTANCE));
    public static final Holder<ActionRegistryEntry> FISHERMAN$COPY = REGISTER.registerHolder("fisherman/copy", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aada", HexDir.EAST), OpFishermanButItCopies.INSTANCE));
    public static final Holder<ActionRegistryEntry> SWIZZLE = REGISTER.registerHolder("swizzle", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qaawdde",
            HexDir.SOUTH_EAST), OpAlwinfyHasAscendedToABeingOfPureMath.INSTANCE));

    // == Math ==

    public static final Holder<ActionRegistryEntry> ADD = make("add",
        new OperationAction(HexPattern.fromAngles("waaw", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> SUB = make("sub",
        new OperationAction(HexPattern.fromAngles("wddw", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> MUL_DOT = make("mul",
        new OperationAction(HexPattern.fromAngles("waqaw", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> DIV_CROSS = make("div",
        new OperationAction(HexPattern.fromAngles("wdedw", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> ABS = make("abs",
        new OperationAction(HexPattern.fromAngles("wqaqw", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> POW_PROJ = make("pow",
        new OperationAction(HexPattern.fromAngles("wedew", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> FLOOR = make("floor",
        new OperationAction(HexPattern.fromAngles("ewq", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> CEIL = make("ceil",
        new OperationAction(HexPattern.fromAngles("qwe", HexDir.EAST)));

    public static final Holder<ActionRegistryEntry> CONSTRUCT_VEC = make("construct_vec",
        new OperationAction(HexPattern.fromAngles("eqqqqq", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> DECONSTRUCT_VEC = make("deconstruct_vec",
        new OperationAction(HexPattern.fromAngles("qeeeee", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> COERCE_AXIAL = REGISTER.registerHolder("coerce_axial", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqaww", HexDir.NORTH_WEST), OpCoerceToAxial.INSTANCE));

    // == Logic ==

    public static final Holder<ActionRegistryEntry> AND = make("and",
        new OperationAction(HexPattern.fromAngles("wdw", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> OR = make("or",
        new OperationAction(HexPattern.fromAngles("waw", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> NOT = make("not",
        new OperationAction(HexPattern.fromAngles("dw", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> XOR = make("xor",
        new OperationAction(HexPattern.fromAngles("dwa", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> GREATER = make("greater", new OperationAction(
        HexPattern.fromAngles("e", HexDir.SOUTH_EAST))
    );
    public static final Holder<ActionRegistryEntry> LESS = make("less", new OperationAction(
        HexPattern.fromAngles("q", HexDir.SOUTH_WEST))
    );
    public static final Holder<ActionRegistryEntry> GREATER_EQ = make("greater_eq", new OperationAction(
        HexPattern.fromAngles("ee", HexDir.SOUTH_EAST))
    );
    public static final Holder<ActionRegistryEntry> LESS_EQ = make("less_eq", new OperationAction(
        HexPattern.fromAngles("qq", HexDir.SOUTH_WEST))
    );
    public static final Holder<ActionRegistryEntry> EQUALS = REGISTER.registerHolder("equals", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("ad", HexDir.EAST), new OpEquality(false)));
    public static final Holder<ActionRegistryEntry> NOT_EQUALS = REGISTER.registerHolder("not_equals", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("da", HexDir.EAST), new OpEquality(true)));
    public static final Holder<ActionRegistryEntry> TYPE_EQUALS = REGISTER.registerHolder("type_equals", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wawdw", HexDir.EAST), new OpTypeEquality(false)));
    public static final Holder<ActionRegistryEntry> TYPE_NOT_EQUALS = REGISTER.registerHolder("type_not_equals", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wdwaw", HexDir.EAST), new OpTypeEquality(true)));
    public static final Holder<ActionRegistryEntry> BOOL_COERCE = REGISTER.registerHolder("bool_coerce", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aw", HexDir.NORTH_EAST), OpCoerceToBool.INSTANCE));
    public static final Holder<ActionRegistryEntry> IF = REGISTER.registerHolder("if", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("awdd", HexDir.SOUTH_EAST), OpBoolIf.INSTANCE));

    public static final Holder<ActionRegistryEntry> RANDOM = REGISTER.registerHolder("random", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eqqq", HexDir.NORTH_WEST), OpRandom.INSTANCE));

    // == Advanced Math ==

    public static final Holder<ActionRegistryEntry> SIN = make("sin",
        new OperationAction(HexPattern.fromAngles("qqqqqaa", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> COS = make("cos",
        new OperationAction(HexPattern.fromAngles("qqqqqad", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> TAN = make("tan",
        new OperationAction(HexPattern.fromAngles("wqqqqqadq", HexDir.SOUTH_WEST)));
    public static final Holder<ActionRegistryEntry> ARCSIN = make("arcsin",
        new OperationAction(HexPattern.fromAngles("ddeeeee", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> ARCCOS = make("arccos",
        new OperationAction(HexPattern.fromAngles("adeeeee", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> ARCTAN = make("arctan",
        new OperationAction(HexPattern.fromAngles("eadeeeeew", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> ARCTAN2 = make("arctan2",
        new OperationAction(HexPattern.fromAngles("deadeeeeewd", HexDir.WEST)));
    public static final Holder<ActionRegistryEntry> LOGARITHM = make("logarithm",
        new OperationAction(HexPattern.fromAngles("eqaqe", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> MODULO = make("modulo",
        new OperationAction(HexPattern.fromAngles("addwaad", HexDir.NORTH_EAST)));

    // == Sets ==

    public static final Holder<ActionRegistryEntry> UNIQUE = make("unique",
        new OperationAction(HexPattern.fromAngles("aweaqa", HexDir.NORTH_EAST)));

    // == Spells ==

    public static final Holder<ActionRegistryEntry> PRINT = REGISTER.registerHolder("print", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("de", HexDir.NORTH_EAST), OpPrint.INSTANCE));
    public static final Holder<ActionRegistryEntry> EXPLODE = REGISTER.registerHolder("explode", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aawaawaa", HexDir.EAST), new OpExplode(false)));
    public static final Holder<ActionRegistryEntry> EXPLODE$FIRE = REGISTER.registerHolder("explode/fire", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("ddwddwdd", HexDir.EAST), new OpExplode(true)));
    public static final Holder<ActionRegistryEntry> ADD_MOTION = REGISTER.registerHolder("add_motion", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("awqqqwaqw", HexDir.SOUTH_WEST), OpAddMotion.INSTANCE));
    public static final Holder<ActionRegistryEntry> BLINK = REGISTER.registerHolder("blink", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("awqqqwaq", HexDir.SOUTH_WEST), OpBlink.INSTANCE));
    public static final Holder<ActionRegistryEntry> BREAK_BLOCK = REGISTER.registerHolder("break_block", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qaqqqqq", HexDir.EAST), OpBreakBlock.INSTANCE));
    public static final Holder<ActionRegistryEntry> PLACE_BLOCK = REGISTER.registerHolder("place_block", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eeeeede", HexDir.SOUTH_WEST), OpPlaceBlock.INSTANCE));
    public static final Holder<ActionRegistryEntry> COLORIZE = REGISTER.registerHolder("colorize", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("awddwqawqwawq", HexDir.EAST), OpColorize.INSTANCE));
    public static final Holder<ActionRegistryEntry> CYCLE_VARIANT = REGISTER.registerHolder("cycle_variant", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("dwaawedwewdwe", HexDir.WEST), OpCycleVariant.INSTANCE));
    public static final Holder<ActionRegistryEntry> CREATE_WATER = REGISTER.registerHolder("create_water", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aqawqadaq", HexDir.SOUTH_EAST), new OpCreateFluid(
            MediaConstants.DUST_UNIT,
            Items.WATER_BUCKET,
            Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL),
            Fluids.WATER)));
    public static final Holder<ActionRegistryEntry> DESTROY_WATER = REGISTER.registerHolder("destroy_water", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("dedwedade", HexDir.SOUTH_WEST), OpDestroyFluid.INSTANCE));
    public static final Holder<ActionRegistryEntry> IGNITE = REGISTER.registerHolder("ignite", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aaqawawa", HexDir.SOUTH_EAST), OpIgnite.INSTANCE));
    public static final Holder<ActionRegistryEntry> EXTINGUISH = REGISTER.registerHolder("extinguish", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("ddedwdwd", HexDir.SOUTH_WEST), OpExtinguish.INSTANCE));
    public static final Holder<ActionRegistryEntry> CONJURE_BLOCK = REGISTER.registerHolder("conjure_block", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqa", HexDir.NORTH_EAST), new OpConjureBlock(false)));
    public static final Holder<ActionRegistryEntry> CONJURE_LIGHT = REGISTER.registerHolder("conjure_light", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqd", HexDir.NORTH_EAST), new OpConjureBlock(true)));
    public static final Holder<ActionRegistryEntry> BONEMEAL = REGISTER.registerHolder("bonemeal", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wqaqwawqaqw",
            HexDir.NORTH_EAST), OpTheOnlyReasonAnyoneDownloadedPsi.INSTANCE));
    public static final Holder<ActionRegistryEntry> RECHARGE = REGISTER.registerHolder("recharge", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqwaeaeaeaeaea", HexDir.NORTH_WEST), OpRecharge.INSTANCE));
    public static final Holder<ActionRegistryEntry> ERASE = REGISTER.registerHolder("erase", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qdqawwaww", HexDir.EAST), OpErase.INSTANCE));
    public static final Holder<ActionRegistryEntry> EDIFY = REGISTER.registerHolder("edify", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wqaqwd", HexDir.NORTH_EAST), OpEdifySapling.INSTANCE));

    public static final Holder<ActionRegistryEntry> BEEP = REGISTER.registerHolder("beep", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("adaa", HexDir.WEST), OpBeep.INSTANCE));

    public static final Holder<ActionRegistryEntry> CRAFT$CYPHER = REGISTER.registerHolder("craft/cypher", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("waqqqqq", HexDir.EAST), 
        new OpMakePackagedSpell(s -> (s.is(HexItems.CYPHER.get())||s.is(HexItems.ANCIENT_CYPHER.get())), HexItems.CYPHER.get()::getDescription, MediaConstants.CRYSTAL_UNIT)
    ));
    public static final Holder<ActionRegistryEntry> CRAFT$TRINKET = REGISTER.registerHolder("craft/trinket", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("wwaqqqqqeaqeaeqqqeaeq", HexDir.EAST), 
        new OpMakePackagedSpell(HexItems.TRINKET.get(), 5 * MediaConstants.CRYSTAL_UNIT)
    ));
    public static final Holder<ActionRegistryEntry> CRAFT$ARTIFACT = REGISTER.registerHolder("craft/artifact", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq", HexDir.EAST),
        new OpMakePackagedSpell(HexItems.ARTIFACT.get(), 10 * MediaConstants.CRYSTAL_UNIT)
    ));
    public static final Holder<ActionRegistryEntry> CRAFT$BATTERY = REGISTER.registerHolder("craft/battery", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("aqqqaqwwaqqqqqeqaqqqawwqwqwqwqwqw", HexDir.SOUTH_WEST), OpMakeBattery.INSTANCE));

    public static final Holder<ActionRegistryEntry> POTION$WEAKNESS = REGISTER.registerHolder("potion/weakness", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqaqwawaw", HexDir.NORTH_WEST), new OpPotionEffect(MobEffects.WEAKNESS,
        MediaConstants.DUST_UNIT / 10, true, false)
    ));
    public static final Holder<ActionRegistryEntry> POTION$LEVITATION = REGISTER.registerHolder("potion/levitation", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqawwawawd", HexDir.WEST), new OpPotionEffect(MobEffects.LEVITATION,
        MediaConstants.DUST_UNIT / 5, false, false)
    ));
    public static final Holder<ActionRegistryEntry> POTION$WITHER = REGISTER.registerHolder("potion/wither", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqaewawawe", HexDir.SOUTH_WEST), new OpPotionEffect(MobEffects.WITHER,
        MediaConstants.DUST_UNIT, true, false)
    ));
    public static final Holder<ActionRegistryEntry> POTION$POISON = REGISTER.registerHolder("potion/poison", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqadwawaww", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.POISON,
        MediaConstants.DUST_UNIT / 3, true, false)
    ));
    public static final Holder<ActionRegistryEntry> POTION$SLOWNESS = REGISTER.registerHolder("potion/slowness", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqadwawaw", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.MOVEMENT_SLOWDOWN,
        MediaConstants.DUST_UNIT / 3, true, false)
    ));

    public static final Holder<ActionRegistryEntry> POTION$REGENERATION = REGISTER.registerHolder("potion/regeneration", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqaawawaedd", HexDir.NORTH_WEST), new OpPotionEffect(MobEffects.REGENERATION,
        MediaConstants.DUST_UNIT, true, true)
    ));
    public static final Holder<ActionRegistryEntry> POTION$NIGHT_VISION = REGISTER.registerHolder("potion/night_vision", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqaawawaeqdd", HexDir.WEST), new OpPotionEffect(MobEffects.NIGHT_VISION,
        MediaConstants.DUST_UNIT / 5, false, true)
    ));
    public static final Holder<ActionRegistryEntry> POTION$ABSORPTION = REGISTER.registerHolder("potion/absorption", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqaawawaeqqdd", HexDir.SOUTH_WEST), new OpPotionEffect(MobEffects.ABSORPTION,
        MediaConstants.DUST_UNIT, true, true)
    ));
    public static final Holder<ActionRegistryEntry> POTION$HASTE = REGISTER.registerHolder("potion/haste", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qaawawaeqqqdd", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.DIG_SPEED,
        MediaConstants.DUST_UNIT / 3, true, true)
    ));
    public static final Holder<ActionRegistryEntry> POTION$STRENGTH = REGISTER.registerHolder("potion/strength", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("aawawaeqqqqdd", HexDir.EAST), new OpPotionEffect(MobEffects.DAMAGE_BOOST,
        MediaConstants.DUST_UNIT / 3, true, true)
    ));

    public static final Holder<ActionRegistryEntry> FLIGHT$RANGE = REGISTER.registerHolder("flight/range", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("awawaawq", HexDir.SOUTH_WEST),
            new OpFlight(OpFlight.Type.LimitRange)));
    public static final Holder<ActionRegistryEntry> FLIGHT$TIME = REGISTER.registerHolder("flight/time", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("dwdwdewq", HexDir.NORTH_EAST),
            new OpFlight(OpFlight.Type.LimitTime)));
    public static final Holder<ActionRegistryEntry> FLIGHT$CAN_FLY = REGISTER.registerHolder("flight/can_fly", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("dwdwdeweaqa", HexDir.NORTH_EAST),
            OpCanEntityHexFly.INSTANCE));

    public static final Holder<ActionRegistryEntry> SENTINEL$CREATE = REGISTER.registerHolder("sentinel/create", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("waeawae", HexDir.EAST), new OpCreateSentinel(false)));
    public static final Holder<ActionRegistryEntry> SENTINEL$DESTROY = REGISTER.registerHolder("sentinel/destroy", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qdwdqdw", HexDir.NORTH_EAST), OpDestroySentinel.INSTANCE));
    public static final Holder<ActionRegistryEntry> SENTINEL$GET_POS = REGISTER.registerHolder("sentinel/get_pos", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("waeawaede", HexDir.EAST), OpGetSentinelPos.INSTANCE));
    public static final Holder<ActionRegistryEntry> SENTINEL$WAYFIND = REGISTER.registerHolder("sentinel/wayfind", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("waeawaedwa", HexDir.EAST), OpGetSentinelWayfind.INSTANCE));

    public static final Holder<ActionRegistryEntry> LIGHTNING = REGISTER.registerHolder("lightning", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("waadwawdaaweewq", HexDir.EAST), OpLightning.INSTANCE));

    public static final Holder<ActionRegistryEntry> ALTIORA = REGISTER.registerHolder("flight", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eawwaeawawaa", HexDir.NORTH_WEST), OpAltiora.INSTANCE));


    public static final Holder<ActionRegistryEntry> CREATE_LAVA = REGISTER.registerHolder("create_lava", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eaqawqadaqd", HexDir.EAST), new OpCreateFluid(
            MediaConstants.CRYSTAL_UNIT,
            Items.LAVA_BUCKET,
            Blocks.LAVA_CAULDRON.defaultBlockState(),
            Fluids.LAVA)));
    public static final Holder<ActionRegistryEntry> TELEPORT = REGISTER.registerHolder("teleport/great", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wwwqqqwwwqqeqqwwwqqwqqdqqqqqdqq",
            HexDir.EAST), OpTeleport.INSTANCE));
    public static final Holder<ActionRegistryEntry> SENTINEL$GREAT = REGISTER.registerHolder("sentinel/create/great", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("waeawaeqqqwqwqqwq", HexDir.EAST), new OpCreateSentinel(true)));
    public static final Holder<ActionRegistryEntry> DISPEL_RAIN = REGISTER.registerHolder("dispel_rain", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eeewwweeewwaqqddqdqd", HexDir.EAST), new OpWeather(false)));
    public static final Holder<ActionRegistryEntry> SUMMON_RAIN = REGISTER.registerHolder("summon_rain", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wwweeewwweewdawdwad", HexDir.WEST), new OpWeather(true)));
    public static final Holder<ActionRegistryEntry> BRAINSWEEP = REGISTER.registerHolder("brainsweep", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qeqwqwqwqwqeqaeqeaqeqaeqaqded",
            HexDir.NORTH_EAST), OpBrainsweep.INSTANCE));

    public static final Holder<ActionRegistryEntry> AKASHIC$READ = REGISTER.registerHolder("akashic/read", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqwqqqqqaq", HexDir.WEST), OpAkashicRead.INSTANCE));
    public static final Holder<ActionRegistryEntry> AKASHIC$WRITE = REGISTER.registerHolder("akashic/write", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eeeweeeeede", HexDir.EAST), OpAkashicWrite.INSTANCE));

    // == Meta stuff ==

    public static final Holder<ActionRegistryEntry> ESCAPE = REGISTER.registerHolder("escape", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqaw", HexDir.WEST), OpEscape.INSTANCE));
    public static final Holder<ActionRegistryEntry> OPEN_PAREN = REGISTER.registerHolder("open_paren", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqq", HexDir.WEST), OpOpenParen.INSTANCE));
    public static final Holder<ActionRegistryEntry> CLOSE_PAREN = REGISTER.registerHolder("close_paren", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eee", HexDir.EAST), OpCloseParen.INSTANCE));
    public static final Holder<ActionRegistryEntry> UNDO = REGISTER.registerHolder("undo", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eeedw", HexDir.EAST), OpUndo.INSTANCE));

    public static final Holder<ActionRegistryEntry> SIMULATE = REGISTER.registerHolder("simulate", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("deaq", HexDir.EAST), OpSimulate.INSTANCE));

    // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
    // eval being a space filling curve feels apt doesn't it
    public static final Holder<ActionRegistryEntry> EVAL = REGISTER.registerHolder("eval", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("deaqq", HexDir.SOUTH_EAST), OpEval.INSTANCE));
    public static final Holder<ActionRegistryEntry> EVAL$CC = REGISTER.registerHolder("eval/cc", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qwaqde", HexDir.NORTH_WEST), OpEvalBreakable.INSTANCE));
    public static final Holder<ActionRegistryEntry> HALT = REGISTER.registerHolder("halt", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aqdee", HexDir.SOUTH_WEST), OpHalt.INSTANCE));

    public static final Holder<ActionRegistryEntry> READ = REGISTER.registerHolder("read", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aqqqqq", HexDir.EAST), OpRead.INSTANCE));
    public static final Holder<ActionRegistryEntry> READ$ENTITY = REGISTER.registerHolder("read/entity", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wawqwqwqwqwqw", HexDir.EAST), OpTheCoolerRead.INSTANCE));
    public static final Holder<ActionRegistryEntry> WRITE = REGISTER.registerHolder("write", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("deeeee", HexDir.EAST), OpWrite.INSTANCE));
    public static final Holder<ActionRegistryEntry> WRITE$ENTITY = REGISTER.registerHolder("write/entity", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wdwewewewewew", HexDir.EAST), OpTheCoolerWrite.INSTANCE));

    public static final Holder<ActionRegistryEntry> READ$LOCAL = REGISTER.registerHolder("read/local", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qeewdweddw", HexDir.NORTH_EAST), OpPeekLocal.INSTANCE));
    public static final Holder<ActionRegistryEntry> WRITE$LOCAL = REGISTER.registerHolder("write/local", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eqqwawqaaw", HexDir.NORTH_WEST), OpPushLocal.INSTANCE));

    public static final Holder<ActionRegistryEntry> THANATOS = REGISTER.registerHolder("thanatos", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqaed", HexDir.SOUTH_EAST), OpThanos.INSTANCE));

    // == Consts ==

    public static final Holder<ActionRegistryEntry> CONST$NULL = REGISTER.registerHolder("const/null", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("d", HexDir.EAST), Action.makeConstantOp(new NullIota())));

    public static final Holder<ActionRegistryEntry> CONST$TRUE = REGISTER.registerHolder("const/true", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aqae",
            HexDir.SOUTH_EAST), Action.makeConstantOp(new BooleanIota(true))));
    public static final Holder<ActionRegistryEntry> CONST$FALSE = REGISTER.registerHolder("const/false", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("dedq",
            HexDir.NORTH_EAST), Action.makeConstantOp(new BooleanIota(false))));

    public static final Holder<ActionRegistryEntry> CONST$VEC$PX = REGISTER.registerHolder("const/vec/px", () ->
        new ActionRegistryEntry(HexPattern.fromAngles(
            "qqqqqea", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(1.0, 0.0, 0.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$PY = REGISTER.registerHolder("const/vec/py", () ->
        new ActionRegistryEntry(HexPattern.fromAngles(
            "qqqqqew", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 1.0, 0.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$PZ = REGISTER.registerHolder("const/vec/pz", () ->
        new ActionRegistryEntry(HexPattern.fromAngles(
            "qqqqqed", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 1.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$NX = REGISTER.registerHolder("const/vec/nx", () ->
        new ActionRegistryEntry(HexPattern.fromAngles(
            "eeeeeqa", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(-1.0, 0.0, 0.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$NY = REGISTER.registerHolder("const/vec/ny", () ->
        new ActionRegistryEntry(HexPattern.fromAngles(
            "eeeeeqw", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, -1.0, 0.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$NZ = REGISTER.registerHolder("const/vec/nz", () ->
        new ActionRegistryEntry(HexPattern.fromAngles(
            "eeeeeqd", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, -1.0)))));
    // Yep, this is what I spend the "plain hexagon" pattern on.
    public static final Holder<ActionRegistryEntry> CONST$VEC$0 = REGISTER.registerHolder("const/vec/0", () ->
        new ActionRegistryEntry(HexPattern.fromAngles(
            "qqqqq", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 0.0)))));

    public static final Holder<ActionRegistryEntry> CONST$DOUBLE$PI = REGISTER.registerHolder("const/double/pi", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qdwdq",
            HexDir.NORTH_EAST), Action.makeConstantOp(new DoubleIota(Math.PI))));
    public static final Holder<ActionRegistryEntry> CONST$DOUBLE$TAU = REGISTER.registerHolder("const/double/tau", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("eawae",
            HexDir.NORTH_WEST), Action.makeConstantOp(new DoubleIota(HexUtils.TAU))));
    public static final Holder<ActionRegistryEntry> CONST$E = REGISTER.registerHolder("const/double/e", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("aaq",
            HexDir.EAST), Action.makeConstantOp(new DoubleIota(Math.E))));
    public static final Holder<ActionRegistryEntry> CONST$PHI = REGISTER.registerHolder("const/double/phi", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("wdded",
            HexDir.NORTH_EAST), Action.makeConstantOp(new DoubleIota(1.618033988749895))));

    // == Entities ==

    public static final Holder<ActionRegistryEntry> GET_ENTITY = REGISTER.registerHolder("get_entity", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqa", HexDir.SOUTH_EAST), new OpGetEntityAt(e -> true)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$ANIMAL = REGISTER.registerHolder("get_entity/animal", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaawa",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isAnimal)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$MONSTER = REGISTER.registerHolder("get_entity/monster", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaawq",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isMonster)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$ITEM = REGISTER.registerHolder("get_entity/item", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaaww",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isItem)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$PLAYER = REGISTER.registerHolder("get_entity/player", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaawe",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isPlayer)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$LIVING = REGISTER.registerHolder("get_entity/living", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqqqqdaqaawd",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isLiving)));

    public static final Holder<ActionRegistryEntry> ZONE_ENTITY = REGISTER.registerHolder("zone_entity", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwded", HexDir.SOUTH_EAST), new OpGetEntitiesBy(e -> true, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$ANIMAL = REGISTER.registerHolder("zone_entity/animal", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddwa", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_ANIMAL = REGISTER.registerHolder("zone_entity/not_animal", () ->
        new ActionRegistryEntry(
            HexPattern.fromAngles("eeeeewaqaawa", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal,
            true)
        ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$MONSTER = REGISTER.registerHolder("zone_entity/monster", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddwq", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_MONSTER = REGISTER.registerHolder("zone_entity/not_monster", () ->
        new ActionRegistryEntry(
            HexPattern.fromAngles("eeeeewaqaawq", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isMonster,
            true)
        ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$ITEM = REGISTER.registerHolder("zone_entity/item", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddww", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isItem, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_ITEM = REGISTER.registerHolder("zone_entity/not_item", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("eeeeewaqaaww", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isItem, true)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$PLAYER = REGISTER.registerHolder("zone_entity/player", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddwe", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_PLAYER = REGISTER.registerHolder("zone_entity/not_player", () ->
        new ActionRegistryEntry(
            HexPattern.fromAngles("eeeeewaqaawe", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer,
            true)
        ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$LIVING = REGISTER.registerHolder("zone_entity/living", () -> new ActionRegistryEntry(
        HexPattern.fromAngles("qqqqqwdeddwd", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_LIVING = REGISTER.registerHolder("zone_entity/not_living", () ->
        new ActionRegistryEntry(
            HexPattern.fromAngles("eeeeewaqaawd", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isLiving,
            true)
        ));

    // == Lists ==

    public static final Holder<ActionRegistryEntry> APPEND = make("append",
        new OperationAction(HexPattern.fromAngles("edqde", HexDir.SOUTH_WEST)));
    public static final Holder<ActionRegistryEntry> UNAPPEND = make("unappend",
        new OperationAction(HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST)));
    //    public static final Holder<ActionRegistryEntry> CONCAT = REGISTER.registerHolder("concat", () ->
//        new ActionRegistryEntry(HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST), OpConcat.INSTANCE));
    public static final Holder<ActionRegistryEntry> INDEX = make("index",
        new OperationAction(HexPattern.fromAngles("deeed", HexDir.NORTH_WEST)));
    //    public static final Holder<ActionRegistryEntry> LIST_SIZE = REGISTER.registerHolder("list_size", () ->
//        new ActionRegistryEntry(HexPattern.fromAngles("aqaeaq", HexDir.EAST), OpListSize.INSTANCE));
    public static final Holder<ActionRegistryEntry> SINGLETON = REGISTER.registerHolder("singleton", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("adeeed", HexDir.EAST), OpSingleton.INSTANCE));
    public static final Holder<ActionRegistryEntry> EMPTY_LIST = REGISTER.registerHolder("empty_list", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qqaeaae", HexDir.NORTH_EAST), OpEmptyList.INSTANCE));
    public static final Holder<ActionRegistryEntry> REVERSE = make("reverse",
        new OperationAction(HexPattern.fromAngles("qqqaede", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> LAST_N_LIST = REGISTER.registerHolder("last_n_list", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("ewdqdwe", HexDir.SOUTH_WEST), OpLastNToList.INSTANCE));
    public static final Holder<ActionRegistryEntry> SPLAT = REGISTER.registerHolder("splat", () ->
        new ActionRegistryEntry(HexPattern.fromAngles("qwaeawq", HexDir.NORTH_WEST), OpSplat.INSTANCE));
    public static final Holder<ActionRegistryEntry> INDEX_OF = make("index_of",
        new OperationAction(HexPattern.fromAngles("dedqde", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> REMOVE_FROM = make("remove_from",
        new OperationAction(HexPattern.fromAngles("edqdewaqa", HexDir.SOUTH_WEST)));
    public static final Holder<ActionRegistryEntry> SLICE = make("slice",
        new OperationAction(HexPattern.fromAngles("qaeaqwded", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> REPLACE = make("replace",
        new OperationAction(HexPattern.fromAngles("wqaeaqw", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> CONSTRUCT = make("construct",
        new OperationAction(HexPattern.fromAngles("ddewedd", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> DECONSTRUCT = make("deconstruct",
        new OperationAction(HexPattern.fromAngles("aaqwqaa", HexDir.SOUTH_WEST)));

    // Xplat interops
    static {
        if (PehkuiInterop.isActive()) {
            REGISTER.registerHolder("interop/pehkui/get", () ->
                new ActionRegistryEntry(HexPattern.fromAngles("aawawwawwa", HexDir.NORTH_WEST), OpGetScale.INSTANCE));
            REGISTER.registerHolder("interop/pehkui/set", () ->
                new ActionRegistryEntry(HexPattern.fromAngles("ddwdwwdwwd", HexDir.NORTH_EAST), OpSetScale.INSTANCE));
        }
    }

    public static Holder<ActionRegistryEntry> make(String name, OperationAction oa) {
        return REGISTER.registerHolder(name, () -> new ActionRegistryEntry(oa.getPattern(), oa));
    }
}
