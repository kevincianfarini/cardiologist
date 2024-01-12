package io.github.kevincianfarini.cardiologist

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.jvm.JvmInline

/**
 * A [Pulse] is a marker class that helps limit the namespace of collecting pulse flows.
 */
@JvmInline
public value class Pulse internal constructor(internal val flow: Flow<Instant>) {

    /**
     * Reinvoke [action] every time this Pulse is set to execute.
     * This operator will execute [action] according to which [mode] is specified.
     *
     * ```kt
     * clock.intervalPulse(5.seconds).beat { pulseInstant ->
     *   println(pulseInstant.toString())
     * }
     * ```
     */
    public suspend fun beat(
        mode: RecurringJobMode = RecurringJobMode.CancellingSequential,
        action: suspend (Instant) -> Unit,
    ): Unit = when (mode) {
        RecurringJobMode.CancellingSequential -> flow.collectLatest(action)
        RecurringJobMode.Concurrent -> coroutineScope {
            flow.collect { instant ->
                launch(context = CoroutineName(instant.toString())) {
                    action(instant)
                }
            }
        }
        RecurringJobMode.DelayBetweenSequential -> flow.collect(action)
    }
}