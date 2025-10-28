package com.victorgangas.arduinotb.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.victorgangas.arduinotb.ui.auth.AuthState
import com.victorgangas.arduinotb.ui.auth.AuthViewModel
import com.victorgangas.arduinotb.ui.auth.LoginScreen
import com.victorgangas.arduinotb.ui.auth.RegisterScreen
import com.victorgangas.arduinotb.ui.auth.ForgotPasswordScreen
import androidx.compose.ui.Modifier
import com.victorgangas.arduinotb.BluetoothViewModel
import com.victorgangas.arduinotb.ui.profile.ProfileScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Main : Screen("main")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)
    
    // Determinar la pantalla inicial basada en el estado de autenticación
    val startDestination = if (isLoggedIn) Screen.Main.route else Screen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Pantalla de Login
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
            
            // Navegar a Main cuando el usuario esté autenticado
            if (authState is AuthState.Authenticated) {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
        
        // Pantalla de Registro
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
            
            // Navegar a Main cuando el usuario se registre exitosamente
            if (authState is AuthState.Authenticated) {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
        
        // Pantalla de Recuperación de Contraseña
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Pantalla Principal (Arduino BT Controller)
        composable(Screen.Main.route) {
            com.victorgangas.arduinotb.AppScreen(
                vm = bluetoothViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
            
            // Navegar a Login si el usuario cierra sesión
            if (authState is AuthState.Unauthenticated) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                }
            }
        }
        
        // Pantalla de Perfil
        composable(Screen.Profile.route) {
            val userId by authViewModel.userId.collectAsState(initial = null)
            
            userId?.let { id ->
                ProfileScreen(
                    authViewModel = authViewModel,
                    userId = id,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

