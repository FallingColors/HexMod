package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.spell.math.HexCoord;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.mojang.datafixers.util.Pair;
import vazkii.patchouli.api.IVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Provide the pattern(s) manually
 */
public class ManualPatternComponent extends AbstractPatternComponent {
    @SerializedName("patterns")
    public String patternsRaw;
    @SerializedName("stroke_order")
    public String strokeOrderRaw;

    protected transient boolean strokeOrder;

    @Override
	public List<Pair<HexPattern, HexCoord>> getPatterns(UnaryOperator<IVariable> lookup) {
        this.strokeOrder = lookup.apply(IVariable.wrap(this.strokeOrderRaw)).asBoolean(true);
        var patsRaw = lookup.apply(IVariable.wrap(patternsRaw)).asListOrSingleton();

        var out = new ArrayList<Pair<HexPattern, HexCoord>>();
        for (var ivar : patsRaw) {
            JsonElement json = ivar.unwrap();
            RawPattern raw = new Gson().fromJson(json, RawPattern.class);

            var dir = HexDir.fromString(raw.startdir);
            var pat = HexPattern.fromAngles(raw.signature, dir);
            var origin = new HexCoord(raw.q, raw.r);
            out.add(new Pair<>(pat, origin));
        }

        return out;
    }

    @Override
    public boolean showStrokeOrder() {
        return this.strokeOrder;
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        this.strokeOrder = IVariable.wrap(this.strokeOrderRaw).asBoolean(true);

        super.onVariablesAvailable(lookup);
    }
}
