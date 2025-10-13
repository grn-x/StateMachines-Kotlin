package machine_reflectable
import machine_reflectable.State
import java.lang.System.out
import kotlin.reflect.full.*

/**
 * Represents a finite state automaton with a starting state and some logic to process input words.
 *
 * @property startState The initial state of the automaton passed to the class constructor
 */
class Automaton(private val startState: State) {

    /**
     * Check whether the input word is accepted by the automaton
     * Processing the input word character by character, and transitioning through states
     *
     * @param word the input word to test
     * @return last state's [State.isAccepting] property:
     * true if the word is accepted, otherwise false
     */
    fun accepts(word: String): Boolean {
        //traverse(startState, { name, state, transitions, elseState ->
         //   printTransitions(name, state, transitions, elseState)})
        traverse(startState, ::printTransitions)

        var current = startState
        for (c in word) {
            current = current.handle(c)
        }
        return current.isAccepting
    }

    /**
     * Traverses all reachable states starting from [start]
     *
     * Uses reflection to read each state's transitions and elseNext
     *
     * @param start The state to begin traversal from (should be / and defaults to [startState])
     * @param onVisit Callback that receives (currentState, transitions, elseState) used for printing or testing for Example
     */
    fun traverse(start: State = startState, onVisit: (State, String, Map<Char, State>, State?) -> Unit = ::printTransitions) {
        val visited = mutableSetOf<State>()
        val toProcess = ArrayDeque<State>()

        toProcess.add(start)

        while (toProcess.isNotEmpty()) {
            val current = toProcess.removeFirst()
            if (!visited.add(current)) continue // skip processed


            // reflectively get name, transitions and elseNext (latter accessible directly) // TODO
            val name = current::class.simpleName ?: "UnnamedState"
            val transitions = current.transitions.mapValues { (_, nextFn) -> nextFn() }
            val elseState = current.elseNext?.invoke()

            onVisit(current, name, transitions, elseState)

            // enqueue unvisited states
            for (next in transitions.values) {
                if (next !in visited) toProcess.add(next)
            }
            if (elseState != null && elseState !in visited) {
                toProcess.add(elseState)
            }
        }
    }
    fun printTransitions(state:State, name:String, transitions:Map<Char, State>, elseState:State?) {
        out.println("State ${name} (accepting=${state.isAccepting})")
        for ((ch, next) in transitions) { // todo next
            out.println("  '$ch' -> ${name}")
        }
        if (elseState != null)
            out.println("  else -> ${elseState::class.simpleName ?: "UnnamedState"}") //TODO ?? avoid using reflection outside of traverse method :/
    }


}

