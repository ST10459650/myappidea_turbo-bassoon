package com.example.pillarpocket.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*
 * The main Room database for the Pillar Pocket application.
 *
 * This class serves as the single source of truth for all local data
 * persistence. It defines the complete set of entities (tables) that
 * make up the database schema and exposes DAO instances for each entity.
 *
 * The database is implemented as a singleton to prevent multiple
 * instances from being created simultaneously, which could cause
 * data inconsistency issues.
 *
 * Current schema version: 7
 * Entities:
 * - [User]       = "users" table
 * - [Category]   = "categories" table
 * - [Expense]    = "expenses" table
 * - [BudgetGoal] = "budget_goals" table
 * - [EarnedBadge] = "earned_badge" table

 * Note: [fallbackToDestructiveMigration] is currently enabled for
 * development convenience. This should be replaced with proper
 * migration strategies before a production release to avoid
 * unintentional data loss on schema updates.
 */
@Database(
    entities = [User::class, Category::class, Expense::class, BudgetGoal::class, EarnedBadge::class],
    version = 7,
    exportSchema = false
)
abstract class PillarPocketDatabase : RoomDatabase() {

    /*
     * Provides access to user authentication operations.
     */
    abstract fun userDao(): UserDao

/*
 * Provides access to category CRUD operations.
 */
    abstract fun categoryDao(): CategoryDao

    /*
     * Provides access to expense CRUD and query operations.
     */
    abstract fun expenseDao(): ExpenseDao

    /*
     * Provides access to budget goal insert and query operations.
     */
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun earnedBadgeDao(): EarnedBadgeDao

    companion object {

        /*
         * The singleton instance of the database.
         *
         * Marked as [@Volatile] to ensure that the value of [INSTANCE]
         * is always up to date and visible to all threads, preventing
         * a situation where one thread reads a cached version while
         * another has already updated it.
         */
        @Volatile
        private var INSTANCE: PillarPocketDatabase? = null

        /*
         * Returns the singleton instance of [PillarPocketDatabase],
         * creating it if it does not yet exist.
         *
         * Uses a double-checked locking pattern inside a [synchronized]
         * block to ensure thread safety during the first instantiation.
         *
         * @param context The application context used to build the database.
         * @return The singleton [PillarPocketDatabase] instance.
         */
        fun getDatabase(context: Context): PillarPocketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PillarPocketDatabase::class.java,
                    "pillar_pocket_database"
                )

                    // Allows Room to destructively recreate the database, if no migration path is found between versions.
                    // Replace with proper migrations before production release.
                    .fallbackToDestructiveMigration()  //proper migration - .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}