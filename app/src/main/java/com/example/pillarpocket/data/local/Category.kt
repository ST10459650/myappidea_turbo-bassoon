package com.example.pillarpocket.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 - Represents a user-defined category used to organise expense entries.
 -Each category belongs to a specific user and carries a display name,
  a colour (stored as a hex string), and an icon name that maps to a
 -Material icon in the UI. Categories are shared across both expense
   entries and budget entries.
 -This entity maps directly to the "categories" table in the Room database.
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)  //Auto-generated unique identifier for the category.
    val id: Int = 0,  //The ID of the user this category belongs to. Used to ensure each user only sees their own categories.
    val userId: Int, //The ID of the user this category belongs to. Used to ensure each user only sees their own categories.
    val name: String, //The display name of the category (e.g. "Food", "Transport"). Must be unique per user and cannot be blank.

    /*
     -The background/accent colour of the category stored as a hex colour string (e.g. "#E53935").
     - Used to visually distinguish categories throughout the app.
     */
    val colorHex: String,

    /*
     - The name of the Material icon representing this category
     - (e.g. "Food", "Home", "Transport").
     - Mapped to an actual [ImageVector] in the UI via [iconFromName].
     */
    val iconName: String
)