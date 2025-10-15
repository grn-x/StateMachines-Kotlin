package machine_reflectable.shared

import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.KMutableProperty1


/*
    * A factory for creating State instances from Strings
 */
class StateFactory {

    companion object StaticFactory {

        /**
         * Create states from a list of StateExchangeObjects
         * Handles circular references:
         *  create all State instances without transitions and store them in a map by name
         *  only then wire up transitions and elseNext using the map and custom
         *
         * @param stateExchangeObjects List of [StateExchangeObject] defining states and their transitions
         *                              first list entry is considered the initial state!
         * @return The initial state (first in the list!) with all transitions properly set up
         */
        fun fromList(stateExchangeObjects: List<StateExchangeObject>): State {
            // Map of state names to State instances
            val stateMap = mutableMapOf<String, State>()
            // create state instances without transitions
            // behave singleton-like, if same reference is used multiple times
            // cannot use object keyword as they need to be created dynamically
            for (obj in stateExchangeObjects) {
                stateMap[obj.name] = object : State(
                    obj.name,
                    obj.isAccepting
                ) {} // braces mean anonymous subclass of abstract State
            }


            // wire up transitions using the stateMap
            /*for (obj in stateExchangeObjects) {
                val transitions = obj.transitions.mapValues { (_, targetName) ->
                    { stateMap[targetName]!! }
                } //remaps values but keeps keys
                // https://kotlinlang.org/docs/lambdas.html#it-implicit-name-of-a-single-parameter
                val elseNext = obj.elseNext?.let { targetName ->
                    { stateMap[targetName]!! } // lambda itself can be null; in which case the handle function in
                      // State will return itself; but if the lambda is not null, the content mustnt be null aswell
                }

                val state = stateMap[obj.name] as State

                state.lateTransitions = transitions
                state.lateElseNext = elseNext
            }*/

            // alternative wire-up using new method:
            for (obj in stateExchangeObjects) {
                val transitions = obj.transitions.mapValues { (_, targetName) ->
                    { stateMap[targetName]!! }
                } //remaps values but keeps keys
                // https://kotlinlang.org/docs/lambdas.html#it-implicit-name-of-a-single-parameter
                val elseNext = obj.elseNext?.let { targetName ->
                    { stateMap[targetName]!! } // lambda itself can be null; in which case the handle function in
                    // State will return itself; but if the lambda is not null, the content mustnt be null aswell
                }
                val state = stateMap[obj.name]!!
                wireState(state, transitions, elseNext)
            }



            return stateMap[stateExchangeObjects.first().name]!!
        }


        fun wireState(state: State, transitions: Map<Char, () -> State>, elseNext: (() -> State)?) {
            val kClass = state::class
            val transitionsProp = kClass.declaredMemberProperties
                .first { it.name == "transitions" } as KMutableProperty1<State, Map<Char, () -> State>>
            transitionsProp.isAccessible = true
            transitionsProp.set(state, transitions)

            val elseNextProp = kClass.declaredMemberProperties
                .first { it.name == "elseNext" } as KMutableProperty1<State, (() -> State)?>
            elseNextProp.isAccessible = true
            elseNextProp.set(state, elseNext)
        }


        @Deprecated("use fromList instead", ReplaceWith("fromList"))
        fun fromString(name: String, isAccepting: Boolean, transitions: Map<Char, String>, elseTarget: String?): State {
            //placeholder
            return null!!
        }

        @Deprecated("use fromList instead", ReplaceWith("fromList"))
        fun fromString(name: String, isAccepting: Boolean, transitions: Map<Char, State>, elseTarget: State?): State {
            val elseTargetLambda: (() -> State)? = elseTarget?.let { { elseTarget } }
            val transitionLambdas: Map<Char, () -> State> = transitions.mapValues { (_, state) -> { state } }


            return object : State(name, isAccepting) {}
        }
    }
}

public data class StateExchangeObject(
    val name: String,
    val transitions: Map<Char, String>,
    val elseNext: String? = null,
    val isAccepting: Boolean = false
)