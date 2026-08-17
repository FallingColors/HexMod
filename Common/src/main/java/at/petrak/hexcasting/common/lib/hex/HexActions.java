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
import at.petrak.hexcasting.common.casting.actions.environment.OpGetMedia;
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
import at.petrak.hexcasting.common.casting.actions.types.OpEntityEquality;
import at.petrak.hexcasting.common.casting.actions.types.OpItemEquality;
import at.petrak.hexcasting.common.casting.actions.types.OpTypeEquality;
import at.petrak.hexcasting.common.casting.actions.types.OpBlockEquality;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

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
        new ActionRegistryEntry(HexPattern.fromAngleString("qaq", HexDir.NORTH_EAST), OpGetCaster.INSTANCE));
    public static final Holder<ActionRegistryEntry> ENTITY_POS$EYE = REGISTER.registerHolder("entity_pos/eye", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aa", HexDir.EAST), new OpEntityPos(false)));
    public static final Holder<ActionRegistryEntry> ENTITY_POS$FOOT = REGISTER.registerHolder("entity_pos/foot", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("dd", HexDir.NORTH_EAST), new OpEntityPos(true)));
    public static final Holder<ActionRegistryEntry> ENTITY_LOOK = REGISTER.registerHolder("get_entity_look", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wa", HexDir.EAST), OpEntityLook.INSTANCE));
    public static final Holder<ActionRegistryEntry> ENTITY_HEIGHT = REGISTER.registerHolder("get_entity_height", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("awq", HexDir.NORTH_EAST), OpEntityHeight.INSTANCE));
    public static final Holder<ActionRegistryEntry> ENTITY_VELOCITY = REGISTER.registerHolder("get_entity_velocity", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wq", HexDir.EAST), OpEntityVelocity.INSTANCE));

    // == Getters ==

    public static final Holder<ActionRegistryEntry> RAYCAST = REGISTER.registerHolder("raycast", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wqaawdd", HexDir.EAST), OpBlockRaycast.INSTANCE));
    public static final Holder<ActionRegistryEntry> RAYCAST_AXIS = REGISTER.registerHolder("raycast/axis", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("weddwaa", HexDir.EAST), OpBlockAxisRaycast.INSTANCE));
    public static final Holder<ActionRegistryEntry> RAYCAST_ENTITY = REGISTER.registerHolder("raycast/entity", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("weaqa", HexDir.EAST), OpEntityRaycast.INSTANCE));
    public static final Holder<ActionRegistryEntry> GET_MEDIA = REGISTER.registerHolder("get_media", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("dde", HexDir.WEST), OpGetMedia.INSTANCE));

    // == spell circle getters ==

    public static final Holder<ActionRegistryEntry> CIRCLE$IMPETUS_POST = REGISTER.registerHolder("circle/impetus_pos", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eaqwqae", HexDir.SOUTH_WEST), OpImpetusPos.INSTANCE));
    public static final Holder<ActionRegistryEntry> CIRCLE$IMPETUS_DIR = REGISTER.registerHolder("circle/impetus_dir", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eaqwqaewede", HexDir.SOUTH_WEST), OpImpetusDir.INSTANCE));
    public static final Holder<ActionRegistryEntry> CIRCLE$BOUNDS$MIN = REGISTER.registerHolder("circle/bounds/min", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eaqwqaewdd", HexDir.SOUTH_WEST), new OpCircleBounds(false)));
    public static final Holder<ActionRegistryEntry> CIRCLE$BOUNDS$MAX = REGISTER.registerHolder("circle/bounds/max", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aqwqawaaqa", HexDir.WEST), new OpCircleBounds(true)));

    // == Modify Stack ==

    public static final Holder<ActionRegistryEntry> SWAP = REGISTER.registerHolder("swap", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aawdd", HexDir.EAST), new OpTwiddling(2, new int[]{1, 0})));
    public static final Holder<ActionRegistryEntry> ROTATE = REGISTER.registerHolder("rotate", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aaeaa", HexDir.EAST), new OpTwiddling(3, new int[]{1, 2, 0})));
    public static final Holder<ActionRegistryEntry> ROTATE_REVERSE = REGISTER.registerHolder("rotate_reverse", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("ddqdd",
            HexDir.NORTH_EAST), new OpTwiddling(3, new int[]{2, 0, 1})));
    public static final Holder<ActionRegistryEntry> DUPLICATE = REGISTER.registerHolder("duplicate", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aadaa", HexDir.EAST), new OpTwiddling(1, new int[]{0, 0})));
    public static final Holder<ActionRegistryEntry> OVER = REGISTER.registerHolder("over", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aaedd", HexDir.EAST), new OpTwiddling(2, new int[]{0, 1, 0})));
    public static final Holder<ActionRegistryEntry> TUCK = REGISTER.registerHolder("tuck", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("ddqaa", HexDir.EAST), new OpTwiddling(2, new int[]{1, 0, 1})));
    public static final Holder<ActionRegistryEntry> TWO_DUP = REGISTER.registerHolder("2dup", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aadadaaw",
            HexDir.EAST), new OpTwiddling(2, new int[]{0, 1, 0, 1})));

    public static final Holder<ActionRegistryEntry> STACK_LEN = REGISTER.registerHolder("stack_len", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qwaeawqaeaqa", HexDir.NORTH_WEST), OpStackSize.INSTANCE));
    public static final Holder<ActionRegistryEntry> DUPLICATE_N = REGISTER.registerHolder("duplicate_n", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aadaadaa", HexDir.EAST), OpDuplicateN.INSTANCE));
    public static final Holder<ActionRegistryEntry> FISHERMAN = REGISTER.registerHolder("fisherman", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("ddad", HexDir.WEST), OpFisherman.INSTANCE));
    public static final Holder<ActionRegistryEntry> FISHERMAN$COPY = REGISTER.registerHolder("fisherman/copy", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aada", HexDir.EAST), OpFishermanButItCopies.INSTANCE));
    public static final Holder<ActionRegistryEntry> SWIZZLE = REGISTER.registerHolder("swizzle", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qaawdde",
            HexDir.SOUTH_EAST), OpAlwinfyHasAscendedToABeingOfPureMath.INSTANCE));

    // == Math ==

    public static final Holder<ActionRegistryEntry> ADD = make("add",
        new OperationAction(HexPattern.fromAngleString("waaw", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> SUB = make("sub",
        new OperationAction(HexPattern.fromAngleString("wddw", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> MUL_DOT = make("mul",
        new OperationAction(HexPattern.fromAngleString("waqaw", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> DIV_CROSS = make("div",
        new OperationAction(HexPattern.fromAngleString("wdedw", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> ABS = make("abs",
        new OperationAction(HexPattern.fromAngleString("wqaqw", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> POW_PROJ = make("pow",
        new OperationAction(HexPattern.fromAngleString("wedew", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> FLOOR = make("floor",
        new OperationAction(HexPattern.fromAngleString("ewq", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> CEIL = make("ceil",
        new OperationAction(HexPattern.fromAngleString("qwe", HexDir.EAST)));

    public static final Holder<ActionRegistryEntry> CONSTRUCT_VEC = make("construct_vec",
        new OperationAction(HexPattern.fromAngleString("eqqqqq", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> DECONSTRUCT_VEC = make("deconstruct_vec",
        new OperationAction(HexPattern.fromAngleString("qeeeee", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> COERCE_AXIAL = REGISTER.registerHolder("coerce_axial", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqqqaww", HexDir.NORTH_WEST), OpCoerceToAxial.INSTANCE));

    // == Logic ==

    public static final Holder<ActionRegistryEntry> AND = make("and",
        new OperationAction(HexPattern.fromAngleString("wdw", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> OR = make("or",
        new OperationAction(HexPattern.fromAngleString("waw", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> NOT = make("not",
        new OperationAction(HexPattern.fromAngleString("dw", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> XOR = make("xor",
        new OperationAction(HexPattern.fromAngleString("dwa", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> GREATER = make("greater", new OperationAction(
        HexPattern.fromAngleString("e", HexDir.SOUTH_EAST))
    );
    public static final Holder<ActionRegistryEntry> LESS = make("less", new OperationAction(
        HexPattern.fromAngleString("q", HexDir.SOUTH_WEST))
    );
    public static final Holder<ActionRegistryEntry> GREATER_EQ = make("greater_eq", new OperationAction(
        HexPattern.fromAngleString("ee", HexDir.SOUTH_EAST))
    );
    public static final Holder<ActionRegistryEntry> LESS_EQ = make("less_eq", new OperationAction(
        HexPattern.fromAngleString("qq", HexDir.SOUTH_WEST))
    );
    public static final Holder<ActionRegistryEntry> EQUALS = REGISTER.registerHolder("equals", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("ad", HexDir.EAST), new OpEquality(false)));
    public static final Holder<ActionRegistryEntry> NOT_EQUALS = REGISTER.registerHolder("not_equals", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("da", HexDir.EAST), new OpEquality(true)));
    public static final Holder<ActionRegistryEntry> TYPE_EQUALS = REGISTER.registerHolder("type_equals", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wawdw", HexDir.EAST), new OpTypeEquality(false)));
    public static final Holder<ActionRegistryEntry> TYPE_NOT_EQUALS = REGISTER.registerHolder("type_not_equals", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wdwaw", HexDir.EAST), new OpTypeEquality(true)));
    public static final Holder<ActionRegistryEntry> BOOL_COERCE = REGISTER.registerHolder("bool_coerce", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aw", HexDir.NORTH_EAST), OpCoerceToBool.INSTANCE));
    public static final Holder<ActionRegistryEntry> IF = REGISTER.registerHolder("if", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("awdd", HexDir.SOUTH_EAST), OpBoolIf.INSTANCE));

    public static final Holder<ActionRegistryEntry> RANDOM = REGISTER.registerHolder("random", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eqqq", HexDir.NORTH_WEST), OpRandom.INSTANCE));

    // == Advanced Math ==

    public static final Holder<ActionRegistryEntry> SIN = make("sin",
        new OperationAction(HexPattern.fromAngleString("qqqqqaa", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> COS = make("cos",
        new OperationAction(HexPattern.fromAngleString("qqqqqad", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> TAN = make("tan",
        new OperationAction(HexPattern.fromAngleString("wqqqqqadq", HexDir.SOUTH_WEST)));
    public static final Holder<ActionRegistryEntry> ARCSIN = make("arcsin",
        new OperationAction(HexPattern.fromAngleString("ddeeeee", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> ARCCOS = make("arccos",
        new OperationAction(HexPattern.fromAngleString("adeeeee", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> ARCTAN = make("arctan",
        new OperationAction(HexPattern.fromAngleString("eadeeeeew", HexDir.NORTH_EAST)));
    public static final Holder<ActionRegistryEntry> ARCTAN2 = make("arctan2",
        new OperationAction(HexPattern.fromAngleString("deadeeeeewd", HexDir.WEST)));
    public static final Holder<ActionRegistryEntry> LOGARITHM = make("logarithm",
        new OperationAction(HexPattern.fromAngleString("eqaqe", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> MODULO = make("modulo",
        new OperationAction(HexPattern.fromAngleString("addwaad", HexDir.NORTH_EAST)));

    // == Sets ==

    public static final Holder<ActionRegistryEntry> UNIQUE = make("unique",
        new OperationAction(HexPattern.fromAngleString("aweaqa", HexDir.NORTH_EAST)));

    // == Spells ==

    public static final Holder<ActionRegistryEntry> PRINT = REGISTER.registerHolder("print", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("de", HexDir.NORTH_EAST), OpPrint.INSTANCE));
    public static final Holder<ActionRegistryEntry> EXPLODE = REGISTER.registerHolder("explode", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aawaawaa", HexDir.EAST), new OpExplode(false)));
    public static final Holder<ActionRegistryEntry> EXPLODE$FIRE = REGISTER.registerHolder("explode/fire", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("ddwddwdd", HexDir.EAST), new OpExplode(true)));
    public static final Holder<ActionRegistryEntry> ADD_MOTION = REGISTER.registerHolder("add_motion", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("awqqqwaqw", HexDir.SOUTH_WEST), OpAddMotion.INSTANCE));
    public static final Holder<ActionRegistryEntry> BLINK = REGISTER.registerHolder("blink", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("awqqqwaq", HexDir.SOUTH_WEST), OpBlink.INSTANCE));
    public static final Holder<ActionRegistryEntry> BREAK_BLOCK = REGISTER.registerHolder("break_block", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qaqqqqq", HexDir.EAST), OpBreakBlock.INSTANCE));
    public static final Holder<ActionRegistryEntry> PLACE_BLOCK = REGISTER.registerHolder("place_block", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eeeeede", HexDir.SOUTH_WEST), OpPlaceBlock.INSTANCE));
    public static final Holder<ActionRegistryEntry> COLORIZE = REGISTER.registerHolder("colorize", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("awddwqawqwawq", HexDir.EAST), OpColorize.INSTANCE));
    public static final Holder<ActionRegistryEntry> CYCLE_VARIANT = REGISTER.registerHolder("cycle_variant", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("dwaawedwewdwe", HexDir.WEST), OpCycleVariant.INSTANCE));
    public static final Holder<ActionRegistryEntry> CREATE_WATER = REGISTER.registerHolder("create_water", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aqawqadaq", HexDir.SOUTH_EAST), new OpCreateFluid(
            MediaConstants.DUST_UNIT,
            Items.WATER_BUCKET,
            Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL),
            Fluids.WATER)));
    public static final Holder<ActionRegistryEntry> DESTROY_WATER = REGISTER.registerHolder("destroy_water", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("dedwedade", HexDir.SOUTH_WEST), OpDestroyFluid.INSTANCE));
    public static final Holder<ActionRegistryEntry> IGNITE = REGISTER.registerHolder("ignite", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aaqawawa", HexDir.SOUTH_EAST), OpIgnite.INSTANCE));
    public static final Holder<ActionRegistryEntry> EXTINGUISH = REGISTER.registerHolder("extinguish", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("ddedwdwd", HexDir.SOUTH_WEST), OpExtinguish.INSTANCE));
    public static final Holder<ActionRegistryEntry> CONJURE_BLOCK = REGISTER.registerHolder("conjure_block", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqa", HexDir.NORTH_EAST), new OpConjureBlock(false)));
    public static final Holder<ActionRegistryEntry> CONJURE_LIGHT = REGISTER.registerHolder("conjure_light", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqd", HexDir.NORTH_EAST), new OpConjureBlock(true)));
    public static final Holder<ActionRegistryEntry> BONEMEAL = REGISTER.registerHolder("bonemeal", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wqaqwawqaqw",
            HexDir.NORTH_EAST), OpTheOnlyReasonAnyoneDownloadedPsi.INSTANCE));
    public static final Holder<ActionRegistryEntry> RECHARGE = REGISTER.registerHolder("recharge", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqqqwaeaeaeaeaea", HexDir.NORTH_WEST), OpRecharge.INSTANCE));
    public static final Holder<ActionRegistryEntry> ERASE = REGISTER.registerHolder("erase", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qdqawwaww", HexDir.EAST), OpErase.INSTANCE));
    public static final Holder<ActionRegistryEntry> EDIFY = REGISTER.registerHolder("edify", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wqaqwd", HexDir.NORTH_EAST), OpEdifySapling.INSTANCE));

    public static final Holder<ActionRegistryEntry> BEEP = REGISTER.registerHolder("beep", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("adaa", HexDir.WEST), OpBeep.INSTANCE));

    public static final Holder<ActionRegistryEntry> CRAFT$CYPHER = REGISTER.registerHolder("craft/cypher", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("waqqqqq", HexDir.EAST),
        new OpMakePackagedSpell(s -> (s.is(HexItems.CYPHER.get())||s.is(HexItems.ANCIENT_CYPHER.get())), HexItems.CYPHER.get()::getDescription, MediaConstants.CRYSTAL_UNIT)
    ));
    public static final Holder<ActionRegistryEntry> CRAFT$TRINKET = REGISTER.registerHolder("craft/trinket", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("wwaqqqqqeaqeaeqqqeaeq", HexDir.EAST),
        new OpMakePackagedSpell(HexItems.TRINKET.get(), 5 * MediaConstants.CRYSTAL_UNIT)
    ));
    public static final Holder<ActionRegistryEntry> CRAFT$ARTIFACT = REGISTER.registerHolder("craft/artifact", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("wwaqqqqqeawqwqwqwqwqwwqqeadaeqqeqqeadaeqq", HexDir.EAST),
        new OpMakePackagedSpell(HexItems.ARTIFACT.get(), 10 * MediaConstants.CRYSTAL_UNIT)
    ));
    public static final Holder<ActionRegistryEntry> CRAFT$BATTERY = REGISTER.registerHolder("craft/battery", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("aqqqaqwwaqqqqqeqaqqqawwqwqwqwqwqw", HexDir.SOUTH_WEST), OpMakeBattery.INSTANCE));

    public static final Holder<ActionRegistryEntry> POTION$WEAKNESS = REGISTER.registerHolder("potion/weakness", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqaqwawaw", HexDir.NORTH_WEST), new OpPotionEffect(MobEffects.WEAKNESS,
        MediaConstants.DUST_UNIT / 10, true, false)
    ));
    public static final Holder<ActionRegistryEntry> POTION$LEVITATION = REGISTER.registerHolder("potion/levitation", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqawwawawd", HexDir.WEST), new OpPotionEffect(MobEffects.LEVITATION,
        MediaConstants.DUST_UNIT / 5, false, false)
    ));
    public static final Holder<ActionRegistryEntry> POTION$WITHER = REGISTER.registerHolder("potion/wither", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqaewawawe", HexDir.SOUTH_WEST), new OpPotionEffect(MobEffects.WITHER,
        MediaConstants.DUST_UNIT, true, false)
    ));
    public static final Holder<ActionRegistryEntry> POTION$POISON = REGISTER.registerHolder("potion/poison", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqadwawaww", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.POISON,
        MediaConstants.DUST_UNIT / 3, true, false)
    ));
    public static final Holder<ActionRegistryEntry> POTION$SLOWNESS = REGISTER.registerHolder("potion/slowness", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqadwawaw", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.MOVEMENT_SLOWDOWN,
        MediaConstants.DUST_UNIT / 3, true, false)
    ));

    public static final Holder<ActionRegistryEntry> POTION$REGENERATION = REGISTER.registerHolder("potion/regeneration", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqaawawaedd", HexDir.NORTH_WEST), new OpPotionEffect(MobEffects.REGENERATION,
        MediaConstants.DUST_UNIT, true, true)
    ));
    public static final Holder<ActionRegistryEntry> POTION$NIGHT_VISION = REGISTER.registerHolder("potion/night_vision", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqaawawaeqdd", HexDir.WEST), new OpPotionEffect(MobEffects.NIGHT_VISION,
        MediaConstants.DUST_UNIT / 5, false, true)
    ));
    public static final Holder<ActionRegistryEntry> POTION$ABSORPTION = REGISTER.registerHolder("potion/absorption", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqaawawaeqqdd", HexDir.SOUTH_WEST), new OpPotionEffect(MobEffects.ABSORPTION,
        MediaConstants.DUST_UNIT, true, true)
    ));
    public static final Holder<ActionRegistryEntry> POTION$HASTE = REGISTER.registerHolder("potion/haste", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qaawawaeqqqdd", HexDir.SOUTH_EAST), new OpPotionEffect(MobEffects.DIG_SPEED,
        MediaConstants.DUST_UNIT / 3, true, true)
    ));
    public static final Holder<ActionRegistryEntry> POTION$STRENGTH = REGISTER.registerHolder("potion/strength", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("aawawaeqqqqdd", HexDir.EAST), new OpPotionEffect(MobEffects.DAMAGE_BOOST,
        MediaConstants.DUST_UNIT / 3, true, true)
    ));

    public static final Holder<ActionRegistryEntry> FLIGHT$RANGE = REGISTER.registerHolder("flight/range", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("awawaawq", HexDir.SOUTH_WEST),
            new OpFlight(OpFlight.Type.LimitRange)));
    public static final Holder<ActionRegistryEntry> FLIGHT$TIME = REGISTER.registerHolder("flight/time", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("dwdwdewq", HexDir.NORTH_EAST),
            new OpFlight(OpFlight.Type.LimitTime)));
    public static final Holder<ActionRegistryEntry> FLIGHT$CAN_FLY = REGISTER.registerHolder("flight/can_fly", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("dwdwdeweaqa", HexDir.NORTH_EAST),
            OpCanEntityHexFly.INSTANCE));

    public static final Holder<ActionRegistryEntry> SENTINEL$CREATE = REGISTER.registerHolder("sentinel/create", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("waeawae", HexDir.EAST), new OpCreateSentinel(false)));
    public static final Holder<ActionRegistryEntry> SENTINEL$DESTROY = REGISTER.registerHolder("sentinel/destroy", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qdwdqdw", HexDir.NORTH_EAST), OpDestroySentinel.INSTANCE));
    public static final Holder<ActionRegistryEntry> SENTINEL$GET_POS = REGISTER.registerHolder("sentinel/get_pos", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("waeawaede", HexDir.EAST), OpGetSentinelPos.INSTANCE));
    public static final Holder<ActionRegistryEntry> SENTINEL$WAYFIND = REGISTER.registerHolder("sentinel/wayfind", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("waeawaedwa", HexDir.EAST), OpGetSentinelWayfind.INSTANCE));

    public static final Holder<ActionRegistryEntry> LIGHTNING = REGISTER.registerHolder("lightning", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("waadwawdaaweewq", HexDir.EAST), OpLightning.INSTANCE));

    public static final Holder<ActionRegistryEntry> ALTIORA = REGISTER.registerHolder("flight", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eawwaeawawaa", HexDir.NORTH_WEST), OpAltiora.INSTANCE));


    public static final Holder<ActionRegistryEntry> CREATE_LAVA = REGISTER.registerHolder("create_lava", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eaqawqadaqd", HexDir.EAST), new OpCreateFluid(
            MediaConstants.CRYSTAL_UNIT,
            Items.LAVA_BUCKET,
            Blocks.LAVA_CAULDRON.defaultBlockState(),
            Fluids.LAVA)));
    public static final Holder<ActionRegistryEntry> TELEPORT = REGISTER.registerHolder("teleport/great", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wwwqqqwwwqqeqqwwwqqwqqdqqqqqdqq",
            HexDir.EAST), OpTeleport.INSTANCE));
    public static final Holder<ActionRegistryEntry> SENTINEL$GREAT = REGISTER.registerHolder("sentinel/create/great", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("waeawaeqqqwqwqqwq", HexDir.EAST), new OpCreateSentinel(true)));
    public static final Holder<ActionRegistryEntry> DISPEL_RAIN = REGISTER.registerHolder("dispel_rain", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eeewwweeewwaqqddqdqd", HexDir.EAST), new OpWeather(false)));
    public static final Holder<ActionRegistryEntry> SUMMON_RAIN = REGISTER.registerHolder("summon_rain", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wwweeewwweewdawdwad", HexDir.WEST), new OpWeather(true)));
    public static final Holder<ActionRegistryEntry> BRAINSWEEP = REGISTER.registerHolder("brainsweep", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qeqwqwqwqwqeqaeqeaqeqaeqaqded",
            HexDir.NORTH_EAST), OpBrainsweep.INSTANCE));

    public static final Holder<ActionRegistryEntry> AKASHIC$READ = REGISTER.registerHolder("akashic/read", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqwqqqqqaq", HexDir.WEST), OpAkashicRead.INSTANCE));
    public static final Holder<ActionRegistryEntry> AKASHIC$WRITE = REGISTER.registerHolder("akashic/write", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eeeweeeeede", HexDir.EAST), OpAkashicWrite.INSTANCE));

    // == Meta stuff ==

    public static final Holder<ActionRegistryEntry> ESCAPE = REGISTER.registerHolder("escape", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqaw", HexDir.WEST), OpEscape.INSTANCE));
    public static final Holder<ActionRegistryEntry> RUNTIME_ESCAPE = REGISTER.registerHolder("runtime_escape", () ->
            new ActionRegistryEntry(HexPattern.fromAngleString("wdeee", HexDir.SOUTH_EAST), OpRuntimeEscape.INSTANCE));
    public static final Holder<ActionRegistryEntry> OPEN_PAREN = REGISTER.registerHolder("open_paren", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqq", HexDir.WEST), OpOpenParen.INSTANCE));
    public static final Holder<ActionRegistryEntry> CLOSE_PAREN = REGISTER.registerHolder("close_paren", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eee", HexDir.EAST), OpCloseParen.INSTANCE));
    public static final Holder<ActionRegistryEntry> OPEN_N_PARENS = REGISTER.registerHolder("open_n_parens", () ->
            new ActionRegistryEntry(HexPattern.fromAngleString("qdaqadq", HexDir.WEST), OpOpenNParens.INSTANCE));
    public static final Holder<ActionRegistryEntry> CLOSE_ALL_PARENS = REGISTER.registerHolder("close_all_parens", () ->
            new ActionRegistryEntry(HexPattern.fromAngleString("eadedae", HexDir.EAST), OpCloseAllParens.INSTANCE));
    public static final Holder<ActionRegistryEntry> READ_INTO_PARENS = REGISTER.registerHolder("read_into_parens", () ->
            new ActionRegistryEntry(HexPattern.fromAngleString("aqqqqqwded", HexDir.EAST), OpReadIntoParens.INSTANCE));
    public static final Holder<ActionRegistryEntry> UNDO = REGISTER.registerHolder("undo", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eeedw", HexDir.EAST), OpUndo.INSTANCE));

    public static final Holder<ActionRegistryEntry> SIMULATE = REGISTER.registerHolder("simulate", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("deaq", HexDir.EAST), OpSimulate.INSTANCE));

    // http://www.toroidalsnark.net/mkss3-pix/CalderheadJMM2014.pdf
    // eval being a space filling curve feels apt doesn't it
    public static final Holder<ActionRegistryEntry> EVAL = REGISTER.registerHolder("eval", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("deaqq", HexDir.SOUTH_EAST), OpEval.INSTANCE));
    public static final Holder<ActionRegistryEntry> EVAL$CC = REGISTER.registerHolder("eval/cc", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qwaqde", HexDir.NORTH_WEST), OpEvalBreakable.INSTANCE));
    public static final Holder<ActionRegistryEntry> HALT = REGISTER.registerHolder("halt", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aqdee", HexDir.SOUTH_WEST), OpHalt.INSTANCE));

    public static final Holder<ActionRegistryEntry> READ = REGISTER.registerHolder("read", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aqqqqq", HexDir.EAST), OpRead.INSTANCE));
    public static final Holder<ActionRegistryEntry> READ$ENTITY = REGISTER.registerHolder("read/entity", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wawqwqwqwqwqw", HexDir.EAST), OpTheCoolerRead.INSTANCE));
    public static final Holder<ActionRegistryEntry> WRITE = REGISTER.registerHolder("write", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("deeeee", HexDir.EAST), OpWrite.INSTANCE));
    public static final Holder<ActionRegistryEntry> WRITE$ENTITY = REGISTER.registerHolder("write/entity", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wdwewewewewew", HexDir.EAST), OpTheCoolerWrite.INSTANCE));

    public static final Holder<ActionRegistryEntry> READ$LOCAL = REGISTER.registerHolder("read/local", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qeewdweddw", HexDir.NORTH_EAST), OpPeekLocal.INSTANCE));
    public static final Holder<ActionRegistryEntry> WRITE$LOCAL = REGISTER.registerHolder("write/local", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eqqwawqaaw", HexDir.NORTH_WEST), OpPushLocal.INSTANCE));

    public static final Holder<ActionRegistryEntry> THANATOS = REGISTER.registerHolder("thanatos", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqaed", HexDir.SOUTH_EAST), OpThanos.INSTANCE));

    // == Consts ==

    public static final Holder<ActionRegistryEntry> CONST$NULL = REGISTER.registerHolder("const/null", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("d", HexDir.EAST), Action.makeConstantOp(new NullIota())));

    public static final Holder<ActionRegistryEntry> CONST$TRUE = REGISTER.registerHolder("const/true", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aqae",
            HexDir.SOUTH_EAST), Action.makeConstantOp(new BooleanIota(true))));
    public static final Holder<ActionRegistryEntry> CONST$FALSE = REGISTER.registerHolder("const/false", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("dedq",
            HexDir.NORTH_EAST), Action.makeConstantOp(new BooleanIota(false))));

    public static final Holder<ActionRegistryEntry> CONST$VEC$PX = REGISTER.registerHolder("const/vec/px", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString(
            "qqqqqea", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(1.0, 0.0, 0.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$PY = REGISTER.registerHolder("const/vec/py", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString(
            "qqqqqew", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 1.0, 0.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$PZ = REGISTER.registerHolder("const/vec/pz", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString(
            "qqqqqed", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 1.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$NX = REGISTER.registerHolder("const/vec/nx", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString(
            "eeeeeqa", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(-1.0, 0.0, 0.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$NY = REGISTER.registerHolder("const/vec/ny", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString(
            "eeeeeqw", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, -1.0, 0.0)))));
    public static final Holder<ActionRegistryEntry> CONST$VEC$NZ = REGISTER.registerHolder("const/vec/nz", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString(
            "eeeeeqd", HexDir.SOUTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, -1.0)))));
    // Yep, this is what I spend the "plain hexagon" pattern on.
    public static final Holder<ActionRegistryEntry> CONST$VEC$0 = REGISTER.registerHolder("const/vec/0", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString(
            "qqqqq", HexDir.NORTH_WEST), Action.makeConstantOp(new Vec3Iota(new Vec3(0.0, 0.0, 0.0)))));

    public static final Holder<ActionRegistryEntry> CONST$DOUBLE$PI = REGISTER.registerHolder("const/double/pi", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qdwdq",
            HexDir.NORTH_EAST), Action.makeConstantOp(new DoubleIota(Math.PI))));
    public static final Holder<ActionRegistryEntry> CONST$DOUBLE$TAU = REGISTER.registerHolder("const/double/tau", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("eawae",
            HexDir.NORTH_WEST), Action.makeConstantOp(new DoubleIota(HexUtils.TAU))));
    public static final Holder<ActionRegistryEntry> CONST$E = REGISTER.registerHolder("const/double/e", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("aaq",
            HexDir.EAST), Action.makeConstantOp(new DoubleIota(Math.E))));
    public static final Holder<ActionRegistryEntry> CONST$PHI = REGISTER.registerHolder("const/double/phi", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("wdded",
            HexDir.NORTH_EAST), Action.makeConstantOp(new DoubleIota(1.618033988749895))));

    // == Entities ==

    public static final Holder<ActionRegistryEntry> GET_ENTITY = REGISTER.registerHolder("get_entity", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqqqdaqa", HexDir.SOUTH_EAST), new OpGetEntityAt(e -> true)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$ANIMAL = REGISTER.registerHolder("get_entity/animal", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqqqdaqaawa",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isAnimal)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$MONSTER = REGISTER.registerHolder("get_entity/monster", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqqqdaqaawq",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isMonster)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$ITEM = REGISTER.registerHolder("get_entity/item", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqqqdaqaaww",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isItem)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$PLAYER = REGISTER.registerHolder("get_entity/player", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqqqdaqaawe",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isPlayer)));
    public static final Holder<ActionRegistryEntry> GET_ENTITY$LIVING = REGISTER.registerHolder("get_entity/living", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqqqqdaqaawd",
            HexDir.SOUTH_EAST), new OpGetEntityAt(OpGetEntitiesBy::isLiving)));

    public static final Holder<ActionRegistryEntry> ZONE_ENTITY = REGISTER.registerHolder("zone_entity", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqwded", HexDir.SOUTH_EAST), new OpGetEntitiesBy(e -> true, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$ANIMAL = REGISTER.registerHolder("zone_entity/animal", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqwdeddwa", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_ANIMAL = REGISTER.registerHolder("zone_entity/not_animal", () ->
        new ActionRegistryEntry(
            HexPattern.fromAngleString("eeeeewaqaawa", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isAnimal,
            true)
        ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$MONSTER = REGISTER.registerHolder("zone_entity/monster", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqwdeddwq", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isMonster, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_MONSTER = REGISTER.registerHolder("zone_entity/not_monster", () ->
        new ActionRegistryEntry(
            HexPattern.fromAngleString("eeeeewaqaawq", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isMonster,
            true)
        ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$ITEM = REGISTER.registerHolder("zone_entity/item", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqwdeddww", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isItem, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_ITEM = REGISTER.registerHolder("zone_entity/not_item", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("eeeeewaqaaww", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isItem, true)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$PLAYER = REGISTER.registerHolder("zone_entity/player", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqwdeddwe", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_PLAYER = REGISTER.registerHolder("zone_entity/not_player", () ->
        new ActionRegistryEntry(
            HexPattern.fromAngleString("eeeeewaqaawe", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isPlayer,
            true)
        ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$LIVING = REGISTER.registerHolder("zone_entity/living", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqwdeddwd", HexDir.SOUTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isLiving, false)
    ));
    public static final Holder<ActionRegistryEntry> ZONE_ENTITY$NOT_LIVING = REGISTER.registerHolder("zone_entity/not_living", () ->
        new ActionRegistryEntry(
            HexPattern.fromAngleString("eeeeewaqaawd", HexDir.NORTH_EAST), new OpGetEntitiesBy(OpGetEntitiesBy::isLiving,
            true)
        ));

    // == Types ==
    public static final Holder<ActionRegistryEntry> COMPARE_BLOCK = REGISTER.registerHolder("compare_block/lenient", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qqqqqeqeeeee", HexDir.NORTH_WEST), new OpBlockEquality(false)
    ));
    public static final Holder<ActionRegistryEntry> COMPARE_BLOCK_STRICT = REGISTER.registerHolder("compare_block/strict", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qwawqwadadwewdwe", HexDir.NORTH_WEST), new OpBlockEquality(true)
    ));
    public static final Holder<ActionRegistryEntry> COMPARE_ENTITY = REGISTER.registerHolder("compare_entity", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("aqaeqded", HexDir.NORTH_WEST), OpEntityEquality.INSTANCE
    ));
    public static final Holder<ActionRegistryEntry> COMPARE_ITEM = REGISTER.registerHolder("compare_item/lenient", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qaeaqeqedqde", HexDir.NORTH_WEST), new OpItemEquality(false)
    ));
    public static final Holder<ActionRegistryEntry> COMPARE_ITEM_STRICT = REGISTER.registerHolder("compare_item/strict", () -> new ActionRegistryEntry(
        HexPattern.fromAngleString("qaeaqewqedqde", HexDir.NORTH_WEST), new OpItemEquality(true)
    ));

    // == Lists ==

    public static final Holder<ActionRegistryEntry> APPEND = make("append",
        new OperationAction(HexPattern.fromAngleString("edqde", HexDir.SOUTH_WEST)));
    public static final Holder<ActionRegistryEntry> UNAPPEND = make("unappend",
        new OperationAction(HexPattern.fromAngleString("qaeaq", HexDir.NORTH_WEST)));
    //    public static final Holder<ActionRegistryEntry> CONCAT = REGISTER.registerHolder("concat", () ->
//        new ActionRegistryEntry(HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST), OpConcat.INSTANCE));
    public static final Holder<ActionRegistryEntry> INDEX = make("index",
        new OperationAction(HexPattern.fromAngleString("deeed", HexDir.NORTH_WEST)));
    //    public static final Holder<ActionRegistryEntry> LIST_SIZE = REGISTER.registerHolder("list_size", () ->
//        new ActionRegistryEntry(HexPattern.fromAngles("aqaeaq", HexDir.EAST), OpListSize.INSTANCE));
    public static final Holder<ActionRegistryEntry> SINGLETON = REGISTER.registerHolder("singleton", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("adeeed", HexDir.EAST), OpSingleton.INSTANCE));
    public static final Holder<ActionRegistryEntry> EMPTY_LIST = REGISTER.registerHolder("empty_list", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qqaeaae", HexDir.NORTH_EAST), OpEmptyList.INSTANCE));
    public static final Holder<ActionRegistryEntry> REVERSE = make("reverse",
        new OperationAction(HexPattern.fromAngleString("qqqaede", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> LAST_N_LIST = REGISTER.registerHolder("last_n_list", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("ewdqdwe", HexDir.SOUTH_WEST), OpLastNToList.INSTANCE));
    public static final Holder<ActionRegistryEntry> SPLAT = REGISTER.registerHolder("splat", () ->
        new ActionRegistryEntry(HexPattern.fromAngleString("qwaeawq", HexDir.NORTH_WEST), OpSplat.INSTANCE));
    public static final Holder<ActionRegistryEntry> INDEX_OF = make("index_of",
        new OperationAction(HexPattern.fromAngleString("dedqde", HexDir.EAST)));
    public static final Holder<ActionRegistryEntry> REMOVE_FROM = make("remove_from",
        new OperationAction(HexPattern.fromAngleString("edqdewaqa", HexDir.SOUTH_WEST)));
    public static final Holder<ActionRegistryEntry> SLICE = make("slice",
        new OperationAction(HexPattern.fromAngleString("qaeaqwded", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> REPLACE = make("replace",
        new OperationAction(HexPattern.fromAngleString("wqaeaqw", HexDir.NORTH_WEST)));
    public static final Holder<ActionRegistryEntry> CONSTRUCT = make("construct",
        new OperationAction(HexPattern.fromAngleString("ddewedd", HexDir.SOUTH_EAST)));
    public static final Holder<ActionRegistryEntry> DECONSTRUCT = make("deconstruct",
        new OperationAction(HexPattern.fromAngleString("aaqwqaa", HexDir.SOUTH_WEST)));

    // Xplat interops
    static {
        if (PehkuiInterop.isActive()) {
            REGISTER.registerHolder("interop/pehkui/get", () ->
                new ActionRegistryEntry(HexPattern.fromAngleString("aawawwawwa", HexDir.NORTH_WEST), OpGetScale.INSTANCE));
            REGISTER.registerHolder("interop/pehkui/set", () ->
                new ActionRegistryEntry(HexPattern.fromAngleString("ddwdwwdwwd", HexDir.NORTH_EAST), OpSetScale.INSTANCE));
        }
    }

    public static Holder<ActionRegistryEntry> make(String name, OperationAction oa) {
        return REGISTER.registerHolder(name, () -> new ActionRegistryEntry(oa.getPattern(), oa));
    }
}
