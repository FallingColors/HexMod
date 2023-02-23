package at.petrak.hexcasting.common.impl;

import at.petrak.hexcasting.api.HexAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HexAPIImpl implements HexAPI {
    private static final ConcurrentMap<EntityType<?>, EntityVelocityGetter<?>> SPECIAL_VELOCITIES
        = new ConcurrentHashMap<>();

    public <T extends Entity> void registerSpecialVelocityGetter(EntityType<T> key,
        EntityVelocityGetter<T> getter) {
        SPECIAL_VELOCITIES.put(key, getter);
    }

    @Override
    public Vec3 getEntityVelocitySpecial(Entity entity) {
        EntityType<?> type = entity.getType();
        if (SPECIAL_VELOCITIES.containsKey(type)) {
            var velGetter = SPECIAL_VELOCITIES.get(type);
            var erasedGetter = (EntityVelocityGetter) velGetter;
            return erasedGetter.getVelocity(entity);
        }
        return entity.getDeltaMovement();
    }
}
