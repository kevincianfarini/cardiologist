package tech.kraken.secretary.impl

import kotlinx.datetime.LocalDateTime
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
        ).nextMatch(atHours = 0..0),
    )

    @Test fun matches_next_five_minute_interval_of_noon() = assertEquals(
        expected = stubDatetime.copy(hour = 12, minute = 5),
        actual = stubDatetime.nextMatch(atMinutes = 5..5, atHours = 12..23),
    )
}