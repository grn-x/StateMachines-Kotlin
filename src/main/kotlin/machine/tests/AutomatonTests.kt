package machine.tests

import machine.Automaton
import machine.tasks.*

//import  org.junit.jupiter.api.Test
//import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.*


/**
 * Unit tests for automaton tasks
 *
 * Beware of the difference between assertTrue/False and assertEquals(true/false,...)!
 */
class AutomatonTests {

    private fun testWords(automaton: Automaton, accepted: List<String>, rejected: List<String>) {
        accepted.forEach {
            assertEquals(true, automaton.accepts(it), "Expected to accept: \"$it\"")
        }
        rejected.forEach {
            assertEquals(false, automaton.accepts(it), "Expected to reject: \"$it\"")
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    inner class Task1_EndingWithTwoBs {

        @Test
        fun `accepts strings ending with two b's`() {
            val a = buildBBAutomaton()

            val accepted = listOf(
                "bb",
                "abb",
                "aabb",
                "aaabb",
                "babb",
                "bbbb",
                "abababb",
                "aaababb"
            )
            val rejected = listOf(
                "",
                "a",
                "b",
                "ab",
                "aba",
                "abba",
                "ba",
                "aaa",
                "baba",
            )
            testWords(a, accepted, rejected)
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    inner class Task2_ThreeInARow {

        @Test
        fun `accepts strings with at least three a's or b's in a row`() {
            val a = buildThreeAOrBInARowAutomaton()

            val accepted = listOf(
                "aaa",
                "bbb",
                "aabbb",
                "aaabb",
                "abaaa",
                "bbbbbb",
                "aaab",
                "baaabb",
                "bbaaabb",
            )
            val rejected = listOf(
                "",
                "a",
                "b",
                "ab",
                "aba",
                "aabb",
                "abab",
                "aabba",
                "babab",
            )
            testWords(a, accepted, rejected)
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    inner class Task3_AFollowedByB {

        @Test
        fun `accepts strings where every a is followed by b`() {
            val a = buildAThenBAutomaton()

            val accepted = listOf(
                "",
                "b",
                "bb",
                "ab",
                "bab",
                "babb",
                "abbb",
                "bbabb",
                "abbabb"
            )
            val rejected = listOf(
                "a",
                "aa",
                "aba",
                "abbaaa",
                "abbaba",
                "aabbaba",
                "abbababa"
            )
            testWords(a, accepted, rejected)
        }
    }

    // ---------------------------------------------------------------------
    @Nested
    inner class Task4_StartsAEndsBDiv3 {

        @Test
        fun `accepts strings starting with a, ending with b, and length divisible by 3`() {
            val a = buildA0BMod3Automaton()

            val accepted = listOf(
                "aab",
                "abb",
                "aaabbb",
                "aababb",
                "aaabbabbb",
            )
            val rejected = listOf(
                "",
                "a",
                "b",
                "ab",
                "aabb",
                "aaba",
                "bba",
                "aaa",
                "babb",
                "abab",
                "aaab",
                "aabab",
                "aabbba",
                "aaabba",
                "aaabbaabba",
                "baabbaabbb"
            )
            testWords(a, accepted, rejected)
        }
    }
}
