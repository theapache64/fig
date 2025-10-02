import com.github.theapache64.fig.Fig
import com.github.theapache64.fig.FigException
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val fig = Fig("https://docs.google.com/spreadsheets/d/1LD1Su7HVzAxPlbRp9MO7lni2E5SOqfAsLMCd1FC9A8s/edit?usp=sharing")
    try {
        fig.load()
    }catch (e : FigException) {
        e.printStackTrace()
    }

    run {
        println("Fruit is '${fig.getString("fruit", null, 5.seconds)}'")
    }
}