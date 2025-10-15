package machine_reflectable.shared
import java.lang.System.out


/**
 * Represents a finite state automaton with a starting state and some logic to process input words.
 *
 * @property startState The initial state of the automaton passed to the class constructor
 */
class Automaton(public val startState: State) {//change from private to public to access automaton.startState in app

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


            /*
            val transitionsProp = current::class.declaredMemberProperties.first { it.name == "transitions" }
            transitionsProp.isAccessible = true
            //val transitions = (transitionsProp.get(current) as Map<Char, () -> State>).mapValues { (_, nextFn) -> nextFn() }
            val transitions = (transitionsProp.getter.call(current) as Map<Char, () -> State>).mapValues { (_, nextFn) -> nextFn() }

            val elseNextProp = current::class.declaredMemberProperties.first { it.name == "elseNext" }
            elseNextProp.isAccessible = true
            //val elseStateFn = elseNextProp.get(current) as (() -> State)?
            val elseStateFn = elseNextProp.getter.call(current) as (() -> State)?
            val elseState = elseStateFn?.invoke()
            */


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
    fun printTransitions(state: State, name:String, transitions:Map<Char, State>, elseState: State?) {
        out.println("State ${name} (accepting=${state.isAccepting})")
        for ((ch, next) in transitions) { // todo next
            out.println("  '$ch' -> ${name}")
        }
        if (elseState != null)
            out.println("  else -> ${elseState::class.simpleName ?: "UnnamedState"}") //TODO ?? avoid using reflection outside of traverse method :/
    }

    fun printStateTree(
        state: State = startState,
        printedCount: MutableMap<State, Int> = mutableMapOf(),
        indent: String = "\t",
        maxCount : Int = 3
    ) {
        val count = printedCount.getOrDefault(state, 0)
        if (count >= maxCount) return
        printedCount[state] = count + 1

        val name = state::class.simpleName ?: "UnnamedState"
        println("$indent$name (accepting=${state.isAccepting})")
        for ((ch, nextFn) in state.transitions) {
            val nextState = nextFn()
            val nextName = nextState::class.simpleName ?: "UnnamedState"
            println("$indent  '$ch' -> $nextName")
            printStateTree(nextState, printedCount, indent + "    ")
        }
        state.elseNext?.let { elseFn ->
            val elseState = elseFn()
            val elseName = elseState::class.simpleName ?: "UnnamedState"
            println("$indent  else -> $elseName")
            printStateTree(elseState, printedCount, indent + "    ")
        }
    }


}

