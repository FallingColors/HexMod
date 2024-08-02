package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.Util
import net.minecraft.world.item.ItemStack

object OpColorize : SpellAction {
    override val argc = 0

    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val (handStack) = env.getHeldItemToOperateOn(IXplatAbstractions.INSTANCE::isPigment)
            ?: throw MishapBadOffhandItem.of(ItemStack.EMPTY, "colorizer") // TODO: hack

        if (!IXplatAbstractions.INSTANCE.isPigment(handStack)) {
            throw MishapBadOffhandItem.of(
                handStack,
                "colorizer"
            )
        }

        return SpellAction.Result(
            Spell(handStack),
            MediaConstants.DUST_UNIT,
            listOf()
        )
    }

    private data class Spell(val stack: ItemStack) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val copy = stack.copy()
            if (env.withdrawItem({ ItemStack.isSameItemSameTags(copy, it) }, 1, true))
                env.setPigment(FrozenPigment(copy, env.castingEntity?.uuid ?: Util.NIL_UUID))
        }
    }
}
