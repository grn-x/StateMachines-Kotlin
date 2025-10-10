package machine.tasks
import machine.State

/**
 * String must start with 'a' and end with 'b', additionally the number of chars must be divisible by 3
 * The methods appear thrice, with the number denoting the progress in the mod 3 cycle
 *
 *      START: initial state, transition to AWAIT_B_1 on 'a'
 *      AWAIT_B: previous char was 'a', if next is 'b' progress to WAS_B, otherwise keep counting while awaiting 'b'
 *      WAS_B: previous char was 'b', if next is also 'b' stay in B-State but progress to next number in cycle
 *          else reverse back to AWAIT_B keeping track of the number in cycle
 *      INVALID: violation (non-accepting), loops to itself
 */

private object X_START : State() {
    override fun handle(ch: Char): State = when (ch) {
        'a' -> X_AWAIT_B_1
        else -> X_INVALID
    }
    override val isAccepting: Boolean = false
}



private object X_AWAIT_B_1 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> X_WAS_B_2
        else -> X_AWAIT_B_2
    }
    override val isAccepting: Boolean = false
}
private object X_AWAIT_B_2 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> X_WAS_B_3
        else -> X_AWAIT_B_3
    }
    override val isAccepting: Boolean = false
}
private object X_AWAIT_B_3 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> X_WAS_B_1
        else -> X_AWAIT_B_1
    }
    override val isAccepting: Boolean = false
}




private object X_WAS_B_1 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> X_WAS_B_2
        else -> X_AWAIT_B_2
    }
    override val isAccepting: Boolean = false
}

private object X_WAS_B_2 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> X_WAS_B_3
        else -> X_AWAIT_B_3
    }
    override val isAccepting: Boolean = false
}


private object X_WAS_B_3 : State() {
    override fun handle(ch: Char): State = when (ch) {
        'b' -> X_WAS_B_1
        else -> X_AWAIT_B_1
    }
    override val isAccepting: Boolean = true
}


private object X_INVALID : State() {
    override fun handle(ch: Char): State = X_INVALID
    override val isAccepting: Boolean = false
}