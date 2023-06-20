package at.petrak.hexcasting.api.misc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class EntityDamageSourceOvercast extends DamageSource {
    public EntityDamageSourceOvercast(Entity entity) {
        super("hexcasting.overcast", entity);
        this.bypassArmor();
        this.bypassMagic();
        this.setMagic();
    }
}
