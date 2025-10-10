package machine

/**
 * Generic automaton that keeps the current state and tests an input word.
 */
class Automaton(private val startState: State) {


    /**
     * Check whether the input word is accepted by the automaton.
     * Processing the input word character by character, and transitioning through states.
     *
     * @param word the input word to test
     * @return last state's [State.isAccepting] property:
     * true if the word is accepted, otherwise false
     */
    fun accepts(word: String): Boolean {
        var current = startState
        for (c in word) {
            current = current.handle(c)
        }
        return current.isAccepting
    }
}