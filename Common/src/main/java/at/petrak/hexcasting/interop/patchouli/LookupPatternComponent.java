package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.annotations.SerializedName;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import vazkii.patchouli.api.IVariable;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Grab the pattern from the registry
 */
public class LookupPatternComponent extends AbstractPatternComponent {
    @SerializedName("op_id")
    public String opNameRaw;

    protected ResourceLocation opName;
    protected boolean strokeOrder;

    @Override
    public List<Pair<HexPattern, HexCoord>> getPatterns(UnaryOperator<IVariable> lookup) {
        var key = ResourceKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), this.opName);
        var entry = IXplatAbstractions.INSTANCE.getActionRegistry().get(key);

        this.strokeOrder =
            !IXplatAbstractions.INSTANCE.getActionRegistry().getHolderOrThrow(key).is(HexTags.Actions.PER_WORLD_PATTERN);
        return List.of(new Pair<>(entry.prototype(), HexCoord.getOrigin()));
    }

    @Override
    public boolean showStrokeOrder() {
        return this.strokeOrder;
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        var opName = lookup.apply(IVariable.wrap(this.opNameRaw)).asString();
        this.opName = ResourceLocation.tryParse(opName);

        super.onVariablesAvailable(lookup);
    }
}
