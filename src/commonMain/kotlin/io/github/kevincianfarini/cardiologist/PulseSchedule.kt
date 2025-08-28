package io.github.kevincianfarini.cardiologist

import dev.drewhamilton.poko.Poko
import io.github.kevincianfarini.cardiologist.impl.parseCronExpression
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

/**
 * A [Pulse] schedule that can be used with [schedulePulse] to define complex schedules.
 */
@Poko
public class PulseSchedule internal constructor(
    public val atSeconds: List<Int>,
    public val atMinutes: List<Int>,
    public val atHours: List<Int>,
    public val onDaysOfMonth: List<Int>,
    public val inMonths: List<Month>,
    public val onDaysOfWeek: List<DayOfWeek>,
) {

    /**
     * Creates a new PulseSchedule. Second values must be in range 0..59, minute values must be in range 0..59,
     * hour values must be in range 0..23, and day of month values must be in range 1..31. This constructor also
     * requires that seconds, minutes, hours, days of month, and months cannot be empty sets.
     *
     * @throws IllegalArgumentException if the constructor is called with any of our bounds value or an improperly empty
     *                                  set.
     */
    public constructor(
        atSeconds: Set<Int>,
        atMinutes: Set<Int>,
        atHours: Set<Int>,
        onDaysOfMonth: Set<Int>,
        inMonths: Set<Month>,
        onDaysOfWeek: Set<DayOfWeek>,
    ) : this(
        atSeconds = atSeconds.sorted(),
        atMinutes = atMinutes.sorted(),
        atHours = atHours.sorted(),
        onDaysOfMonth = onDaysOfMonth.sorted(),
        inMonths = inMonths.sorted(),
        onDaysOfWeek = onDaysOfWeek.sorted()
    )

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

    public companion object {
        /**
         * Parse a cron [expression] into a [PulseSchedule].
         *
         * This function supports the standard cron specification, which includes:
         *
         * - Five fields specifying the minute, hour, day of month, month, and day of week.
         * - `*` denoting a wildcard value.
         * - List of values using commas, like `1,5,7`.
         * - Ranges of values, like `5-10`.
         * - Mixed range and list values, like `5-10,20-25`.
         * - The integers 0-59 for the minute field.
         * - The integers 0-23 for the hour field.
         * - The integers 1-31 for the day of week field.
         * - The integers 1-12 for the month field.
         * - The integers 0-6 or strings SUN-SAT for the day of week field.
         *
         * Functionality not articulated above is considered non-standard and is therefore explicitly not supported.
         *
         * @throws IllegalArgumentException Malformed cron expression or unsupported cron feature.
         */
        public fun parseCron(expression: String): PulseSchedule {
            return expression.parseCronExpression()
        }
    }
}

public class PulseScheduleBuilder internal constructor() {

    private var _atSeconds: Set<Int>? = null
    private var _atMinutes: Set<Int>? = null
    private var _atHours: Set<Int> ? = null
    private var _onDaysOfMonth: Set<Int>? = null
    private var _inMonths: Set<Month>? = null
    private var _onDaysOfWeek: Set<DayOfWeek>? = null

    /**
     * Add one or more second values to this schedule.
     *
     * Valid values range from 0..59. Attempting to build a [PulseSchedule] with an out of range value will throw
     * an [IllegalArgumentException].
     */
    public fun atSeconds(value: Int, vararg values: Int) {
        _atSeconds = buildSet {
            _atSeconds?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    /**
     * Add one or more second ranges to this schedule.
     *
     * Valid values range from 0..59. Attempting to build a [PulseSchedule] with an out of range value will throw
     * an [IllegalArgumentException].
     */
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
    /**
     * Add one or more minute values to this schedule.
     *
     * Valid values range from 0..59. Attempting to build a [PulseSchedule] with an out of range value will throw
     * an [IllegalArgumentException].
     */
    public fun atMinutes(value: Int, vararg values: Int) {
        _atMinutes = buildSet {
            _atMinutes?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    /**
     * Add one or more minute ranges to this schedule.
     *
     * Valid values range from 0..59. Attempting to build a [PulseSchedule] with an out of range value will throw
     * an [IllegalArgumentException].
     */
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

    /**
     * Add one or more hour values to this schedule.
     *
     * Valid values range from 0..23. Attempting to build a [PulseSchedule] with an out of range value will throw
     * an [IllegalArgumentException].
     */
    public fun atHours(value: Int, vararg values: Int) {
        _atHours = buildSet {
            _atHours?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    /**
     * Add one or more hour ranges to this schedule.
     *
     * Valid values range from 0..23. Attempting to build a [PulseSchedule] with an out of range value will throw
     * an [IllegalArgumentException].
     */
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

    /**
     * Add one or more day of month values to this schedule.
     *
     * Valid values range from 1..31. Attempting to build a [PulseSchedule] with an out of range value will throw
     * an [IllegalArgumentException].
     */
    public fun onDaysOfMonth(value: Int, vararg values: Int) {
        _onDaysOfMonth = buildSet {
            _onDaysOfMonth?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    /**
     * Add one or more day of month ranges to this schedule.
     *
     * Valid values range from 1..31. Attempting to build a [PulseSchedule] with an out of range value will throw
     * an [IllegalArgumentException].
     */
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

    /**
     * Add one or more month values to this schedule.
     */
    public fun inMonths(value: Month, vararg values: Month) {
        _inMonths = buildSet {
            _inMonths?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    /**
     * Add one or more month ranges to this schedule.
     */
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

    /**
     * Add one or more day of week values to this schedule.
     */
    public fun onDaysOfWeek(value: DayOfWeek, vararg values: DayOfWeek) {
        _onDaysOfWeek = buildSet {
            _onDaysOfWeek?.let(this::addAll)
            add(value)
            values.forEach(this::add)
        }
    }

    /**
     * Add one or more day of week ranges to this schedule.
     */
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