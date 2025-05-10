package io.github.kevincianfarini.cardiologist

import kotlin.jvm.JvmInline

@JvmInline
public value class PulseBackpressureStrategy private constructor(private val value: Byte) {
    public companion object {
        /**
         * Schedules recurring jobs concurrently. That is if job `n` is still active when job
         * `n + 1` should begin, then job `n` continues to run concurrently alongside job `n + 1`.
         */
        public val ExecuteConcurrently: PulseBackpressureStrategy get() = PulseBackpressureStrategy(0)

        /**
         * Schedules recurring jobs sequentially by cancelling an unfinished job if a new one
         * should begin executing.
         */
        public val CancelPrevious: PulseBackpressureStrategy get() = PulseBackpressureStrategy(1)


        /**
         * Schedules recurring jobs sequentially by skipping a new job if an unfinished job
         * is still executing.
         */
        public val SkipNext: PulseBackpressureStrategy get() = PulseBackpressureStrategy(2)
    }
}