package at.petrak.hexcasting.api.misc;

import net.minecraft.world.damagesource.DamageSource;

public class DamageSourceOvercast extends DamageSource {
    public DamageSourceOvercast() {
        super("hexcasting.overcast");
        this.bypassArmor();
        this.bypassMagic();
        this.setMagic();
    }
}
