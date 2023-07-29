package at.petrak.hexcasting.common.command;

import at.petrak.hexcasting.client.render.PatternTextureManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.awt.*;

public class PatternTexturesCommand
{
    public static void add(LiteralArgumentBuilder<CommandSourceStack> cmd) {
        cmd.then(Commands.literal("textureToggle")
                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
                .executes(ctx -> {
                    PatternTextureManager.useTextures = !PatternTextureManager.useTextures;
                    return 1;
                }));

//        cmd.then(Commands.literal("textureSetColor")
//                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
//                .then(Commands.argument("r", IntegerArgumentType.integer())
//                        .then(Commands.argument("g", IntegerArgumentType.integer())
//                                .then(Commands.argument("b", IntegerArgumentType.integer()).executes(ctx -> {
//                                    var r = IntegerArgumentType.getInteger(ctx, "r");
//                                    var g = IntegerArgumentType.getInteger(ctx, "g");
//                                    var b = IntegerArgumentType.getInteger(ctx, "b");
//                                    PatternTextureManager.color = new Color(r,g,b,255);
//                                    PatternTextureManager.repaint();
//                                    return 1;
//                                })))));
//
//        cmd.then(Commands.literal("textureSetResolution")
//                .requires(dp -> dp.hasPermission(Commands.LEVEL_ADMINS))
//                .then(Commands.argument("integer", IntegerArgumentType.integer()).executes(ctx -> {
//                    var integer = IntegerArgumentType.getInteger(ctx, "integer");
//                    PatternTextureManager.resolutionByBlockSize = integer;
//                    PatternTextureManager.repaint();
//                    return 1;
//                })));
    }
}
