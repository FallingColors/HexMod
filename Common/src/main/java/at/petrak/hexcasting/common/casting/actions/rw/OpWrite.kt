package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerPlayer

// we make this a spell cause imo it's a little ... anticlimactic for it to just make no noise
object OpWrite : SpellAction {
    override val argc = 1
    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val datum = args[0]

        val (handStack) = env.getHeldItemToOperateOn {
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

            datumHolder != null && datumHolder.writeIota(datum, true)
        }
            // If there are no data holders that are writeable, find a data holder that isn't writeable
            // so that the error message is more helpful.
            ?: env.getHeldItemToOperateOn {
                val dataHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)
                dataHolder != null
        } ?: throw MishapBadOffhandItem.of(null, "iota.write")

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: throw MishapBadOffhandItem.of(handStack, "iota.write")

        if (!datumHolder.writeIota(datum, true))
            throw MishapBadOffhandItem.of(handStack, "iota.readonly", datum.display())

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, env.castingEntity as? ServerPlayer)
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
