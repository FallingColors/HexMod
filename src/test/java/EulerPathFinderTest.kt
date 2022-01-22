import at.petrak.hex.hexmath.EulerPathFinder.findAltDrawing
import at.petrak.hex.hexmath.HexDir
import at.petrak.hex.hexmath.HexPattern.Companion.FromAnglesSig
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