package machine.tasks

import machine_reflectable.State as R_State

/**
 * Accepts whenever there is a run of 3 'a' or 3 'b' in a row
 * States track the last character and number of occurrences
 * After streak of 3, stay in final state
 */
private object R_Unseen : R_State(
    transitions = mapOf(
        'a' to { R_A1 },
        'b' to { R_B1 }
    ),
    elseNext = { R_Unseen },
    isAccepting = false
)

private object R_A1 : R_State(
    transitions = mapOf(
        'a' to { R_A2 },
        'b' to { R_B1 }
    ),
    elseNext = { R_Unseen },
    isAccepting = false
)

private object R_A2 : R_State(
    transitions = mapOf(
        'a' to { R_ACCEPT },
        'b' to { R_B1 }
    ),
    elseNext = { R_Unseen },
    isAccepting = false
)

private object R_B1 : R_State(
    transitions = mapOf(
        'b' to { R_B2 },
        'a' to { R_A1 }
    ),
    elseNext = { R_Unseen },
    isAccepting = false
)

private object R_B2 : R_State(
    transitions = mapOf(
        'b' to { R_ACCEPT },
        'a' to { R_A1 }
    ),
    elseNext = { R_Unseen },
    isAccepting = false
)

private object R_ACCEPT : R_State(
    transitions = emptyMap(),
    elseNext = { R_ACCEPT },
    isAccepting = true
)

fun r_buildThreeAOrBInARowAutomaton(): machine_reflectable.Automaton = machine_reflectable.Automaton(R_Unseen)