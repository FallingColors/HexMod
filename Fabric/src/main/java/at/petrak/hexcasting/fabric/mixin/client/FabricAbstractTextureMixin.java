package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.fabric.client.ExtendedTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractTexture.class)
public abstract class FabricAbstractTextureMixin implements ExtendedTexture {
    @Shadow
    protected boolean blur;

    @Shadow
    protected boolean mipmap;

    @Shadow
    public abstract void setFilter(boolean bilinear, boolean mipmap);

    @Unique
    private boolean lastBilinear;

    @Unique
    private boolean lastMipmap;

    @Override
    public void setFilterSave(boolean bilinear, boolean mipmap) {
        this.lastBilinear = this.blur;
        this.lastMipmap = this.mipmap;
        setFilter(bilinear, mipmap);
    }

    @Override
    public void restoreLastFilter() {
        setFilter(this.lastBilinear, this.lastMipmap);
    }
}
