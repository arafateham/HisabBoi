package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PDFExporter {

    fun generateAndShareReport(
        context: Context,
        transactions: List<TransactionEntity>,
        loans: List<LoanEntity>,
        dateRangeTitle: String
    ) {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595 // Standard A4 width at 72dpi
            val pageHeight = 842 // Standard A4 height at 72dpi

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(235, 94, 60) // Primary Coral
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(40, 40, 40)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.rgb(60, 60, 60)
                textSize = 10f
                isAntiAlias = true
            }

            val incomePaint = Paint().apply {
                color = Color.rgb(46, 125, 50) // Green
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val expensePaint = Paint().apply {
                color = Color.rgb(211, 47, 47) // Red
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = Color.rgb(220, 220, 220)
                strokeWidth = 1f
            }

            var yPos = 40f

            // App Header
            canvas.drawText("হিসাববই — আর্থিক বিবরণী", 36f, yPos, titlePaint)
            yPos += 20f

            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = "রিপোর্ট সময়সীমা: $dateRangeTitle | তৈরি: ${sdf.format(Date())}"
            canvas.drawText(dateStr, 36f, yPos, textPaint)
            yPos += 24f

            // Divider
            canvas.drawLine(36f, yPos, pageWidth - 36f, yPos, linePaint)
            yPos += 20f

            // Summary Totals
            val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            val netBalance = totalIncome - totalExpense

            canvas.drawText("আর্থিক সারসংক্ষেপ:", 36f, yPos, headerPaint)
            yPos += 16f

            canvas.drawText("মোট আয়: ${CurrencyFormatter.formatPaisaToTaka(totalIncome)}", 36f, yPos, incomePaint)
            canvas.drawText("মোট খরচ: ${CurrencyFormatter.formatPaisaToTaka(totalExpense)}", 200f, yPos, expensePaint)
            canvas.drawText("অবশিষ্ট স্থিতি: ${CurrencyFormatter.formatPaisaToTaka(netBalance)}", 380f, yPos, headerPaint)
            yPos += 24f

            canvas.drawLine(36f, yPos, pageWidth - 36f, yPos, linePaint)
            yPos += 20f

            // Table Header
            canvas.drawText("তারিখ", 36f, yPos, headerPaint)
            canvas.drawText("বিবরণ / খাত", 120f, yPos, headerPaint)
            canvas.drawText("পদ্ধতি", 280f, yPos, headerPaint)
            canvas.drawText("ধরণ", 370f, yPos, headerPaint)
            canvas.drawText("পরিমাণ", 460f, yPos, headerPaint)
            yPos += 14f

            canvas.drawLine(36f, yPos, pageWidth - 36f, yPos, linePaint)
            yPos += 16f

            val itemDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

            // List first 35 transactions (to fit comfortably on single-page export)
            val displayList = transactions.take(35)
            for (tx in displayList) {
                if (yPos > pageHeight - 50f) break

                val dateLabel = itemDateFormat.format(Date(tx.date))
                val title = (tx.note ?: tx.sourceName ?: tx.category).take(22)
                val methodLabel = tx.method.uppercase()
                val isIncome = tx.type == "income"
                val typeLabel = if (isIncome) "আয়" else "খরচ"
                val amountStr = CurrencyFormatter.formatPaisaToTaka(tx.amount)

                canvas.drawText(dateLabel, 36f, yPos, textPaint)
                canvas.drawText(title, 120f, yPos, textPaint)
                canvas.drawText(methodLabel, 280f, yPos, textPaint)
                canvas.drawText(typeLabel, 370f, yPos, if (isIncome) incomePaint else expensePaint)
                canvas.drawText(amountStr, 460f, yPos, if (isIncome) incomePaint else expensePaint)

                yPos += 16f
            }

            if (transactions.size > 35) {
                yPos += 8f
                canvas.drawText("... এবং আরও ${transactions.size - 35} টি লেনদেন", 36f, yPos, textPaint)
            }

            // Footer
            val footerPaint = Paint().apply {
                color = Color.rgb(150, 150, 150)
                textSize = 8f
                isAntiAlias = true
            }
            canvas.drawText("হিসাববই অ্যাপ থেকে স্বয়ংক্রিয়ভাবে তৈরি", 36f, pageHeight - 20f, footerPaint)

            pdfDoc.finishPage(page)

            // Save PDF to cache
            val cacheFile = File(context.cacheDir, "hisabboi_report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(cacheFile)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()

            // Share via FileProvider
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "HisabBoi Financial Report")
                putExtra(Intent.EXTRA_TEXT, "হিসাববই অ্যাপ থেকে আমার আর্থিক স্টেটমেন্ট শেয়ার করছি।")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "রিপোর্ট শেয়ার করুন")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "পিডিএফ তৈরি করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
