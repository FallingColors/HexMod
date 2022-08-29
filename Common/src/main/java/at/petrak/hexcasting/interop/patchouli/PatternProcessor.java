package at.petrak.hexcasting.interop.patchouli;

import net.minecraft.client.resources.language.I18n;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class PatternProcessor implements IComponentProcessor {
    private String translationKey;

    @Override
    public void setup(IVariableProvider vars) {
        if (vars.has("header"))
            translationKey = vars.get("header").asString();
        else {
            IVariable key = vars.get("op_id");
            String opName = key.asString();

            String prefix = "hexcasting.spell.";
            boolean hasOverride = I18n.exists(prefix + "book." + opName);
            translationKey = prefix + (hasOverride ? "book." : "") + opName;
        }
    }

    @Override
    public IVariable process(String key) {
        if (key.equals("translation_key")) {
            return IVariable.wrap(translationKey);
        }

        return null;
    }
}
