package io.github.kevincianfarini.cardiologist

public enum class RecurringJobMode {
    /**
     * Schedules recurring jobs concurrently. That is if job `n` is still active when job
     * `n + 1` should begin, then job `n` continues to run concurrently alongside job `n + 1`.
     */
    Concurrent,
    /**
     * Schedules recurring jobs sequentially by cancelling an unfinished job if a new one
     * should begin executing.
     */
    CancellingSequential,
}
