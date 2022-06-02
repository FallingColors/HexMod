package at.petrak.hexcasting.fabric.interop.gravity

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import me.andrew.gravitychanger.api.GravityChangerAPI
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpGetGravity : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val target = args.getChecked<Entity>(1)
        val grav = GravityChangerAPI.getGravityDirection(target)
        return Vec3.atLowerCornerOf(grav.normal).asSpellResult
    }
}