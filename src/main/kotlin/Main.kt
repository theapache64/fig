import com.github.theapache64.fig.Fig
import com.github.theapache64.fig.FigException
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val fig = Fig(
        sheetUrl = "https://docs.google.com/spreadsheets/d/1LD1Su7HVzAxPlbRp9MO7lni2E5SOqfAsLMCd1FC9A8s/edit?usp=sharing",
        cacheTTL = 5.seconds
    )
    try {
        // one time load
        fig.load()

        // with 5 seconds TTL 🆕 🎊
        var fruit = fig.getString("fruit", null)
        println("Fruit is '${fruit}'")

        Thread.sleep(6000)
        fruit = fig.getString("fruit", null)
        println("Fruit is '${fruit}'")

    } catch (e: FigException) {
        e.printStackTrace()
    }
}