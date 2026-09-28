package org.yodgorbek.fieldai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.yodgorbek.fieldai.ui.assistant.AssistantScreen

// Typed Navigation Schema (Phase 37)

sealed interface Route {
    @Serializable
    data object Splash : Route
    
    @Serializable
    data object Login : Route

    @Serializable
    data object Home : Route
    
    @Serializable
    data class Assistant(val conversationId: String? = null) : Route
    @Serializable
    data object DocumentList : Route
    
    @Serializable
    data object ContactPicker : Route
    
    @Serializable
    data object CalendarAgenda : Route
    
    @Serializable
    data object MapView : Route
    
    @Serializable
    data object DeviceManagement : Route
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: Route = Route.Splash
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Route.Splash> {
            org.yodgorbek.fieldai.ui.screens.SplashScreen(onSplashComplete = {
                navController.navigate(Route.Login) {
                    popUpTo(Route.Splash) { inclusive = true }
                }
            })
        }
        
        composable<Route.Login> {
            org.yodgorbek.fieldai.ui.screens.LoginScreen(onLoginSuccess = {
                navController.navigate(Route.Home) {
                    popUpTo(Route.Login) { inclusive = true }
                }
            })
        }

        composable<Route.Home> {
            org.yodgorbek.fieldai.ui.screens.HomeScreen(
                onNavigateToAssistant = { navController.navigate(Route.Assistant()) },
                onNavigateToDocuments = { navController.navigate(Route.DocumentList) },
                onNavigateToContacts = { navController.navigate(Route.ContactPicker) },
                onNavigateToCalendar = { navController.navigate(Route.CalendarAgenda) },
                onNavigateToMap = { navController.navigate(Route.MapView) },
                onNavigateToDevices = { navController.navigate(Route.DeviceManagement) }
            )
        }
        
        composable<Route.Assistant> {
            AssistantScreen()
        }
        
        composable<Route.DocumentList> {
            org.yodgorbek.fieldai.ui.screens.DocumentListScreen(onBackClick = { navController.popBackStack() })
        }
        
        composable<Route.ContactPicker> {
            org.yodgorbek.fieldai.ui.screens.ContactPickerScreen(onBackClick = { navController.popBackStack() })
        }
        
        composable<Route.CalendarAgenda> {
            org.yodgorbek.fieldai.ui.screens.CalendarAgendaScreen(onBackClick = { navController.popBackStack() })
        }
        
        composable<Route.MapView> {
            org.yodgorbek.fieldai.ui.screens.MapViewScreen(onBackClick = { navController.popBackStack() })
        }
        
        composable<Route.DeviceManagement> {
            org.yodgorbek.fieldai.ui.screens.DeviceManagementScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
