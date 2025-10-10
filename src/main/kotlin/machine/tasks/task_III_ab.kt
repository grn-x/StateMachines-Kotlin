package machine.tasks
import machine.*

/**
 * Every 'a' in the string must be immediately followed by 'b'.
 *      BASE: normal state (accepting), transition to AWAIT_B on 'a'
 *      AWAIT_B: previous char was 'a', if next is 'b' cycle back to BASE, invalid otherwise
 *      INVALID: violation (non-accepting), loops to itself
 */
private object Z_BASE : State() {
    override fun handle(ch: Char): State = when (ch) {
        'a' -> Z_AWAIT_B
        'b' -> Z_BASE
        else -> Z_INVALID
    }
    override val isAccepting: Boolean = true
}


private object Z_AWAIT_B : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> Z_BASE
        else -> Z_INVALID
    }
    override val isAccepting: Boolean = false
}


private object Z_INVALID : State() {
    override fun handle(ch: Char): State = Z_INVALID
    override val isAccepting: Boolean = false
}


fun buildAThenBAutomaton(): Automaton = Automaton(Z_BASE)