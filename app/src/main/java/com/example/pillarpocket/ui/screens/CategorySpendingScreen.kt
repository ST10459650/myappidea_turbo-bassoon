package com.example.pillarpocket.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.pillarpocket.ui.components.PillarDatePickerDialog
import com.example.pillarpocket.ui.components.formatDisplayDate
import com.example.pillarpocket.ui.components.iconFromName
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySpendingScreen(
    userId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillarPocketApp
    val viewModel: CategorySpendingViewModel = viewModel(
        factory = CategorySpendingViewModelFactory(
            app.expenseRepository,
            app.categoryRepository,
            userId
        )
    )

    val categorySpending by viewModel.categorySpending.collectAsStateWithLifecycle()
    val totalSpending by viewModel.totalSpending.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val endDate by viewModel.endDate.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Spending", fontWeight = FontWeight.Bold) },
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
        ) {
            // Date Range Selector
            SpendingDateRangeCard(
                startDate = startDate,
                endDate = endDate,
                onApply = { start, end -> viewModel.setDateRange(start, end) },
                onResetToCurrentMonth = { viewModel.resetToCurrentMonth() }
            )

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PillarGreen)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Spent",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R %.2f".format(totalSpending),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatDisplayDate(startDate)} – ${formatDisplayDate(endDate)}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${categorySpending.size} categor${if (categorySpending.size == 1) "y" else "ies"}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Category Breakdown
            if (categorySpending.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.PieChart,
                            null,
                            tint = PillarGrey,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No spending data",
                            fontSize = 18.sp,
                            color = PillarGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No expenses found for this period",
                            fontSize = 14.sp,
                            color = PillarGrey
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Breakdown by Category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PillarGrey,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(categorySpending, key = { it.category.id }) { item ->
                        CategorySpendingItem(
                            item = item,
                            rank = categorySpending.indexOf(item) + 1
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// Date Range Selector Card

@Composable
fun SpendingDateRangeCard(
    startDate: String,
    endDate: String,
    onApply: (String, String) -> Unit,
    onResetToCurrentMonth: () -> Unit
) {
    var tempStart by remember { mutableStateOf(startDate) }
    var tempEnd by remember { mutableStateOf(endDate) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startDate, endDate) {
        tempStart = startDate
        tempEnd = endDate
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = PillarGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Selected Period",
                            fontSize = 13.sp,
                            color = PillarGrey
                        )
                        Text(
                            "${formatDisplayDate(startDate)} – ${formatDisplayDate(endDate)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PillarOnSurface
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = PillarGrey
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // From Date
                OutlinedTextField(
                    value = if (tempStart.isEmpty()) "" else formatDisplayDate(tempStart),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("From") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, tint = PillarGreen) },
                    trailingIcon = {
                        IconButton(onClick = { showStartPicker = true }) {
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PillarGreen,
                        focusedLabelColor = PillarGreen
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // To Date
                OutlinedTextField(
                    value = if (tempEnd.isEmpty()) "" else formatDisplayDate(tempEnd),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("To") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, tint = PillarGreen) },
                    trailingIcon = {
                        IconButton(onClick = { showEndPicker = true }) {
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PillarGreen,
                        focusedLabelColor = PillarGreen
                    )
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(error!!, color = PillarRed, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reset to current month
                    OutlinedButton(
                        onClick = {
                            onResetToCurrentMonth()
                            error = null
                            expanded = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PillarGreen)
                    ) {
                        Text("This Month", fontSize = 13.sp)
                    }

                    // Apply
                    Button(
                        onClick = {
                            when {
                                tempStart.isEmpty() -> error = "Select a start date"
                                tempEnd.isEmpty() -> error = "Select an end date"
                                tempStart > tempEnd -> error = "Start must be before end"
                                else -> {
                                    error = null
                                    onApply(tempStart, tempEnd)
                                    expanded = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PillarGreen)
                    ) {
                        Text("Apply", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        PillarDatePickerDialog(
            onDateSelected = { tempStart = it },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        PillarDatePickerDialog(
            onDateSelected = { tempEnd = it },
            onDismiss = { showEndPicker = false }
        )
    }
}

// Category Spending Item

@Composable
fun CategorySpendingItem(
    item: CategorySpendingItem,
    rank: Int
) {
    val categoryColor = remember(item.category.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(item.category.colorHex)) }
            .getOrDefault(Color.Gray)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = item.percentage / 100f,
        animationSpec = tween(durationMillis = 700),
        label = "categoryProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "#$rank",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Category Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = iconFromName(item.category.iconName),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Percentage
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.category.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PillarOnSurface
                    )
                    Text(
                        text = "${"%.1f".format(item.percentage)}% of total",
                        fontSize = 12.sp,
                        color = PillarGrey
                    )
                }

                // Amount
                Text(
                    text = "R %.2f".format(item.totalAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PillarOnSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = categoryColor,
                trackColor = categoryColor.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("R 0.00", fontSize = 10.sp, color = PillarGrey)
                Text(
                    "R %.2f".format(item.totalAmount),
                    fontSize = 10.sp,
                    color = categoryColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}