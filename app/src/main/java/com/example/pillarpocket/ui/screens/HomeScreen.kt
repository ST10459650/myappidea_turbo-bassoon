package com.example.pillarpocket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.BadgeViewModel
import com.example.pillarpocket.viewmodel.BadgeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    username: String = "",
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToBudgetGoals: () -> Unit = {},
    onNavigateToCategorySpending: () -> Unit = {},
    onNavigateToSpendingGraph: () -> Unit = {},
    onNavigateToBadges: () -> Unit = {},
    onNavigateToMonthlyReport: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val app     = context.applicationContext as PillarPocketApp

    val badgeViewModel: BadgeViewModel = viewModel(
        factory = BadgeViewModelFactory(
            app.badgeRepository,
            app.expenseRepository,
            app.categoryRepository,
            app.budgetGoalRepository,
            userId
        )
    )
    val newBadgeCount by badgeViewModel.newBadgeCount.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pillar Pocket", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor        = PillarGreen,
                    titleContentColor     = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PillarSurface)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Icon(Icons.Filled.AccountBalanceWallet, null, tint = PillarGreen, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Welcome, $username! 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PillarOnSurface)
            Text("Manage your finances with ease.", fontSize = 14.sp, color = PillarGrey)

            Spacer(modifier = Modifier.height(32.dp))

            // Dashboard — highlighted as primary action
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = PillarGreenPale),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                HomeMenuButton(
                    icon        = Icons.Filled.Dashboard,
                    label       = "Budget Dashboard",
                    description = "See your overall budget health at a glance",
                    onClick     = onNavigateToDashboard,
                    tint        = PillarGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    HomeMenuButton(
                        icon        = Icons.Filled.Category,
                        label       = "Categories",
                        description = "Manage your expense & budget categories",
                        onClick     = onNavigateToCategories
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    HomeMenuButton(
                        icon        = Icons.Filled.ReceiptLong,
                        label       = "Expenses",
                        description = "Track and record your expenses",
                        onClick     = onNavigateToExpenses
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    HomeMenuButton(
                        icon        = Icons.Filled.TrackChanges,
                        label       = "Budget Goals",
                        description = "Set your monthly minimum & maximum goals",
                        onClick     = onNavigateToBudgetGoals
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    HomeMenuButton(
                        icon        = Icons.Filled.PieChart,
                        label       = "Category Spending",
                        description = "View spending totals per category",
                        onClick     = onNavigateToCategorySpending
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    HomeMenuButton(
                        icon        = Icons.Filled.BarChart,
                        label       = "Spending Graph",
                        description = "Visualise spending with min & max goal lines",
                        onClick     = onNavigateToSpendingGraph
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    HomeMenuButton(
                        icon        = Icons.Filled.PictureAsPdf,
                        label       = "Monthly Report",
                        description = "Export a PDF summary of your monthly spending",
                        onClick     = onNavigateToMonthlyReport
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    HomeMenuButtonWithBadge(
                        icon        = Icons.Filled.EmojiEvents,
                        label       = "My Badges",
                        description = "View your earned achievements and rewards",
                        badgeCount  = newBadgeCount,
                        onClick     = onNavigateToBadges
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon    = { Icon(Icons.Filled.Logout, null, tint = PillarRed) },
            title   = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text    = { Text("Are you sure you want to log out of Pillar Pocket?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Log Out", color = PillarRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun HomeMenuButton(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    tint: Color = PillarGreen
) {
    TextButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PillarOnSurface)
                Text(description, fontSize = 12.sp, color = PillarGrey)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = PillarGrey)
        }
    }
}

@Composable
fun HomeMenuButtonWithBadge(
    icon: ImageVector,
    label: String,
    description: String,
    badgeCount: Int,
    onClick: () -> Unit
) {
    TextButton(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = PillarGreen, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PillarOnSurface)
                Text(description, fontSize = 12.sp, color = PillarGrey)
            }
            if (badgeCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PillarRed)
                ) {
                    Text(
                        text       = if (badgeCount > 9) "9+" else "$badgeCount",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Filled.ChevronRight, null, tint = PillarGrey)
        }
    }
}