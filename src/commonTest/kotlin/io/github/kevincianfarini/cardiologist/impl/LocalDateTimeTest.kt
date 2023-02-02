package io.github.kevincianfarini.cardiologist.impl

import io.github.kevincianfarini.cardiologist.PulseSchedule
import io.github.kevincianfarini.cardiologist.buildPulseSchedule
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class LocalDateTimeTest {

    private val stubDatetime = LocalDateTime(
        year = 2023,
        month = Month.OCTOBER,
        day = 4,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0,
    )

    @Test
    fun does_not_match_to_equivalent_value() = assertEquals(
        expected = stubDatetime.copy(second = 1),
        actual = stubDatetime.nextMatch()
    )

    @Test
    fun half_second_is_adjusted_up_to_second() = assertEquals(
        expected = stubDatetime.copy(second = 1),
        actual = stubDatetime.copy(nanosecond = 500).nextMatch(),
    )

    @Test
    fun half_second_is_adjusted_up_to_2_seconds() = assertEquals(
        expected = stubDatetime.copy(second = 2),
        actual = stubDatetime.copy(nanosecond = 500).nextMatch { atSeconds(2) },
    )

    @Test
    fun beginning_of_minute_adjusted_up_to_30_seconds() = assertEquals(
        expected = stubDatetime.copy(second = 30),
        actual = stubDatetime.copy(nanosecond = 500).nextMatch { atSeconds(30) },
    )

    @Test
    fun middle_of_minute_adjusted_up_to_minute() = assertEquals(
        expected = stubDatetime.copy(minute = 1),
        actual = stubDatetime.copy(second = 30).nextMatch { atSeconds(0) },
    )

    @Test
    fun beginning_of_minute_adjusted_up_to_minimum_of_range() = assertEquals(
        expected = stubDatetime.copy(second = 30),
        actual = stubDatetime.copy(nanosecond = 500).nextMatch { atSeconds(30..59) },
    )

    @Test
    fun beginning_of_hour_adjusted_up_to_30_minutes() = assertEquals(
        expected = stubDatetime.copy(minute = 30),
        actual = stubDatetime.nextMatch { atMinutes(30) },
    )

    @Test
    fun beginning_of_hour_adjusted_up_to_30_minutes_of_range() = assertEquals(
        expected = stubDatetime.copy(minute = 30),
        actual = stubDatetime.nextMatch { atMinutes(30..59) },
    )

    @Test
    fun noon_adjusted_to_next_day() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 5),
        actual = stubDatetime.copy(hour = 12).nextMatch { atHours(0) },
    )

    @Test
    fun noon_october_31_adjusted_to_midnight_november_1() = assertEquals(
        expected = stubDatetime.copy(monthNumber = 11, dayOfMonth = 1),
        actual = stubDatetime.copy(dayOfMonth = 31, hour = 12).nextMatch { atHours(0) },
    )

    @Test
    fun noon_february_28_adjusted_to_midnight_march_1() = assertEquals(
        expected = stubDatetime.copy(monthNumber = 3, dayOfMonth = 1),
        actual = stubDatetime.copy(monthNumber = 2, dayOfMonth = 28, hour = 12).nextMatch { atHours(0) },
    )

    @Test
    fun noon_february_28_2024_adjusted_to_midnight_feb_29() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 2, dayOfMonth = 29),
        actual = stubDatetime.copy(year = 2024, monthNumber = 2, dayOfMonth = 28, hour = 12).nextMatch { atHours(0) },
    )

    @Test
    fun noon_december_31_2023_adjusted_to_midnight_jan_1_2024() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 1, dayOfMonth = 1),
        actual = stubDatetime.copy(year = 2023, monthNumber = 12, dayOfMonth = 31, hour = 12).nextMatch { atHours(0) },
    )

    @Test
    fun nanosecond_before_2024_cascades_all_fields() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 1, dayOfMonth = 1),
        actual = LocalDateTime(
            year = 2023,
            month = Month.DECEMBER,
            day = 31,
            hour = 23,
            minute = 59,
            second = 59,
            nanosecond = 999_999_999,
        ).nextMatch { atSeconds(0) }
    )

    @Test
    fun matches_next_five_minute_interval_of_noon() = assertEquals(
        expected = stubDatetime.copy(hour = 12, minute = 5),
        actual = stubDatetime.nextMatch {
            atMinutes(5)
            atHours(12..23)
        },
    )

    @Test
    fun matches_up_to_next_minute_complex() = assertEquals(
        expected = LocalDateTime(
            year = 2023,
            month = Month.OCTOBER,
            day = 5,
            hour = 8,
            minute = 58,
            second = 0,
        ),
        actual = LocalDateTime(
            year = 2023,
            month = Month.OCTOBER,
            day = 5,
            hour = 8,
            minute = 56,
            second = 17,
            nanosecond = 707401025,
        ).nextMatch { atMinutes(58, 59) },
    )

    @Test
    fun matches_next_month() = assertEquals(
        expected = stubDatetime.copy(monthNumber = 11, dayOfMonth = 1),
        actual = stubDatetime.nextMatch { inMonths(Month.NOVEMBER) },
    )

    @Test
    fun matches_10_seconds() = assertEquals(
        expected = stubDatetime.copy(hour = 20, minute = 30, second = 10),
        actual = stubDatetime.copy(hour = 20, minute = 29, second = 36).nextMatch { atSeconds(10) },
    )

    @Test
    fun matches_10_seconds_through_45_seconds() = assertEquals(
        expected = stubDatetime.copy(hour = 20, minute = 36, second = 10, nanosecond = 0),
        actual = stubDatetime.copy(
            hour = 20,
            minute = 35,
            second = 45,
            nanosecond = 1,
        ).nextMatch { atSeconds(10..45) },
    )

    @Test
    fun matches_10_minutes_through_57_minutes() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 5, hour = 0, minute = 10, second = 0),
        actual = stubDatetime.copy(
            hour = 23,
            minute = 57,
            second = 1,
        ).nextMatch {
            atSeconds(0)
            atMinutes(10..57)
        },
    )

    @Test
    fun matches_september_2024_from_october_2023() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 9, dayOfMonth = 1),
        actual = stubDatetime.nextMatch {
            atSeconds(0)
            atMinutes(0)
            atHours(0)
            onDaysOfMonth(1)
            inMonths(Month.SEPTEMBER)
        },
    )

    @Test
    fun matches_september_2024_from_october_2023_any_hour() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 9, dayOfMonth = 1),
        actual = stubDatetime.copy(dayOfMonth = 6, hour = 0, minute = 41, second = 7).nextMatch {
            atSeconds(0)
            atMinutes(0)
            onDaysOfMonth(1)
            inMonths(Month.SEPTEMBER)
        },
    )

    @Test
    fun matches_jan_18_2024_to_jan_19_2024() = assertEquals(
        expected = LocalDateTime(year = 2024, month = Month.JANUARY, day = 19, hour = 0, minute = 0),
        actual = LocalDateTime(year = 2024, month = Month.JANUARY, day = 18, hour = 23, minute = 59, second = 48)
            .nextMatch {
                atSeconds(0)
                atMinutes(0)
                atHours(0)
            },
    )

    @Test
    fun one_month_gap_for_two_years() {
        LocalDateTime(year = 2023, month = Month.JANUARY, day = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(months = 1),
            schedule = buildPulseSchedule {
                atSeconds(0)
                atMinutes(0)
                atHours(0)
                onDaysOfMonth(1)
            },
        ) { it.year < 2025 }
    }

    @Test
    fun one_month_gap_for_a_year() {
        LocalDateTime(year = 2023, month = Month.JANUARY, day = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(months = 1),
            schedule = buildPulseSchedule {
                atSeconds(0)
                atMinutes(0)
                atHours(0)
                onDaysOfMonth(1)
            },
        ) { it.year < 2024 }
    }

    @Test
    fun one_day_gap_for_a_year() {
        LocalDateTime(year = 2023, month = Month.JANUARY, day = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(days = 1),
            schedule = buildPulseSchedule {
                atSeconds(0)
                atMinutes(0)
                atHours(0)
            },
        ) { it.year < 2024 }
    }

    @Test
    fun one_day_gap_for_two_years() {
        LocalDateTime(year = 2023, month = Month.JANUARY, day = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(days = 1),
            schedule = buildPulseSchedule {
                atSeconds(0)
                atMinutes(0)
                atHours(0)
            },
        ) { it.year < 2025 }
    }

    @Test
    fun one_hour_gap_for_a_year() {
        LocalDateTime(year = 2023, month = Month.JANUARY, day = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(hours = 1),
            schedule = buildPulseSchedule {
                atSeconds(0)
                atMinutes(0)
            },
        ) { it.year < 2024 }
    }

    @Test
    fun one_minute_gap_for_a_year() {
        LocalDateTime(year = 2023, month = Month.JANUARY, day = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(minutes = 1),
            schedule = buildPulseSchedule { atSeconds(0) },
        ) { it.year < 2024 }
    }

    @Test
    fun one_second_gap_for_a_year() {
        LocalDateTime(year = 2023, month = Month.JANUARY, day = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(seconds = 1),
        ) { it.year < 2024 }
    }

    @Test
    fun non_continuous_seconds_matches() = assertEquals(
        expected = stubDatetime.copy(second = 5),
        actual = stubDatetime.copy(second = 2).nextMatch {
            atSeconds(0, 5, 10)
        },
    )

    @Test
    fun non_continuous_unsorted_matches() = assertEquals(
        expected = stubDatetime.copy(second = 5),
        actual = stubDatetime.copy(second = 2).nextMatch {
            atSeconds(10, 0, 5)
        },
    )

    @Test
    fun non_continuous_minutes_matches() = assertEquals(
        expected = stubDatetime.copy(minute = 5),
        actual = stubDatetime.copy(minute = 2).nextMatch {
            atMinutes(0, 5, 10)
        },
    )

    @Test
    fun non_continuous_minutes_unsorted_matches() = assertEquals(
        expected = stubDatetime.copy(minute = 5),
        actual = stubDatetime.copy(minute = 2).nextMatch {
            atMinutes(10, 0, 5)
        },
    )

    @Test
    fun non_continuous_hours_matches() = assertEquals(
        expected = stubDatetime.copy(hour = 5),
        actual = stubDatetime.copy(hour = 2).nextMatch {
            atHours(0, 5, 10)
        },
    )

    @Test
    fun non_continuous_hours_unsorted_matches() = assertEquals(
        expected = stubDatetime.copy(hour = 5),
        actual = stubDatetime.copy(hour = 2).nextMatch {
            atHours(10, 5, 0)
        },
    )

    @Test
    fun non_continuous_days_of_month_matches() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 5),
        actual = stubDatetime.copy(dayOfMonth = 2).nextMatch {
            onDaysOfMonth(1, 5, 10)
        },
    )

    @Test
    fun non_continuous_days_of_month_unsorted_matches() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 5),
        actual = stubDatetime.copy(dayOfMonth = 2).nextMatch {
            onDaysOfMonth(10, 5, 1)
        },
    )

    @Test
    fun non_continuous_month_matches() = assertEquals(
        expected = LocalDateTime(year = 2023, month = 5, day = 1, hour = 0, minute = 0, second = 0),
        actual = stubDatetime.copy(monthNumber = 2).nextMatch {
            inMonths(Month(1), Month(5), Month(10))
        },
    )

    @Test
    fun non_continuous_month_unsorted_matches() = assertEquals(
        expected = LocalDateTime(year = 2023, month = 5, day = 1, hour = 0, minute = 0, second = 0),
        actual = stubDatetime.copy(monthNumber = 2).nextMatch {
            inMonths(Month(10), Month(5), Month(1))
        },
    )

    @Test
    fun non_continuous_match_complicated() = assertEquals(
        expected = LocalDateTime(
            year = 2023,
            month = 5,
            day = 1,
            hour = 0,
            minute = 0,
            second = 0,
            nanosecond = 0
        ),
        actual = LocalDateTime(
            year = 2023,
            month = 2,
            day = 2,
            hour = 2,
            minute = 2,
            second = 2,
            nanosecond = 0,
        ).nextMatch {
            inMonths(Month(10), Month(5), Month(1))
            onDaysOfMonth(10, 5, 1)
            atHours(10, 5, 0)
            atMinutes(10, 0, 5)
            atSeconds(10, 0, 5)
        },
    )

    @Test
    fun check_2023_10_04T00_00_59_000000001_next_match_is_correct() = assertEquals(
        expected = stubDatetime.copy(minute = 1, second = 1),
        actual = stubDatetime.copy(second = 59, nanosecond = 1).nextMatch {
            atSeconds(1)
        },
    )

    @Test
    fun schedules_day_of_week_no_day_of_month() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 7), // Saturday.
        actual = stubDatetime.nextMatch {
            // stubDatetime is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.SATURDAY)
        }
    )

    @Test
    fun schedules_day_of_week_multiple_values() = assertEquals(
        expected = LocalDateTime(year = 2023, month = 10, day = 5, hour = 0, minute = 0, second = 0), // Thursday.
        actual = stubDatetime.nextMatch {
            // stubDatetime is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
    )

    @Test
    fun schedules_day_of_week_multiple_values_not_continuous() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 8), // Sunday.
        actual = stubDatetime.nextMatch {
            // stubDatetime is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY)
        }
    )

    @Test
    fun schedules_day_of_week_day_of_month_before_day_of_week() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 5),
        actual = stubDatetime.nextMatch {
            // stubDatetime is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.SATURDAY)
            onDaysOfMonth(5)
        }
    )

    @Test
    fun schedules_day_of_week_day_of_month_after_next_day_of_week() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 7),
        actual = stubDatetime.nextMatch {
            // stubDatetime is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.SATURDAY)
            onDaysOfMonth(8)
        }
    )

    @Test
    fun schedules_day_of_week_increments_month() = assertEquals(
        expected = stubDatetime.copy(monthNumber = 11, dayOfMonth = 4),
        actual = stubDatetime.copy(dayOfMonth = 31).nextMatch {
            // October 31st, 2023 was a Tuesday.
            onDaysOfWeek(DayOfWeek.SATURDAY)
        }
    )

    @Test
    fun schedules_day_of_week_wraps_around_week() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 10),
        actual = stubDatetime.nextMatch {
            // October 4th, 2023 was a Wednesday.
            onDaysOfWeek(DayOfWeek.TUESDAY)
        }
    )

    @Test
    fun github_issue_151() = assertEquals(
        expected = LocalDateTime(2025, 7, 25, 13, 15, 0),
        actual = LocalDateTime(2025, 7, 25, 13, 14, 8, 89612504).nextMatch {
            atMinutes(0, 15, 30, 45)
            atSeconds(0)
        }
    )

    @Test
    fun next_hour_middle_of_multi_value_range_works() = assertEquals(
        expected = LocalDateTime(2025, 7, 25, 15, 0, 0, 0),
        actual = LocalDateTime(2025, 7, 25, 13, 14, 8, 89612504).nextMatch {
            atHours(12, 15, 17)
            atMinutes(0)
            atSeconds(0)
        }
    )

    @Test
    fun next_day_of_month_middle_of_multi_value_range_works() = assertEquals(
        expected = LocalDateTime(2025, 7, 26, 0, 0, 0, 0),
        actual = LocalDateTime(2025, 7, 25, 13, 14, 8, 89612504).nextMatch {
            onDaysOfMonth(24, 26, 29)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
    )

    @Test
    fun next_month_middle_of_multi_value_range_works() = assertEquals(
        expected = LocalDateTime(2025, 8, 1, 0, 0, 0, 0),
        actual = LocalDateTime(2025, 7, 25, 13, 14, 8, 89612504).nextMatch {
            inMonths(Month.JUNE, Month.AUGUST, Month.SEPTEMBER)
            onDaysOfMonth(1)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
    )

    @Test
    fun next_day_of_week_middle_of_multi_value_range_works() = assertEquals(
        expected = LocalDateTime(2025, 7, 26, 0, 0, 0, 0),
        actual = LocalDateTime(2025, 7, 25, 13, 14, 8, 89612504).nextMatch {
            onDaysOfWeek(DayOfWeek.THURSDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
    )
}

@ExperimentalTime
private fun LocalDateTime.assertGap(
    assertPeriod: DateTimePeriod,
    schedule: PulseSchedule = buildPulseSchedule {  },
    takeWhile: (LocalDateTime) -> Boolean,
) = nextMatchSequence(schedule)
    .takeWhile(takeWhile)
    .windowed(2) { (first, second) ->
        assertEquals(
            expected = assertPeriod,
            actual = first.toInstant(TimeZone.UTC).periodUntil(
                other = second.toInstant(TimeZone.UTC),
                timeZone = TimeZone.UTC
            ),
            message = "The difference between $first and $second is not $assertPeriod."
        )
    }.last()

private fun LocalDateTime.nextMatchSequence(schedule: PulseSchedule): Sequence<LocalDateTime> {
    return generateSequence(seed = this) { seed -> seed.nextMatch(schedule) }
}
