package machine_reflectable.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import machine_reflectable.shared.tasks.task_III_ab
import machine_reflectable.shared.tasks.task_II_3a_or_3b
import machine_reflectable.shared.tasks.task_IV_a0b_mod3
import machine_reflectable.shared.tasks.task_I_bb
import java.util.stream.Collectors
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

import kotlin.reflect.KClass
import kotlin.reflect.KFunction0

@Composable
fun AutomatonApp() {
    var inputText by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf("Select Task") }
    var currentState by remember { mutableStateOf("State: None") }
    var highlightedIndex by remember { mutableStateOf(-1) }
    var result by remember { mutableStateOf("") }
    var automaton: Automaton? by remember { mutableStateOf(null) }
    var currentStepState: State? by remember { mutableStateOf(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val taskClasses = remember { loadTaskClasses() }

    MaterialTheme {
        Row(Modifier.fillMaxSize().padding(16.dp)) {

            // left panel
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Input String") }
                )

                Spacer(Modifier.height(8.dp))

                Button(onClick = { dropdownExpanded = true }) {
                    Text(selectedTask)
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    taskClasses.forEach { task ->
                        DropdownMenuItem(onClick = {
                            selectedTask = task.name
                            automaton = task.buildFunction.call()
                            currentStepState = automaton?.startState
                            currentState = buildStateDisplay(currentStepState)
                            highlightedIndex = -1
                            result = ""
                            dropdownExpanded = false
                        }) {
                            Text(task.name)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text("Selected Task: $selectedTask")

                Text(buildAnnotatedString {
                    currentStepState?.let { state ->
                        val color = if (state.isAccepting) Color.Green else Color.Red
                        withStyle(style = SpanStyle(color = color)) {
                            append(buildStateDisplay(state))
                        }
                    } ?: append(currentState)
                })

                Text("Result: $result")

                Spacer(Modifier.height(8.dp))

                Row {
                    Button(onClick = {
                        automaton?.let { autom ->
                            result = if (autom.accepts(inputText)) "Accepted" else "Rejected"
                            currentStepState = autom.startState
                            currentState = buildStateDisplay(currentStepState)
                            highlightedIndex = inputText.lastIndex
                        }
                    }) {
                        Text("Evaluate")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        automaton?.let { autom ->
                            currentStepState?.let { state ->
                                if (highlightedIndex < inputText.length - 1) {
                                    highlightedIndex++
                                    val currentChar = inputText[highlightedIndex]
                                    val nextState = state.handle(currentChar)
                                    currentStepState = nextState
                                    currentState = buildStateDisplay(nextState)
                                    if (highlightedIndex == inputText.lastIndex) {
                                        result = if (nextState.isAccepting) "Accepted" else "Rejected"
                                    }
                                } else {
                                    result = "End of Input"
                                }
                            }
                        }
                    }) {
                        Text("Step")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        inputText = ""
                        selectedTask = "Select Task"
                        currentState = "State: None"
                        highlightedIndex = -1
                        result = ""
                        automaton = null
                        currentStepState = null
                    }) {
                        Text("Reset")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(buildAnnotatedString {
                    inputText.forEachIndexed { index, char ->
                        if (index == highlightedIndex) {
                            withStyle(style = SpanStyle(color = Color.Red)) {
                                append(char)
                            }
                        } else append(char)
                    }
                })
            }

            // right panel
            Box(Modifier.weight(1f).fillMaxHeight()) {
                automaton?.let {
                    StateGraphView(it)
                } ?: Text(
                    "Load an automaton to visualize it.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun StateGraphView(automaton: Automaton) {
    // state collection by name // TODO: change this to a "layered pyramid like" list
    val states = remember(automaton) { collectStatesByName(automaton.startState) }.toList()
    val nodeRadius = 40f
    val vSpacing = 140f
    val hSpacing = 180f
    val textMeasurer = rememberTextMeasurer()

    // camera
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // wheel-based zoom and pan
    val zoomModifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val scroll = event.changes.firstOrNull()?.scrollDelta ?: Offset.Zero
                    val km = event.keyboardModifiers
                    if (scroll == Offset.Zero) continue

                    if (km.isCtrlPressed) {
                        // zoom around the center
                        val zoomFactor = if (scroll.y < 0f) 1.1f else 0.9f
                        scale = (scale * zoomFactor).coerceIn(0.2f, 5f)
                    } else {
                        // pan
                        offset += scroll * 1.5f
                    }
                }
            }
        }

    val positions = remember(states) { computeTreeLayout(states, automaton.startState, hSpacing, vSpacing) }

    Canvas(zoomModifier.padding(8.dp)) {
        withTransform({
            translate(offset.x, offset.y)
            scale(scale)
        }) {
            // transitions
            for ((state, start) in positions) {
                for ((_, nextFn) in state.transitions) {
                    val next = try { nextFn.invoke() } catch (_: Exception) { null }
                    if (next == null) continue

                    val target = positions.values.firstOrNull {
                        it.stateName == next.name
                    } ?: continue

                    if (state.name == next.name) {
                        // self-loop for self-references
                        drawArc(
                            color = Color(0xFFAA44FF),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = Offset(start.position.x - nodeRadius, start.position.y - nodeRadius * 2),
                            size = Size(nodeRadius * 2, nodeRadius * 2),
                            style = Stroke(2f)
                        )
                    } else {
                        drawLine(
                            color = Color.Gray,
                            start = start.position,
                            end = target.position,
                            strokeWidth = 2f
                        )
                    }
                }
            }

            // nodes
            for ((state, pos) in positions) {
                val fill = if (state.isAccepting) Color(0xFFA9F0A1) else Color(0xFFEFEFEF)
                drawCircle(fill, nodeRadius, pos.position)
                drawCircle(Color.Black, nodeRadius, pos.position, style = Stroke(2f))

                // label
                val label = buildAnnotatedString {
                    append(state.name)
                    append("\n")
                    append(if (state.isAccepting) "accept" else "reject")
                }
                drawText(
                    textMeasurer,
                    text = label,
                    topLeft = Offset(pos.position.x - nodeRadius, pos.position.y + nodeRadius + 6f),
                    style = TextStyle(fontSize = 12.sp, color = Color.Black)
                )
            }
        }
    }
}

// Canvas utilities

data class PositionedState(
    val state: State,
    val stateName: String,
    val position: Offset
)

// traverse by state.name something lambda different lambda references? TODO change
fun collectStatesByName(start: State?): Set<State> {
    if (start == null) return emptySet()
    val visited = mutableMapOf<String, State>()
    val queue = ArrayDeque<State>()
    queue.add(start)

    while (queue.isNotEmpty()) {
        val s = queue.removeFirst()
        if (visited.containsKey(s.name)) continue
        visited[s.name] = s

        for (fn in s.transitions.values) {
            try {
                val nxt = fn.invoke()
                if (nxt != null && !visited.containsKey(nxt.name)) {
                    queue.add(nxt)
                }
            } catch (_: Throwable) { }
        }
    }

    return visited.values.toSet()
}

// tree layout, top-down, centered horizontally
fun computeTreeLayout(states: List<State>, root: State?, hGap: Float, vGap: Float): Map<State, PositionedState> {
    if (root == null) return emptyMap()
    val byName = states.associateBy { it.name }

    val positions = mutableMapOf<State, PositionedState>()
    val levels = linkedMapOf<Int, MutableList<State>>()
    val visited = mutableSetOf<String>()

    val queue = ArrayDeque<Pair<State, Int>>()
    queue.add(root to 0)

    while (queue.isNotEmpty()) {
        val (s, depth) = queue.removeFirst()
        if (!visited.add(s.name)) continue
        levels.getOrPut(depth) { mutableListOf() }.add(s)
        for (fn in s.transitions.values) {
            try {
                val nxt = fn.invoke()
                if (nxt != null && nxt.name !in visited) queue.add(nxt to depth + 1)
            } catch (_: Throwable) {}
        }
    }

    for ((depth, nodes) in levels) {
        val totalWidth = (nodes.size - 1) * hGap
        nodes.forEachIndexed { i, n ->
            val x = i * hGap - totalWidth / 2
            val y = depth * vGap
            positions[n] = PositionedState(n, n.name, Offset(x, y))
        }
    }

    return positions
}

fun buildStateDisplay(state: State?): String {
    return if (state != null) {
        "State: ${state.name} | Accepting: ${state.isAccepting}"
    } else {
        "State: None"
    }
}

data class TaskReference(
    val clazz: KClass<*>,
    val buildFunction: KFunction0<Automaton>
) {
    val name: String = clazz.simpleName ?: "Unnamed"
    override fun toString(): String = name
}

fun loadTaskClasses(): List<TaskReference> {
    return listOf(
        TaskReference(
            task_I_bb::class,
            task_I_bb::r_buildBBAutomaton
        ),
        TaskReference(
            task_II_3a_or_3b::class,
            task_II_3a_or_3b::r_buildThreeAOrBInARowAutomaton
        ),
        TaskReference(
            task_III_ab::class,
            task_III_ab::r_buildAThenBAutomaton
        ),
        TaskReference(
            task_IV_a0b_mod3::class,
            task_IV_a0b_mod3::r_buildA0BMod3Automaton
        )
    )
}

fun createAutomaton(taskClassName: String): Automaton? {
    return try {
        val taskReference = loadTaskClasses().firstOrNull {
            it.clazz.simpleName == taskClassName
        } ?: return null
        taskReference.buildFunction.call() as Automaton
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        AutomatonApp()
    }
}
