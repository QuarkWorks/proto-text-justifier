package net.quarkworks.dev.proto_text_justifier

import java.util.*


class AlignCoder @Throws(WordWidthException::class)
    constructor(private val width: Int, words: Sequence<String>, private val alignment: Alignment)
{
    val markup = LinkedList<StreamCode>()
    init {
        markup.add(ContentStart())
        val walker = Walker()
        for (word in words) {
            // no word can be wider than width
            if (word.length > width) throw WordWidthException(word)
            walker.step(word)
        }
        walker.finishWalking()
        markup.add(ContentEnd())
    }

    private inner class Walker {
        val acum = LinkedList<String>()
        // just non WS accounting
        var accounted = 0
        val inter_space : Int
            get() { return if(acum.size > 0) acum.size - 1 else 0 }
        val leftOver : Int
            get() { return width - accounted - inter_space }

        fun step(word: String) {
            inc(word)
            if (leftOver >= 0) return
            // we are now wider than width
            // time to rewind
            dec()
            // convert the acum to markup
            convert()
            // at end of line
            markup.add(ColEOL())
            // reset internal state
            reset()
            // save the word that
            // made things wider than width
            inc(word)
        }

        fun finishWalking() {
            convert()
            markup.add(ColEOL())
            reset()
        }

        fun inc(word:String) {
            acum.add(word)
            accounted += word.length
        }

        fun dec() {
            val save = acum.removeLast()
            accounted -= save.length
        }

        fun reset() {
            accounted = 0
            acum.clear()
        }

        fun convert() {
            when (alignment) {

                Alignment.Left -> {
                    for (word in acum){
                        markup.add(Content(word))
                        markup.add(Space(" "))
                    }
                    markup.removeLast()
                    markup.add(Space(" ".repeat(leftOver)))
                }
                Alignment.Right -> {
                    markup.add(Space(" ".repeat(leftOver)))
                    val i = acum.iterator()
                    markup.add(Content(i.next()))
                    for (word in i)
                    {
                        markup.add(Space(" "))
                        markup.add(Content(word))
                    }
                }
                Alignment.Justified -> {
                    when (acum.size) {
                        1 -> {
                            val left = leftOver / 2
                            val right = left + leftOver % 2
                            markup.add(Space(" ".repeat(left)))
                            markup.add(Content(acum.remove()))
                            markup.add(Space(" ".repeat(right)))
                        }
                        in 2..Int.MAX_VALUE -> {
                            val ws_count = width - accounted
                            var ( a, b ) = divmod(ws_count - inter_space, inter_space)
                            val i = acum.iterator()
                            markup.add(Content(i.next()))
                            for (word in i)
                            {
                                markup.add(Space(" ".repeat(a + 1 + if(b > 0) 1 else 0)))
                                markup.add(Content(word))
                                if (b > 0) {
                                    b--
                                }
                            }
                        }
                    }
                }
            }

        }
    }

}
class WordWidthException(message: String) : Exception(message)

enum class Alignment {
    Left, Right, Justified
}

sealed class StreamCode
sealed class StreamCodeWithValue(val v:String) : StreamCode()

class ContentStart:StreamCode ()
class ContentEnd:StreamCode ()

class ColEOL:StreamCode ()
class ColRowWrap:StreamCode ()   //not used
class ContentBreak:StreamCode () //not used

class Content(v: String):StreamCodeWithValue(v)
class Space(v: String):StreamCodeWithValue(v)

fun divmod(num: Int, den: Int): Pair<Int, Int> {
    return Pair(num / den, num % den)
}