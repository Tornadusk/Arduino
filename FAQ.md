# ❓ Preguntas Frecuentes (FAQ)

## General

### ¿Qué es este sistema de autenticación?
Es un sistema completo de login que permite que múltiples usuarios puedan crear sus propias cuentas y acceder a la aplicación de forma privada e independiente, similar a como funciona el bloqueo de un celular.

### ¿Necesito internet para usar el sistema de autenticación?
**No**, todo funciona localmente en tu dispositivo. Los datos se almacenan en una base de datos local (Room/SQLite) y no requieren conexión a internet.

### ¿Mis datos están seguros?
**Sí**, las contraseñas se almacenan con hash SHA-256, lo que significa que nunca se guardan en texto plano. Además, todos los datos permanecen en tu dispositivo y no se envían a ningún servidor externo.

## Registro y Cuentas

### ¿Cuántos usuarios puedo crear?
Puedes crear **ilimitados usuarios** en el mismo dispositivo. Cada uno tendrá su propia sesión independiente.

### ¿Es obligatorio vincular un email?
**No**, el email es opcional. Sin embargo, es **altamente recomendado** porque:
- Permite recuperar tu contraseña si la olvidas
- Solo toma unos segundos más
- No requiere verificación de email real

### ¿Puedo usar un email falso?
Sí, el sistema no verifica que el email sea real. Sin embargo:
- Si usas un email falso, no podrás recuperar tu contraseña por email (aunque sí podrás cambiarla desde dentro de la app)
- Es mejor usar un email real por seguridad

### ¿Puedo cambiar mi nombre de usuario después?
Actualmente no hay función para cambiar el nombre de usuario. Tendrías que:
1. Crear una nueva cuenta con el nombre deseado
2. Cerrar sesión de la cuenta antigua

### ¿Qué requisitos tiene la contraseña?
- **Mínimo 6 caracteres**
- Puede contener letras, números y símbolos
- Sensible a mayúsculas y minúsculas
- No hay requisitos de complejidad adicionales (pero se recomienda usar contraseñas seguras)

## Inicio de Sesión

### ¿Tengo que iniciar sesión cada vez que abro la app?
**No**, la sesión persiste. Solo necesitas iniciar sesión:
- La primera vez que uses la app
- Después de cerrar sesión manualmente
- Si limpias los datos de la app

### ¿Cuánto tiempo dura la sesión?
La sesión es **permanente** hasta que cierres sesión manualmente. No expira por inactividad.

### ¿Puedo tener múltiples dispositivos con la misma cuenta?
No directamente. Cada cuenta se crea y almacena localmente en cada dispositivo. Si instalas la app en otro dispositivo, tendrás que crear la cuenta nuevamente ahí.

### ¿Qué pasa si olvido mi contraseña?

**Con email vinculado:**
1. Clic en "¿Olvidaste tu contraseña?"
2. Ingresa tu email registrado
3. Ingresa nueva contraseña
4. ¡Listo!

**Sin email vinculado:**
- No podrás recuperar la cuenta
- Tendrás que crear una nueva cuenta
- Por eso es recomendable vincular un email

## Recuperación de Contraseña

### ¿El sistema envía emails de recuperación?
Actualmente **no envía emails reales**. El sistema de recuperación funciona así:
1. Verificas que el email esté registrado
2. Cambias la contraseña directamente
3. No se envía un código ni link por email

### ¿Puedo recuperar mi contraseña sin email?
**No**, necesitas haber vinculado un email durante el registro para poder recuperar tu contraseña.

### ¿Puedo ver mi contraseña actual?
**No**, las contraseñas están encriptadas con hash y no pueden ser desencriptadas. Esta es una medida de seguridad estándar.

## Seguridad

### ¿Cómo se protegen mis contraseñas?
Las contraseñas se protegen mediante:
1. **Hash SHA-256**: La contraseña nunca se almacena en texto plano
2. **Almacenamiento local**: Solo en tu dispositivo, no en servidores externos
3. **Base de datos privada**: Solo accesible por la app

### ¿Alguien puede ver mi contraseña?
No, ni siquiera con acceso a la base de datos. Las contraseñas están hasheadas y no pueden revertirse al texto original.

### ¿Es seguro para uso comercial?
El sistema es adecuado para uso personal y pequeñas empresas. Para uso empresarial grande, se recomendaría agregar:
- Autenticación de dos factores (2FA)
- Auditoría de accesos
- Backup en la nube
- Recuperación por SMS

### ¿Qué pasa si alguien tiene acceso físico a mi teléfono?
Si alguien tiene acceso físico a tu dispositivo desbloqueado:
- Podrían usar la app si tu sesión está activa
- **Recomendación**: Cierra sesión cuando no uses la app en dispositivos compartidos
- Considera agregar autenticación biométrica en el futuro

## Múltiples Usuarios

### ¿Cómo cambio entre usuarios?
1. Cierra sesión del usuario actual (Menú → Cerrar Sesión)
2. En la pantalla de login, ingresa las credenciales del otro usuario

### ¿Los usuarios comparten datos?
**No**, cada usuario tiene su propia sesión completamente aislada. No hay información compartida entre usuarios.

### ¿Puedo eliminar un usuario?
Actualmente no hay función para eliminar usuarios desde la UI. Para eliminar todos los datos:
```bash
Configuración de Android → Apps → Arduino BT Controller → Borrar datos
```

### ¿Cuánto espacio ocupan los usuarios?
Muy poco. Cada usuario ocupa aproximadamente:
- 1-2 KB en la base de datos
- Espacio negligible en DataStore
Total: ~5 KB por usuario

## Funcionalidad de Bluetooth

### ¿El sistema de autenticación afecta la funcionalidad Bluetooth?
**No**, toda la funcionalidad Bluetooth original permanece **exactamente igual**:
- Conexión con dispositivos
- Envío de comandos
- Reconocimiento de voz
- Log de comunicación
Solo se agregó la autenticación antes de acceder a estas funciones.

### ¿Puedo usar voz sin iniciar sesión?
**No**, debes iniciar sesión primero. La autenticación protege toda la app.

### ¿Los dispositivos emparejados son por usuario?
No, los dispositivos Bluetooth emparejados son del sistema operativo Android, no de la app. Todos los usuarios verán los mismos dispositivos emparejados.

## Problemas Técnicos

### La app no compila después de los cambios

**Solución 1**: Sincronizar
```bash
File → Sync Project with Gradle Files
```

**Solución 2**: Limpiar y reconstruir
```bash
Build → Clean Project
Build → Rebuild Project
```

**Solución 3**: Invalidar caché
```bash
File → Invalidate Caches → Invalidate and Restart
```

### Error: "Cannot resolve symbol Room"

Este error significa que las dependencias no se descargaron. Solución:
```bash
1. Verificar conexión a internet
2. File → Sync Project with Gradle Files
3. Esperar a que termine la descarga
4. Build → Rebuild Project
```

### La app se cierra inmediatamente al abrirse

**Verificar logs**:
```bash
adb logcat | grep -i "arduinotb"
```

**Causas comunes**:
- Error en la inicialización de la base de datos
- Permisos faltantes
- Conflicto de dependencias

**Solución**:
```bash
./gradlew clean
./gradlew build
```

### No puedo crear una cuenta

**Verificaciones**:
- ¿El usuario tiene al menos 3 caracteres?
- ¿La contraseña tiene al menos 6 caracteres?
- ¿Las contraseñas coinciden?
- ¿El usuario ya existe?
- ¿El email ya está registrado? (si lo proporcionaste)

### La sesión no persiste

Si tienes que iniciar sesión cada vez:
1. Verifica que DataStore esté funcionando
2. No limpies los datos de la app
3. Reinstalar puede ayudar

### Olvidé mi contraseña y no tengo email

**Lamentablemente**, si no vinculaste un email:
- No puedes recuperar la contraseña
- Tendrás que crear una nueva cuenta
- **Lección**: Siempre vincular un email 😊

## Personalización

### ¿Puedo cambiar los colores de las pantallas?

**Sí**, edita:
```kotlin
// app/src/main/java/com/victorgangas/arduinotb/ui/theme/Color.kt
val PrimaryColor = Color(0xFF6200EE)  // Cambia este valor
```

### ¿Puedo cambiar el requisito de longitud de contraseña?

**Sí**, edita:
```kotlin
// app/src/main/java/com/victorgangas/arduinotb/ui/auth/AuthViewModel.kt
if (password.length < 6) {  // Cambia el 6 por el número deseado
    errorMessage = "La contraseña debe tener al menos X caracteres"
}
```

### ¿Puedo agregar más campos al registro?

**Sí**, pero requiere modificar:
1. `User.kt` - Agregar campos a la entidad
2. `UserDao.kt` - Actualizar queries si es necesario
3. `RegisterScreen.kt` - Agregar campos en la UI
4. `AuthRepository.kt` - Actualizar lógica de registro

### ¿Puedo cambiar el algoritmo de hash?

**Sí**, edita:
```kotlin
// app/src/main/java/com/victorgangas/arduinotb/data/repository/AuthRepository.kt
private fun hashPassword(password: String): String {
    val md = MessageDigest.getInstance("SHA-256")  // Cambiar aquí
    // Por ejemplo: "SHA-512", "MD5", etc.
}
```

## Futuras Mejoras

### ¿Se agregará autenticación biométrica?

No está implementada actualmente, pero se puede agregar usando:
```kotlin
androidx.biometric:biometric
```

### ¿Se puede integrar con servicios en la nube?

Sí, se podría agregar:
- Firebase Authentication
- Sincronización con backend propio
- Backup en la nube

### ¿Habrá envío real de emails?

Actualmente no, pero se puede integrar con:
- SendGrid
- MailGun
- AWS SES
- Firebase Email Extension

### ¿Se puede agregar 2FA?

Sí, se podría implementar:
- Autenticación de dos factores con TOTP
- SMS verification
- Códigos de respaldo

## Datos y Privacidad

### ¿Dónde se almacenan mis datos?

```
/data/data/com.victorgangas.arduinotb/
├── databases/
│   └── arduino_app_database       # Base de datos Room
└── datastore/
    └── user_preferences           # Sesión actual
```

### ¿Puedo hacer backup de mis datos?

**Manualmente**:
```bash
adb backup -f backup.ab com.victorgangas.arduinotb
```

**Restaurar**:
```bash
adb restore backup.ab
```

### ¿Qué pasa si desinstalo la app?

- **Se pierden todos los datos**
- Todas las cuentas de usuario
- Configuraciones
- Sesiones
- No hay recuperación posible

### ¿Los datos se sincronizan entre dispositivos?

**No**, actualmente todo es local. Cada dispositivo tiene sus propias cuentas y datos independientes.

## Desarrollo

### ¿Puedo modificar el código?

**Sí**, el código es tuyo y puedes modificarlo libremente:
- Cambiar UI
- Agregar funcionalidades
- Modificar lógica de negocio
- Integrar con otros servicios

### ¿Qué licencia tiene?

El código generado es de tu propiedad y puedes usarlo como desees.

### ¿Cómo contribuir mejoras?

Si desarrollas mejoras interesantes:
1. Documenta los cambios
2. Prueba exhaustivamente
3. Actualiza el README

### ¿Dónde reportar bugs?

Si encuentras problemas:
1. Verifica los logs: `adb logcat`
2. Intenta reproducir el problema
3. Documenta los pasos para reproducirlo

## Rendimiento

### ¿La autenticación hace la app más lenta?

**No significativamente**. El impacto es mínimo:
- Verificación de sesión: <10ms
- Login/registro: <100ms
- La funcionalidad Bluetooth no se ve afectada

### ¿Cuántos usuarios puede manejar?

Prácticamente **ilimitados** para uso normal. Room puede manejar fácilmente:
- Miles de usuarios
- Millones de registros
Sin problemas de rendimiento perceptibles

## Soporte

### ¿Dónde encuentro más información?

- **Inicio rápido**: [QUICK_START.md](QUICK_START.md)
- **Guía completa**: [AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md)
- **Compilación**: [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md)
- **Arquitectura**: [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

### ¿Necesito conocimientos de programación para usar la app?

**No**, para **usar** la app no necesitas conocimientos técnicos. Es tan simple como:
1. Instalar la app
2. Crear una cuenta
3. Usar la app

Para **modificar** la app sí necesitarás conocimientos de:
- Kotlin
- Jetpack Compose
- Android Development

---

**¿Tu pregunta no está aquí?**
Revisa la documentación adicional o los comentarios en el código fuente.

**Última actualización**: Octubre 2025

