package at.petrak.hexcasting.api.misc;

import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;

public class EntityDamageSourceOvercast extends EntityDamageSource {
    public EntityDamageSourceOvercast(Entity entity) {
        super("hexcasting.overcast", entity);
        this.bypassArmor();
        this.bypassMagic();
        this.setMagic();
    }
}
