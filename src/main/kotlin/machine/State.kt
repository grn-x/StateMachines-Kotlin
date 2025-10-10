package machine

/**
 * Abstract base class for states to define the design pattern being implemented in each state.
 */
abstract class State {
    /** Process a character and return the next state (singleton instances!).
     * This follows the State pattern: the current state object decides the
     * next state and returns a reference to it.
     * @param ch the input character to process
     * @return the next state the automaton falls into after processing the character
     * */
    abstract fun handle(ch: Char): State


    /** Called by the automaton after having completed the input stack.
     * @return true if this state is an accepting state, false otherwise
     */
    abstract val isAccepting: Boolean
}