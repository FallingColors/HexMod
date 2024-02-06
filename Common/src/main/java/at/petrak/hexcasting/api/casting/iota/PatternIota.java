package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapEvalTooMuch;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidPattern;
import at.petrak.hexcasting.api.casting.mishaps.MishapUnenlightened;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.utils.HexUtils.isOfTag;

public class PatternIota extends Iota {
    public PatternIota(@NotNull HexPattern pattern) {
        super(HexIotaTypes.PATTERN, pattern);
    }

    public HexPattern getPattern() {
        return (HexPattern) this.payload;
    }

    protected PatternIota(@NotNull IotaType<?> type, @NotNull Object payload) {
        super(type, payload);
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
            && that instanceof PatternIota piota
            && this.getPattern().getAngles().equals(piota.getPattern().getAngles());
    }

    @Override
    public @NotNull Tag serialize() {
        return this.getPattern().serializeToNBT();
    }

    @Override
    public @NotNull CastResult execute(CastingVM vm, ServerLevel world, SpellContinuation continuation) {
        Supplier<@Nullable Component> castedName = () -> null;
        try {
            var lookup = PatternRegistryManifest.matchPattern(this.getPattern(), vm.getEnv(), false);
            vm.getEnv().precheckAction(lookup);

            Action action;
            if (lookup instanceof PatternShapeMatch.Normal || lookup instanceof PatternShapeMatch.PerWorld) {
                ResourceKey<ActionRegistryEntry> key;
                if (lookup instanceof PatternShapeMatch.Normal normal) {
                    key = normal.key;
                } else {
                    PatternShapeMatch.PerWorld perWorld = (PatternShapeMatch.PerWorld) lookup;
                    key = perWorld.key;
                }

                var reqsEnlightenment = isOfTag(IXplatAbstractions.INSTANCE.getActionRegistry(), key,
                        HexTags.Actions.REQUIRES_ENLIGHTENMENT);

                castedName = () -> HexAPI.instance().getActionI18n(key, reqsEnlightenment);
                action = Objects.requireNonNull(IXplatAbstractions.INSTANCE.getActionRegistry().get(key)).action();

                if (reqsEnlightenment && !vm.getEnv().isEnlightened()) {
                    // this gets caught down below
                    throw new MishapUnenlightened();
                }
            } else if (lookup instanceof PatternShapeMatch.Special special) {
                castedName = special.handler::getName;
                action = special.handler.act();
            } else if (lookup instanceof PatternShapeMatch.Nothing) {
                throw new MishapInvalidPattern();
            } else throw new IllegalStateException();

            // do the actual calculation!!
            var result = action.operate(
                    vm.getEnv(),
                    vm.getImage(),
                    continuation
            );

            if (result.getNewImage().getOpsConsumed() > vm.getEnv().maxOpCount()) {
                throw new MishapEvalTooMuch();
            }

            var cont2 = result.getNewContinuation();
            // TODO parens also break prescience
            var sideEffects = result.getSideEffects();

            return new CastResult(
                this,
                cont2,
                result.getNewImage(),
                sideEffects,
                ResolvedPatternType.EVALUATED,
                result.getSound());

        } catch (Mishap mishap) {
            return new CastResult(
                this,
                continuation,
                null,
                List.of(new OperatorSideEffect.DoMishap(mishap, new Mishap.Context(this.getPattern(), castedName.get()))),
                mishap.resolutionType(vm.getEnv()),
                HexEvalSounds.MISHAP);
        }
    }

    @Override
    public boolean executable() {
        return true;
    }

    public static IotaType<PatternIota> TYPE = new IotaType<>() {
        @Override
        public PatternIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return PatternIota.deserialize(tag);
        }

        @Override
        public Component display(Tag tag) {
            return PatternIota.display(PatternIota.deserialize(tag).getPattern());
        }

        @Override
        public int color() {
            return 0xff_ffaa00;
        }
    };

    public static PatternIota deserialize(Tag tag) throws IllegalArgumentException {
        var patTag = HexUtils.downcast(tag, CompoundTag.TYPE);
        HexPattern pat = HexPattern.fromNBT(patTag);
        return new PatternIota(pat);
    }

    public static Component display(HexPattern pat) {
        var bob = new StringBuilder();
        bob.append(pat.getStartDir());

        var sig = pat.anglesSignature();
        if (!sig.isEmpty()) {
            bob.append(" ");
            bob.append(sig);
        }
        return Component.translatable("hexcasting.tooltip.pattern_iota",
                Component.literal(bob.toString()).withStyle(ChatFormatting.WHITE))
            .withStyle(ChatFormatting.GOLD);
    }
}
