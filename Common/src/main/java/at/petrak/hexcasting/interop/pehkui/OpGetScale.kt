package at.petrak.hexcasting.interop.pehkui

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpGetScale : ConstManaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val target = args.getEntity(0, argc)
        return IXplatAbstractions.INSTANCE.pehkuiApi.getScale(target).toDouble().asActionResult
    }
}