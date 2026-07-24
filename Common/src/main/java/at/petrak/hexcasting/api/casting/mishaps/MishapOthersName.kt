package at.petrak.hexcasting.api.casting.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.server.level.ServerLevel
import at.petrak.hexcasting.api.utils.TreeList
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor

/**
 * Also throwable for your *own* name, for cases like Chronicler's Gambit.
 * confidant == null means the player is offline.
 */
class MishapOthersName(val confidant: Player?) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BLACK)

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        val seconds = if (this.confidant != null && this.confidant == ctx.castingEntity) 5 else 60
        ctx.mishapEnvironment.blind(seconds * 20)
        return stack
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context) =
        when (this.confidant) {
            null -> error("others_name.offline")
            ctx.castingEntity -> error("others_name.self")
            else -> error("others_name", confidant.name)
        }

    companion object {
        /**
         * Returns a mishap if any true names are found in this iota.
         *
         * If `caster` is non-null, it will ignore that when checking.
         */
        @JvmStatic
        fun getTrueNameMishapFromDatum(level: ServerLevel, datum: Iota, caster: Player?): MishapOthersName? {
            val poolToSearch = ArrayDeque<Iota>()
            poolToSearch.addLast(datum)

            while (poolToSearch.isNotEmpty()) {
                val datumToCheck = poolToSearch.removeFirst()

                if(datumToCheck is EntityIota) {
                    val ent = datumToCheck.getEntity(level)
                    if (ent == null && datumToCheck.isPlayer)
                        return MishapOthersName(null)
                    if(ent is Player && ent != caster)
                        return MishapOthersName(ent)
                }

                val datumSubIotas = datumToCheck.subIotas()
                if (datumSubIotas != null)
                    poolToSearch.addAll(datumSubIotas)
            }

            return null
        }

        @JvmStatic
        fun getTrueNameMishapFromArgs(level: ServerLevel, datums: List<Iota>, caster: Player?): MishapOthersName? {
            return datums.firstNotNullOfOrNull { getTrueNameMishapFromDatum(level, it, caster) }
        }
    }
}
