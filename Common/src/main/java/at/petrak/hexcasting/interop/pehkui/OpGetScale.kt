package at.petrak.hexcasting.interop.pehkui

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.Entity

object OpGetScale : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val target = args.getChecked<Entity>(0)
        return IXplatAbstractions.INSTANCE.pehkuiApi.getScale(target).toDouble().asSpellResult
    }
}