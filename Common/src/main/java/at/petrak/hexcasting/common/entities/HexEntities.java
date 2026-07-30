package at.petrak.hexcasting.common.entities;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexEntities {
    private static final IXplatRegister<EntityType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.ENTITY_TYPE);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<EntityType<EntityWallScroll>> WALL_SCROLL = REGISTER.register("wall_scroll", () ->
        EntityType.Builder.<EntityWallScroll>of(EntityWallScroll::new, MobCategory.MISC)
            .sized(0.5f, 0.5f).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE)
            .build(HexAPI.MOD_ID + ":wall_scroll"));
}
