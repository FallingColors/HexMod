package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import net.minecraft.world.entity.Entity

object OpEntityLook : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val e: Entity = args.getChecked(0, argc)
        ctx.assertEntityInRange(e)
        return e.lookAngle.asSpellResult
    }
}
