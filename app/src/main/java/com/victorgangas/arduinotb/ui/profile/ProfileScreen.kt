package com.victorgangas.arduinotb.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.victorgangas.arduinotb.data.local.entity.User
import com.victorgangas.arduinotb.ui.auth.AuthState
import com.victorgangas.arduinotb.ui.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    userId: Int,
    onNavigateBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showChangePasswordSection by remember { mutableStateOf(false) }
    var userInfo by remember { mutableStateOf<User?>(null) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    // Observar el estado de autenticación
    val authState by authViewModel.authState.collectAsState()
    val username by authViewModel.username.collectAsState(initial = "")
    val email by authViewModel.userEmail.collectAsState(initial = "")
    
    // Diálogo de éxito al cambiar contraseña
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(authState) {
        if (authState is AuthState.PasswordChanged) {
            showSuccessDialog = true
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
            showChangePasswordSection = false
        }
    }
    
    // Diálogo de confirmación
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                authViewModel.resetAuthState()
            },
            title = { Text("¡Contraseña Actualizada!") },
            text = { Text("Tu contraseña ha sido cambiada exitosamente.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSuccessDialog = false
                        authViewModel.resetAuthState()
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
                title = { Text("Mi Perfil") },
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Información de la Cuenta",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Card: Información del Usuario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Datos del Usuario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Usuario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Usuario",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = username ?: "Cargando...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    androidx.compose.material3.Divider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Email
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Correo Electrónico",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = email?.takeIf { it.isNotBlank() } ?: "No vinculado",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (email.isNullOrBlank()) 
                                    MaterialTheme.colorScheme.onSurfaceVariant 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            // Botón para mostrar/ocultar sección de cambio de contraseña
            Button(
                onClick = { showChangePasswordSection = !showChangePasswordSection },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (showChangePasswordSection) 
                        "Cancelar Cambio de Contraseña" 
                    else 
                        "Cambiar Contraseña",
                    fontSize = 16.sp
                )
            }
            
            // Sección de cambio de contraseña (colapsable)
            if (showChangePasswordSection) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Cambiar Contraseña",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Contraseña actual
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { 
                                currentPassword = it
                                authViewModel.clearError()
                            },
                            label = { Text("Contraseña Actual") },
                            singleLine = true,
                            visualTransformation = if (currentPasswordVisible) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { 
                                    currentPasswordVisible = !currentPasswordVisible 
                                }) {
                                    Icon(
                                        imageVector = if (currentPasswordVisible) 
                                            Icons.Filled.Visibility 
                                        else 
                                            Icons.Filled.VisibilityOff,
                                        contentDescription = if (currentPasswordVisible) 
                                            "Ocultar contraseña" 
                                        else 
                                            "Mostrar contraseña"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                        
                        // Nueva contraseña
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { 
                                newPassword = it
                                authViewModel.clearError()
                            },
                            label = { Text("Nueva Contraseña") },
                            supportingText = { Text("Mínimo 6 caracteres") },
                            singleLine = true,
                            visualTransformation = if (newPasswordVisible) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { 
                                    newPasswordVisible = !newPasswordVisible 
                                }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) 
                                            Icons.Filled.Visibility 
                                        else 
                                            Icons.Filled.VisibilityOff,
                                        contentDescription = if (newPasswordVisible) 
                                            "Ocultar contraseña" 
                                        else 
                                            "Mostrar contraseña"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                        
                        // Confirmar nueva contraseña
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                authViewModel.clearError()
                            },
                            label = { Text("Confirmar Nueva Contraseña") },
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { 
                                    confirmPasswordVisible = !confirmPasswordVisible 
                                }) {
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
                                    authViewModel.changePassword(
                                        userId,
                                        currentPassword,
                                        newPassword,
                                        confirmPassword
                                    )
                                }
                            )
                        )
                        
                        // Mensaje de error
                        if (authViewModel.errorMessage != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = authViewModel.errorMessage!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Botón guardar contraseña
                        Button(
                            onClick = {
                                authViewModel.changePassword(
                                    userId,
                                    currentPassword,
                                    newPassword,
                                    confirmPassword
                                )
                            },
                            enabled = !authViewModel.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (authViewModel.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Guardar Nueva Contraseña", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Card informativo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ℹ️ Información",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Tu contraseña está protegida con encriptación SHA-256\n" +
                              "• Recomendamos usar una contraseña segura de al menos 8 caracteres\n" +
                              "• Si olvidas tu contraseña, puedes recuperarla con tu email",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

