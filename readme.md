# AB State machine

A Kotlin-based implementation of a state machine designed to simulate and validate string patterns.

It uses the State design pattern to process input strings and determine if they are accepted based on predefined rules. The project includes a generic `Automaton` class, abstract `State` class, and a simple Compose Desktop UI for interaction.

Using Kotlins Compose Multiplatform and Gradle for the UI and Building Process.

Roadmap:
- [ ] Load Rules from config files
  - [ ] Display rules in UI to visualize program behavior
- [ ] Micro optimize by prematurely ending invalid strings when stuck in invalid state
