package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import kotlin.math.exp

object OpVectorSpring : ConstMediaAction {
    override val argc: Int
        get() = 5

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val pos = args.getVec3(0, argc)
        val vel = args.getVec3(1, argc)
        val target = args.getVec3(2, argc)
        val stiffness = args.getDouble(3, argc)
        val dt = args.getDouble(4, argc)

        // Simple damped spring / harmonic oscillator approximation using exponential decay damping
        val omega = kotlin.math.sqrt(kotlin.math.max(0.0, stiffness))
        val decay = exp(-dt * omega * 0.5)
        
        val diff = pos.subtract(target)
        val newPos = target.add(diff.scale(decay).add(vel.scale(dt * decay)))
        val newVel = vel.scale(decay).subtract(diff.scale(dt * omega * omega * decay))

        // Return a list or pair of pos and vel? Wait, Hexcasting operators typically return a single Iota or list. Let's return a list containing [newPos, newVel] or just newPos? 
        // Wait, let's check how multi-return operators are implemented or if returning a list iota or list of iotas works.
        // In Hexcasting, execute returns List<Iota>. Returning multiple iotas pushes multiple items to stack!
        return listOf(newPos.asActionResult[0], newVel.asActionResult[0])
    }
}
