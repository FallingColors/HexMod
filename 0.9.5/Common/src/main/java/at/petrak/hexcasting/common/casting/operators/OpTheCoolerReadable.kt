package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.Entity

object OpTheCoolerReadable : ConstManaOperator {
    override val argc = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): List<SpellDatum<*>> {
        val target = args.getChecked<Entity>(0, OpTheCoolerRead.argc)
        ctx.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: return false.asSpellResult

        datumHolder.readDatum(ctx.world)
            ?: datumHolder.emptyDatum()
            ?: return false.asSpellResult

        return true.asSpellResult
    }
}
