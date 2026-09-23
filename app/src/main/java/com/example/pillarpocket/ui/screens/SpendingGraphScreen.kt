package com.example.pillarpocket.ui.screens

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.ui.components.SpendingBarChart
import com.example.pillarpocket.ui.components.iconFromName
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.*
import java.text.DateFormatSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingGraphScreen(
    userId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillarPocketApp
    val viewModel: SpendingGraphViewModel = viewModel(
        factory = SpendingGraphViewModelFactory(
            app.expenseRepository,
            app.categoryRepository,
            app.budgetGoalRepository,
            userId
        )
    )

    val selectedMonthYear by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val categorySpending  by viewModel.categorySpending.collectAsStateWithLifecycle()
    val budgetGoal        by viewModel.budgetGoal.collectAsStateWithLifecycle()
    val monthlyTotal      by viewModel.monthlyTotal.collectAsStateWithLifecycle()

    val monthName  = DateFormatSymbols().months[selectedMonthYear.month - 1]
    val monthLabel = "$monthName ${selectedMonthYear.year}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Graph", fontWeight = FontWeight.Bold) },
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

            //Month Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.navigateToPreviousMonth() }) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = PillarGreen
                        )
                    }
                    Text(
                        text = monthLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PillarOnSurface,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { viewModel.navigateToNextMonth() }) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "Next Month",
                            tint = PillarGreen
                        )
                    }
                }
            }

            //Monthly Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PillarGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStatItem(
                        label = "Total Spent",
                        value = "R %.2f".format(monthlyTotal),
                        valueColor = Color.White
                    )
                    VerticalDivider(
                        modifier = Modifier.height(48.dp),
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    SummaryStatItem(
                        label = "Min Goal",
                        value = if (budgetGoal != null)
                            "R %.2f".format(budgetGoal!!.minimumGoal) else "Not set",
                        valueColor = PillarAmber
                    )
                    VerticalDivider(
                        modifier = Modifier.height(48.dp),
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    SummaryStatItem(
                        label = "Max Goal",
                        value = if (budgetGoal != null)
                            "R %.2f".format(budgetGoal!!.maximumGoal) else "Not set",
                        valueColor = Color(0xFFFF6B6B)
                    )
                }
            }

            // Bar Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spending by Category",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PillarOnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = monthLabel,
                        fontSize = 12.sp,
                        color = PillarGrey
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (categorySpending.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.BarChart,
                                    null,
                                    tint = PillarGrey,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No spending data for $monthLabel",
                                    fontSize = 14.sp,
                                    color = PillarGrey,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        SpendingBarChart(
                            items    = categorySpending,
                            minGoal  = budgetGoal?.minimumGoal,
                            maxGoal  = budgetGoal?.maximumGoal,
                            chartHeight = 300.dp
                        )
                    }
                }
            }

            // Legend Card
            if (categorySpending.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Legend",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PillarOnSurface
                        )

                        // Goal lines legend
                        if (budgetGoal != null) {
                            LegendLineItem(
                                color = PillarAmber,
                                label = "Minimum Goal — R %.2f".format(budgetGoal!!.minimumGoal),
                                dashed = true
                            )
                            LegendLineItem(
                                color = PillarRed,
                                label = "Maximum Goal — R %.2f".format(budgetGoal!!.maximumGoal),
                                dashed = true
                            )
                        } else {
                            Text(
                                text = "No budget goal set for $monthLabel.\nGo to Budget Goals to set one.",
                                fontSize = 13.sp,
                                color = PillarGrey
                            )
                        }

                        HorizontalDivider()

                        // Category colour legend
                        categorySpending.forEach { item ->
                            val color = runCatching {
                                Color(android.graphics.Color.parseColor(item.category.colorHex))
                            }.getOrDefault(Color.Gray)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = iconFromName(item.category.iconName),
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        item.category.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = PillarOnSurface
                                    )
                                    Text(
                                        text = "R ${"%.2f".format(item.totalAmount)}  •  ${"%.1f".format(item.percentage)}%",
                                        fontSize = 12.sp,
                                        color = PillarGrey
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Helper Composable

@Composable
fun SummaryStatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun LegendLineItem(color: Color, label: String, dashed: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(label, fontSize = 13.sp, color = PillarOnSurface)
    }
}