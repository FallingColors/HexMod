package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBoolean
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota

object OpListPartition : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        val boolList = args.getList(1, argc)

        val trueList = mutableListOf<Iota>()
        val falseList = mutableListOf<Iota>()

        for (i in list.indices) {
            val item = list[i]
            val keep = if (i < boolList.size) {
                // If boolList has a boolean at index i, use it
                // To be robust, let's extract boolean using getBoolean or check BooleanIota
                val bIota = boolList[i]
                if (bIota is at.petrak.hexcasting.api.casting.iota.BooleanIota) {
                    bIota.bool
                } else {
                    true
                }
            } else {
                false
            }

            if (keep) {
                trueList.add(item)
            } else {
                falseList.add(item)
            }
        }

        return listOf(ListIota(trueList), ListIota(falseList))
    }
}
