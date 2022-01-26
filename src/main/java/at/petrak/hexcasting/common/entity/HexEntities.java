package at.petrak.hexcasting.common.entity;

import at.petrak.hexcasting.HexMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
        ForgeRegistries.ENTITIES,
        HexMod.MOD_ID);

    public static final RegistryObject<EntityType<EntitySentinel>> SENTINEL = ENTITIES.register(
        "sentinel",
        () -> EntityType.Builder.<EntitySentinel>of(EntitySentinel::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(16)
            .updateInterval(Integer.MAX_VALUE)
            .setShouldReceiveVelocityUpdates(false)
            .build("sentinel")
    );
}
