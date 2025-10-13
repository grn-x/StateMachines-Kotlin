package machine_reflectable.tasks

import machine_reflectable.State as R_State

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

fun r_buildBBAutomaton(): machine_reflectable.Automaton = machine_reflectable.Automaton(R_Z0)