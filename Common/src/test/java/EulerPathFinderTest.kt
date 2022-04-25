import at.petrak.hexcasting.api.spell.math.EulerPathFinder.findAltDrawing
import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern.Companion.FromAnglesSig
import org.testng.annotations.Test

internal class EulerPathFinderTest {
    @Test
    fun findAltDrawing() {
        val sig = "dadaddwwaadada"
        val pat = FromAnglesSig(sig, HexDir.NORTH_EAST)
        for (i in 0 until 8) {
            val scrungled = findAltDrawing(pat, i.toLong())
            println(scrungled)
        }
    }
}
