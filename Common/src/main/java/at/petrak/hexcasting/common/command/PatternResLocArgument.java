package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class PatternResLocArgument extends ResourceLocationArgument {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PATTERN = new DynamicCommandExceptionType(
        (errorer) ->
            Component.translatable("hexcasting.pattern.unknown", errorer)
    );

    public static PatternResLocArgument id() {
        return new PatternResLocArgument();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
            PatternRegistryManifest.getAllPerWorldActions().stream().map(key -> key.location().toString()), builder);
    }

    public static HexPattern getPattern(
        CommandContext<CommandSourceStack> ctx, String argumentName) throws CommandSyntaxException {
        var targetId = ctx.getArgument(argumentName, ResourceLocation.class);
        var targetKey = ResourceKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), targetId);
        var foundPat = PatternRegistryManifest.getCanonicalStrokesPerWorld(targetKey, ctx.getSource().getLevel());
        if (foundPat == null) {
            throw ERROR_UNKNOWN_PATTERN.create(targetId);
        } else {
            return foundPat;
        }
    }
}
