package at.petrak.hexcasting.common.casting.actions.types

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.common.lib.HexItemHolderHandlers
import net.minecraft.world.item.ItemStack

class OpItemEquality(val exact: Boolean) : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val itemA = args.getEntity(0).also { env.assertEntityInRange(it) }
            .let { HexItemHolderHandlers.applyHandlerFor(it) }
            ?: throw MishapInvalidIota.ofType(args[0], 0, "entity.item_holder")

        val itemB = args.getEntity(1).also { env.assertEntityInRange(it) }
            .let { HexItemHolderHandlers.applyHandlerFor(it) }
            ?: throw MishapInvalidIota.ofType(args[1], 1, "entity.item_holder")

        val matches = if (exact) ItemStack.isSameItemSameTags(itemA, itemB)
        else ItemStack.isSameItem(itemA, itemB)

        return matches.asActionResult
    }
}
