package com.example.pillarpocket.data.preferences

import android.content.Context

/*
 * Manages lightweight persistent storage for the currently logged-in
 * user's ID using SharedPreferences.
 *
 * This is used by the background WorkManager worker to know which
 * user's data to check when the app is not in the foreground.
 */
class UserPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(
        "pillar_pocket_prefs", Context.MODE_PRIVATE
    )

    /** Saves the logged-in user's ID when they sign in. */
    fun saveUserId(userId: Int) =
        prefs.edit().putInt("current_user_id", userId).apply()

    /**
     * Returns the currently logged-in user's ID.
     * Returns -1 if no user is logged in.
     */
    fun getUserId(): Int = prefs.getInt("current_user_id", -1)

    /** Clears the saved user ID when the user logs out. */
    fun clearUserId() =
        prefs.edit().remove("current_user_id").apply()
}