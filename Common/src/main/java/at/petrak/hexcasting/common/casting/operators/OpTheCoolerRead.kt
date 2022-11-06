package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.spell.getItemEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.Entity

object OpTheCoolerRead : ConstManaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): List<Iota> {
        val target = args.getEntity(0, argc)

        ctx.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: throw MishapBadEntity.of(target, "iota.read")

        val datum = datumHolder.readIota(ctx.world)
            ?: datumHolder.emptyIota()
            ?: throw MishapBadItem.of(target, "iota.read")
        return listOf(datum)
    }
}
