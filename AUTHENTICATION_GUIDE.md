# Guía del Sistema de Autenticación

## 📱 Descripción General

Se ha implementado un sistema completo de autenticación para tu aplicación Arduino BT Controller. Ahora cada usuario puede crear su propia cuenta y acceder de forma privada a la aplicación, similar a un bloqueo de celular.

## ✨ Características Implementadas

### 1. **Registro de Usuarios**
- Cada usuario puede crear su propia cuenta con usuario y contraseña
- Opción de vincular correo electrónico (opcional pero recomendado)
- Validaciones de seguridad:
  - Usuario: mínimo 3 caracteres
  - Contraseña: mínimo 6 caracteres
  - Verificación de contraseñas coincidentes
- Contraseñas almacenadas con hash SHA-256 para seguridad

### 2. **Inicio de Sesión**
- Login con usuario y contraseña
- Sesión persistente (no necesitas ingresar credenciales cada vez)
- Mensajes de error claros

### 3. **Recuperación de Contraseña**
- Permite restablecer la contraseña usando el correo electrónico registrado
- Requiere que el usuario haya vinculado un email durante el registro

### 4. **Múltiples Usuarios**
- Cada usuario tiene su propia sesión independiente
- Base de datos local (Room) que almacena múltiples usuarios
- Cada usuario puede acceder solo con sus credenciales

### 5. **Cierre de Sesión**
- Opción de cerrar sesión desde el menú de la aplicación principal
- Limpia la sesión actual de forma segura

## 🏗️ Arquitectura Técnica

### Componentes Principales

```
├── data/
│   ├── local/
│   │   ├── entity/User.kt          # Modelo de datos del usuario
│   │   ├── dao/UserDao.kt           # Operaciones de base de datos
│   │   └── database/AppDatabase.kt  # Base de datos Room
│   ├── preferences/
│   │   └── UserPreferences.kt       # Gestión de sesión con DataStore
│   └── repository/
│       └── AuthRepository.kt        # Lógica de autenticación
├── ui/
│   └── auth/
│       ├── AuthViewModel.kt         # ViewModel de autenticación
│       ├── LoginScreen.kt           # Pantalla de inicio de sesión
│       ├── RegisterScreen.kt        # Pantalla de registro
│       └── ForgotPasswordScreen.kt  # Pantalla de recuperación
└── navigation/
    └── NavGraph.kt                  # Configuración de navegación
```

### Tecnologías Utilizadas

1. **Room Database**: Base de datos local para almacenar usuarios
2. **DataStore**: Almacenamiento de preferencias para sesión persistente
3. **Navigation Compose**: Navegación entre pantallas
4. **Material Design 3**: UI moderna y atractiva
5. **Jetpack Compose**: Framework de UI declarativo
6. **Coroutines & Flow**: Manejo asíncrono de datos

## 🚀 Flujo de Usuario

### Primera vez (Registro)
1. Usuario abre la app
2. Ve la pantalla de Login
3. Selecciona "Crear Cuenta Nueva"
4. Ingresa usuario, contraseña y opcionalmente email
5. Se crea la cuenta y accede automáticamente a la app

### Usuarios Existentes (Login)
1. Usuario abre la app
2. Si tiene sesión activa → accede directamente a la app
3. Si no tiene sesión → ve pantalla de Login
4. Ingresa credenciales y accede a la app

### Recuperación de Contraseña
1. Usuario selecciona "¿Olvidaste tu contraseña?"
2. Ingresa el email registrado
3. Ingresa nueva contraseña
4. Sistema valida y actualiza la contraseña
5. Puede iniciar sesión con la nueva contraseña

## 🔐 Seguridad

- **Contraseñas hasheadas**: Uso de SHA-256 para almacenar contraseñas
- **Validaciones del lado del cliente**: Verificación antes de enviar datos
- **Sesión persistente segura**: DataStore encriptado
- **Usuarios aislados**: Cada usuario solo accede a su sesión

## 📝 Uso de la Aplicación

### Crear Primera Cuenta
```
1. Abrir la app
2. Clic en "Crear Cuenta Nueva"
3. Ingresar:
   - Usuario: mínimo 3 caracteres
   - Contraseña: mínimo 6 caracteres
   - Confirmar contraseña
   - (Opcional) Email para recuperación
4. Clic en "Crear Cuenta"
5. ¡Listo! Acceso automático a la app
```

### Vincular Email (Recomendado)
```
Durante el registro:
1. Activar el switch "¿Vincular correo electrónico?"
2. Ingresar email válido
3. Este email se usará para recuperar la contraseña
```

### Cerrar Sesión
```
1. En la pantalla principal de la app
2. Clic en el menú (⋮) arriba a la derecha
3. Seleccionar "Cerrar Sesión"
4. Confirmación automática
```

### Cambiar de Usuario
```
1. Cerrar sesión del usuario actual
2. En Login, ingresar credenciales del otro usuario
   O
3. Crear nueva cuenta si es un usuario nuevo
```

## ⚙️ Configuración y Personalización

### Cambiar Requisitos de Contraseña
Editar en `AuthViewModel.kt`:
```kotlin
if (password.length < 6) { // Cambiar el 6 por el mínimo deseado
    errorMessage = "La contraseña debe tener al menos X caracteres"
    return
}
```

### Modificar Algoritmo de Hash
Editar en `AuthRepository.kt`:
```kotlin
private fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256") // Cambiar algoritmo aquí
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}
```

## 🐛 Solución de Problemas

### "Usuario o contraseña incorrectos"
- Verificar que las credenciales sean correctas
- Recordar que son sensibles a mayúsculas/minúsculas

### "No existe una cuenta con este correo electrónico"
- Asegurarse de haber vinculado un email durante el registro
- Verificar que el email ingresado sea correcto

### La app no recuerda la sesión
- Verificar que DataStore esté funcionando
- Reinstalar la app puede limpiar las preferencias

### Olvidé mi contraseña y no tengo email
- Si no vinculaste un email, necesitarás crear una nueva cuenta
- La contraseña está encriptada y no puede recuperarse

## 🎨 Personalización de UI

Los colores y estilos se pueden modificar en:
- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`

## 📊 Base de Datos

La base de datos se crea automáticamente en:
```
/data/data/com.victorgangas.arduinotb/databases/arduino_app_database
```

### Estructura de la tabla `users`:
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    password TEXT NOT NULL,
    email TEXT,
    createdAt INTEGER NOT NULL
);
```

## 🔄 Actualizaciones Futuras Sugeridas

1. **Envío de email real**: Integrar con servicio de email (SendGrid, etc.)
2. **Biometría**: Agregar huella digital o reconocimiento facial
3. **2FA**: Autenticación de dos factores
4. **Recuperación por SMS**: Alternativa al email
5. **Foto de perfil**: Personalización de cuenta
6. **Gestión de sesiones**: Ver dispositivos conectados
7. **Cambio de contraseña**: Desde la app sin necesidad de recuperación

## 💡 Mejores Prácticas

1. **Siempre vincular un email** durante el registro
2. **Usar contraseñas seguras** con combinación de caracteres
3. **Cerrar sesión** en dispositivos compartidos
4. **No compartir credenciales** entre usuarios
5. **Mantener backup** de credenciales importantes

## 📞 Soporte

Para cualquier problema o sugerencia:
1. Revisar esta guía primero
2. Verificar logs en Android Studio
3. Consultar la documentación de Room, DataStore y Navigation Compose

---

**Versión**: 1.0  
**Última actualización**: Octubre 2025  
**Desarrollado con**: Kotlin + Jetpack Compose + Room + DataStore

