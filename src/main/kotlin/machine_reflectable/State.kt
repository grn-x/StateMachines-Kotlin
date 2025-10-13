package machine_reflectable

/**
 * Abstract base class for states to define the design pattern being implemented in each state.
 */
abstract class State(


    // Map<Char, () -> State> where state is a lambda supplier
    // public and not protected to work in parent package // TODO: make private and retrieve via reflection
    public val transitions: Map<Char, () -> State>,

    // (() -> State)?
    // nullable function (?) supplying a state
    val elseNext: (() -> State)? = null, //allows for lazy evaluation


    // 'open' letting subclasses override
    open val isAccepting: Boolean = false
) {


    //  '=' one-line function returning the expression result (lambda like).
    //
    // transitions[ch]?.invoke():
    //   -return supplier lambda as value from map[key] return value or null
    //   '?.' safe call operator; call if non-null, else return null
    //   '?:' Elvis operator; if left is null, evaluate right side
    /** Process a character and return the next state.
     * This follows the State pattern: the current state object decides the
     * next state and returns a reference to it.
     * @param ch the input character to process
     * @return the next state the automaton falls into after processing the character
     * */
    fun handle(ch: Char): State =
        transitions[ch]?.invoke() ?: elseNext?.invoke() ?: this
}

