package io.github.kevincianfarini.cardiologist

public enum class PulseBackpressureStrategy {

    /**
     * Schedules recurring jobs concurrently. That is if job `n` is still active when job
     * `n + 1` should begin, then job `n` continues to run concurrently alongside job `n + 1`.
     */
    ExecuteConcurrently,

    /**
     * Schedules recurring jobs sequentially by cancelling an unfinished job if a new one
     * should begin executing.
     */
    CancelPrevious,

    /**
     * Schedules recurring jobs sequentially by skipping a new job if an unfinished job
     * is still executing.
     */
    SkipNext,
}
