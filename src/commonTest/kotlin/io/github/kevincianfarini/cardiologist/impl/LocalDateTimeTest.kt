package io.github.kevincianfarini.cardiologist.impl

import io.github.kevincianfarini.cardiologist.buildPulseSchedule
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class LocalDateTimeTest {

    @Test
    fun does_not_match_to_equivalent_value() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 1, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch()
    )

    @Test
    fun half_second_is_adjusted_up_to_second() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 1, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 500).nextMatch(),
    )

    @Test
    fun half_second_is_adjusted_up_to_2_seconds() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 2, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 500).nextMatch {
            atSeconds(2)
        },
    )

    @Test
    fun beginning_of_minute_adjusted_up_to_30_seconds() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 30, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 500).nextMatch {
            atSeconds(30)
        },
    )

    @Test
    fun middle_of_minute_adjusted_up_to_minute() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 1, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 30, 0).nextMatch {
            atSeconds(0)
        },
    )

    @Test
    fun beginning_of_minute_adjusted_up_to_minimum_of_range() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 30, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 500).nextMatch {
            atSeconds(30..59)
        },
    )

    @Test
    fun beginning_of_hour_adjusted_up_to_30_minutes() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 30, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            atMinutes(30)
        },
    )

    @Test
    fun beginning_of_hour_adjusted_up_to_30_minutes_of_range() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 30, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            atMinutes(30..59)
        },
    )

    @Test
    fun noon_adjusted_to_next_day() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 5, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 12, 0, 0, 0).nextMatch {
            atHours(0)
        },
    )

    @Test
    fun noon_october_31_adjusted_to_midnight_november_1() = assertEquals(
        expected = LocalDateTime(2023, Month.NOVEMBER, 1, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 31, 12, 0, 0, 0).nextMatch {
            atHours(0)
        },
    )

    @Test
    fun noon_february_28_adjusted_to_midnight_march_1() = assertEquals(
        expected = LocalDateTime(2023, Month.MARCH, 1, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.FEBRUARY, 28, 12, 0, 0, 0).nextMatch {
            atHours(0)
        },
    )

    @Test
    fun noon_february_28_2024_adjusted_to_midnight_feb_29() = assertEquals(
        expected = LocalDateTime(2024, Month.FEBRUARY, 29, 0, 0, 0, 0),
        actual = LocalDateTime(2024, Month.FEBRUARY, 28, 12, 0, 0, 0).nextMatch {
            atHours(0)
        },
    )

    @Test
    fun noon_december_31_2023_adjusted_to_midnight_jan_1_2024() = assertEquals(
        expected = LocalDateTime(2024, Month.JANUARY, 1, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.DECEMBER, 31, 12, 0, 0, 0).nextMatch {
            atHours(0)
        },
    )

    @Test
    fun nanosecond_before_2024_cascades_all_fields() = assertEquals(
        expected = LocalDateTime(2024, Month.JANUARY, 1, 0, 0, 0, 0),
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
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 12, 5, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
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
        expected = LocalDateTime(2023, Month.NOVEMBER, 1, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            inMonths(Month.NOVEMBER)
        },
    )

    @Test
    fun matches_10_seconds() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 20, 30, 10, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 20, 29, 35, 0).nextMatch {
            atSeconds(10)
        },
    )

    @Test
    fun matches_10_seconds_through_45_seconds() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 20, 36, 10, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 20, 35, 45, 1).nextMatch {
            atSeconds(10..45)
        },
    )

    @Test
    fun matches_10_minutes_through_57_minutes() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 5, 0, 10, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 23, 57, 1, 0).nextMatch {
            atSeconds(0)
            atMinutes(10..57)
        },
    )

    @Test
    fun matches_september_2024_from_october_2023() = assertEquals(
        expected = LocalDateTime(2024, Month.SEPTEMBER, 1, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            atSeconds(0)
            atMinutes(0)
            atHours(0)
            onDaysOfMonth(1)
            inMonths(Month.SEPTEMBER)
        },
    )

    @Test
    fun matches_september_2024_from_october_2023_any_hour() = assertEquals(
        expected = LocalDateTime(2024, Month.SEPTEMBER, 1, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 6, 0, 41, 7, 0).nextMatch {
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
    fun non_continuous_seconds_matches() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 5, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 2, 0).nextMatch {
            atSeconds(0, 5, 10)
        },
    )

    @Test
    fun non_continuous_unsorted_matches() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 5, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 2, 0).nextMatch {
            atSeconds(10, 0, 5)
        },
    )

    @Test
    fun non_continuous_minutes_matches() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 5, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 2, 0, 0).nextMatch {
            atMinutes(0, 5, 10)
        },
    )

    @Test
    fun non_continuous_minutes_unsorted_matches() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 5, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 2, 0, 0).nextMatch {
            atMinutes(10, 0, 5)
        },
    )

    @Test
    fun non_continuous_hours_matches() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 5, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 2, 0, 0, 0).nextMatch {
            atHours(0, 5, 10)
        },
    )

    @Test
    fun non_continuous_hours_unsorted_matches() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 5, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 2, 0, 0, 0).nextMatch {
            atHours(10, 5, 0)
        },
    )

    @Test
    fun non_continuous_days_of_month_matches() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 5, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 2, 0, 0, 0, 0).nextMatch {
            onDaysOfMonth(1, 5, 10)
        },
    )

    @Test
    fun non_continuous_days_of_month_unsorted_matches() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 5, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 2, 0, 0, 0, 0).nextMatch {
            onDaysOfMonth(10, 5, 1)
        },
    )

    @Test
    fun non_continuous_month_matches() = assertEquals(
        expected = LocalDateTime(year = 2023, month = 5, day = 1, hour = 0, minute = 0, second = 0),
        actual = LocalDateTime(2023, Month.FEBRUARY, 4, 0, 0, 0, 0).nextMatch {
            inMonths(Month(1), Month(5), Month(10))
        },
    )

    @Test
    fun non_continuous_month_unsorted_matches() = assertEquals(
        expected = LocalDateTime(year = 2023, month = 5, day = 1, hour = 0, minute = 0, second = 0),
        actual = LocalDateTime(2023, Month.FEBRUARY, 4, 0, 0, 0, 0).nextMatch {
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
        expected = LocalDateTime(2023, Month.OCTOBER, 4, 0, 1, 1, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 59, 1).nextMatch {
            atSeconds(1)
        },
    )

    @Test
    fun schedules_day_of_week_no_day_of_month() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 7, 0, 0, 0, 0), // Saturday.
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            // LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0) is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.SATURDAY)
        }
    )

    @Test
    fun schedules_day_of_week_multiple_values() = assertEquals(
        expected = LocalDateTime(year = 2023, month = 10, day = 5, hour = 0, minute = 0, second = 0), // Thursday.
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            // LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0) is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
    )

    @Test
    fun schedules_day_of_week_multiple_values_not_continuous() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 8, 0, 0, 0, 0), // Sunday.
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            // LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0) is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY)
        }
    )

    @Test
    fun schedules_day_of_week_day_of_month_before_day_of_week() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 5, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            // LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0) is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.SATURDAY)
            onDaysOfMonth(5)
        }
    )

    @Test
    fun schedules_day_of_week_day_of_month_after_next_day_of_week() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 7, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
            // LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0) is October 4th, 2023; a Wednesday.
            onDaysOfWeek(DayOfWeek.SATURDAY)
            onDaysOfMonth(8)
        }
    )

    @Test
    fun schedules_day_of_week_increments_month() = assertEquals(
        expected = LocalDateTime(2023, Month.NOVEMBER, 4, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 31, 0, 0, 0, 0).nextMatch {
            // October 31st, 2023 was a Tuesday.
            onDaysOfWeek(DayOfWeek.SATURDAY)
        }
    )

    @Test
    fun schedules_day_of_week_wraps_around_week() = assertEquals(
        expected = LocalDateTime(2023, Month.OCTOBER, 10, 0, 0, 0, 0),
        actual = LocalDateTime(2023, Month.OCTOBER, 4, 0, 0, 0, 0).nextMatch {
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

    @Test
    fun apscheduler_weekday_range() {
        val schedule = buildPulseSchedule {
            onDaysOfWeek(DayOfWeek.FRIDAY..DayOfWeek.SUNDAY)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
        assertEquals(
            actual = LocalDateTime(2020, 1, 1, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2020, 1, 3, 0, 0),
        )
        assertEquals(
            actual = LocalDateTime(2020, 1, 3, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2020, 1, 4, 0, 0),
        )
        assertEquals(
            actual = LocalDateTime(2020, 1, 4, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2020, 1, 5, 0, 0),
        )
    }

    @Test
    fun apscheduler_month_rollover() {
        val schedule = buildPulseSchedule {
            onDaysOfMonth(30)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
        assertEquals(
            actual = LocalDateTime(2016, 2, 1, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2016, 3, 30, 0, 0),
        )
        assertEquals(
            actual = LocalDateTime(2016, 3, 30, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2016, 4, 30, 0, 0),
        )
    }

    @Test
    fun apscheduler_increment_weekday() {
        val schedule = buildPulseSchedule {
            atHours(5, 6)
            atMinutes(0)
            atSeconds(0)
        }
        assertEquals(
            actual = LocalDateTime(2009, 9, 25, 7, 0).nextMatch(schedule),
            expected = LocalDateTime(2009, 9, 26, 5, 0),
        )
    }

    @Test
    fun apscheduler_cron_schedule_1() {
        val schedule = buildPulseSchedule {
            inMonths(Month.JANUARY, Month.APRIL)
            onDaysOfMonth(5, 6)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
        assertEquals(
            actual = LocalDateTime(2008, 12, 1, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2009, 1, 5, 0, 0),
        )
        assertEquals(
            actual = LocalDateTime(2009, 1, 5, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2009, 1, 6, 0, 0),
        )
        assertEquals(
            actual = LocalDateTime(2009, 1, 6, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2009, 4, 5, 0, 0),
        )
        assertEquals(
            actual = LocalDateTime(2009, 4, 5, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2009, 4, 6, 0, 0),
        )
        assertEquals(
            actual = LocalDateTime(2009, 4, 6, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2010, 1, 5, 0, 0),
        )
    }

    @Test
    fun apscheduler_cron_trigger_2() {
        val schedule = buildPulseSchedule {
            inMonths(Month.JANUARY..Month.MARCH)
            onDaysOfMonth(5)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
        assertEquals(
            actual = LocalDateTime(2009, 10, 14, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2010, 1, 5, 0, 0)
        )
        assertEquals(
            actual = LocalDateTime(2010, 1, 5, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2010, 2, 5, 0, 0)
        )
        assertEquals(
            actual = LocalDateTime(2010, 2, 5, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2010, 3, 5, 0, 0)
        )
        assertEquals(
            actual = LocalDateTime(2010, 3, 5, 0, 0).nextMatch(schedule),
            expected = LocalDateTime(2011, 1, 5, 0, 0)
        )
    }

    @Test
    fun next_hour_february_rollover_to_march() = assertEquals(
        expected = LocalDateTime(2025, 3, 1, 0, 0),
        actual = LocalDateTime(2025, 2, 28, 23, 0).nextMatch {
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
    )

    @Test
    fun month_increment() = assertEquals(
        expected = LocalDateTime(2025, 12, 1, 0, 0),
        actual = LocalDateTime(2025, 10, 10, 23, 0).nextMatch {
            inMonths(Month.DECEMBER)
            onDaysOfMonth(1)
            atHours(0)
            atMinutes(0)
            atSeconds(0)
        }
    )
}