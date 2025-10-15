package machine_reflectable.shared

/**
 * Abstract base class for states to define the design pattern being implemented in each state.
 * @property transitions A map defining character-based transitions to other states
 */
abstract class State(


    // Map<Char, () -> State> where state is a lambda supplier
    // public and not protected to work in parent package // TODO: make private and retrieve via reflection
    val transitions: Map<Char, () -> State>,

    // (() -> State)?
    // nullable function (?) supplying a state
    val elseNext: (() -> State)? = null, //allows for lazy evaluation


    // 'open' letting subclasses override
    open val isAccepting: Boolean = false
) {
    //public final val name: String by lazy { this::class.simpleName ?: "UnnamedState" }
    private final var customName: String? = null


    /**
     * Secondary protected constructor allowing to set a custom name for the state
     * This is useful when creating states via a factory where the class name
     * reflectively retrieved via ::class.simpleName may not be meaningful
     * @param lateElseNext
     * @param customName the custom name for the state, used if provided; otherwise, the class's simple name is used
     * @property lateTransitions replaces transitions and allows for late initialization to handle circular references
     */
    protected constructor(
        customName: String,
        isAccepting: Boolean = false
    ) : this(emptyMap(), null, isAccepting) {
        this.customName = customName
    }
    // and i cant even shadow them, this makes my oop obsessed heart cry
    //https://stackoverflow.com/questions/48443167/kotlin-lateinit-to-val-or-alternatively-a-var-that-can-set-once
    lateinit var lateTransitions: Map<Char, () -> State>
    /*lateinit*/ var lateElseNext: (() -> State)? = null //fukc this lateinit, cannot be null, cannot pass itself from different context wilauehgjoremvcnqpewrnoi

    //abstract val customName: String?
    public final val name: String by lazy { customName ?: this::class.simpleName ?: "UnnamedState" }



    //  '=' one-line function returning the expression result (lambda like).
    //
    // transitions[ch]?.invoke():
    //   -return supplier lambda as value from map[key] return value or null
    //   '?.' safe call operator; call if non-null, else return null
    //   '?:' Elvis operator; if left is null, evaluate right side
    /** Process a character and return the next state.
     *  Pull the lookuptable from [lateTransitions] if initialized, otherwise from [transitions]
     *  [lateTransitions] is being used when programmatically instantiating states with their circular references
     * @param ch the input character to process
     * @return the next state the automaton falls into after processing the character
     * */
    fun handle(ch: Char): State {
        val activeTransitions = if (::lateTransitions.isInitialized) lateTransitions else transitions
        val activeElseNext = lateElseNext ?: elseNext
        return activeTransitions[ch]?.invoke() ?: elseNext?.invoke() ?: this
    }

    //private val activeTransitions: Map<Char, () -> State> by lazy {
    //    if (::lateTransitions.isInitialized) lateTransitions else transitions
    //}
}

