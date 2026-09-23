package com.example.pillarpocket.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.data.local.BudgetGoal
import com.example.pillarpocket.ui.components.iconFromName
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.*
import com.example.pillarpocket.workers.SpendingCheckWorker
import java.text.DateFormatSymbols
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userId: Int,
    onBack: () -> Unit,
    onNavigateToBudgetGoals: () -> Unit
) {
    val context = LocalContext.current
    val app     = context.applicationContext as PillarPocketApp
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            app.expenseRepository,
            app.categoryRepository,
            app.budgetGoalRepository,
            userId
        )
    )

    val budgetGoal          by viewModel.budgetGoal.collectAsStateWithLifecycle()
    val dashboardCategories by viewModel.dashboardCategories.collectAsStateWithLifecycle()
    val monthlyTotal        by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val budgetStatus        by viewModel.budgetStatus.collectAsStateWithLifecycle()
    val overspendingCount   by viewModel.overspendingCount.collectAsStateWithLifecycle()

    val calendar   = Calendar.getInstance()
    val monthName  = DateFormatSymbols().months[calendar.get(Calendar.MONTH)]
    val year       = calendar.get(Calendar.YEAR)
    val monthLabel = "$monthName $year"

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled silently */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<SpendingCheckWorker>().build()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text(
                text = "📅  $monthLabel",
                fontSize = 14.sp,
                color = PillarGrey,
                fontWeight = FontWeight.Medium
            )

            BudgetHealthCard(
                status       = budgetStatus,
                monthlyTotal = monthlyTotal,
                budgetGoal   = budgetGoal,
                onSetGoal    = onNavigateToBudgetGoals
            )

            if (overspendingCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = PillarRed.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Warning, null, tint = PillarRed, modifier = Modifier.size(22.dp))
                        Column {
                            Text(
                                "$overspendingCount overspending " +
                                        "categor${if (overspendingCount == 1) "y" else "ies"} detected",
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = PillarRed
                            )
                            Text("Highlighted in red below", fontSize = 12.sp, color = PillarRed.copy(0.7f))
                        }
                    }
                }
            }

            if (budgetGoal != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val remaining = (budgetGoal!!.maximumGoal - monthlyTotal).coerceAtLeast(0.0)
                    val overspent = (monthlyTotal - budgetGoal!!.maximumGoal).coerceAtLeast(0.0)

                    QuickStatCard(
                        icon       = Icons.Filled.ShoppingCart,
                        label      = "Spent",
                        value      = "R ${"%.2f".format(monthlyTotal)}",
                        valueColor = PillarOnSurface,
                        modifier   = Modifier.weight(1f)
                    )
                    if (overspent > 0) {
                        QuickStatCard(
                            icon       = Icons.AutoMirrored.Filled.TrendingUp,
                            label      = "Over by",
                            value      = "R ${"%.2f".format(overspent)}",
                            valueColor = PillarRed,
                            modifier   = Modifier.weight(1f)
                        )
                    } else {
                        QuickStatCard(
                            icon       = Icons.Filled.Savings,
                            label      = "Remaining",
                            value      = "R ${"%.2f".format(remaining)}",
                            valueColor = PillarGreen,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                    QuickStatCard(
                        icon       = Icons.Filled.Category,
                        label      = "Categories",
                        value      = "${dashboardCategories.size}",
                        valueColor = PillarOnSurface,
                        modifier   = Modifier.weight(1f)
                    )
                }
            }

            if (dashboardCategories.isNotEmpty()) {
                Text(
                    "Category Performance",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = PillarOnSurface
                )
                dashboardCategories.forEach { item ->
                    DashboardCategoryCard(item = item, maxGoal = budgetGoal?.maximumGoal)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, tint = PillarGrey, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No expenses recorded this month",
                            fontSize  = 15.sp,
                            color     = PillarGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BudgetHealthCard(
    status: BudgetHealthStatus,
    monthlyTotal: Double,
    budgetGoal: BudgetGoal?,
    onSetGoal: () -> Unit
) {
    val (title, color, icon) = when (status) {
        BudgetHealthStatus.NO_GOAL -> Triple("No Goal Set", PillarGrey, Icons.Filled.AddChart)
        BudgetHealthStatus.UNDER_MIN -> Triple("Under Minimum", PillarGreen, Icons.Filled.CheckCircle)
        BudgetHealthStatus.HEALTHY -> Triple("Healthy Spending", PillarGreen, Icons.Filled.CheckCircle)
        BudgetHealthStatus.OVER_MAX -> Triple("Over Maximum", PillarRed, Icons.Filled.Error)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            if (status == BudgetHealthStatus.NO_GOAL) {
                Text("You haven't set a budget goal for this month yet.", fontSize = 14.sp, color = PillarOnSurface)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSetGoal,
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Text("Set Monthly Goal")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Spent", fontSize = 12.sp, color = PillarGrey)
                        Text("R ${"%.2f".format(monthlyTotal)}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PillarOnSurface)
                    }
                    budgetGoal?.let {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Max Goal", fontSize = 12.sp, color = PillarGrey)
                            Text("R ${"%.2f".format(it.maximumGoal)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PillarOnSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = PillarGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 10.sp, color = PillarGrey)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun DashboardCategoryCard(
    item: DashboardCategoryItem,
    maxGoal: Double?
) {
    val progress = item.percentageOfMax / 100f
    val barColor = if (item.isOverspending) PillarRed else PillarGreen
    
    // Parse hex color string to Color
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(item.category.colorHex))
    } catch (e: Exception) {
        PillarGreen // Fallback
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            iconFromName(item.category.iconName),
                            null,
                            tint = categoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.category.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${"%.1f".format(item.percentageOfTotal)}% of total spend", fontSize = 12.sp, color = PillarGrey)
                }
                Text("R ${"%.2f".format(item.totalAmount)}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
            
            if (maxGoal != null && maxGoal > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).background(PillarSurface, RoundedCornerShape(4.dp)),
                    color = barColor,
                    trackColor = PillarSurface,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${(progress * 100).toInt()}% of budget", fontSize = 11.sp, color = if (item.isOverspending) PillarRed else PillarGrey)
                }
            }
        }
    }
}
