package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.numOrVec
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.spellListOf
import net.minecraft.world.phys.Vec3

object OpSub : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = numOrVec(args[0], 1)
        val rhs = numOrVec(args[1], 0)

        return spellListOf(
            lhs.map({ lnum ->
                rhs.map(
                    { rnum -> lnum - rnum }, { rvec -> Vec3(lnum - rvec.x, lnum - rvec.y, lnum - rvec.z) }
                )
            }, { lvec ->
                rhs.map(
                    { rnum -> lvec.subtract(rnum, rnum, rnum) }, { rvec -> lvec.subtract(rvec) }
                )
            })
        )
    }
}
