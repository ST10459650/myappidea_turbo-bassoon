package com.example.pillarpocket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.pillarpocket.PillarPocketApp
import com.example.pillarpocket.data.preferences.UserPreferences
import com.example.pillarpocket.ui.screens.*
import com.example.pillarpocket.viewmodel.AuthViewModel
import com.example.pillarpocket.viewmodel.AuthViewModelFactory

object Routes {
    const val LOGIN             = "login"
    const val REGISTER          = "register"
    const val HOME              = "home/{userId}/{username}"
    const val CATEGORIES        = "categories/{userId}"
    const val EXPENSES          = "expenses/{userId}"
    const val ADD_EXPENSE       = "add_expense/{userId}"
    const val BUDGET_GOALS      = "budget_goals/{userId}"
    const val CATEGORY_SPENDING = "category_spending/{userId}"
    const val SPENDING_GRAPH    = "spending_graph/{userId}"
    const val DASHBOARD         = "dashboard/{userId}"
    const val BADGES            = "badges/{userId}"
    const val MONTHLY_REPORT    = "monthly_report/{userId}"

    fun homeRoute(userId: Int, username: String) = "home/$userId/$username"
    fun categoriesRoute(userId: Int)             = "categories/$userId"
    fun expensesRoute(userId: Int)               = "expenses/$userId"
    fun addExpenseRoute(userId: Int)             = "add_expense/$userId"
    fun budgetGoalsRoute(userId: Int)            = "budget_goals/$userId"
    fun categorySpendingRoute(userId: Int)       = "category_spending/$userId"
    fun spendingGraphRoute(userId: Int)          = "spending_graph/$userId"
    fun dashboardRoute(userId: Int)              = "dashboard/$userId"
    fun badgesRoute(userId: Int)                 = "badges/$userId"
    fun monthlyReportRoute(userId: Int)          = "monthly_report/$userId"
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val context       = LocalContext.current
    val app           = context.applicationContext as PillarPocketApp
    val userPrefs     = UserPreferences(context)
    val authViewModel = viewModel<AuthViewModel>(
        factory = AuthViewModelFactory(app.userRepository)
    )

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel        = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess       = { user ->
                    userPrefs.saveUserId(user.id)
                    navController.navigate(Routes.homeRoute(user.id, user.username)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel     = authViewModel,
                onNavigateToLogin = { authViewModel.resetState(); navController.popBackStack() },
                onRegisterSuccess = { user ->
                    userPrefs.saveUserId(user.id)
                    navController.navigate(Routes.homeRoute(user.id, user.username)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route     = Routes.HOME,
            arguments = listOf(
                navArgument("userId")   { type = NavType.IntType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId   = backStackEntry.arguments?.getInt("userId") ?: 0
            val username = backStackEntry.arguments?.getString("username") ?: ""
            HomeScreen(
                userId                       = userId,
                username                     = username,
                onNavigateToDashboard        = { navController.navigate(Routes.dashboardRoute(userId)) },
                onNavigateToCategories       = { navController.navigate(Routes.categoriesRoute(userId)) },
                onNavigateToExpenses         = { navController.navigate(Routes.expensesRoute(userId)) },
                onNavigateToBudgetGoals      = { navController.navigate(Routes.budgetGoalsRoute(userId)) },
                onNavigateToCategorySpending = { navController.navigate(Routes.categorySpendingRoute(userId)) },
                onNavigateToSpendingGraph    = { navController.navigate(Routes.spendingGraphRoute(userId)) },
                onNavigateToBadges           = { navController.navigate(Routes.badgesRoute(userId)) },
                onNavigateToMonthlyReport    = { navController.navigate(Routes.monthlyReportRoute(userId)) },
                onLogout                     = {
                    userPrefs.clearUserId()
                    authViewModel.resetState()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route     = Routes.DASHBOARD,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            DashboardScreen(
                userId                  = userId,
                onBack                  = { navController.popBackStack() },
                onNavigateToBudgetGoals = { navController.navigate(Routes.budgetGoalsRoute(userId)) }
            )
        }

        composable(
            route     = Routes.CATEGORIES,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            CategoriesScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        composable(
            route     = Routes.EXPENSES,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            ExpensesScreen(
                userId       = userId,
                onBack       = { navController.popBackStack() },
                onAddExpense = { navController.navigate(Routes.addExpenseRoute(userId)) }
            )
        }

        composable(
            route     = Routes.ADD_EXPENSE,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            AddExpenseScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        composable(
            route     = Routes.BUDGET_GOALS,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            BudgetGoalScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        composable(
            route     = Routes.CATEGORY_SPENDING,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            CategorySpendingScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        composable(
            route     = Routes.SPENDING_GRAPH,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            SpendingGraphScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        composable(
            route     = Routes.BADGES,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            BadgesScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        composable(
            route     = Routes.MONTHLY_REPORT,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            MonthlyReportScreen(userId = userId, onBack = { navController.popBackStack() })
        }
    }
}