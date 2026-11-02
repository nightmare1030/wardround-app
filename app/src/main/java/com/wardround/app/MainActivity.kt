package com.wardround.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wardround.app.ui.HistoryScreen
import com.wardround.app.ui.HomeScreen
import com.wardround.app.ui.PatientDetailScreen
import com.wardround.app.ui.PatientEditScreen
import com.wardround.app.ui.DeletedPatientScreen
import com.wardround.app.ui.PatientListScreen
import com.wardround.app.ui.SettingsScreen
import com.wardround.app.ui.TemplateScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WardRoundTheme {
                WardRoundNavHost()
            }
        }
    }
}

@Composable
fun WardRoundNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) }
    ) {
        composable("home") {
            HomeScreen(
                onOpenPatientFocus = { id -> navController.navigate("patient_detail/$id?tab=1") },
                onOpenPatients = { navController.navigate("patients") },
                onOpenSettings = { navController.navigate("settings") },
                onOpenDischarged = { navController.navigate("discharged") }
            )
        }
        composable("patients") {
            PatientListScreen(
                onBack = { navController.popBackStack() },
                onAddPatient = { navController.navigate("patient_edit/-1") },
                onOpenPatient = { id -> navController.navigate("patient_detail/$id") }
            )
        }
        composable("discharged") {
            PatientListScreen(
                onBack = { navController.popBackStack() },
                onAddPatient = {},
                onOpenPatient = { id -> navController.navigate("patient_detail/$id") },
                statusFilter = "出院"
            )
        }
        composable(
            route = "patient_detail/{patientId}?tab={tab}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType }, navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: -1L
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            PatientDetailScreen(
                patientId = patientId,
                initialTab = initialTab,
                onBack = { navController.popBackStack() },
                onEditPatient = { id -> navController.navigate("patient_edit/$id") }
            )
        }
        composable(
            route = "patient_edit/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: -1L
            PatientEditScreen(
                patientId = patientId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenHistory = { navController.navigate("history") },
                onOpenTemplates = { navController.navigate("templates") },
                onOpenDeletedPatients = { navController.navigate("deleted_patients") }
            )
        }
        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("templates") {
            TemplateScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("deleted_patients") {
            DeletedPatientScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun WardRoundTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF0A84FF),
            secondary = Color(0xFF5E5CE6),
            tertiary = Color(0xFFFF9F0A),
            background = Color(0xFF000000),
            surface = Color(0xFF1C1C1E),
            surfaceVariant = Color(0xFF2C2C2E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF007AFF),
            secondary = Color(0xFF5856D6),
            tertiary = Color(0xFFFF9500),
            background = Color(0xFFF2F2F7),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF2F2F7)
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

