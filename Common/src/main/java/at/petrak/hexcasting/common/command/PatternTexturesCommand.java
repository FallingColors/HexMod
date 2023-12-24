package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.client.render.PatternTextureManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PatternTexturesCommand
{
    public static void add(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        cmd.then(Commands.literal("textureToggle")
                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
                .executes(ctx -> {
                    PatternTextureManager.useTextures = !PatternTextureManager.useTextures;
                    return 1;
                }));
        cmd.then(Commands.literal("textureRepaint")
                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
                .executes(ctx -> {
                    PatternTextureManager.repaint();
                    return 1;
                }));
        cmd.then(Commands.literal("textureSetResolutionScaler")
                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.argument("integer", IntegerArgumentType.integer()).executes(ctx -> {
                    PatternTextureManager.setResolutionScaler(IntegerArgumentType.getInteger(ctx, "integer"));
                    PatternTextureManager.repaint();
                    return 1;
                })));
    }
}