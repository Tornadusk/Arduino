# Instrucciones de Compilación y Ejecución

## 📋 Requisitos Previos

- **Android Studio**: Versión más reciente (recomendado: Hedgehog o superior)
- **JDK**: Java 11 o superior
- **Gradle**: Se incluye con el proyecto (Gradle Wrapper)
- **Dispositivo Android**: API 24+ (Android 7.0 Nougat o superior)

## 🔧 Configuración Inicial

### 1. Sincronizar el Proyecto

Después de implementar todos los cambios:

```bash
# En Android Studio:
1. File → Sync Project with Gradle Files
   o
2. Clic en el ícono de sincronización (🔄) en la barra de herramientas
```

Esto descargará todas las nuevas dependencias:
- Room (base de datos)
- DataStore (preferencias)
- Navigation Compose
- Security Crypto

### 2. Limpiar y Reconstruir

```bash
# En terminal de Android Studio:
./gradlew clean
./gradlew build

# O usando la UI:
Build → Clean Project
Build → Rebuild Project
```

## 🚀 Ejecutar la Aplicación

### Opción 1: Desde Android Studio

1. Conecta tu dispositivo Android o inicia un emulador
2. Selecciona el dispositivo/emulador en la lista desplegable
3. Clic en el botón "Run" (▶️) o presiona `Shift + F10`

### Opción 2: Línea de Comandos

```bash
# Instalar en dispositivo conectado
./gradlew installDebug

# Ejecutar
adb shell am start -n com.victorgangas.arduinotb/.MainActivity
```

### Opción 3: APK Manual

```bash
# Generar APK
./gradlew assembleDebug

# El APK estará en:
app/build/outputs/apk/debug/app-debug.apk

# Instalar manualmente
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🧪 Probar el Sistema de Autenticación

### Primera Ejecución

1. **Iniciar la app**: Verás la pantalla de Login
2. **Crear primera cuenta**:
   - Clic en "Crear Cuenta Nueva"
   - Usuario: `admin` (o cualquier nombre)
   - Contraseña: `123456` (mínimo 6 caracteres)
   - Email (opcional): `admin@test.com`
   - Clic en "Crear Cuenta"
3. **Acceso automático**: Serás redirigido a la pantalla principal

### Probar Múltiples Usuarios

1. **Cerrar sesión**: Menú (⋮) → Cerrar Sesión
2. **Crear segunda cuenta**: Repetir proceso con diferentes credenciales
3. **Alternar usuarios**: Cerrar sesión y entrar con diferentes credenciales

### Probar Recuperación de Contraseña

1. En Login, clic en "¿Olvidaste tu contraseña?"
2. Ingresa el email registrado
3. Ingresa nueva contraseña
4. Intenta iniciar sesión con la nueva contraseña

## 🔍 Verificar Base de Datos

### Ver contenido de la base de datos:

```bash
# Conectar al dispositivo
adb shell

# Navegar a la base de datos
cd /data/data/com.victorgangas.arduinotb/databases/

# Ver archivos
ls -la

# Abrir la base de datos
sqlite3 arduino_app_database

# Ver usuarios registrados
SELECT * FROM users;

# Salir
.quit
```

### Limpiar base de datos (para pruebas):

```bash
# Desinstalar la app (limpia todos los datos)
adb uninstall com.victorgangas.arduinotb

# O borrar datos sin desinstalar
adb shell pm clear com.victorgangas.arduinotb
```

## 🐛 Solución de Problemas Comunes

### Error: "Cannot resolve symbol 'Room'"

**Solución**:
```bash
1. File → Invalidate Caches → Invalidate and Restart
2. Esperar a que se reindexe
3. Sync Project with Gradle Files
```

### Error: "Unresolved reference: ksp"

**Solución**:
Verificar que `gradle/libs.versions.toml` incluya:
```toml
[versions]
ksp = "2.0.21-1.0.25"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### Error: "No ActivityFound"

**Solución**:
Verificar que `AndroidManifest.xml` tenga:
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    ...
```

### Error de compilación: "Duplicate class"

**Solución**:
```bash
./gradlew clean
# Borrar carpetas:
rm -rf app/build
rm -rf build
# Sincronizar
./gradlew build
```

### La app se cierra al iniciar

**Solución**:
Ver logs en Logcat:
```bash
# En Android Studio:
View → Tool Windows → Logcat

# O en terminal:
adb logcat | grep -i "arduinotb"
```

## 📊 Verificar Instalación Correcta

### Checklist de Verificación:

- [ ] El proyecto sincroniza sin errores
- [ ] No hay errores de compilación
- [ ] La app se instala correctamente
- [ ] Aparece la pantalla de Login
- [ ] Se puede crear una cuenta nueva
- [ ] El login funciona correctamente
- [ ] La sesión persiste al cerrar y abrir la app
- [ ] Se puede cerrar sesión
- [ ] La pantalla principal (Bluetooth) funciona
- [ ] El menú tiene la opción "Cerrar Sesión"

## 🎯 Comandos Útiles

```bash
# Ver dispositivos conectados
adb devices

# Ver logs en tiempo real
adb logcat

# Limpiar logs
adb logcat -c

# Capturar pantalla
adb shell screencap /sdcard/screen.png
adb pull /sdcard/screen.png

# Desinstalar app
adb uninstall com.victorgangas.arduinotb

# Reinstalar
./gradlew clean installDebug

# Ver tamaño del APK
du -h app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Permisos Necesarios

La app solicitará los siguientes permisos:

1. **Bluetooth** (automático en API 31+):
   - BLUETOOTH_CONNECT
   - BLUETOOTH_SCAN
   - BLUETOOTH_ADVERTISE

2. **Ubicación** (solo API ≤ 30):
   - ACCESS_FINE_LOCATION

3. **Audio**:
   - RECORD_AUDIO (para reconocimiento de voz)

4. **Internet** (opcional):
   - INTERNET (para futuras funciones de email)

## 🔐 Datos de Prueba

Para facilitar las pruebas, puedes usar estas credenciales de ejemplo:

```
Usuario 1:
- Usuario: admin
- Contraseña: admin123
- Email: admin@test.com

Usuario 2:
- Usuario: test
- Contraseña: test123
- Email: test@test.com

Usuario 3:
- Usuario: user01
- Contraseña: password
- Email: user@test.com
```

## 🌐 Modo Debug vs Release

### Debug (desarrollo):
```bash
./gradlew assembleDebug
# APK con logs y debugging habilitado
```

### Release (producción):
```bash
./gradlew assembleRelease
# APK optimizado, requiere firma
```

## 📈 Optimización

### Reducir tamaño del APK:

En `app/build.gradle.kts`:
```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true  // Habilitar ProGuard
            isShrinkResources = true // Reducir recursos
        }
    }
}
```

## 🆘 Obtener Ayuda

Si tienes problemas:

1. **Verificar logs**: `adb logcat`
2. **Limpiar proyecto**: `Build → Clean Project`
3. **Invalidar cache**: `File → Invalidate Caches`
4. **Revisar documentación**: [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md)
5. **Verificar dependencias**: Asegurarse de tener conexión a internet

## 📚 Recursos Adicionales

- [Android Studio Guide](https://developer.android.com/studio/intro)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

---

**Última actualización**: Octubre 2025  
**Compatibilidad**: Android 7.0 (API 24) - Android 14+ (API 36)

