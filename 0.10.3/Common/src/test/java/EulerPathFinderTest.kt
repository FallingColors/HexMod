import at.petrak.hexcasting.api.spell.math.EulerPathFinder.findAltDrawing
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern.Companion.fromAngles
import org.junit.jupiter.api.Test

internal class EulerPathFinderTest {
    @Test
    fun findAltDrawing() {
        val sig = "dadaddwwaadada"
        val pat = fromAngles(sig, HexDir.NORTH_EAST)
        for (i in 0 until 8) {
            val scrungled = findAltDrawing(pat, i.toLong())
            println(scrungled)
        }
    }
}
