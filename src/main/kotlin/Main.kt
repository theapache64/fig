import com.github.theapache64.fig.Fig
import com.github.theapache64.fig.FigException
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val fig = Fig("https://docs.google.com/spreadsheets/d/1LD1Su7HVzAxPlbRp9MO7lni2E5SOqfAsLMCd1FC9A8s/edit?usp=sharing")
    try {
        fig.load() // one time load
        val fruit = fig.getString("fruit", null, 5.seconds)
        println("Fruit is '${fruit}'") // with 5 seconds TTL 🆕 🎊
    } catch (e: FigException) {
        e.printStackTrace()
    }
}