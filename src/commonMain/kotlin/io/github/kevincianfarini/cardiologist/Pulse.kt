package io.github.kevincianfarini.cardiologist

import kotlinx.datetime.Instant

/**
 * A [Pulse] is a marker class that helps limit the namespace of collecting pulse flows.
 */
public class Pulse internal constructor(internal val instant: Instant) {
    override fun equals(other: Any?): Boolean = other is Pulse && instant == other.instant
    override fun hashCode(): Int = instant.hashCode()
    override fun toString(): String = "Pulse($instant)"
}