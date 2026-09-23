package com.example.pillarpocket.data.local

/*
 * Defines all available badge types in Pillar Pocket.
 * Each badge has a title, description, emoji, and category
 * for display on the Badges screen.
 */
enum class BadgeType(
    val title: String,
    val description: String,
    val emoji: String,
    val category: String
) {
    // Getting Started
    WELCOME(
        "Welcome!",
        "Joined Pillar Pocket",
        "👋",
        "Getting Started"
    ),

    // Expense Logging
    FIRST_EXPENSE(
        "First Step",
        "Logged your very first expense",
        "⭐",
        "Expense Logging"
    ),
    FIVE_EXPENSES(
        "On a Roll",
        "Logged 5 expenses",
        "🔥",
        "Expense Logging"
    ),
    TEN_EXPENSES(
        "Dedicated Logger",
        "Logged 10 expenses",
        "🚀",
        "Expense Logging"
    ),
    TWENTY_FIVE_EXPENSES(
        "Expense Tracker",
        "Logged 25 expenses",
        "💪",
        "Expense Logging"
    ),
    FIFTY_EXPENSES(
        "Expense Master",
        "Logged 50 expenses",
        "🏆",
        "Expense Logging"
    ),
    RECEIPT_COLLECTOR(
        "Receipt Keeper",
        "Attached a photo to an expense",
        "📸",
        "Expense Logging"
    ),

    // Organisation
    FIRST_CATEGORY(
        "Organised",
        "Created your first category",
        "📂",
        "Organisation"
    ),
    FIVE_CATEGORIES(
        "Category Pro",
        "Created 5 or more categories",
        "🗂️",
        "Organisation"
    ),

    // Budget Goals
    FIRST_GOAL(
        "Goal Setter",
        "Set your first budget goal",
        "🎯",
        "Budget Goals"
    ),
    WITHIN_BUDGET(
        "On Target",
        "Stayed within budget for a month",
        "✅",
        "Budget Goals"
    ),
    THREE_MONTHS_BUDGET(
        "Budget Champion",
        "Stayed within budget for 3 months",
        "🥇",
        "Budget Goals"
    )
}