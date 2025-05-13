package com.example.moneychanger.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

object DateUtils {

    fun moveToPreviousMonth(currentYear: Int, currentMonth: Int): Pair<Int, Int> {
        return if (currentMonth == 1) {
            Pair(currentYear - 1, 12)
        } else {
            Pair(currentYear, currentMonth - 1)
        }
    }

    fun moveToNextMonth(currentYear: Int, currentMonth: Int): Pair<Int, Int> {
        return if (currentMonth == 12) {
            Pair(currentYear + 1, 1)
        } else {
            Pair(currentYear, currentMonth + 1)
        }
    }

    fun getWeekOfMonth(date: LocalDate): Int {
        val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
        return date.get(weekFields.weekOfMonth())
    }

    fun getWeekRange(year: Int, month: Int, week: Int): Pair<LocalDate, LocalDate> {
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
        val firstWeekStart = firstDayOfMonth.with(weekFields.dayOfWeek(), 1)
        val start = firstWeekStart.plusWeeks((week - 1).toLong())
        val end = start.plusDays(6)
        return start to end
    }

    fun generateDateList(year: Int, month: Int): List<LocalDate?> {
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7  // 일요일 = 0

        val dates = mutableListOf<LocalDate?>()

        repeat(dayOfWeek) { dates.add(null) }

        for (day in 1..daysInMonth) {
            dates.add(LocalDate.of(year, month, day))
        }

        while (dates.size % 7 != 0) {
            dates.add(null)
        }

        return dates
    }
}
