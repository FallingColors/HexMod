package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.misc.HexMobEffect;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexMobEffects {

    private static final IXplatRegister<MobEffect> REGISTER = IXplatAbstractions.INSTANCE
            .createRegistar(Registries.MOB_EFFECT);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Holder<MobEffect> ENLARGE_GRID = REGISTER.registerHolder("enlarge_grid",
            () ->  new HexMobEffect(MobEffectCategory.BENEFICIAL, 0xc875ff).addAttributeModifier(HexAttributes.GRID_ZOOM, HexAPI.modLoc("enlarge_grid"),
                    0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final Holder<MobEffect> SHRINK_GRID = REGISTER.registerHolder("shrink_grid",
            () -> new HexMobEffect(MobEffectCategory.HARMFUL, 0xc0e660).addAttributeModifier(HexAttributes.GRID_ZOOM, HexAPI.modLoc("shrink_grid"),
                -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
}
