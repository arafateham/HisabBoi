package com.example.ui.screens.report

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.TransactionEntity
import com.example.util.Category
import com.example.util.CurrencyFormatter
import com.example.util.DateHelper
import java.io.File
import java.io.FileOutputStream

object PDFExporter {

    fun generateAndShareReport(
        context: Context,
        transactions: List<TransactionEntity>,
        loans: List<LoanEntity>,
        dateRangeTitle: String
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = AndroidColor.rgb(233, 69, 96) // Primary Coral
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = AndroidColor.rgb(100, 100, 120)
                textSize = 12f
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = AndroidColor.rgb(30, 30, 50)
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = AndroidColor.rgb(40, 40, 40)
                textSize = 11f
                isAntiAlias = true
            }

            val greenPaint = Paint().apply {
                color = AndroidColor.rgb(0, 163, 108)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val redPaint = Paint().apply {
                color = AndroidColor.rgb(255, 107, 107)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val linePaint = Paint().apply {
                color = AndroidColor.rgb(220, 220, 230)
                strokeWidth = 1f
            }

            var currentY = 50f

            // 1. Header
            canvas.drawText("HisabBoi (হিসাববই) - রিপোর্ট", 40f, currentY, titlePaint)
            currentY += 20f
            canvas.drawText("সময়কাল: $dateRangeTitle | তৈরি: ${DateHelper.formatDate(System.currentTimeMillis())}", 40f, currentY, subtitlePaint)
            currentY += 15f
            canvas.drawLine(40f, currentY, 555f, currentY, linePaint)
            currentY += 25f

            // 2. Summary
            val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            val net = totalIncome - totalExpense

            canvas.drawText("সারসংক্ষেপ", 40f, currentY, headerPaint)
            currentY += 20f

            canvas.drawText("মোট আয়: ${CurrencyFormatter.formatPaisaToTaka(totalIncome)}", 40f, currentY, greenPaint)
            canvas.drawText("মোট খরচ: ${CurrencyFormatter.formatPaisaToTaka(totalExpense)}", 200f, currentY, redPaint)
            canvas.drawText("সঞ্চয়/ব্যালেন্স: ${CurrencyFormatter.formatPaisaToTaka(net)}", 380f, currentY, headerPaint)
            currentY += 20f
            canvas.drawLine(40f, currentY, 555f, currentY, linePaint)
            currentY += 25f

            // 3. Category Breakdown
            canvas.drawText("ক্যাটাগরি ভিত্তিক খরচের বিবরণ", 40f, currentY, headerPaint)
            currentY += 18f

            val catSums = transactions.filter { it.type == "expense" }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { tx -> tx.amount } }
                .toList()
                .sortedByDescending { it.second }

            if (catSums.isEmpty()) {
                canvas.drawText("কোনো খরচ নেই", 40f, currentY, bodyPaint)
                currentY += 15f
            } else {
                catSums.take(5).forEach { (catKey, amt) ->
                    val cat = Category.fromKey(catKey)
                    val pct = if (totalExpense > 0) (amt.toFloat() / totalExpense.toFloat()) * 100f else 0f
                    canvas.drawText("• ${cat.label}: ${CurrencyFormatter.formatPaisaToTaka(amt)} (${String.format("%.1f", pct)}%)", 40f, currentY, bodyPaint)
                    currentY += 16f
                }
            }
            currentY += 10f
            canvas.drawLine(40f, currentY, 555f, currentY, linePaint)
            currentY += 25f

            // 4. Recent Transactions Table
            canvas.drawText("লেনদেনের তালিকা (সর্বশেষ এন্ট্রি)", 40f, currentY, headerPaint)
            currentY += 20f

            canvas.drawText("তারিখ", 40f, currentY, subtitlePaint)
            canvas.drawText("বিবরণ / খাত", 140f, currentY, subtitlePaint)
            canvas.drawText("মাধ্যম", 320f, currentY, subtitlePaint)
            canvas.drawText("পরিমাণ", 450f, currentY, subtitlePaint)
            currentY += 14f

            transactions.take(15).forEach { tx ->
                val isExp = tx.type == "expense"
                val cat = Category.fromKey(tx.category)
                val label = if (isExp) cat.label else (tx.sourceName ?: "আয়")
                val amtStr = "${if (isExp) "-" else "+"}${CurrencyFormatter.formatPaisaToTaka(tx.amount)}"

                canvas.drawText(DateHelper.formatDayMonth(tx.date), 40f, currentY, bodyPaint)
                canvas.drawText(label.take(20), 140f, currentY, bodyPaint)
                canvas.drawText(tx.method.uppercase(), 320f, currentY, bodyPaint)
                canvas.drawText(amtStr, 450f, currentY, if (isExp) redPaint else greenPaint)
                currentY += 16f
            }

            // 5. Loan Summary
            if (loans.isNotEmpty() && currentY < 750f) {
                currentY += 15f
                canvas.drawLine(40f, currentY, 555f, currentY, linePaint)
                currentY += 20f
                canvas.drawText("ধারের সারসংক্ষেপ", 40f, currentY, headerPaint)
                currentY += 18f
                loans.take(3).forEach { loan ->
                    val dir = if (loan.direction == "they_owe_me") "পাওনা" else "দেনা"
                    canvas.drawText("• ${loan.person} ($dir): বাকি ${CurrencyFormatter.formatPaisaToTaka(loan.remaining)}", 40f, currentY, bodyPaint)
                    currentY += 15f
                }
            }

            // Footer
            canvas.drawText("HisabBoi — তোমার হিসাব, তোমার হাতে", 200f, 810f, subtitlePaint)

            pdfDocument.finishPage(page)

            // Save to Cache / Output
            val file = File(context.cacheDir, "HisabBoi_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Open Share Sheet
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "HisabBoi রিপোর্ট")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "রিপোর্ট শেয়ার বা ডাউনলোড করুন"))
            Toast.makeText(context, "PDF রিপোর্ট তৈরি সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(context, "PDF তৈরি করতে ব্যর্থ: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
