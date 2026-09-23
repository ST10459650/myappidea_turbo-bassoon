package com.example.pillarpocket.ui.screens

import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.data.local.Expense
import com.example.pillarpocket.ui.components.PillarDatePickerDialog
import com.example.pillarpocket.ui.components.formatDisplayDate
import com.example.pillarpocket.ui.components.iconFromName
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    userId: Int,
    onBack: () -> Unit,
    onAddExpense: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillarPocketApp
    val viewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(app.expenseRepository, app.categoryRepository, userId)
    )

    val filteredExpenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val filteredTotal by viewModel.filteredTotal.collectAsStateWithLifecycle()
    val isFilterActive by viewModel.isFilterActive.collectAsStateWithLifecycle()
    val filterStartDate by viewModel.filterStartDate.collectAsStateWithLifecycle()
    val filterEndDate by viewModel.filterEndDate.collectAsStateWithLifecycle()

    // Photo viewer state: stores (photoUri, description)
    var photoViewerData by remember { mutableStateOf<Pair<String, String>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense, containerColor = PillarGreen) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PillarSurface)
                .padding(innerPadding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PillarGreen)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isFilterActive) "Filtered Total" else "Total Expenses",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R %.2f".format(filteredTotal),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${filteredExpenses.size} transaction(s)" +
                                if (isFilterActive) "  •  ${formatDisplayDate(filterStartDate!!)} – ${formatDisplayDate(filterEndDate!!)}" else "",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Date Range Filter
            DateRangeFilterCard(
                startDate = filterStartDate,
                endDate = filterEndDate,
                isFilterActive = isFilterActive,
                onApply = { start, end -> viewModel.setDateFilter(start, end) },
                onClear = { viewModel.clearFilter() }
            )

            // Expense List
            if (filteredExpenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.ReceiptLong,
                            null,
                            tint = PillarGrey,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isFilterActive) "No expenses in this period" else "No expenses yet",
                            fontSize = 18.sp,
                            color = PillarGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isFilterActive) "Try adjusting the date range" else "Tap + to record your first expense",
                            fontSize = 14.sp,
                            color = PillarGrey
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        val category = categories.find { it.id == expense.categoryId }
                        ExpenseItem(
                            expense = expense,
                            category = category,
                            onPhotoClick = { uri, desc ->
                                photoViewerData = Pair(uri, desc)
                            }
                        )
                    }
                }
            }
        }
    }

    // Photo Viewer Dialog
    photoViewerData?.let { (uri, description) ->
        PhotoViewerDialog(
            photoUri = uri,
            description = description,
            onDismiss = { photoViewerData = null }
        )
    }
}

// Date Range Filter Card

@Composable
fun DateRangeFilterCard(
    startDate: String?,
    endDate: String?,
    isFilterActive: Boolean,
    onApply: (String, String) -> Unit,
    onClear: () -> Unit
) {
    var tempStartDate by remember { mutableStateOf(startDate ?: "") }
    var tempEndDate by remember { mutableStateOf(endDate ?: "") }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var filterError by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(startDate, endDate) {
        tempStartDate = startDate ?: ""
        tempEndDate = endDate ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFilterActive) PillarGreenPale else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header Row
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
                        Icons.Filled.FilterList,
                        contentDescription = null,
                        tint = if (isFilterActive) PillarGreen else PillarGrey,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isFilterActive) "Filter Active" else "Filter by Date Range",
                        fontSize = 14.sp,
                        fontWeight = if (isFilterActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isFilterActive) PillarGreen else PillarGrey
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFilterActive) {
                        TextButton(
                            onClick = { onClear(); filterError = null; expanded = false },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Clear", color = PillarRed, fontSize = 13.sp)
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = PillarGrey
                    )
                }
            }

            // Expandable filter form
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // From Date
                OutlinedTextField(
                    value = if (tempStartDate.isEmpty()) "" else formatDisplayDate(tempStartDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("From") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, tint = PillarGreen) },
                    trailingIcon = {
                        IconButton(onClick = { showStartPicker = true }) {
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }
                    },
                    placeholder = { Text("Select start date") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PillarGreen,
                        focusedLabelColor = PillarGreen
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // To Date
                OutlinedTextField(
                    value = if (tempEndDate.isEmpty()) "" else formatDisplayDate(tempEndDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("To") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null, tint = PillarGreen) },
                    trailingIcon = {
                        IconButton(onClick = { showEndPicker = true }) {
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }
                    },
                    placeholder = { Text("Select end date") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PillarGreen,
                        focusedLabelColor = PillarGreen
                    )
                )

                if (filterError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(filterError!!, color = PillarRed, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        when {
                            tempStartDate.isEmpty() -> filterError = "Please select a start date"
                            tempEndDate.isEmpty() -> filterError = "Please select an end date"
                            tempStartDate > tempEndDate -> filterError = "Start date must be before end date"
                            else -> {
                                filterError = null
                                onApply(tempStartDate, tempEndDate)
                                expanded = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PillarGreen)
                ) {
                    Icon(Icons.Filled.FilterList, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Filter", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showStartPicker) {
        PillarDatePickerDialog(
            onDateSelected = { tempStartDate = it },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        PillarDatePickerDialog(
            onDateSelected = { tempEndDate = it },
            onDismiss = { showEndPicker = false }
        )
    }
}

// Expense item

@Composable
fun ExpenseItem(
    expense: Expense,
    category: Category?,
    onPhotoClick: (uri: String, description: String) -> Unit
) {
    val categoryColor = remember(category?.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(category?.colorHex ?: "#9E9E9E")) }
            .getOrDefault(Color.Gray)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = iconFromName(category?.iconName ?: "Other"),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        expense.description,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PillarOnSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${category?.name ?: "Unknown"} • ${formatDisplayDate(expense.date)}",
                        fontSize = 12.sp,
                        color = PillarGrey
                    )
                    Text(
                        text = "${expense.startTime} – ${expense.endTime}",
                        fontSize = 12.sp,
                        color = PillarGrey
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "R %.2f".format(expense.amount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PillarRed
                    )
                    if (expense.photoUri != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Filled.Photo,
                                contentDescription = "Has receipt",
                                tint = PillarAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text("Receipt", fontSize = 10.sp, color = PillarAccent)
                        }
                    }
                }
            }

            // Tappable receipt thumbnail
            if (expense.photoUri != null) {
                AsyncImage(
                    model = Uri.parse(expense.photoUri),
                    contentDescription = "Receipt — tap to view",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onPhotoClick(expense.photoUri, expense.description) }
                )
                Text(
                    text = "Tap photo to view full size",
                    fontSize = 11.sp,
                    color = PillarGrey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

//Full screen photo viewer

@Composable
fun PhotoViewerDialog(
    photoUri: String,
    description: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Full-size photo
            AsyncImage(
                model = Uri.parse(photoUri),
                contentDescription = "Receipt: $description",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                }
                Text(
                    text = description,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottom label
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Receipt — $description",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}