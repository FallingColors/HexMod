package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor

/**
 * Also throwable for your *own* name, for cases like Chronicler's Gambit
 */
class MishapOthersName(val confidant: Player) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        val seconds = if (this.confidant == ctx.caster) 5 else 60
        ctx.mishapEnvironment.blind(seconds * 20)
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        if (this.confidant == ctx.caster)
            error("others_name.self")
        else
            error("others_name", confidant.name)

    companion object {
        /**
         * Return any true names found in this iota.
         *
         * If `caster` is non-null, it will ignore that when checking.
         */
        @JvmStatic
        fun getTrueNameFromDatum(datum: Iota, caster: Player?): Player? {
            val poolToSearch = ArrayDeque<Iota>()
            poolToSearch.addLast(datum)

            while (poolToSearch.isNotEmpty()) {
                val datumToCheck = poolToSearch.removeFirst()
                if (datumToCheck is EntityIota && datumToCheck.entity is Player && datumToCheck.entity != caster)
                    return datumToCheck.entity as Player
                if (datumToCheck is ListIota)
                    poolToSearch.addAll(datumToCheck.list)
            }

            return null
        }

        @JvmStatic
        fun getTrueNameFromArgs(datums: List<Iota>, caster: Player?): Player? {
            return datums.firstNotNullOfOrNull { getTrueNameFromDatum(it, caster) }
        }
    }
}
