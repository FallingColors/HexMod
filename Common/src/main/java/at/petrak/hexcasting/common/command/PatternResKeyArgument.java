package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PatternResKeyArgument extends ResourceKeyArgument<ActionRegistryEntry> {
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PATTERN = new DynamicCommandExceptionType(
        (errorer) ->
            Component.translatable("hexcasting.pattern.unknown", errorer)
    );

    public PatternResKeyArgument() {
        super(HexRegistries.ACTION);
    }

    public static PatternResKeyArgument id() {
        return new PatternResKeyArgument();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var suggestions = new ArrayList<String>();
        var registry = IXplatAbstractions.INSTANCE.getActionRegistry();
        for (var key : registry.registryKeySet()) {
            if (HexUtils.isOfTag(registry, key, HexTags.Actions.PER_WORLD_PATTERN)) {
                suggestions.add(key.location().toString());
            }
        }

        return SharedSuggestionProvider.suggest(suggestions, builder);
    }

    public static <T> ResourceKey<T> getResourceKey(
            CommandContext<CommandSourceStack> context,
            String argumentName,
            ResourceKey<Registry<T>> registryKey,
            DynamicCommandExceptionType exceptionType
    ) throws CommandSyntaxException {
        ResourceKey<?> key = context.getArgument(argumentName, ResourceKey.class);
        Optional<ResourceKey<T>> optional = key.cast(registryKey);
        return optional.orElseThrow(() -> exceptionType.create(key.location()));
    }

    public static ResourceKey<ActionRegistryEntry> getPatternKey(
            CommandContext<CommandSourceStack> ctx, String argumentName) throws CommandSyntaxException {
        return getResourceKey(ctx, argumentName, HexRegistries.ACTION, ERROR_UNKNOWN_PATTERN);
    }

    public static HexPattern getPattern(
        CommandContext<CommandSourceStack> ctx, String argumentName) throws CommandSyntaxException {
        var targetKey = getPatternKey(ctx, argumentName);
        var foundPat = PatternRegistryManifest.getCanonicalStrokesPerWorld(targetKey, ctx.getSource().getServer().overworld());
        if (foundPat == null) {
            throw ERROR_UNKNOWN_PATTERN.create(targetKey.location());
        } else {
            return foundPat;
        }
    }
}
