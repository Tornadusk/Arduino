package com.victorgangas.arduinotb.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var includeEmail by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    // Observar el estado de autenticación
    val authState by viewModel.authState.collectAsState()
    
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            // La navegación se manejará en el MainActivity
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta") },
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
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Registro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Crea tu cuenta para acceder a la app",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )
            
            // Campo de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    viewModel.clearError()
                },
                label = { Text("Usuario") },
                supportingText = { Text("Mínimo 3 caracteres") },
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
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (includeEmail) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { 
                        focusManager.clearFocus()
                        if (!includeEmail) {
                            viewModel.register(username, password, confirmPassword, null)
                        }
                    }
                )
            )
            
            // Switch para incluir email
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Vincular correo electrónico? (Opcional)",
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp
                )
                Switch(
                    checked = includeEmail,
                    onCheckedChange = { includeEmail = it }
                )
            }
            
            // Campo de email (opcional)
            if (includeEmail) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        viewModel.clearError()
                    },
                    label = { Text("Correo Electrónico") },
                    supportingText = { Text("Para recuperación de contraseña") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { 
                            focusManager.clearFocus()
                            viewModel.register(
                                username, 
                                password, 
                                confirmPassword, 
                                if (email.isBlank()) null else email
                            )
                        }
                    )
                )
            }
            
            // Mensaje de error
            if (viewModel.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón de registro
            Button(
                onClick = { 
                    viewModel.register(
                        username, 
                        password, 
                        confirmPassword, 
                        if (includeEmail && email.isNotBlank()) email else null
                    )
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
                    Text("Crear Cuenta", fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Texto de información
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ℹ️ Información importante",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Cada usuario tiene su propia sesión privada\n" +
                              "• Tu contraseña se almacena de forma segura\n" +
                              "• El correo es opcional pero recomendado para recuperar tu contraseña",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

