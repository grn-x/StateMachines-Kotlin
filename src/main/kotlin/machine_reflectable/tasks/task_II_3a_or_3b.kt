package machine_reflectable.tasks
import machine.*

/**
 * Accepts whenever there is a run of 3 'a' or 3 'b' in a row
 * States track the last character and number of occurrences
 * After streak of 3, stay in final state
 */
private object Z_Unseen : State() { // base state, no occurrence yet
    override fun handle(ch: Char): State = when (ch) {
        'a' -> Z_A1
        'b' -> Z_B1
        else -> Z_Unseen
    }
    override val isAccepting: Boolean = false
}


private object Z_A1 : State() { // last char a, count = 1
    override fun handle(ch: Char): State = when (ch) {
        'a' -> Z_A2
        'b' -> Z_B1
        else -> Z_Unseen
    }
    override val isAccepting: Boolean = false
}


private object Z_A2 : State() { // last two are aa
    override fun handle(ch: Char): State = when (ch) {
        'a' -> Z_ACCEPT // third a -> accept
        'b' -> Z_B1
        else -> Z_Unseen
    }
    override val isAccepting: Boolean = false
}


private object Z_B1 : State() { // last char b, count = 1
    override fun handle(ch: Char): State = when (ch) {
        'b' -> Z_B2
        'a' -> Z_A1
        else -> Z_Unseen
    }
    override val isAccepting: Boolean = false
}


private object Z_B2 : State() { // last two are bb
    override fun handle(ch: Char): State = when (ch) {
        'b' -> Z_ACCEPT
        'a' -> Z_A1
        else -> Z_Unseen
    }
    override val isAccepting: Boolean = false
}


private object Z_ACCEPT : State() { // final accepting state
    override fun handle(ch: Char): State = Z_ACCEPT
    override val isAccepting: Boolean = true
}

fun buildThreeAOrBInARowAutomaton(): Automaton = Automaton(Z_Unseen)