package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateHelper {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
    private val fullDateTimeFormat = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault())
    private val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    fun formatTime(epochMillis: Long): String {
        return timeFormat.format(Date(epochMillis))
    }

    fun formatDate(epochMillis: Long): String {
        return dateFormat.format(Date(epochMillis))
    }

    fun formatDayMonth(epochMillis: Long): String {
        return dayMonthFormat.format(Date(epochMillis))
    }

    fun formatFullDateTime(epochMillis: Long): String {
        return fullDateTimeFormat.format(Date(epochMillis))
    }

    fun formatRelativeBengali(epochMillis: Long): String {
        val nowCal = Calendar.getInstance()
        val targetCal = Calendar.getInstance().apply { timeInMillis = epochMillis }

        val isToday = nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            return "আজ, ${formatTime(epochMillis)}"
        }

        nowCal.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return "গতকাল, ${formatTime(epochMillis)}"
        }

        return formatFullDateTime(epochMillis)
    }

    fun getTodayStartAndEnd(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    fun getThisWeekStartAndEnd(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        // Set to start of week (Saturday in Bangladesh or Sunday/Monday)
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        val start = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    fun getThisMonthStartAndEnd(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    fun getLast30DaysStartAndEnd(): Pair<Long, Long> {
        val end = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        return Pair(start, end)
    }

    fun calculateNextDueDate(dayOfMonth: Int, frequency: String = "monthly"): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        when (frequency.lowercase()) {
            "daily" -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            "weekly" -> {
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
            "yearly" -> {
                cal.add(Calendar.YEAR, 1)
            }
            else -> { // monthly
                val currentDay = cal.get(Calendar.DAY_OF_MONTH)
                val maxDayThisMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val targetDay = dayOfMonth.coerceIn(1, maxDayThisMonth)

                if (currentDay < targetDay) {
                    cal.set(Calendar.DAY_OF_MONTH, targetDay)
                } else {
                    cal.add(Calendar.MONTH, 1)
                    val maxDayNextMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceIn(1, maxDayNextMonth))
                }
            }
        }
        return cal.timeInMillis
    }

    fun formatRecurringScheduleText(dayOfMonth: Int, frequency: String = "monthly"): String {
        return when (frequency.lowercase()) {
            "daily" -> "প্রতিদিন"
            "weekly" -> "প্রতি সপ্তাহে"
            "yearly" -> "প্রতি বছর"
            else -> "প্রতি মাসের ${CurrencyFormatter.toBengaliNumerals(dayOfMonth.toString())} তারিখ"
        }
    }
}
