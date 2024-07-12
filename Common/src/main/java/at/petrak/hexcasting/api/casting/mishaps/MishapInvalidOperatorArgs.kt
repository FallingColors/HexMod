package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentContents
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.DyeColor

/**
 * The value failed some kind of predicate.
 */
class MishapInvalidOperatorArgs(
    private val perpetrators: List<Iota>
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GRAY)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        for (i in perpetrators.indices) {
            stack[stack.size - 1 - i] = GarbageIota()
        }
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        return if (perpetrators.size == 1) {
            error(
                "invalid_operator_args.single",
                0,
                perpetrators[0].display()
            )
        } else {
            error(
                "invalid_operator_args.plural",
                perpetrators.size,
                0,
                perpetrators.lastIndex,
                collateIotas(perpetrators)
            )
        }
    }
    private fun collateIotas(iotas: List<Iota>): MutableComponent {
        val out = MutableComponent.create(ComponentContents.EMPTY)
        for (i in iotas.indices) {
            out.append(iotas[i].display())
            if (i < iotas.size-1) {
                out.append(", ")
            }
        }
        return out
    }
}
