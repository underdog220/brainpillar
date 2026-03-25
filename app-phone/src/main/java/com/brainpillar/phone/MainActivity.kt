package com.brainpillar.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.brainpillar.phone.ui.screens.ExportDashboardScreen
import com.brainpillar.phone.ui.screens.PhotoGalleryScreen
import com.brainpillar.phone.ui.screens.ProjectListScreen
import com.brainpillar.phone.ui.screens.TranscriptScreen
import com.brainpillar.phone.ui.theme.BrainPillarPhoneTheme

/**
 * Navigations-Routen fuer die Bottom Navigation.
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Projekte : BottomNavItem("projekte", "Projekte", Icons.Default.FolderOpen)
    data object Transkript : BottomNavItem("transkript", "Transkript", Icons.Default.Description)
    data object Fotos : BottomNavItem("fotos", "Fotos", Icons.Default.CameraAlt)
    data object Export : BottomNavItem("export", "Export", Icons.Default.Upload)
}

// Alle Bottom-Navigation-Eintraege
private val bottomNavItems = listOf(
    BottomNavItem.Projekte,
    BottomNavItem.Transkript,
    BottomNavItem.Fotos,
    BottomNavItem.Export
)

/**
 * Hauptaktivitaet der BrainPillar Phone Companion App.
 * Stellt Navigation mit Bottom Bar und 4 Screens bereit.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrainPillarPhoneTheme(darkTheme = true) {
                BrainPillarApp()
            }
        }
    }
}

/**
 * Haupt-Composable mit NavHost und BottomNavigation.
 */
@Composable
private fun BrainPillarApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Verhindere mehrfaches Aufstapeln derselben Route
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Projekte.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Projekte.route) { ProjectListScreen() }
            composable(BottomNavItem.Transkript.route) { TranscriptScreen() }
            composable(BottomNavItem.Fotos.route) { PhotoGalleryScreen() }
            composable(BottomNavItem.Export.route) { ExportDashboardScreen() }
        }
    }
}
