package at.petrak.hexcasting.common.lib;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexEntityTags {
    public static final TagKey<EntityType<?>> STICKY_TELEPORTERS = create("sticky_teleporters");

    public static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, modLoc(name));
    }
}
