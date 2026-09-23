package com.example.pillarpocket.data.local

import androidx.room.*

/*
 -Data Access Object (DAO) for user-related database operations.

 -Provides the interface between the app's data layer and the "users"
  table in the Room database. All authentication queries go through
  this DAO.
 */
@Dao
interface UserDao {

    /*
     -Inserts a new user record into the "users" table.
     -Uses [OnConflictStrategy.ABORT] to prevent duplicate entries —
      if a user with the same primary key already exists, the operation
      will fail and throw an exception, which is caught in the repository.
     -@param user The [User] entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    /*
     -Retrieves a single user by their username.
     -Used during both login (to verify credentials) and registration
     (to check if the username is already taken). Returns null if no
     matching user is found.

     -@param username The username to search for.
     -@return The matching [User], or null if not found.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
}