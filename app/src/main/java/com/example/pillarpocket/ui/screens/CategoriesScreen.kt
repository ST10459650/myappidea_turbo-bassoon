package com.example.pillarpocket.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.data.local.Category
import com.example.pillarpocket.ui.components.*
import com.example.pillarpocket.ui.theme.*
import com.example.pillarpocket.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    userId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as PillarPocketApp
    val viewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(app.categoryRepository, userId)
    )

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val categoryState by viewModel.categoryState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(categoryState) {
        if (categoryState is CategoryState.Success) {
            showAddDialog = false
            categoryToEdit = null
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { viewModel.resetState(); showAddDialog = true },
                containerColor = PillarGreen
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PillarSurface)
                .padding(innerPadding)
        ) {
            if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Category, null, tint = PillarGrey, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No categories yet", fontSize = 18.sp, color = PillarGrey, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap + to create your first category", fontSize = 14.sp, color = PillarGrey)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryItem(
                            category = category,
                            onEdit = { viewModel.resetState(); categoryToEdit = it },
                            onDelete = { categoryToDelete = it }
                        )
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AddEditCategoryDialog(
            title = "New Category",
            onDismiss = { showAddDialog = false; viewModel.resetState() },
            onConfirm = { name, color, icon -> viewModel.addCategory(name, color, icon) },
            errorMessage = (categoryState as? CategoryState.Error)?.message,
            isLoading = categoryState is CategoryState.Loading
        )
    }

    // Edit Dialog
    categoryToEdit?.let { cat ->
        AddEditCategoryDialog(
            title = "Edit Category",
            initialName = cat.name,
            initialColor = cat.colorHex,
            initialIcon = cat.iconName,
            onDismiss = { categoryToEdit = null; viewModel.resetState() },
            onConfirm = { name, color, icon -> viewModel.updateCategory(cat, name, color, icon) },
            errorMessage = (categoryState as? CategoryState.Error)?.message,
            isLoading = categoryState is CategoryState.Loading
        )
    }

    // Delete Confirmation Dialog
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = PillarRed) },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete \"${cat.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCategory(cat); categoryToDelete = null }) {
                    Text("Delete", color = PillarRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    val color = remember(category.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(category.colorHex)) }
            .getOrDefault(Color.Gray)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = iconFromName(category.iconName),
                    contentDescription = category.name,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = category.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = PillarOnSurface
            )
            IconButton(onClick = { onEdit(category) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = PillarGrey)
            }
            IconButton(onClick = { onDelete(category) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = PillarRed)
            }
        }
    }
}

@Composable
fun AddEditCategoryDialog(
    title: String,
    initialName: String = "",
    initialColor: String = "#2E7D32",
    initialIcon: String = "Other",
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String, iconName: String) -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var selectedIcon by remember { mutableStateOf(initialIcon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PillarGreen,
                        focusedLabelColor = PillarGreen
                    )
                )

                Text("Pick a Colour", fontSize = 13.sp, color = PillarGrey, fontWeight = FontWeight.Medium)
                CategoryColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )

                Text("Pick an Icon", fontSize = 13.sp, color = PillarGrey, fontWeight = FontWeight.Medium)
                CategoryIconPicker(
                    selectedIcon = selectedIcon,
                    selectedColor = selectedColor,
                    onIconSelected = { selectedIcon = it }
                )

                if (errorMessage != null) {
                    Text(errorMessage, color = PillarRed, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedColor, selectedIcon) },
                colors = ButtonDefaults.buttonColors(containerColor = PillarGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}