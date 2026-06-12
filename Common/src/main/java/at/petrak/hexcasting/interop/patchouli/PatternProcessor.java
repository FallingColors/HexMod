package at.petrak.hexcasting.interop.patchouli;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class PatternProcessor implements IComponentProcessor {
    private String translationKey;
    private boolean hasArgs = false;

    @Override
    public void setup(Level level, IVariableProvider vars) {
        if (vars.has("header")) {
            translationKey = vars.get("header").asString();
        } else {
            IVariable key = vars.get("op_id");
            String opName = key.asString();

            String prefix = "hexcasting.action.";
            boolean hasOverride = I18n.exists(prefix + "book." + opName);
            translationKey = prefix + (hasOverride ? "book." : "") + opName;

            if (vars.has("input") && !vars.get("input").asString().isEmpty())
                hasArgs = true;
            else if (vars.has("output") && !vars.get("output").asString().isEmpty())
                hasArgs = true;
        }
    }

    @Override
    public IVariable process(Level level, String key) {
        if (key.equals("translation_key")) {
            return IVariable.wrap(translationKey);
        } else if (key.equals("has_signature")) {
            return IVariable.wrap(hasArgs);
        }

        return null;
    }
}
