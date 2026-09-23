package com.example.pillarpocket.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.ui.components.iconFromName
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.*
import java.text.DateFormatSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    userId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app     = context.applicationContext as PillarPocketApp
    val viewModel: ReportViewModel = viewModel(
        factory = ReportViewModelFactory(
            app.expenseRepository,
            app.categoryRepository,
            app.budgetGoalRepository,
            userId
        )
    )

    val selectedMonthYear by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val reportState       by viewModel.reportState.collectAsStateWithLifecycle()
    val filteredExpenses  by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val categorySpending  by viewModel.categorySpending.collectAsStateWithLifecycle()
    val budgetGoal        by viewModel.budgetGoal.collectAsStateWithLifecycle()
    val monthlyTotal      by viewModel.monthlyTotal.collectAsStateWithLifecycle()

    val monthName  = DateFormatSymbols().months[selectedMonthYear.month - 1]
    val monthLabel = "$monthName ${selectedMonthYear.year}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PillarGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PillarSurface)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Month Selector
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.navigateToPreviousMonth() }) {
                        Icon(Icons.Filled.ChevronLeft, null, tint = PillarGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(monthLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PillarOnSurface)
                        Text("Tap arrows to change month", fontSize = 11.sp, color = PillarGrey)
                    }
                    IconButton(onClick = { viewModel.navigateToNextMonth() }) {
                        Icon(Icons.Filled.ChevronRight, null, tint = PillarGreen)
                    }
                }
            }

            // Report Preview Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = PillarGreen)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Description, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Report Preview — $monthLabel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ReportStatItem("Total Spent",   "R ${"%.2f".format(monthlyTotal)}", Color.White)
                        ReportStatItem("Expenses",      "${filteredExpenses.size}", Color.White)
                        ReportStatItem("Categories",    "${categorySpending.size}", Color.White)
                    }

                    if (budgetGoal != null) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ReportStatItem(
                                "Min Goal",
                                "R ${"%.2f".format(budgetGoal!!.minimumGoal)}",
                                Color(0xFFFFE082)
                            )
                            ReportStatItem(
                                "Max Goal",
                                "R ${"%.2f".format(budgetGoal!!.maximumGoal)}",
                                Color(0xFFFF8A80)
                            )
                            val statusText = when {
                                monthlyTotal > budgetGoal!!.maximumGoal -> "Over Budget ⚠"
                                monthlyTotal < budgetGoal!!.minimumGoal -> "Under Min"
                                else -> "On Track ✓"
                            }
                            ReportStatItem("Status", statusText, Color.White)
                        }
                    }
                }
            }

            // What's in the Report
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Report Contents", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PillarOnSurface)
                    ReportContentRow("📊", "Spending Summary", "Total spent, transactions & budget goal comparison")
                    ReportContentRow("🗂️", "Category Breakdown", "Amount & percentage per category")
                    ReportContentRow("🧾", "Expense Entries", "Full list of all ${filteredExpenses.size} expenses")
                }
            }

            // Top Categories Preview
            if (categorySpending.isNotEmpty()) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Top Categories", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PillarOnSurface)

                        categorySpending.take(3).forEach { item ->
                            val color = runCatching {
                                Color(android.graphics.Color.parseColor(item.category.colorHex))
                            }.getOrDefault(Color.Gray)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f))
                                ) {
                                    Icon(iconFromName(item.category.iconName), null, tint = color, modifier = Modifier.size(18.dp))
                                }
                                Text(item.category.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PillarOnSurface, modifier = Modifier.weight(1f))
                                Text("R ${"%.2f".format(item.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PillarOnSurface)
                                Text("${"%.1f".format(item.percentage)}%", fontSize = 12.sp, color = PillarGrey)
                            }
                        }
                        if (categorySpending.size > 3) {
                            Text(
                                "+ ${categorySpending.size - 3} more categories in the full report",
                                fontSize  = 12.sp,
                                color     = PillarGrey,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Generate / Share Section
            when (val state = reportState) {

                is ReportState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = PillarGreenPale)
                    ) {
                        Column(
                            modifier            = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = PillarGreen, modifier = Modifier.size(40.dp))
                            Text("Report Ready!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PillarGreen)
                            Text("Your PDF has been generated successfully.", fontSize = 13.sp, color = PillarGrey, textAlign = TextAlign.Center)

                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        state.file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Pillar Pocket — $monthLabel Report")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Share Report via")
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape    = RoundedCornerShape(12.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = PillarGreen)
                            ) {
                                Icon(Icons.Filled.Share, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share PDF Report", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }

                            TextButton(onClick = { viewModel.resetState() }) {
                                Text("Generate for Another Month", color = PillarGrey, fontSize = 13.sp)
                            }
                        }
                    }
                }

                is ReportState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Error, null, tint = PillarRed, modifier = Modifier.size(22.dp))
                            Text(state.message, fontSize = 13.sp, color = PillarRed)
                        }
                    }
                    Button(
                        onClick  = { viewModel.generateReport(context, monthLabel) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = PillarGreen)
                    ) {
                        Text("Try Again", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                else -> {
                    val isEmpty = filteredExpenses.isEmpty()
                    Button(
                        onClick  = { viewModel.generateReport(context, monthLabel) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = PillarGreen),
                        enabled  = state !is ReportState.Loading && !isEmpty
                    ) {
                        if (state is ReportState.Loading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color       = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Generating PDF...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isEmpty) "No expenses to report"
                                else "Generate PDF Report",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (isEmpty) {
                        Text(
                            "Add some expenses for $monthLabel to generate a report.",
                            fontSize  = 12.sp,
                            color     = PillarGrey,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Helper Composables

@Composable
fun ReportStatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor, textAlign = TextAlign.Center)
    }
}

@Composable
fun ReportContentRow(emoji: String, title: String, description: String) {
    Row(
        verticalAlignment      = Alignment.CenterVertically,
        horizontalArrangement  = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PillarOnSurface)
            Text(description, fontSize = 11.sp, color = PillarGrey)
        }
    }
}