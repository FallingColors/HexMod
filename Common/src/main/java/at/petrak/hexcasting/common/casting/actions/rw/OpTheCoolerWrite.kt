package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3

object OpTheCoolerWrite : SpellAction {
    override val argc = 2
    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getEntity(0, argc)
        val datum = args[1]

        env.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: throw MishapBadEntity.of(target, "iota.write")

        if (!datumHolder.writeIota(datum, true))
            throw MishapBadEntity.of(target, "iota.write")

        // We pass null here so that even the own caster won't be allowed into a focus.
        // Otherwise, you could sentinel scout to people and remotely write their names into things using a cleric circle.
        val trueName = MishapOthersName.getTrueNameFromDatum(datum, null)
        if (trueName != null)
            throw MishapOthersName(trueName)

        val burstPos = if (target is ItemEntity) {
            // Special case these because the render is way above the entity
            target.position().add(0.0, 3.0 / 8.0, 0.0)
        } else {
            target.position()
        }
        return SpellAction.Result(
            Spell(datum, datumHolder),
            0,
            listOf(ParticleSpray(burstPos, Vec3(1.0, 0.0, 0.0), 0.25, 3.14, 40))
        )
    }

    private data class Spell(val datum: Iota, val datumHolder: ADIotaHolder) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            datumHolder.writeIota(datum, false)
        }
    }
}
