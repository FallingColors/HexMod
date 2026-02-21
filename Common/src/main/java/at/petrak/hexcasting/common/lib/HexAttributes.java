package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import static at.petrak.hexcasting.api.HexAPI.MOD_ID;

public class HexAttributes {
    private static final IXplatRegister<Attribute> REGISTER = IXplatAbstractions.INSTANCE
            .createRegistar(Registries.ATTRIBUTE);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Holder<Attribute> GRID_ZOOM = REGISTER.registerHolder("grid_zoom", () -> new RangedAttribute(
            MOD_ID + ".attributes.grid_zoom", 1.0, 0.5, 4.0).setSyncable(true));

    /**
     * Whether you have the lens overlay when looking at something. 0 = no, > 0 = yes.
     */
    public static final Holder<Attribute> SCRY_SIGHT = REGISTER.registerHolder("scry_sight", () -> new RangedAttribute(
            MOD_ID + ".attributes.scry_sight", 0.0, 0.0, 1.0).setSyncable(true));

    //whether the player is allowed to use staffcasting and scrying lenses
    public static final Holder<Attribute> FEEBLE_MIND = REGISTER.registerHolder("feeble_mind", () -> new RangedAttribute(
            MOD_ID + ".attributes.feeble_mind", 0.0, 0.0, 1.0).setSyncable(true));

    //a multiplier to adjust media consumption across the board
    public static final Holder<Attribute> MEDIA_CONSUMPTION_MODIFIER = REGISTER.registerHolder("media_consumption", () -> new RangedAttribute(
            MOD_ID + ".attributes.media_consumption", 1.0, 0.0, Double.MAX_VALUE).setSyncable(true));

    public static final Holder<Attribute> AMBIT_RADIUS = REGISTER.registerHolder("ambit_radius", () -> new RangedAttribute(
            MOD_ID + ".attributes.ambit_radius", PlayerBasedCastEnv.DEFAULT_AMBIT_RADIUS, 0.0, Double.MAX_VALUE).setSyncable(true));

    public static final Holder<Attribute> SENTINEL_RADIUS = REGISTER.registerHolder("sentinel_radius", () -> new RangedAttribute(
            MOD_ID + ".attributes.sentinel_radius", PlayerBasedCastEnv.DEFAULT_SENTINEL_RADIUS, 0.0, Double.MAX_VALUE).setSyncable(true));
}
