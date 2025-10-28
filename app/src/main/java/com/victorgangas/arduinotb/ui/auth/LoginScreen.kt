package com.victorgangas.arduinotb.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // Observar errores
    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage != null) {
            // El error se mostrará en la UI
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar Sesión") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "¡Bienvenido!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Inicia sesión para continuar",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Campo de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    viewModel.clearError()
                },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            // Campo de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    viewModel.clearError()
                },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Filled.Visibility 
                            else 
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) 
                                "Ocultar contraseña" 
                            else 
                                "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        viewModel.login(username, password)
                    }
                )
            )
            
            // Mensaje de error
            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Start
                )
            }
            
            // ¿Olvidaste tu contraseña?
            TextButton(
                onClick = onNavigateToForgotPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("¿Olvidaste tu contraseña?")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de inicio de sesión
            Button(
                onClick = { viewModel.login(username, password) },
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Iniciar Sesión", fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Divider(modifier = Modifier.weight(1f))
                Text(
                    text = "o",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.Divider(modifier = Modifier.weight(1f))
            }
            
            // Botón de registro
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Crear Cuenta Nueva", fontSize = 16.sp)
            }
        }
    }
}

