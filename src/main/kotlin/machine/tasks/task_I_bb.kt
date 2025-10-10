package machine.tasks
import machine.*
/**
 * Accepts all strings that end with two b's.
 * States (object singletons):
 *      Z0: base state, last char not 'b'
 *      Z1: single last char is 'b'
 *      Z2: final (accepting) state, last two or more chars are 'b'
 */
private object Z0 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> Z1
        else -> Z0 // 'a' or other: remain in Z0
    }
    override val isAccepting: Boolean = false
}


private object Z1 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> Z2
        else -> Z0 // any non-b resets
    }
    override val isAccepting: Boolean = false
}


private object Z2 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> Z2 // still ends with bb
        else -> Z0
    }
    override val isAccepting: Boolean = true
}


fun buildBBAutomaton(): Automaton = Automaton(Z0)