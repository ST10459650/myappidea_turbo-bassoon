package com.example.pillarpocket.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.pillarpocket.data.local.BudgetGoal
import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.data.local.Expense
import com.example.pillarpocket.viewmodel.CategorySpendingItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/*
 * Generates a multi-page PDF monthly expense summary report
 * using Android's built-in PdfDocument API — no external libraries needed.

 * The report contains:
 * 1. A branded header with the report period
 * 2. A spending summary with budget goal comparison
 * 3. A category breakdown table
 * 4. A full expense entries table
 */
object PdfReportGenerator {

    // A4 page dimensions at 72 dpi
    private const val PAGE_WIDTH  = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN      = 40f

    /*
      Generates the PDF and saves it to the app's cache directory.
      @return The generated [File] ready for sharing.
     */
    fun generate(
        context: Context,
        month: Int,
        year: Int,
        monthLabel: String,
        expenses: List<Expense>,
        categories: List<Category>,
        categorySpending: List<CategorySpendingItem>,
        budgetGoal: BudgetGoal?,
        monthlyTotal: Double
    ): File {
        val document = PdfDocument()

        // Mutable page state — updated each time a new page is started
        var pageNumber = 1
        var page       = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas = page.canvas
        var y      = MARGIN

        // Paint styles
        val titlePaint = makePaint(
            color = "#2E7D32", size = 20f, bold = true
        )
        val headingPaint = makePaint(color = "#1C1C1C", size = 13f, bold = true)
        val bodyPaint    = makePaint(color = "#1C1C1C", size = 10f)
        val greyPaint    = makePaint(color = "#9E9E9E", size = 9f)
        val greenPaint   = makePaint(color = "#2E7D32", size = 10f, bold = true)
        val redPaint     = makePaint(color = "#D32F2F", size = 10f, bold = true)
        val whitePaint   = makePaint(color = "#FFFFFF", size = 10f)
        val linePaint    = Paint().apply {
            color = Color.parseColor("#E0E0E0"); strokeWidth = 1f
        }
        val greenBgPaint = Paint().apply { color = Color.parseColor("#2E7D32") }
        val lightBgPaint = Paint().apply { color = Color.parseColor("#E8F5E9") }
        val rowBgPaint   = Paint().apply { color = Color.parseColor("#FAFAFA") }
        val headerBgPaint = Paint().apply { color = Color.parseColor("#F5F5F5") }

        // Page management helpers
        // Finishes the current page and starts a fresh one.
        fun newPage() {
            document.finishPage(page)
            pageNumber++
            page   = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            canvas = page.canvas
            y      = MARGIN
        }

        /** Starts a new page if there isn't enough space for [needed] points. */
        fun checkSpace(needed: Float) {
            if (y + needed > PAGE_HEIGHT - MARGIN) newPage()
        }

        fun drawHR() {
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 8f
        }

        fun drawText(text: String, x: Float, paint: Paint) {
            canvas.drawText(text, x, y, paint)
        }

        // Page 1 Header
        // Green header banner
        canvas.drawRect(RectF(0f, 0f, PAGE_WIDTH.toFloat(), 75f), greenBgPaint)
        canvas.drawText(
            "Pillar Pocket", MARGIN, 32f,
            makePaint(color = "#FFFFFF", size = 20f, bold = true)
        )
        canvas.drawText(
            "Monthly Expense Report", MARGIN, 54f,
            makePaint(color = "#C8E6C9", size = 12f)
        )
        val genDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText(
            "Generated: $genDate",
            PAGE_WIDTH - MARGIN - 120f, 42f,
            makePaint(color = "#FFFFFF", size = 9f)
        )

        y = 90f

        // Period label
        canvas.drawText("Report Period: $monthLabel", MARGIN, y, headingPaint)
        y += 8f
        drawHR()

        // Summary Section
        canvas.drawText("Summary", MARGIN, y, headingPaint)
        y += 14f

        // Summary background box
        val summaryRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 88f)
        canvas.drawRoundRect(summaryRect, 8f, 8f, lightBgPaint)

        val c1 = MARGIN + 10f
        val c2 = MARGIN + 260f

        canvas.drawText("Total Spent",       c1, y + 16f, greyPaint)
        canvas.drawText(
            "R ${"%.2f".format(monthlyTotal)}",
            c1, y + 32f,
            makePaint(color = "#1C1C1C", size = 15f, bold = true)
        )
        canvas.drawText("Transactions",      c1, y + 50f, greyPaint)
        canvas.drawText("${expenses.size} expense(s)", c1, y + 64f, bodyPaint)

        if (budgetGoal != null) {
            canvas.drawText("Minimum Goal", c2, y + 16f, greyPaint)
            canvas.drawText("R ${"%.2f".format(budgetGoal.minimumGoal)}", c2, y + 32f, bodyPaint)
            canvas.drawText("Maximum Goal", c2, y + 50f, greyPaint)
            canvas.drawText("R ${"%.2f".format(budgetGoal.maximumGoal)}", c2, y + 64f, bodyPaint)

            val (statusText, statusPaint) = when {
                monthlyTotal > budgetGoal.maximumGoal ->
                    "⚠  Exceeded maximum goal" to redPaint
                monthlyTotal < budgetGoal.minimumGoal ->
                    "Below minimum goal" to makePaint(color = "#F9A825", size = 10f, bold = true)
                else ->
                    "✓  Within budget goal" to greenPaint
            }
            canvas.drawText(statusText, c1, y + 82f, statusPaint)
        } else {
            canvas.drawText("No budget goal set for this month", c2, y + 32f, greyPaint)
        }

        y += 98f
        drawHR()

        // Category Breakdown
        checkSpace(30f)
        canvas.drawText("Category Breakdown", MARGIN, y, headingPaint)
        y += 16f

        if (categorySpending.isEmpty()) {
            canvas.drawText("No spending data for this period.", MARGIN, y, greyPaint)
            y += 20f
        } else {
            // Table column positions
            val catCols = listOf(
                MARGIN + 4f,   // Category
                MARGIN + 190f, // Amount
                MARGIN + 280f, // % of Total
                MARGIN + 370f  // % of Max
            )
            val catHeaders = listOf("Category", "Amount", "% of Total", "% of Max Goal")

            // Header row
            canvas.drawRect(RectF(MARGIN, y - 12f, PAGE_WIDTH - MARGIN, y + 5f), headerBgPaint)
            catHeaders.zip(catCols).forEach { (h, x) ->
                canvas.drawText(h, x, y, greyPaint)
            }
            y += 12f
            drawHR()

            categorySpending.forEachIndexed { i, item ->
                checkSpace(20f)
                if (i % 2 == 0) {
                    canvas.drawRect(
                        RectF(MARGIN, y - 12f, PAGE_WIDTH - MARGIN, y + 6f), rowBgPaint
                    )
                }
                // Colour dot
                canvas.drawCircle(
                    MARGIN + 8f, y - 2f, 5f,
                    Paint().apply {
                        isAntiAlias = true
                        color = runCatching {
                            Color.parseColor(item.category.colorHex)
                        }.getOrDefault(Color.GRAY)
                    }
                )
                canvas.drawText(item.category.name, catCols[0] + 16f, y, bodyPaint)
                canvas.drawText("R ${"%.2f".format(item.totalAmount)}", catCols[1], y, bodyPaint)
                canvas.drawText("${"%.1f".format(item.percentage)}%", catCols[2], y, bodyPaint)

                if (budgetGoal != null && budgetGoal.maximumGoal > 0) {
                    val pctMax = item.totalAmount / budgetGoal.maximumGoal * 100
                    canvas.drawText(
                        "${"%.1f".format(pctMax)}%",
                        catCols[3], y,
                        if (pctMax > 40) redPaint else bodyPaint
                    )
                } else {
                    canvas.drawText("—", catCols[3], y, greyPaint)
                }
                y += 18f
            }
        }

        y += 6f
        drawHR()

        // Expense Entries
        checkSpace(30f)
        canvas.drawText("Expense Entries", MARGIN, y, headingPaint)
        y += 16f

        if (expenses.isEmpty()) {
            canvas.drawText("No expenses recorded for this period.", MARGIN, y, greyPaint)
            y += 20f
        } else {
            val expCols    = listOf(MARGIN + 4f, MARGIN + 72f, MARGIN + 222f, MARGIN + 332f, MARGIN + 430f)
            val expHeaders = listOf("Date", "Description", "Category", "Time", "Amount")

            // Header row
            canvas.drawRect(
                RectF(MARGIN, y - 12f, PAGE_WIDTH - MARGIN, y + 5f), headerBgPaint
            )
            expHeaders.zip(expCols).forEach { (h, x) ->
                canvas.drawText(h, x, y, greyPaint)
            }
            y += 12f
            drawHR()

            val inFmt  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outFmt = SimpleDateFormat("dd/MM/yy",  Locale.getDefault())

            expenses.sortedByDescending { it.date }.forEachIndexed { i, expense ->
                checkSpace(22f)
                if (i % 2 == 0) {
                    canvas.drawRect(
                        RectF(MARGIN, y - 12f, PAGE_WIDTH - MARGIN, y + 6f), rowBgPaint
                    )
                }

                val dispDate = runCatching { outFmt.format(inFmt.parse(expense.date)!!) }
                    .getOrDefault(expense.date)
                val desc     = expense.description.truncate(22)
                val catName  = (categories.find { it.id == expense.categoryId }?.name ?: "Unknown").truncate(14)

                canvas.drawText(dispDate, expCols[0], y, bodyPaint)
                canvas.drawText(desc,     expCols[1], y, bodyPaint)
                canvas.drawText(catName,  expCols[2], y, bodyPaint)
                canvas.drawText("${expense.startTime}–${expense.endTime}", expCols[3], y, bodyPaint)
                canvas.drawText("R ${"%.2f".format(expense.amount)}", expCols[4], y, redPaint)

                y += 18f
            }
        }

        // Total row
        checkSpace(28f)
        y += 4f
        canvas.drawRect(
            RectF(MARGIN, y - 12f, PAGE_WIDTH - MARGIN, y + 8f),
            Paint().apply { color = Color.parseColor("#E8F5E9") }
        )
        canvas.drawText(
            "Total", MARGIN + 4f, y, makePaint(color = "#2E7D32", size = 10f, bold = true)
        )
        canvas.drawText(
            "R ${"%.2f".format(monthlyTotal)}", MARGIN + 430f, y,
            makePaint(color = "#2E7D32", size = 10f, bold = true)
        )
        y += 20f

        // Footer
        checkSpace(20f)
        drawHR()
        canvas.drawText(
            "Generated by Pillar Pocket  •  $genDate  •  Page $pageNumber",
            MARGIN, y, greyPaint
        )

        document.finishPage(page)

        // Save to cache directory
        val file = File(
            context.cacheDir,
            "PillarPocket_Report_${year}_${"%.2d".format(month)}.pdf"
        )
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    // Helpers

    private fun makePaint(
        color: String,
        size: Float,
        bold: Boolean = false
    ) = Paint().apply {
        this.color    = Color.parseColor(color)
        textSize      = size
        isAntiAlias   = true
        if (bold) typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun String.truncate(max: Int) =
        if (length > max) take(max - 1) + "…" else this
}