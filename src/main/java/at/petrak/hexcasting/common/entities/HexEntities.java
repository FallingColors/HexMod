package at.petrak.hexcasting.common.entities;

import at.petrak.hexcasting.HexMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES,
        HexMod.MOD_ID);

    public static final RegistryObject<EntityType<EntityWallScroll>> WALL_SCROLL = ENTITIES.register("wall_scroll",
        () -> EntityType.Builder.<EntityWallScroll>of(EntityWallScroll::new, MobCategory.MISC)
            .sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE)
            .build("wall_scroll"));
}
