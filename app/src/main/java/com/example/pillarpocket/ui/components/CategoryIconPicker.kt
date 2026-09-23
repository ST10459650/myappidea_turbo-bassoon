package com.example.pillarpocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class CategoryIconOption(val name: String, val icon: ImageVector)

val categoryIcons = listOf(
    CategoryIconOption("Home",          Icons.Filled.Home),
    CategoryIconOption("Food",          Icons.Filled.Restaurant),
    CategoryIconOption("Transport",     Icons.Filled.DirectionsCar),
    CategoryIconOption("Shopping",      Icons.Filled.ShoppingCart),
    CategoryIconOption("Health",        Icons.Filled.HealthAndSafety),
    CategoryIconOption("Education",     Icons.Filled.School),
    CategoryIconOption("Entertainment", Icons.Filled.Movie),
    CategoryIconOption("Travel",        Icons.Filled.Flight),
    CategoryIconOption("Utilities",     Icons.Filled.ElectricBolt),
    CategoryIconOption("Savings",       Icons.Filled.Savings),
    CategoryIconOption("Work",          Icons.Filled.Work),
    CategoryIconOption("Gifts",         Icons.Filled.CardGiftcard),
    CategoryIconOption("Pets",          Icons.Filled.Pets),
    CategoryIconOption("Sports",        Icons.Filled.SportsSoccer),
    CategoryIconOption("Other",         Icons.Filled.Category)
)

val categoryColors = listOf(
    "#E53935", "#D81B60", "#8E24AA", "#5E35B1",
    "#1E88E5", "#00ACC1", "#00897B", "#43A047",
    "#F4511E", "#FB8C00", "#F9A825", "#6D4C41",
    "#546E7A", "#757575", "#2E7D32"
)

fun iconFromName(name: String): ImageVector =
    categoryIcons.find { it.name == name }?.icon ?: Icons.Filled.Category

@Composable
fun CategoryIconPicker(
    selectedIcon: String,
    selectedColor: String,
    onIconSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier.height(160.dp),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categoryIcons) { option ->
            val isSelected = option.name == selectedIcon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color(android.graphics.Color.parseColor(selectedColor))
                        else Color(android.graphics.Color.parseColor(selectedColor)).copy(alpha = 0.15f)
                    )
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color(android.graphics.Color.parseColor(selectedColor)), CircleShape)
                        else Modifier
                    )
                    .clickable { onIconSelected(option.name) }
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.name,
                    tint = if (isSelected) Color.White
                    else Color(android.graphics.Color.parseColor(selectedColor)),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier.height(100.dp),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categoryColors) { colorHex ->
            val isSelected = colorHex == selectedColor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .then(
                        if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(colorHex) }
            )
        }
    }
}