package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    /**
     * Converts paisa (Long) to formatted Taka string e.g. "৳500" or "৳1,250"
     */
    fun formatPaisaToTaka(paisa: Long, includeSymbol: Boolean = true): String {
        val taka = paisa / 100.0
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = if (paisa % 100L == 0L) 0 else 2
            minimumFractionDigits = 0
        }
        val formattedNum = formatter.format(taka)
        return if (includeSymbol) "৳$formattedNum" else formattedNum
    }

    /**
     * Parse entered text string (in Taka) to paisa (Long)
     */
    fun takaToPaisa(takaString: String): Long {
        val clean = takaString.filter { it.isDigit() || it == '.' }
        if (clean.isEmpty()) return 0L
        val value = clean.toDoubleOrNull() ?: 0.0
        return (value * 100).toLong()
    }

    /**
     * Converts English numbers to Bengali numerals if needed
     */
    fun toBengaliNumerals(numberString: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in numberString) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
