package io.github.kevincianfarini.cardiologist

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

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

    public fun atSeconds(value: IntRange, vararg values: IntRange) {
        require(!value.isEmpty()) { "Cannot add empty range of values: $value" }
        values.forEach { v ->
            require(!v.isEmpty()) { "Cannot add empty range of values: $v" }
        }
        _atSeconds = buildSet {
            _atSeconds?.let(this::addAll)
            addAll(value)
            values.forEach(this::addAll)
        }
    }

    public fun atMinutes(value: Int, vararg values: Int) {
        _atMinutes = buildSet {
            _atMinutes?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun atMinutes(value: IntRange, vararg values: IntRange) {
        require(!value.isEmpty()) { "Cannot add empty range of values: $value" }
        values.forEach { v ->
            require(!v.isEmpty()) { "Cannot add empty range of values: $v" }
        }
        _atMinutes = buildSet {
            _atMinutes?.let(this::addAll)
            addAll(value)
            values.forEach(this::addAll)
        }
    }

    public fun atHours(value: Int, vararg values: Int) {
        _atHours = buildSet {
            _atHours?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun atHours(value: IntRange, vararg values: IntRange) {
        require(!value.isEmpty()) { "Cannot add empty range of values: $value" }
        values.forEach { v ->
            require(!v.isEmpty()) { "Cannot add empty range of values: $v" }
        }
        _atHours = buildSet {
            _atHours?.let(this::addAll)
            addAll(value)
            values.forEach(this::addAll)
        }
    }

    public fun onDaysOfMonth(value: Int, vararg values: Int) {
        _onDaysOfMonth = buildSet {
            _onDaysOfMonth?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun onDaysOfMonth(value: IntRange, vararg values: IntRange) {
        require(!value.isEmpty()) { "Cannot add empty range of values: $value" }
        values.forEach { v ->
            require(!v.isEmpty()) { "Cannot add empty range of values: $v" }
        }
        _onDaysOfMonth = buildSet {
            _onDaysOfMonth?.let(this::addAll)
            addAll(value)
            values.forEach(this::addAll)
        }
    }

    public fun inMonths(value: Month, vararg values: Month) {
        _inMonths = buildSet {
            _inMonths?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun inMonths(value: ClosedRange<Month>, vararg values: ClosedRange<Month>) {
        require(!value.isEmpty()) { "Cannot add empty range of values: $value" }
        values.forEach { v ->
            require(!v.isEmpty()) { "Cannot add empty range of values: $v" }
        }
        _inMonths = buildSet {
            _inMonths?.let(this::addAll)
            addAllMonths(value)
            values.forEach(this::addAllMonths)
        }
    }

    public fun onDaysOfWeek(value: DayOfWeek, vararg values: DayOfWeek) {
        _onDaysOfWeek = buildSet {
            _onDaysOfWeek?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    public fun onDaysOfWeek(value: ClosedRange<DayOfWeek>, vararg values: ClosedRange<DayOfWeek>) {
        require(!value.isEmpty()) { "Cannot add empty range of values: $value" }
        values.forEach { v ->
            require(!v.isEmpty()) { "Cannot add empty range of values: $v" }
        }
        _onDaysOfWeek = buildSet {
            _onDaysOfWeek?.let(this::addAll)
            addAllDaysOfWeek(value)
            values.forEach(this::addAllDaysOfWeek)
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

private fun MutableSet<Month>.addAllMonths(range: ClosedRange<Month>) {
    (range.start.number..range.endInclusive.number).forEach { monthNumber ->
        add(Month(monthNumber))
    }
}

private fun MutableSet<DayOfWeek>.addAllDaysOfWeek(range: ClosedRange<DayOfWeek>) {
    (range.start.isoDayNumber..range.endInclusive.isoDayNumber).forEach { isoDayNumber ->
        add(DayOfWeek(isoDayNumber))
    }
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
