package io.github.kevincianfarini.cardiologist

import io.github.kevincianfarini.cardiologist.impl.collectCurrent
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
     * This operator will execute [action] according to which [strategy] is specified.
     */
    public suspend fun beat(
        strategy: PulseBackpressureStrategy = PulseBackpressureStrategy.ExecuteConcurrently,
        action: suspend (scheduled: Instant, occurred: Instant) -> Unit,
    ): Unit = when (strategy) {
        PulseBackpressureStrategy.CancelPrevious -> flow.collectLatest { (scheduled, occurred) ->
            action(scheduled, occurred)
        }
        PulseBackpressureStrategy.ExecuteConcurrently -> coroutineScope {
            flow.collect { (scheduled, occurred) ->
                launch { action(scheduled, occurred) }
            }
        }
        PulseBackpressureStrategy.SkipNext -> flow.collectCurrent { (scheduled, occurred) ->
            action(scheduled, occurred)
        }
    }

    /**
     * Invoke [action] every time this Pulse is set to execute.
     *
     * This operator will execute [action] according to which [strategy] is specified.
     */
    public suspend fun beat(
        strategy: PulseBackpressureStrategy = PulseBackpressureStrategy.ExecuteConcurrently,
        action: suspend () -> Unit,
    ): Unit = beat(strategy) { _, _, -> action }
}