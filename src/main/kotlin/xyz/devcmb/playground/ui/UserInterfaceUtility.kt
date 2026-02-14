package xyz.devcmb.playground.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.NamespacedKey
import xyz.devcmb.playground.ParkourPlayground
import kotlin.math.roundToInt

object UserInterfaceUtility {
    val negative_advances: HashMap<Int, String> = hashMapOf(
        -1 to "\uF000",
        -5 to "\uF001",
        -10 to "\uF002",
        -15 to "\uF003",
        -20 to "\uF004",
        -25 to "\uF005",
        -30 to "\uF006",
        -35 to "\uF007",
        -40 to "\uF008",
        -45 to "\uF009",
        -50 to "\uF00A",
        -55 to "\uF00B",
        -60 to "\uF00C",
        -65 to "\uF00D",
        -70 to "\uF00E",
        -75 to "\uF00F",
        -80 to "\uF010",
        -85 to "\uF011",
        -90 to "\uF012",
        -95 to "\uF013",
        -100 to "\uF014",
        -105 to "\uF015",
        -110 to "\uF016",
        -115 to "\uF017",
        -120 to "\uF018",
        -125 to "\uF019",
        -130 to "\uF01A",
        -135 to "\uF01B",
        -140 to "\uF01C",
        -145 to "\uF01D",
        -150 to "\uF01E",
        -155 to "\uF01F",
        -160 to "\uF020",
        -165 to "\uF021",
        -170 to "\uF022",
        -175 to "\uF023",
        -180 to "\uF024",
        -185 to "\uF025",
        -190 to "\uF026",
        -195 to "\uF027",
        -200 to "\uF028",
        -205 to "\uF029",
        -210 to "\uF02A",
        -215 to "\uF02B",
        -220 to "\uF02C",
        -225 to "\uF02D",
        -230 to "\uF02E",
        -235 to "\uF02F",
        -240 to "\uF030",
        -245 to "\uF031",
        -250 to "\uF032",
        -255 to "\uF033",
        -260 to "\uF034",
        -265 to "\uF035",
        -270 to "\uF036",
        -275 to "\uF037",
        -280 to "\uF038",
        -285 to "\uF039",
        -290 to "\uF03A",
        -295 to "\uF03B",
        -300 to "\uF03C"
    )
    val positive_advances: HashMap<Int, String> = hashMapOf(
        1 to "\uE000",
        5 to "\uE001",
        10 to "\uE002",
        15 to "\uE003",
        20 to "\uE004",
        25 to "\uE005",
        30 to "\uE006",
        35 to "\uE007",
        40 to "\uE008",
        45 to "\uE009",
        50 to "\uE00A",
        55 to "\uE00B",
        60 to "\uE00C",
        65 to "\uE00D",
        70 to "\uE00E",
        75 to "\uE00F",
        80 to "\uE010",
        85 to "\uE011",
        90 to "\uE012",
        95 to "\uE013",
        100 to "\uE014",
        105 to "\uE015",
        110 to "\uE016",
        115 to "\uE017",
        120 to "\uE018",
        125 to "\uE019",
        130 to "\uE01A",
        135 to "\uE01B",
        140 to "\uE01C",
        145 to "\uE01D",
        150 to "\uE01E",
        155 to "\uE01F",
        160 to "\uE020",
        165 to "\uE021",
        170 to "\uE022",
        175 to "\uE023",
        180 to "\uE024",
        185 to "\uE025",
        190 to "\uE026",
        195 to "\uE027",
        200 to "\uE028",
        205 to "\uE029",
        210 to "\uE02A",
        215 to "\uE02B",
        220 to "\uE02C",
        225 to "\uE02D",
        230 to "\uE02E",
        235 to "\uE02F",
        240 to "\uE030",
        245 to "\uE031",
        250 to "\uE032",
        255 to "\uE033",
        260 to "\uE034",
        265 to "\uE035",
        270 to "\uE036",
        275 to "\uE037",
        280 to "\uE038",
        285 to "\uE039",
        290 to "\uE03A",
        295 to "\uE03B",
        300 to "\uE03C"
    )

    val fonts: HashMap<String, NamespacedKey> = hashMapOf(
        "negative_space" to NamespacedKey("playground", "spaces"),
        "normal" to NamespacedKey("playground", "normal"),
        "size10shift11" to NamespacedKey("playground", "size10shift11"),
        "size10shift22" to NamespacedKey("playground", "size10shift22"),
        "size20shift10" to NamespacedKey("playground", "size20shift10"),
        "size16shift6" to NamespacedKey("playground", "size16shift6"),
    )

    val defaultFontWidths = mapOf(
        '0' to 12,
        '1' to 10,
        '2' to 12,
        '3' to 12,
        '4' to 12,
        '5' to 14,
        '6' to 12,
        '7' to 12,
        '8' to 12,
        '9' to 12,
        'a' to 12,
        'b' to 12,
        'c' to 12,
        'd' to 12,
        'e' to 10,
        'f' to 12,
        'g' to 12,
        'h' to 12,
        'i' to 4,
        'j' to 10,
        'k' to 12,
        'l' to 4,
        'm' to 20,
        'n' to 12,
        'o' to 12,
        'p' to 12,
        'q' to 12,
        'r' to 10,
        's' to 10,
        't' to 8,
        'u' to 12,
        'v' to 14,
        'w' to 16,
        'x' to 14,
        'y' to 12,
        'z' to 12,
        'A' to 12,
        'B' to 14,
        'C' to 16,
        'D' to 14,
        'E' to 14,
        'F' to 12,
        'G' to 16,
        'H' to 14,
        'I' to 8,
        'J' to 10,
        'K' to 12,
        'L' to 12,
        'M' to 16,
        'N' to 16,
        'O' to 16,
        'P' to 14,
        'Q' to 16,
        'R' to 16,
        'S' to 12,
        'T' to 14,
        'U' to 14,
        'V' to 14,
        'W' to 16,
        'X' to 14,
        'Y' to 14,
        'Z' to 14,
        '!' to 4,
        '"' to 12,
        '#' to 14,
        '$' to 12,
        '%' to 20,
        '&' to 14,
        '\'' to 4,
        '(' to 8,
        ')' to 10,
        '*' to 10,
        '+' to 14,
        ',' to 4,
        '-' to 8,
        '.' to 4,
        '/' to 12,
        ':' to 6,
        ';' to 6,
        '<' to 10,
        '=' to 12,
        '>' to 10,
        '?' to 12,
        '@' to 20,
        '[' to 6,
        '\\' to 10,
        ']' to 6,
        '^' to 12,
        '_' to 12,
        '`' to 6,
        '{' to 8,
        '|' to 4,
        '}' to 6,
        '~' to 14,
        ' ' to 6,
    )

    fun NegativeSpace(pixels: Int): Component {
        return Component.text(buildNegativeSpacing(pixels)).font(fonts["negative_space"])
    }

    fun PositiveSpace(pixels: Int): Component {
        return Component.text(buildPositiveSpacing(pixels)).font(fonts["negative_space"])
    }

    private fun buildNegativeSpacing(targetPixels: Int): String {
        var remaining = targetPixels
        val result = StringBuilder()
        val sorted = negative_advances.keys.sortedByDescending { it }

        while (remaining > 0) {
            var matched = false

            for (value in sorted) {
                val abs = -value
                if (remaining >= abs) {
                    result.append(negative_advances[value])
                    remaining -= abs
                    matched = true
                    break
                }
            }

            if (!matched) {
                throw IllegalArgumentException("Cannot perfectly match spacing for $targetPixels px")
            }
        }

        return result.toString()
    }

    private fun buildPositiveSpacing(targetPixels: Int): String {
        var remaining = targetPixels
        val result = StringBuilder()
        val sorted = positive_advances.keys.sortedByDescending { it }

        while (remaining > 0) {
            var matched = false

            for (value in sorted) {
                val abs = value
                if (remaining >= abs) {
                    result.append(positive_advances[value])
                    remaining -= abs
                    matched = true
                    break
                }
            }

            if (!matched) {
                throw IllegalArgumentException("Cannot perfectly match spacing for $targetPixels px")
            }
        }

        return result.toString()
    }

    fun getNegativeTextCenter(component: Component, size: Float, oversample: Float): Component {
        val text = PlainTextComponentSerializer.plainText().serialize(component)
        var space = 0.0
        for(c in text) {
            space += getScaledWidth(c, size, oversample)
        }

        return NegativeSpace((space / 2.0).roundToInt())
    }

    fun getPositiveTextCenter(component: Component, size: Float, oversample: Float): Component {
        val text = PlainTextComponentSerializer.plainText().serialize(component)
        var space = 0.0
        for(c in text) {
            space += getScaledWidth(c, size, oversample)
        }

        return PositiveSpace((space / 2.0).roundToInt())
    }

    // TODO: Fix this to work with longer length titles
    fun MultiLineCenteredText(size: Float, oversample: Float, vararg components: Component) : Component {
        var fullComponent = Component.empty()
        var lastComponent = components[0]

        components.forEachIndexed { index, component ->
            if(index != 0) {
                fullComponent = fullComponent.append(getNegativeTextCenter(lastComponent, size, oversample))
            }

            fullComponent = fullComponent.append(component)
            lastComponent = component
        }

        return fullComponent
    }

    private fun getScaledWidth(char: Char, newFontSize: Float, newOversample: Float): Double {
        val baseWidth = (defaultFontWidths[char] ?: 14).toDouble()
        return (baseWidth / 20.0) * newOversample * (newFontSize / 10.0)
    }
}