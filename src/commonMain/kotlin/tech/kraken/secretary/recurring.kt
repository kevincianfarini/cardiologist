package tech.kraken.secretary

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

public enum class RecurringJobMode {

    /**
     * Schedules recurring jobs concurrently. That is if job `n` is still active when job
     * `n + 1` should begin, then job `n` continues to run concurrently with job `n + 1`.
     */
    Concurrent,

    /**
     * Schedules recurring jobs sequentially by cancelling an unfinished job if a new one
     * should begin executing.
     */
    CancellingSequential,

    /**
     * Schedules recurring jobs sequentially by ensuring delay periods are interleaved bwteen
     * jobs.
     */
    DelayBetweenSequential,
}

@OptIn(DelicateSecretaryApi::class)
public suspend fun Clock.repeat(
    interval: Duration,
    mode: RecurringJobMode = RecurringJobMode.CancellingSequential,
    block: suspend (Instant) -> Unit,
): Nothing = when (mode) {
    RecurringJobMode.Concurrent -> repeatConcurrent(interval, block)
    RecurringJobMode.CancellingSequential -> repeatCancelling(interval, block)
    RecurringJobMode.DelayBetweenSequential -> repeatDelayBetween(interval, block)
}

@DelicateSecretaryApi
private suspend fun Clock.repeatCancelling(
    interval: Duration,
    block: suspend (Instant) -> Unit,
): Nothing {
    pulse(interval).collectLatest(block)
    error("Impossible")
}

@DelicateSecretaryApi
private suspend fun Clock.repeatConcurrent(
    interval: Duration,
    block: suspend (Instant) -> Unit,
): Nothing {
    coroutineScope {
        pulse(interval).collect { instant ->
            launch { block(instant) }
        }
    }
    error("Impossible")
}

@DelicateSecretaryApi
private suspend fun Clock.repeatDelayBetween(
    interval: Duration,
    block: suspend (Instant) -> Unit,
): Nothing {
    pulse(interval).collect(block)
    error("Impossible")
}