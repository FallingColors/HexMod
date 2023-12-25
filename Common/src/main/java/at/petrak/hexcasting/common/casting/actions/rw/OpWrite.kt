package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.item.ItemStack

// we make this a spell cause imo it's a little ... anticlimactic for it to just make no noise
object OpWrite : SpellAction {
    override val argc = 1
    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val datum = args[0]

        val (handStack, hand) = env.getHeldItemToOperateOn {
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

            datumHolder != null && datumHolder.writeIota(datum, true)
        } ?: throw MishapBadOffhandItem.of(ItemStack.EMPTY.copy(), null, "iota.write") // TODO: hack

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.write")

        if (!datumHolder.writeIota(datum, true))
            throw MishapBadOffhandItem.of(handStack, hand, "iota.readonly", datum.display())

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, env.caster)
        if (trueName != null)
            throw MishapOthersName(trueName)

        return SpellAction.Result(
            Spell(datum, datumHolder),
            0,
            listOf()
        )
    }

    private data class Spell(val datum: Iota, val datumHolder: ADIotaHolder) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            datumHolder.writeIota(datum, false)
        }
    }
}
