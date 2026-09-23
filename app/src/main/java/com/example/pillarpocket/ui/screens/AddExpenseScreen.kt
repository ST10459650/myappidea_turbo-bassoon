package com.example.pillarpocket.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.ui.components.*
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.*
import java.io.File

fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("receipt_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    userId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillarPocketApp
    val viewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModelFactory(app.expenseRepository, app.categoryRepository, userId)
    )

    val expenseState by viewModel.expenseState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            photoUri = tempCameraUri.toString()
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { photoUri = it.toString() }
    }

    LaunchedEffect(expenseState) {
        if (expenseState is ExpenseState.Success) {
            viewModel.resetState()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
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

                    // Amount Field
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) amount = it },
                        label = { Text("Amount") },
                        leadingIcon = {
                            Text("R", fontWeight = FontWeight.Bold, color = PillarGreen, modifier = Modifier.padding(start = 4.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillarGreen,
                            focusedLabelColor = PillarGreen
                        )
                    )

                    // Date Picker
                    OutlinedTextField(
                        value = if (selectedDate.isEmpty()) "" else formatDisplayDate(selectedDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        leadingIcon = { Icon(Icons.Filled.CalendarToday, null, tint = PillarGreen) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Filled.ArrowDropDown, null)
                            }
                        },
                        placeholder = { Text("Select date") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillarGreen,
                            focusedLabelColor = PillarGreen
                        )
                    )

                    // Start Time Picker
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Start Time") },
                        leadingIcon = { Icon(Icons.Filled.AccessTime, null, tint = PillarGreen) },
                        trailingIcon = {
                            IconButton(onClick = { showStartTimePicker = true }) {
                                Icon(Icons.Filled.ArrowDropDown, null)
                            }
                        },
                        placeholder = { Text("Select start time") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillarGreen,
                            focusedLabelColor = PillarGreen
                        )
                    )

                    // End Time Picker
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("End Time") },
                        leadingIcon = { Icon(Icons.Filled.AccessTime, null, tint = PillarGreen) },
                        trailingIcon = {
                            IconButton(onClick = { showEndTimePicker = true }) {
                                Icon(Icons.Filled.ArrowDropDown, null)
                            }
                        },
                        placeholder = { Text("Select end time") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillarGreen,
                            focusedLabelColor = PillarGreen
                        )
                    )

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            leadingIcon = { Icon(Icons.Filled.Category, null, tint = PillarGreen) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            placeholder = { Text("Select category") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PillarGreen,
                                focusedLabelColor = PillarGreen
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            if (categories.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No categories — create one first", color = PillarGrey) },
                                    onClick = { categoryExpanded = false }
                                )
                            } else {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Icon(
                                                    imageVector = iconFromName(category.iconName),
                                                    contentDescription = null,
                                                    tint = try {
                                                        Color(android.graphics.Color.parseColor(category.colorHex))
                                                    } catch (e: Exception) { PillarGreen },
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(category.name)
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = category
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Description Field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        leadingIcon = { Icon(Icons.Filled.Description, null, tint = PillarGreen) },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PillarGreen,
                            focusedLabelColor = PillarGreen
                        )
                    )

                    HorizontalDivider()

                    // Photo Attachment Section
                    Text(
                        text = "Receipt Photo (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PillarGrey
                    )

                    if (photoUri != null) {
                        // Show photo preview
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = Uri.parse(photoUri),
                                contentDescription = "Receipt photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, PillarGreenPale, RoundedCornerShape(12.dp))
                            )
                            // Remove photo button
                            IconButton(
                                onClick = { photoUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove photo", tint = Color.White)
                            }
                        }
                    } else {
                        // Show camera / gallery buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PillarGreen)
                            ) {
                                Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera")
                            }
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PillarGreen)
                            ) {
                                Icon(Icons.Filled.Photo, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery")
                            }
                        }
                    }

                    // Error Message
                    if (expenseState is ExpenseState.Error) {
                        Text(
                            text = (expenseState as ExpenseState.Error).message,
                            color = PillarRed,
                            fontSize = 13.sp
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            viewModel.addExpense(
                                categoryId = selectedCategory?.id ?: 0,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                date = selectedDate,
                                startTime = startTime,
                                endTime = endTime,
                                description = description,
                                photoUri = photoUri
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PillarGreen),
                        enabled = expenseState !is ExpenseState.Loading
                    ) {
                        if (expenseState is ExpenseState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Filled.Save, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Expense", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        PillarDatePickerDialog(
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showStartTimePicker) {
        PillarTimePickerDialog(
            title = "Select Start Time",
            onTimeSelected = { startTime = it },
            onDismiss = { showStartTimePicker = false }
        )
    }
    if (showEndTimePicker) {
        PillarTimePickerDialog(
            title = "Select End Time",
            onTimeSelected = { endTime = it },
            onDismiss = { showEndTimePicker = false }
        )
    }
}