package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexCoord;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import com.google.gson.annotations.SerializedName;
import com.mojang.datafixers.util.Pair;
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
        var entry = PatternRegistry.lookupPattern(this.opName);
        this.strokeOrder = !entry.isPerWorld();
        return List.of(new Pair<>(entry.getPrototype(), HexCoord.getOrigin()));
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
