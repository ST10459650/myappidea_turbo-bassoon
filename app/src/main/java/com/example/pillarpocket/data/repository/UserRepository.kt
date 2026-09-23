package com.example.pillarpocket.data.repository

import com.example.pillarpocket.data.local.User
import com.example.pillarpocket.data.local.UserDao
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun register(username: String, email: String, password: String
    ): Result<Unit> {
        return try {
            val existing = userDao.getUserByUsername(username)
            if (existing != null) {
                Result.failure(Exception("Username already taken"))
            } else {
                userDao.insertUser(
                    User(
                        username = username,
                        email = email.trim(),
                        passwordHash = hashPassword(password)
                    )
                )
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val user = userDao.getUserByUsername(username)
            when {
                user == null -> Result.failure(Exception("Username not found"))
                user.passwordHash != hashPassword(password) -> Result.failure(Exception("Incorrect password"))
                else -> Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}