package com.example.pillarpocket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.data.local.BadgeType
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    userId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app     = context.applicationContext as PillarPocketApp
    val viewModel: BadgeViewModel = viewModel(
        factory = BadgeViewModelFactory(
            app.badgeRepository,
            app.expenseRepository,
            app.categoryRepository,
            app.budgetGoalRepository,
            userId
        )
    )

    val allBadges    by viewModel.allBadges.collectAsStateWithLifecycle()
    val earnedCount  by viewModel.earnedCount.collectAsStateWithLifecycle()
    val totalBadges   = BadgeType.values().size

    // Mark all badges as seen when screen opens
    LaunchedEffect(Unit) { viewModel.markAllSeen() }

    // Group badges by category
    val grouped = allBadges.groupBy { it.type.category }
    val categoryOrder = listOf(
        "Getting Started",
        "Expense Logging",
        "Organisation",
        "Budget Goals"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Badges", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Progress Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = PillarGreen)
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🏅", fontSize = 40.sp)
                    Text(
                        "$earnedCount / $totalBadges Badges Earned",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    LinearProgressIndicator(
                        progress   = { earnedCount.toFloat() / totalBadges },
                        modifier   = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .padding(horizontal = 8.dp),
                        color      = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        "${"%.0f".format(earnedCount.toFloat() / totalBadges * 100)}% complete",
                        fontSize = 13.sp,
                        color    = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Badge Sections
            categoryOrder.forEach { category ->
                val badges = grouped[category] ?: return@forEach

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    // Section header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text       = category,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color      = PillarOnSurface
                        )
                        val earnedInCategory = badges.count { it.isEarned }
                        Text(
                            text    = "$earnedInCategory/${badges.size}",
                            fontSize = 12.sp,
                            color   = PillarGrey
                        )
                    }

                    // 2-column grid
                    badges.chunked(2).forEach { row ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { badge ->
                                BadgeCard(
                                    item     = badge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill empty space if odd number of badges in row
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Badge Card

@Composable
fun BadgeCard(
    item: BadgeDisplayItem,
    modifier: Modifier = Modifier
) {
    val isEarned = item.isEarned
    val isNew    = item.isNew

    val bgColor     = if (isEarned) MaterialTheme.colorScheme.surface else PillarSurface
    val borderColor = when {
        isNew    -> PillarAccent
        isEarned -> PillarGreenPale
        else     -> Color.Transparent
    }

    Card(
        modifier  = modifier.then(
            if (borderColor != Color.Transparent)
                Modifier.border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            else Modifier
        ),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (isEarned) 2.dp else 0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // NEW dot
            if (isNew) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(PillarAccent, RoundedCornerShape(50))
                            .align(Alignment.TopEnd)
                    )
                }
            }

            // Emoji
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .size(56.dp)
                    .background(
                        if (isEarned) PillarGreenPale else Color(0xFFF0F0F0),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Text(
                    text     = if (isEarned) item.type.emoji else "🔒",
                    fontSize = 28.sp
                )
            }

            // Title
            Text(
                text       = item.type.title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = if (isEarned) PillarOnSurface else PillarGrey,
                textAlign  = TextAlign.Center
            )

            // Description
            Text(
                text      = item.type.description,
                fontSize  = 11.sp,
                color     = PillarGrey,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )

            // Earned date or locked label
            if (isEarned && item.earnedBadge != null) {
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(Date(item.earnedBadge.earnedAt))
                Text(
                    text       = "Earned $dateStr",
                    fontSize   = 10.sp,
                    color      = PillarGreen,
                    fontWeight = FontWeight.Medium,
                    textAlign  = TextAlign.Center
                )
            } else {
                Text(
                    text      = "Not yet earned",
                    fontSize  = 10.sp,
                    color     = PillarGrey.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}