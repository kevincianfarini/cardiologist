package io.github.kevincianfarini.cardiologist

import dev.drewhamilton.poko.Poko
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

/**
 * A [Pulse] schedule that can be used with [schedulePulse] to define complex schedules.
 *
 * @constructor Creates a new PulseSchedule. Second values must be in range 0..59, minute values must be in range 0..59
 *              hour values must be in range 0..23, and day of month values must be in range 1..31. This constructor also
 *              requires that seconds, minutes, hours, days of month, and months cannot be empty sets.
 * @throws IllegalArgumentException if the constructor is called with any of our bounds value or an improperly empty set.
 */
@Poko
public class PulseSchedule(
    public val atSeconds: Set<Int>,
    public val atMinutes: Set<Int>,
    public val atHours: Set<Int>,
    public val onDaysOfMonth: Set<Int>,
    public val inMonths: Set<Month>,
    public val onDaysOfWeek: Set<DayOfWeek>,
) {
    init {
        require(!atSeconds.any { it !in 0..59 }) { "Seconds has an out of bound value: $atSeconds" }
        require(atSeconds.isNotEmpty()) { "Seconds cannot be empty!" }
        require(!atMinutes.any { it !in 0..59 }) { "Minutes has an out of bound value: $atMinutes" }
        require(atMinutes.isNotEmpty()) { "Minutes cannot be empty!" }
        require(!atHours.any { it !in 0..23 }) { "Hours has an out of bound value: $atHours" }
        require(atHours.isNotEmpty()) { "Hours cannot be empty!" }
        require(!onDaysOfMonth.any { it !in 1..31 }) { "Days of month has an out of bound value: $onDaysOfMonth" }
        require(onDaysOfMonth.isNotEmpty()) { "Days of month cannot be empty!" }
        require(inMonths.isNotEmpty()) { "Months cannot be empty!" }
        // Don't check if onDaysOfWeek is empty because an empty set is equivalent to the wildcard `*` value in cron
        // expressions.
    }
}

public class PulseScheduleBuilder internal constructor() {

    private var _atSeconds: Set<Int>? = null
    private var _atMinutes: Set<Int>? = null
    private var _atHours: Set<Int> ? = null
    private var _onDaysOfMonth: Set<Int>? = null
    private var _inMonths: Set<Month>? = null
    private var _onDaysOfWeek: Set<DayOfWeek>? = null

    public fun atSeconds(value: Int, vararg values: Int) {
        _atSeconds = buildSet {
            _atSeconds?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun atMinutes(value: Int, vararg values: Int) {
        _atMinutes = buildSet {
            _atMinutes?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun atHours(value: Int, vararg values: Int) {
        _atHours = buildSet {
            _atHours?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun onDaysOfMonth(value: Int, vararg values: Int) {
        _onDaysOfMonth = buildSet {
            _onDaysOfMonth?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun inMonths(value: Month, vararg values: Month) {
        _inMonths = buildSet {
            _inMonths?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun onDaysOfWeek(value: DayOfWeek, vararg values: DayOfWeek) {
        _onDaysOfWeek = buildSet {
            _onDaysOfWeek?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    internal fun build(): PulseSchedule = PulseSchedule(
        atSeconds = _atSeconds ?: (0..59).toSet(),
        atMinutes = _atMinutes ?: (0..59).toSet(),
        atHours = _atHours ?: (0..23).toSet(),
        onDaysOfMonth = _onDaysOfMonth ?: (1..31).toSet(),
        inMonths = _inMonths ?: Month.entries.toSet(),
        onDaysOfWeek = _onDaysOfWeek ?: emptySet(),
    )
}

/**
 * Build a complex [PulseSchedule] with a DSL.
 *
 * For example, the following code will build a [schedule][PulseSchedule] that, when used with [schedulePulse], will
 * beat once per minute at the 0th second on Mondays and Fridays.
 *
 * ```kt
 * val schedule = buildPulseSchedule {
 *   atSeconds(0)
 *   onDaysOfWeek(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
 * }
 * ```
 *
 * Multiple successive calls to the same function on [PulseScheduleBuilder] are additive. This is useful for building
 * schedules imperatively. For example, the following will create a schedule that, when used with [schedulePulse], will
 * beat on three random seconds of each minute.
 *
 * ```kt
 * val schedule = buildPulseSchedule {
 *   val random = Random.Default
 *   repeat(3) {
 *     atSeconds(random.nextInt(0..59))
 *   }
 * }
 * ```
 *
 * The valid values for each function are:
 *
 * - `atSeconds`: 0..59
 * - `atMinutes`: 0..59
 * - `atHours`: 0..23
 * - `onDaysOfMonth`: 1..31
 *
 * @throws IllegalArgumentException if the [builder] lambda completes with any values outside the above range.
 */
public fun buildPulseSchedule(builder: PulseScheduleBuilder.() -> Unit): PulseSchedule {
    return PulseScheduleBuilder().apply(builder).build()
}