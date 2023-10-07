package tech.kraken.secretary

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

    /**
     * Schedules recurring jobs sequentially by ensuring delay periods are interleaved between
     * jobs.
     */
    DelayBetweenSequential,
}
