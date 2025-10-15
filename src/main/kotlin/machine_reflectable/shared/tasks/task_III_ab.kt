package machine_reflectable.shared.tasks

import machine_reflectable.shared.Automaton
import machine_reflectable.shared.State as R_State

object task_III_ab {


    /**
     * Every 'a' in the string must be immediately followed by 'b'.
     *      R_BASE: normal state (accepting), transition to R_AWAIT_B on 'a'
     *      R_AWAIT_B: previous char was 'a', if next is 'b' cycle back to R_BASE, invalid otherwise
     *      R_INVALID: violation (non-accepting), loops to itself
     */
    private object R_BASE : R_State(
        transitions = mapOf(
            'a' to { R_AWAIT_B },
            'b' to { R_BASE }
        ),
        elseNext = { R_INVALID },
        isAccepting = true
    )

    private object R_AWAIT_B : R_State(
        transitions = mapOf(
            'b' to { R_BASE }
        ),
        elseNext = { R_INVALID },
        isAccepting = false
    )

    private object R_INVALID : R_State(
        transitions = emptyMap(),
        elseNext = { R_INVALID },
        isAccepting = false
    )

    fun r_buildAThenBAutomaton(): Automaton = Automaton(R_BASE)
}