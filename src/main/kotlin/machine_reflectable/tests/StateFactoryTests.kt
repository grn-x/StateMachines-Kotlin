package machine_reflectable.tests

import machine_reflectable.shared.Automaton
import machine_reflectable.shared.State
import machine_reflectable.shared.StateExchangeObject
import machine_reflectable.shared.StateFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Test

/**
 * StateFactoryTests
 *
 * Tests the StateFactory.fromList() method which creates states from StateExchangeObjects
 * and handles circular dependencies through two-phase construction.
 *
 * Test 1: Verify factory creates states with correct properties (per-state subtests)
 * Test 2: Verify transitions resolve to correct state references (per-state subtests)
 * Test 3: Verify state machine behavior matches expected traversal
 * Test 4: Verify circular references work correctly
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StateFactoryTests {

    // --- Test automaton: Accepts strings matching pattern (ab)*
    // Q0: initial state, accepts empty string (accepting)
    // Q1: after 'a', expects 'b' to complete pair (non-accepting)
    // Q2: error/trap state for invalid sequences (non-accepting, final)

    private val testStateObjects = listOf(
        StateExchangeObject(
            name = "Q0",
            transitions = mapOf(
                'a' to "Q1"   // start of 'ab' pair
            ),
            elseNext = "Q2",  // anything else goes to error
            isAccepting = true
        ),
        StateExchangeObject(
            name = "Q1",
            transitions = mapOf(
                'b' to "Q0"   // complete 'ab' pair, back to Q0
            ),
            elseNext = "Q2",  // anything else goes to error
            isAccepting = false
        ),
        StateExchangeObject(
            name = "Q2",
            transitions = emptyMap(),  // no transitions - trap state
            elseNext = null,           // stays in error state forever
            isAccepting = false
        )
    )

    // --- Expected state properties

    private data class ExpectedState(
        val name: String,
        val isAccepting: Boolean,
        val transitionTargets: Map<Char, String>,
        val elseTarget: String?
    )

    private val expectedStates = mapOf(
        "Q0" to ExpectedState("Q0", true, mapOf('a' to "Q1"), "Q2"),
        "Q1" to ExpectedState("Q1", false, mapOf('b' to "Q0"), "Q2"),
        "Q2" to ExpectedState("Q2", false, emptyMap(), null)
    )

    // --- Helper: extract state name from State instance

    private fun getStateName(state: State): String =
        state.name.ifBlank { state::class.simpleName ?: "UnnamedState" }

    // --- Test 1: State properties verification

    @TestFactory
    fun `fromList creates states with correct properties (per-state subtests)`(): List<DynamicTest> {
        val initialState = StateFactory.fromList(testStateObjects)
        val stateMap = mutableMapOf<String, State>()

        // Collect all states via traversal
        Automaton(initialState).traverse(initialState) { state, name, _, _ ->
            val effectiveName = name.ifBlank { state::class.simpleName ?: "UnnamedState" }
            stateMap[effectiveName] = state
        }

        // Generate dynamic test for each expected state
        return expectedStates.keys.map { stateName ->
            DynamicTest.dynamicTest("State $stateName has correct properties") {
                val expected = expectedStates[stateName]!!
                val actual = stateMap[stateName]

                assertNotNull(actual, "State $stateName was not created")
                actual!!

                assertEquals(expected.name, getStateName(actual), "Name mismatch for $stateName")
                assertEquals(expected.isAccepting, actual.isAccepting, "isAccepting mismatch for $stateName")
            }
        }
    }

    // --- Test 2: Transition resolution verification

    @TestFactory
    fun `fromList resolves transitions to correct state references (per-state subtests)`(): List<DynamicTest> {
        val initialState = StateFactory.fromList(testStateObjects)
        val stateMap = mutableMapOf<String, State>()

        // Collect all states
        Automaton(initialState).traverse(initialState) { state, name, _, _ ->
            val effectiveName = name.ifBlank { state::class.simpleName ?: "UnnamedState" }
            stateMap[effectiveName] = state
        }

        // Generate tests for each state's transitions
        return expectedStates.keys.flatMap { stateName ->
            val expected = expectedStates[stateName]!!
            val state = stateMap[stateName]!!

            val tests = mutableListOf<DynamicTest>()

            // Test each transition target
            expected.transitionTargets.forEach { (char, targetName) ->
                tests.add(DynamicTest.dynamicTest("State $stateName: transition '$char' -> $targetName") {
                    val targetState = state.handle(char)
                    val actualTargetName = getStateName(targetState)
                    assertEquals(targetName, actualTargetName,
                        "Transition '$char' from $stateName points to wrong state")
                })
            }

            // Test elseNext target
            tests.add(DynamicTest.dynamicTest("State $stateName: elseNext -> ${expected.elseTarget ?: "self"}") {
                if (expected.elseTarget != null) {
                    // Test with unmapped character
                    val targetState = state.handle('x')  // unmapped char
                    val actualTargetName = getStateName(targetState)
                    assertEquals(expected.elseTarget, actualTargetName,
                        "elseNext from $stateName points to wrong state")
                } else {
                    // Should return self when no elseNext
                    val targetState = state.handle('x')
                    assertSame(state, targetState,
                        "State $stateName should return self when elseNext is null")
                }
            })

            tests
        }
    }

    // --- Test 3: State machine behavior verification

    @Test
    fun `fromList creates functional state machine - accepts (ab)* pattern`() {
        val initialState = StateFactory.fromList(testStateObjects)
        val automaton = Automaton(initialState)

        // Test cases: valid (ab)* strings (should accept)
        val acceptCases = listOf(
            "",           // empty string (zero pairs)
            "ab",         // one pair
            "abab",       // two pairs
            "ababab",     // three pairs
            "abababab"    // four pairs
        )

        acceptCases.forEach { input ->
            val result = automaton.accepts(input)
            assertTrue(result, "Should accept '$input' (valid (ab)* pattern)")
        }

        // Test cases: invalid strings (should reject)
        val rejectCases = listOf(
            "a",          // incomplete pair
            "ba",         // wrong order
            "aba",        // incomplete last pair
            "aab",        // double a
            "abb",        // double b
            "abc",        // invalid character
            "abx"         // invalid character at end
        )

        rejectCases.forEach { input ->
            val result = automaton.accepts(input)
            assertFalse(result, "Should reject '$input' (invalid pattern)")
        }
    }

    // --- Test 4: Circular reference handling

    @Test
    fun `fromList handles circular references correctly`() {
        val initialState = StateFactory.fromList(testStateObjects)

        // Q0 -'a'-> Q1 -'b'-> Q0 (circular reference)
        val q0 = initialState
        val q1 = q0.handle('a')
        val backToQ0 = q1.handle('b')

        // Verify we get back to the same instance (reference equality)
        assertSame(q0, backToQ0, "Circular reference should return to same Q0 instance")
        assertEquals("Q0", getStateName(backToQ0))
    }

    // --- Test 5: Returns correct initial state

    @Test
    fun `fromList returns first state in list as initial state`() {
        val initialState = StateFactory.fromList(testStateObjects)

        assertEquals("Q0", getStateName(initialState),
            "Should return first state (Q0) as initial state")
        assertTrue(initialState.isAccepting,
            "Initial state Q0 should be accepting")
    }

    // --- Test 6: Error/trap state behavior

    @Test
    fun `error state is final and traps all input`() {
        val initialState = StateFactory.fromList(testStateObjects)

        // Navigate to Q2 (error state)
        val q2 = initialState.handle('x')  // triggers elseNext to Q2
        assertEquals("Q2", getStateName(q2))
        assertFalse(q2.isAccepting)

        // Q2 has null elseNext and no transitions - should trap forever
        val staysInQ2_a = q2.handle('a')
        assertSame(q2, staysInQ2_a, "Error state should trap on 'a'")

        val staysInQ2_b = q2.handle('b')
        assertSame(q2, staysInQ2_b, "Error state should trap on 'b'")

        val staysInQ2_x = q2.handle('x')
        assertSame(q2, staysInQ2_x, "Error state should trap on any input")
    }

    // --- Test 7: Valid sequence followed by error

    @Test
    fun `valid sequence followed by invalid input enters error state`() {
        val initialState = StateFactory.fromList(testStateObjects)
        val automaton = Automaton(initialState)

        // Process valid prefix "abab"
        var current = initialState
        current = current.handle('a')  // Q0 -> Q1
        current = current.handle('b')  // Q1 -> Q0
        current = current.handle('a')  // Q0 -> Q1
        current = current.handle('b')  // Q1 -> Q0

        assertTrue(current.isAccepting, "Should be in accepting state after 'abab'")
        assertEquals("Q0", getStateName(current))

        // Now send invalid input
        current = current.handle('b')  // Q0 -'b'-> Q2 (via elseNext)
        assertEquals("Q2", getStateName(current))
        assertFalse(current.isAccepting, "Should be in non-accepting error state")

        // Can't escape error state
        current = current.handle('a')
        assertEquals("Q2", getStateName(current))
    }
}