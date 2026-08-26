package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.world.item.DyeColor
import java.io.PrintWriter
import java.io.StringWriter

class MishapInternalException(val exception: Exception) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLACK)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        // NO-OP
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        var message = Component.literal("$exception")

        // dump stack trace
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val trace = sw.toString()

        message = message.withStyle(
            Style.EMPTY
                .withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, trace))
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(trace)))
        )

        return error("unknown", message)
    }
}
