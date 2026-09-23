package com.example.pillarpocket.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.viewmodel.*
import com.example.pillarpocket.ui.theme.*
import java.text.DateFormatSymbols

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetGoalScreen(
    userId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillarPocketApp
    val viewModel: BudgetGoalViewModel = viewModel(
        factory = BudgetGoalViewModelFactory(
            app.budgetGoalRepository,
            app.expenseRepository,
            userId
        )
    )

    val selectedMonthYear by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val currentGoal by viewModel.currentGoal.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val budgetGoalState by viewModel.budgetGoalState.collectAsStateWithLifecycle()

    var minGoalInput by remember { mutableStateOf("") }
    var maxGoalInput by remember { mutableStateOf("") }

    // Pre-fill fields when goal loads
    LaunchedEffect(currentGoal) {
        minGoalInput = currentGoal?.minimumGoal?.let { "%.2f".format(it) } ?: ""
        maxGoalInput = currentGoal?.maximumGoal?.let { "%.2f".format(it) } ?: ""
    }

    LaunchedEffect(budgetGoalState) {
        if (budgetGoalState is BudgetGoalState.Success) {
            viewModel.resetState()
        }
    }

    val monthName = DateFormatSymbols().months[selectedMonthYear.month - 1]
    val monthLabel = "$monthName ${selectedMonthYear.year}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Goals", fontWeight = FontWeight.Bold) },
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
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month", tint = PillarGreen)
                    }
                    Text(
                        text = monthLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PillarOnSurface,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { viewModel.navigateToNextMonth() }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month", tint = PillarGreen)
                    }
                }
            }

            // Spending Status Card
            SpendingStatusCard(
                monthLabel = monthLabel,
                monthlyTotal = monthlyTotal,
                currentGoal = currentGoal
            )

            // Set Goal Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (currentGoal != null) "Update Goal for $monthLabel" else "Set Goal for $monthLabel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PillarOnSurface
                    )

                    // Minimum Goal Field
                    OutlinedTextField(
                        value = minGoalInput,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) minGoalInput = it },
                        label = { Text("Minimum Monthly Goal") },
                        leadingIcon = {
                            Text("R", fontWeight = FontWeight.Bold, color = PillarGreen, modifier = Modifier.padding(start = 4.dp))
                        },
                        trailingIcon = {
                            Icon(Icons.Filled.TrendingDown, contentDescription = null, tint = PillarAmber)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillarGreen,
                            focusedLabelColor = PillarGreen
                        )
                    )

                    // Maximum Goal Field
                    OutlinedTextField(
                        value = maxGoalInput,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) maxGoalInput = it },
                        label = { Text("Maximum Monthly Goal") },
                        leadingIcon = {
                            Text("R", fontWeight = FontWeight.Bold, color = PillarGreen, modifier = Modifier.padding(start = 4.dp))
                        },
                        trailingIcon = {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = PillarRed)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillarGreen,
                            focusedLabelColor = PillarGreen
                        )
                    )

                    // Error / Success Message
                    when (budgetGoalState) {
                        is BudgetGoalState.Error -> Text(
                            text = (budgetGoalState as BudgetGoalState.Error).message,
                            color = PillarRed,
                            fontSize = 13.sp
                        )
                        is BudgetGoalState.Success -> Text(
                            text = "✓ Goal saved successfully!",
                            color = PillarGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        else -> {}
                    }

                    // Save Button
                    Button(
                        onClick = {
                            viewModel.setGoal(
                                minimumGoal = minGoalInput.toDoubleOrNull() ?: 0.0,
                                maximumGoal = maxGoalInput.toDoubleOrNull() ?: 0.0
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PillarGreen),
                        enabled = budgetGoalState !is BudgetGoalState.Loading
                    ) {
                        if (budgetGoalState is BudgetGoalState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentGoal != null) "Update Goal" else "Save Goal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpendingStatusCard(
    monthLabel: String,
    monthlyTotal: Double,
    currentGoal: com.example.pillarpocket.data.local.BudgetGoal?
) {
    val progress = if (currentGoal != null && currentGoal.maximumGoal > 0) {
        (monthlyTotal / currentGoal.maximumGoal).toFloat().coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    val statusInfo = when {
        currentGoal == null -> Triple("No goal set for this month", PillarGrey, Icons.Filled.Info)
        monthlyTotal < currentGoal.minimumGoal -> Triple("Below minimum goal", PillarAmber, Icons.Filled.Warning)
        monthlyTotal > currentGoal.maximumGoal -> Triple("Exceeded maximum goal!", PillarRed, Icons.Filled.Error)
        else -> Triple("Within budget goal ✓", PillarGreen, Icons.Filled.CheckCircle)
    }

    val progressColor = when {
        currentGoal == null -> PillarGrey
        monthlyTotal > currentGoal.maximumGoal -> PillarRed
        monthlyTotal < currentGoal.minimumGoal -> PillarAmber
        else -> PillarGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Spending Overview — $monthLabel",
                fontSize = 14.sp,
                color = PillarGrey,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "R %.2f".format(monthlyTotal),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = PillarOnSurface
            )

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.15f)
            )

            // Min / Max Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Min Goal", fontSize = 11.sp, color = PillarGrey)
                    Text(
                        text = if (currentGoal != null) "R %.2f".format(currentGoal.minimumGoal) else "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PillarAmber
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Max Goal", fontSize = 11.sp, color = PillarGrey)
                    Text(
                        text = if (currentGoal != null) "R %.2f".format(currentGoal.maximumGoal) else "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PillarRed
                    )
                }
            }

            // Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = statusInfo.third,
                    contentDescription = null,
                    tint = statusInfo.second,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = statusInfo.first,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusInfo.second
                )
            }
        }
    }
}