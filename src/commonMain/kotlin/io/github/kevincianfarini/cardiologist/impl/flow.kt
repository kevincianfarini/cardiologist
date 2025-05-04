package io.github.kevincianfarini.cardiologist.impl

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Terminal flow operator that collects the given flow with a provided action. The crucial difference from collect is
 * that when the original flow emits a new value and the action block from the previous value has not yet completed,
 * then the newly emitted value gets skipped.
 *
 * It can be demonstrated by the following example:
 *
 * ```kt
 * flow {
 *     emit(1)
 *     delay(50)
 *     emit(2)
 * }.collectLatest { value ->
 *     println("Collecting $value")
 *     delay(100) // Emulate work
 *     println("$value collected")
 * }
 * ```
 *
 * which prints:
 *
 * ```
 * Collecting 1
 * 1 collected
 * ```
 */
internal suspend fun <T> Flow<T>.collectCurrent(action: suspend (value: T) -> Unit) = coroutineScope {
    var job: Job? = null
    collect { value ->
        if (job?.isActive != true) {
            job = launch { action(value) }
        }
    }
}