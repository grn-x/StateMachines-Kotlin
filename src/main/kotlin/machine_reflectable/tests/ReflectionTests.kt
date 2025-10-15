package machine_reflectable.tests
import machine_reflectable.shared.Automaton
import machine_reflectable.shared.State


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestInstance

/**
 * ReflectionTests
 *
 *
 *  Automaton.traverse(start, callback)
 *  Test 1: verify the content extracted by traverse against a fixed expected dataset
 *           (order-insensitive, subtests per state)
 *  Test 2: verify the order + occurrence of visited states
 *          (order-sensitive, subtests per position)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReflectionTests {


    private object Z0 : State(
        transitions = mapOf(
            'a' to { Z1 },
            'b' to { Z2 }
        ),
        elseNext = { Z0 },
        isAccepting = false
    )

    private object Z1 : State(
        transitions = mapOf(
            'a' to { Z3 },
            'b' to { Z2 }
        ),
        elseNext = { Z0 },
        isAccepting = false
    )

    private object Z2 : State(
        transitions = mapOf(
            'c' to { Z4 }
        ),
        elseNext = { Z0 },
        isAccepting = false
    )

    private object Z3 : State(
        transitions = emptyMap(),
        elseNext = { Z1 },
        isAccepting = true
    )

    private object Z4 : State(
        transitions = mapOf('a' to { Z4 }), // self loop on 'a'
        elseNext = { Z0 },
        isAccepting = true
    )

    private val automaton = Automaton(Z0)

    // --- Expected results

    private data class ExpectedState(
        val name: String,
        val isAccepting: Boolean,
        val transitions: Map<Char, String>,
        val elseTarget: String?
    )

    private val expectedMap: Map<String, ExpectedState> = mapOf(
        "Z0" to ExpectedState("Z0", false, mapOf('a' to "Z1", 'b' to "Z2"), "Z0"),
        "Z1" to ExpectedState("Z1", false, mapOf('a' to "Z3", 'b' to "Z2"), "Z0"),
        "Z2" to ExpectedState("Z2", false, mapOf('c' to "Z4"), "Z0"),
        "Z3" to ExpectedState("Z3", true, emptyMap(), "Z1"),
        "Z4" to ExpectedState("Z4", true, mapOf('a' to "Z4"), "Z0")
    )

    private fun namesOfTransitions(trans: Map<Char, State>): Map<Char, String> =
        trans.mapValues { it.value::class.simpleName ?: "UnnamedState" }

    private data class CapturedState(
        val name: String,
        val isAccepting: Boolean,
        val transitions: Map<Char, String>,
        val elseTarget: String?
    )

    // --- Test 1: content verification

    @TestFactory
    fun `traverse extracts expected state content (per-state subtests)`(): List<DynamicTest> {
        //automaton.printStateTree(maxCount=1);

        val captured = mutableMapOf<String, CapturedState>()

        automaton.traverse(Z0) { state, name, transitions, elseState ->
            val effectiveName = name.ifBlank { state::class.simpleName ?: "UnnamedState" }
            val transitionsByName = namesOfTransitions(transitions)
            val elseName = elseState?.let { it::class.simpleName ?: "UnnamedState" }

            captured[effectiveName] = CapturedState(
                name = effectiveName,
                isAccepting = state.isAccepting,
                transitions = transitionsByName,
                elseTarget = elseName
            )
        }

        // DynamicTest
        return expectedMap.keys.map { name ->
            DynamicTest.dynamicTest("State $name content matches expected") {
                val expected = expectedMap[name]
                val actual = captured[name]
                assertNotNull(actual, "State $name was not visited")
                actual!!

                assertEquals(expected!!.isAccepting, actual.isAccepting, "isAccepting mismatch for $name")
                assertEquals(expected.transitions, actual.transitions, "Transitions mismatch for $name")
                assertEquals(expected.elseTarget, actual.elseTarget, "elseNext mismatch for $name")
            }
        }
    }

    // --- Test 2: traversal order verification

    @TestFactory
    fun `traverse visits states in expected order (per-position subtests)`(): List<DynamicTest> {
        val visitedOrder = mutableListOf<String>()

        automaton.traverse(Z0) { state, name, _, _ ->
            val effectiveName = name.ifBlank { state::class.simpleName ?: "UnnamedState" }
            visitedOrder.add(effectiveName)
        }

        val expectedOrder = listOf("Z0", "Z1", "Z2", "Z3", "Z4")

        //DynamicTest
        val orderTests = expectedOrder.mapIndexed { index, expectedName ->
            DynamicTest.dynamicTest("Position $index should be $expectedName") {
                assertTrue(index < visitedOrder.size, "Visited list shorter than expected")
                val actual = visitedOrder[index]
                assertEquals(expectedName, actual, "State mismatch at position $index")
            }
        }

        val extraCountTest = DynamicTest.dynamicTest("No unexpected extra visited states") {
            assertEquals(expectedOrder.size, visitedOrder.size, "Visited order length mismatch")
        }

        return orderTests + extraCountTest
    }
}
