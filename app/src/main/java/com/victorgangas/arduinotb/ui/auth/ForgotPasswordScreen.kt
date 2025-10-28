package com.victorgangas.arduinotb.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // Observar el estado de autenticación
    val authState by viewModel.authState.collectAsState()
    
    // Mostrar diálogo de éxito cuando se resetee la contraseña
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(authState) {
        if (authState is AuthState.PasswordReset) {
            showSuccessDialog = true
        }
    }
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                viewModel.resetAuthState()
                onNavigateBack()
            },
            title = { Text("¡Éxito!") },
            text = { Text("Tu contraseña ha sido actualizada correctamente. Ahora puedes iniciar sesión con tu nueva contraseña.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSuccessDialog = false
                        viewModel.resetAuthState()
                        onNavigateBack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
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
                text = "Restablecer Contraseña",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Ingresa tu correo electrónico registrado y tu nueva contraseña",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )
            
            // Campo de email
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    viewModel.clearError()
                },
                label = { Text("Correo Electrónico") },
                supportingText = { Text("Debe estar registrado en tu cuenta") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            // Campo de nueva contraseña
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    viewModel.clearError()
                },
                label = { Text("Nueva Contraseña") },
                supportingText = { Text("Mínimo 6 caracteres") },
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
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            
            // Campo de confirmar contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    viewModel.clearError()
                },
                label = { Text("Confirmar Contraseña") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) 
                                Icons.Filled.Visibility 
                            else 
                                Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) 
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
                        viewModel.resetPassword(email, newPassword, confirmPassword)
                    }
                )
            )
            
            // Mensaje de error
            if (viewModel.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Botón de restablecer
            Button(
                onClick = { 
                    viewModel.resetPassword(email, newPassword, confirmPassword)
                },
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
                    Text("Restablecer Contraseña", fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Información
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ℹ️ Nota importante",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Para recuperar tu contraseña necesitas haber vinculado un correo electrónico a tu cuenta durante el registro.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

