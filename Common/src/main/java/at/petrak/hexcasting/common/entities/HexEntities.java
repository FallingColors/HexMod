package at.petrak.hexcasting.common.entities;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class HexEntities {
    private static final IXplatRegistrar<EntityType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistrar(Registries.ENTITY_TYPE);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<EntityType<EntityWallScroll>> WALL_SCROLL = REGISTER.register("wall_scroll", () ->
        EntityType.Builder.<EntityWallScroll>of(EntityWallScroll::new, MobCategory.MISC)
            .sized(0.5f, 0.5f).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE)
            .build(HexAPI.MOD_ID + ":wall_scroll"));
}
