package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.client.render.PatternTextureManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PatternTexturesCommand
{
    public static void add(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        // TODO: do we want these in release ??
        cmd.then(Commands.literal("textureToggle")
                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
                .executes(ctx -> {
                    PatternTextureManager.useTextures = !PatternTextureManager.useTextures;
                    String log = (PatternTextureManager.useTextures ? "Enabled" : "Disabled") + " pattern texture rendering. This is meant for debugging.";
                    ctx.getSource().sendSuccess(() -> Component.literal(log), true);
                    return 1;
                }));
        cmd.then(Commands.literal("textureRepaint")
                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
                .executes(ctx -> {
                    PatternTextureManager.repaint();
                    ctx.getSource().sendSuccess(() -> Component.literal("Repainting pattern textures. This is meant for debugging."), true);
                    return 1;
                }));
    }
}