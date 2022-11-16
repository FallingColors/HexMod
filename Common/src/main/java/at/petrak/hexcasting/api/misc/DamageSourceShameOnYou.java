package at.petrak.hexcasting.api.misc;

import net.minecraft.world.damagesource.DamageSource;

public class DamageSourceShameOnYou extends DamageSource {
    public DamageSourceShameOnYou() {
        super("hexcasting.shame");
        this.bypassArmor();
        this.bypassMagic();
        this.setMagic();
    }
}
