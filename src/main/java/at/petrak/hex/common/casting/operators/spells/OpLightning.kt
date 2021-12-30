package at.petrak.hex.common.casting.operators.spells

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.RenderedSpell
import at.petrak.hex.common.casting.RenderedSpellImpl
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.phys.Vec3

object OpLightning : SimpleOperator, RenderedSpellImpl {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<List<SpellDatum<*>>, Int> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)
        return Pair(
            spellListOf(RenderedSpell(OpLightning, spellListOf(target))),
            1500
        )
    }

    override fun cast(args: List<SpellDatum<*>>, ctx: CastingContext) {
        val target = args.getChecked<Vec3>(0)

        val lightning = LightningBolt(EntityType.LIGHTNING_BOLT, ctx.world)
        lightning.setPosRaw(target.x, target.y, target.z)
        ctx.world.addWithUUID(lightning) // why the hell is it called this it doesnt even involve a uuid
    }
}