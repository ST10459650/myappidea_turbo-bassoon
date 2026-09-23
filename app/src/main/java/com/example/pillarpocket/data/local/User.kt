package com.example.pillarpocket.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 -Represents a registered user in the Pillar Pocket app.

 -Each user has a unique auto-generated ID, a username used for login,
   an email address provided during registration, and a hashed password
    for secure local authentication.

 -This entity maps directly to the "users" table in the Room database.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) //Auto-generated unique identifier for the user.Serves as the primary key across all user-related tables.
    val id: Int = 0,
    val username: String, //The user's chosen display name and login identifier. Must be unique and at least 3 characters long.
    val email: String, //The user's email address, collected during registration. Validated against a standard email format before being saved.

    /*
     -A SHA-256 hash of the user's password.
     -The raw password is never stored — only its hash is persisted
       to protect user credentials in local storage.
     */
    val passwordHash: String
)
