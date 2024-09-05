package io.github.kevincianfarini.cardiologist

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.jvm.JvmInline

/**
 * A [Pulse] is a cadence which informs consumers when to execute work by calling [Pulse.beat].
 */
@JvmInline
public value class Pulse internal constructor(private val flow: Flow<Pair<Instant, Instant>>) {

    /**
     * Returns a pulse that beats [count] times.
     *
     * @throws IllegalArgumentException is count is not positive.
     */
    public fun take(count: Int): Pulse = Pulse(flow.take(count))

    /**
     * Returns a pulse that beats while [predicate] is satisfied.
     */
    public fun takeWhile(predicate: (Instant, Instant) -> Boolean): Pulse = Pulse(
        flow.takeWhile { (scheduled, occurred) -> predicate(scheduled, occurred) }
    )

    /**
     * Invoke [action] every time this Pulse is set to execute. [Action][action] provides two [instants][Instant]
     * denoting when the pulse was scheduled to occur, and when it actually occurred.
     *
     * This operator will execute [action] according to which [mode] is specified.
     */
    public suspend fun beat(
        mode: RecurringJobMode = RecurringJobMode.CancellingSequential,
        action: suspend (scheduled: Instant, occurred: Instant) -> Unit,
    ): Unit = when (mode) {
        RecurringJobMode.CancellingSequential -> flow.collectLatest { (scheduled, occurred) ->
            action(scheduled, occurred)
        }
        RecurringJobMode.Concurrent -> coroutineScope {
            flow.collect { (scheduled, occurred) ->
                launch { action(scheduled, occurred) }
            }
        }
    }
}