package io.github.kevincianfarini.cardiologist.impl

import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateTimeTest {

    private val stubDatetime = LocalDateTime(
        year = 2023,
        monthNumber = 10,
        dayOfMonth = 4,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0,
    )

    @Test fun half_second_is_adjusted_up_to_second() = assertEquals(
        expected = stubDatetime.copy(second = 1),
        actual = stubDatetime.copy(nanosecond = 500).nextMatch(),
    )

    @Test fun half_second_is_adjusted_up_to_2_seconds() = assertEquals(
        expected = stubDatetime.copy(second = 2),
        actual = stubDatetime.copy(nanosecond = 500).nextMatch(atSeconds = 2..2),
    )

    @Test fun beginning_of_minute_adjusted_up_to_30_seconds() = assertEquals(
        expected = stubDatetime.copy(second = 30),
        actual = stubDatetime.copy(nanosecond = 500).nextMatch(atSeconds = 30..30),
    )

    @Test fun middle_of_minute_adjusted_up_to_minute() = assertEquals(
        expected = stubDatetime.copy(minute = 1),
        actual = stubDatetime.copy(second = 30).nextMatch(atSeconds = 0..0),
    )

    @Test fun beginning_of_minute_adjusted_up_to_minimum_of_range() = assertEquals(
        expected = stubDatetime.copy(second = 30),
        actual = stubDatetime.copy(nanosecond = 500).nextMatch(atSeconds = 30..59),
    )

    @Test fun beginning_of_hour_adjusted_up_to_30_minutes() = assertEquals(
        expected = stubDatetime.copy(minute = 30),
        actual = stubDatetime.nextMatch(atMinutes = 30..30),
    )

    @Test fun beginning_of_hour_adjusted_up_to_30_minutes_of_range() = assertEquals(
        expected = stubDatetime.copy(minute = 30),
        actual = stubDatetime.nextMatch(atMinutes = 30..59),
    )

    @Test fun noon_adjusted_to_next_day() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 5),
        actual = stubDatetime.copy(hour = 12).nextMatch(atHours = 0..0),
    )

    @Test fun noon_october_31_adjusted_to_midnight_november_1() = assertEquals(
        expected = stubDatetime.copy(monthNumber = 11, dayOfMonth = 1),
        actual = stubDatetime.copy(dayOfMonth = 31, hour = 12).nextMatch(atHours = 0..0),
    )

    @Test fun noon_february_28_adjusted_to_midnight_march_1() = assertEquals(
        expected = stubDatetime.copy(monthNumber = 3, dayOfMonth = 1),
        actual = stubDatetime.copy(monthNumber = 2, dayOfMonth = 28, hour = 12).nextMatch(atHours = 0..0),
    )

    @Test fun noon_february_28_2024_adjusted_to_midnight_feb_29() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 2, dayOfMonth = 29),
        actual = stubDatetime.copy(year = 2024, monthNumber = 2, dayOfMonth = 28, hour = 12).nextMatch(atHours = 0..0),
    )

    @Test fun noon_december_31_2023_adjusted_to_midnight_jan_1_2024() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 1, dayOfMonth = 1),
        actual = stubDatetime.copy(year = 2023, monthNumber = 12, dayOfMonth = 31, hour = 12).nextMatch(atHours = 0..0),
    )

    @Test fun nanosecond_before_2024_cascades_all_fields() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 1, dayOfMonth = 1),
        actual = LocalDateTime(
            year = 2023,
            monthNumber = 12,
            dayOfMonth = 31,
            hour = 23,
            minute = 59,
            second = 59,
            nanosecond = 999_999_999,
        ).nextMatch(atSeconds = 0..0),
    )

    @Test fun matches_next_five_minute_interval_of_noon() = assertEquals(
        expected = stubDatetime.copy(hour = 12, minute = 5),
        actual = stubDatetime.nextMatch(atMinutes = 5..5, atHours = 12..23),
    )

    @Test fun matches_up_to_next_minute_complex() = assertEquals(
        expected = LocalDateTime(
            year = 2023,
            monthNumber = 10,
            dayOfMonth = 5,
            hour = 8,
            minute = 58,
            second = 0,
        ),
        actual = LocalDateTime(
            year = 2023,
            monthNumber = 10,
            dayOfMonth = 5,
            hour = 8,
            minute = 56,
            second = 17,
            nanosecond = 707401025,
        ).nextMatch(atMinutes = 58..59),
    )

    @Test fun matches_next_month() = assertEquals(
        expected = stubDatetime.copy(monthNumber = 11, dayOfMonth = 1),
        actual = stubDatetime.nextMatch(inMonths = Month.NOVEMBER..Month.NOVEMBER),
    )

    @Test fun matches_10_seconds() = assertEquals(
        expected = stubDatetime.copy(hour = 20, minute = 30, second = 10),
        actual = stubDatetime.copy(hour = 20, minute = 29, second = 36).nextMatch(atSeconds = 10..10)
    )

    @Test fun matches_10_seconds_through_45_seconds() = assertEquals(
        expected = stubDatetime.copy(hour = 20, minute = 36, second = 10, nanosecond = 0),
        actual = stubDatetime.copy(
            hour = 20,
            minute = 35,
            second = 45,
            nanosecond = 1,
        ).nextMatch(atSeconds = 10..45)
    )

    @Test fun matches_10_minutes_through_57_minutes() = assertEquals(
        expected = stubDatetime.copy(dayOfMonth = 5, hour = 0, minute = 10, second = 0),
        actual = stubDatetime.copy(
            hour = 23,
            minute = 57,
            second = 1,
        ).nextMatch(atMinutes = 10..57, atSeconds = 0..0)
    )

    @Test fun matches_september_2024_from_october_2023() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 9, dayOfMonth = 1),
        actual = stubDatetime.nextMatch(
            inMonths = Month.SEPTEMBER..Month.SEPTEMBER,
            onDaysOfMonth = 1..1,
            atHours = 0..0,
            atMinutes = 0..0,
            atSeconds = 0..0,
        )
    )

    @Test fun matches_september_2024_from_october_2023_any_hour() = assertEquals(
        expected = stubDatetime.copy(year = 2024, monthNumber = 9, dayOfMonth = 1),
        actual = stubDatetime.copy(dayOfMonth = 6, hour = 0, minute = 41, second = 7).nextMatch(
            inMonths = Month.SEPTEMBER..Month.SEPTEMBER,
            onDaysOfMonth = 1..1,
            atMinutes = 0..0,
            atSeconds = 0..0,
        )
    )

    @Test fun one_month_gap_for_two_years() {
        LocalDateTime(year = 2023, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(months = 1),
            atSeconds = 0..0,
            atMinutes = 0..0,
            atHours = 0..0,
            onDaysOfMonth = 1..1,
        ) { it.year < 2025 }
    }

    @Test fun one_month_gap_for_a_year() {
        LocalDateTime(year = 2023, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(months = 1),
            atSeconds = 0..0,
            atMinutes = 0..0,
            atHours = 0..0,
            onDaysOfMonth = 1..1,
        ) { it.year < 2024 }
    }

    @Test fun one_day_gap_for_a_year() {
        LocalDateTime(year = 2023, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(days = 1),
            atSeconds = 0..0,
            atMinutes = 0..0,
            atHours = 0..0,
        ) { it.year < 2024 }
    }

    @Test fun one_day_gap_for_two_years() {
        LocalDateTime(year = 2023, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(days = 1),
            atSeconds = 0..0,
            atMinutes = 0..0,
            atHours = 0..0,
        ) { it.year < 2025 }
    }

    @Test fun one_hour_gap_for_a_year() {
        LocalDateTime(year = 2023, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(hours = 1),
            atSeconds = 0..0,
            atMinutes = 0..0,
        ) { it.year < 2024 }
    }

    @Test fun one_minute_gap_for_a_year() {
        LocalDateTime(year = 2023, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(minutes = 1),
            atSeconds = 0..0,
        ) { it.year < 2024 }
    }

    @Test fun one_second_gap_for_a_year() {
        LocalDateTime(year = 2023, monthNumber = 1, dayOfMonth = 1, hour = 0, minute = 0).assertGap(
            assertPeriod = DateTimePeriod(seconds = 1),
        ) { it.year < 2024 }
    }
}

private fun LocalDateTime.assertGap(
    assertPeriod: DateTimePeriod,
    atSeconds: IntRange = 0..59,
    atMinutes: IntRange = 0..59,
    atHours: IntRange = 0..23,
    onDaysOfMonth: IntRange = 1..31,
    inMonths: ClosedRange<Month> = Month.JANUARY..Month.DECEMBER,
    takeWhile: (LocalDateTime) -> Boolean,
) = nextMatchSequence(atSeconds, atMinutes, atHours, onDaysOfMonth, inMonths)
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

private fun LocalDateTime.nextMatchSequence(
    atSeconds: IntRange = 0..59,
    atMinutes: IntRange = 0..59,
    atHours: IntRange = 0..23,
    onDaysOfMonth: IntRange = 1..31,
    inMonths: ClosedRange<Month> = Month.JANUARY..Month.DECEMBER,
): Sequence<LocalDateTime> = generateSequence(seed = this) { seed ->
    when (val next = seed.nextMatch(atSeconds, atMinutes, atHours, onDaysOfMonth, inMonths)) {
        seed -> seed.copy(nanosecond = 1).nextMatch(atSeconds, atMinutes, atHours, onDaysOfMonth, inMonths)
        else -> next
    }
}