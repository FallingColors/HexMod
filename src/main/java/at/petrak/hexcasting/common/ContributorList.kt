package at.petrak.hexcasting.common

import at.petrak.hexcasting.HexMod
import com.electronwill.nightconfig.toml.TomlParser
import net.minecraft.DefaultUncaughtExceptionHandler
import java.io.IOException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object ContributorList {
    private val contributors = ConcurrentHashMap<String, ContributorInfo>()
    private var startedLoading = false

    fun loadContributors() {
        if (!startedLoading) {
            val thread = Thread(this::fetch)
            thread.name = "Hexcasting Contributor Fanciness Thread"
            thread.isDaemon = true
            thread.uncaughtExceptionHandler = DefaultUncaughtExceptionHandler(HexMod.getLogger())
            thread.start()

            startedLoading = true
        }
    }

    fun fetch() {
        try {
            val url = URL("https://raw.githubusercontent.com/gamma-delta/HexMod/main/contributors.toml")
            val toml = TomlParser().parse(url).unmodifiable()

            val keys = toml.valueMap().keys
            for (key in keys) {
                val info: ContributorInfo = toml.get(key)
                contributors[key] = info
            }
        } catch (e: IOException) {
            HexMod.getLogger().info("Couldn't load contributors.toml. Nothing to be done, carry on...")
        }
    }

    data class ContributorInfo(val colorizer: List<Int>)
}