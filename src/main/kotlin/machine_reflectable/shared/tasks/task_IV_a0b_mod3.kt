package machine_reflectable.shared.tasks

import machine_reflectable.shared.Automaton
import machine_reflectable.shared.State as R_State

object task_IV_a0b_mod3 {


    /**
     * String must start with 'a' and end with 'b', additionally the number of chars must be divisible by 3
     * The methods appear thrice, with the number denoting the progress in the mod 3 cycle
     *
     *      R_START: initial state, transition to R_AWAIT_B_1 on 'a'
     *      R_AWAIT_B: previous char was 'a', if next is 'b' progress to R_WAS_B, otherwise keep counting while awaiting 'b'
     *      R_WAS_B: previous char was 'b', if next is also 'b' stay in B-State but progress to next number in cycle
     *          else reverse back to R_AWAIT_B keeping track of the number in cycle
     *      R_INVALID: violation (non-accepting), loops to itself
     */

    private object R_START : R_State(
        transitions = mapOf(
            'a' to { R_AWAIT_B_1 }
        ),
        elseNext = { R0_INVALID },
        isAccepting = false
    )

    private object R_AWAIT_B_1 : R_State(
        transitions = mapOf(
            'b' to { R_WAS_B_2 }
        ),
        elseNext = { R_AWAIT_B_2 },
        isAccepting = false
    )

    private object R_AWAIT_B_2 : R_State(
        transitions = mapOf(
            'b' to { R_WAS_B_3 }
        ),
        elseNext = { R_AWAIT_B_3 },
        isAccepting = false
    )

    private object R_AWAIT_B_3 : R_State(
        transitions = mapOf(
            'b' to { R_WAS_B_1 }
        ),
        elseNext = { R_AWAIT_B_1 },
        isAccepting = false
    )

    private object R_WAS_B_1 : R_State(
        transitions = mapOf(
            'b' to { R_WAS_B_2 }
        ),
        elseNext = { R_AWAIT_B_2 },
        isAccepting = false
    )

    private object R_WAS_B_2 : R_State(
        transitions = mapOf(
            'b' to { R_WAS_B_3 }
        ),
        elseNext = { R_AWAIT_B_3 },
        isAccepting = false
    )

    private object R_WAS_B_3 : R_State(
        transitions = mapOf(
            'b' to { R_WAS_B_1 }
        ),
        elseNext = { R_AWAIT_B_1 },
        isAccepting = true
    )

    private object R0_INVALID : R_State(
        transitions = emptyMap(),
        elseNext = { R0_INVALID },
        isAccepting = false
    )

    fun r_buildA0BMod3Automaton(): Automaton = Automaton(R_START)
}