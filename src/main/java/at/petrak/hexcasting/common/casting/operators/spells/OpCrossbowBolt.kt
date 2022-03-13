package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.ParticleSpray
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.Items
import net.minecraft.world.item.SpectralArrowItem
import net.minecraft.world.phys.Vec3


object OpCrossbowBolt : SpellOperator {
    override val argc = 2
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val origin = args.getChecked<Vec3>(0)
        val direction = args.getChecked<Vec3>(1)
        ctx.assertVecInRange(origin)

        return Triple(
            Spell(origin, direction),
            direction.length().toInt() * 10_000,
            listOf(ParticleSpray.Burst(Vec3.atCenterOf(BlockPos(origin)), 1.0))
        )
    }

    private data class Spell(val origin: Vec3, val direction: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val imaginaryCrossbow: CrossbowItem = Items.CROSSBOW as CrossbowItem
            val imaginaryArrow: SpectralArrowItem = Items.SPECTRAL_ARROW as SpectralArrowItem
            val projectile = imaginaryArrow.createArrow(ctx.caster.level, imaginaryCrossbow.defaultInstance, ctx.caster)

            val directionNormal = direction.normalize()

            projectile.pickup = AbstractArrow.Pickup.DISALLOWED
            projectile.setPos(origin)
            projectile.shoot(directionNormal.x, directionNormal.y, directionNormal.z, direction.length().toFloat(), 0.0f)

            ctx.caster.level.addFreshEntity(projectile)
        }
    }
}