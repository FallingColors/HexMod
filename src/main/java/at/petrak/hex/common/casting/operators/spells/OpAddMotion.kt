package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.RenderedSpellImpl
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellOperator.Companion.getChecked
import at.petrak.hex.common.casting.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.operators.SimpleOperator
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpAddMotion : SimpleOperator, RenderedSpellImpl {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val target = args.getChecked<Entity>(0)
        val motion = args.getChecked<Vec3>(1)
        return spellListOf(RenderedSpell(OpAddMotion, spellListOf(target, motion)))
    }

    override fun cast(args: List<SpellDatum<*>>, ctx: CastingContext) {
        val target = args.getChecked<Entity>(0)
        val motion = args.getChecked<Vec3>(1)
        target.deltaMovement = target.deltaMovement.add(motion)
    }
}