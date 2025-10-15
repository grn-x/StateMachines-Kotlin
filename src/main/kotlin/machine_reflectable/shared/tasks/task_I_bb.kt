package machine_reflectable.shared.tasks

import machine_reflectable.shared.Automaton
import machine_reflectable.shared.State as R_State

object task_I_bb {


    /**
     * Accepts all strings that end with two b's.
     * States (object singletons):
     *      R_Z0: base state, last char not 'b'
     *      R_Z1: single last char is 'b'
     *      R_Z2: final (accepting) state, last two or more chars are 'b'
     */
    private object R_Z0 : R_State(
        transitions = mapOf(
            'b' to { R_Z1 }
        ),
        elseNext = { R_Z0 },
        isAccepting = false
    )

    private object R_Z1 : R_State(
        transitions = mapOf(
            'b' to { R_Z2 }
        ),
        elseNext = { R_Z0 },
        isAccepting = false
    )

    private object R_Z2 : R_State(
        transitions = mapOf(
            'b' to { R_Z2 }
        ),
        elseNext = { R_Z0 },
        isAccepting = true
    )

    fun r_buildBBAutomaton(): Automaton = Automaton(R_Z0)
}