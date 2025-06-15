package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import net.minecraft.core.HolderLookup;
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
    private transient HolderLookup.RegistryLookup.Provider registries;

    @Override
    public List<HexPattern> getPatterns(UnaryOperator<IVariable> lookup) {
        this.strokeOrder = lookup.apply(IVariable.wrap(this.strokeOrderRaw)).asBoolean(true);
        var patsRaw = lookup.apply(IVariable.wrap(patternsRaw)).asListOrSingleton(registries);

        var out = new ArrayList<HexPattern>();
        for (var ivar : patsRaw) {
            JsonElement json = ivar.unwrap();
            RawPattern raw = new Gson().fromJson(json, RawPattern.class);

            var dir = HexDir.fromString(raw.startdir);
            var pat = HexPattern.fromAngles(raw.signature, dir);
            out.add(pat);
        }

        return out;
    }

    @Override
    public boolean showStrokeOrder() {
        return this.strokeOrder;
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup, HolderLookup.RegistryLookup.Provider registries) {
        this.strokeOrder = IVariable.wrap(this.strokeOrderRaw).asBoolean(true);
        this.registries = registries;

        super.onVariablesAvailable(lookup, registries);
    }
}
