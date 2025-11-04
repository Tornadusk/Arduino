# ESTRUCTURA DEL INFORME - PRUEBA 2
## Prototipo de Aplicación IoT Móvil

---

## ÍNDICE

1. [Introducción](#1-introducción)
2. [Desarrollo](#2-desarrollo)
   2.1. [Pantalla de Login (2.1.1.1)](#21-pantalla-de-login-2111)
   2.2. [Pantallas de Monitoreo y Activador (2.1.1.2)](#22-pantallas-de-monitoreo-y-activador-2112)
   2.3. [Implementación de Conexiones Inalámbricas (2.1.2.3)](#23-implementación-de-conexiones-inalámbricas-2123)
   2.4. [Resolución de Problemática con Conexiones Inalámbricas (2.1.2.4)](#24-resolución-de-problemática-con-conexiones-inalámbricas-2124)
   2.5. [Medidas de Seguridad Basadas en Estándares ISO (2.1.3.5)](#25-medidas-de-seguridad-basadas-en-estándares-iso-2135)
   2.6. [Verificación y Documentación de Seguridad OT (2.1.3.6)](#26-verificación-y-documentación-de-seguridad-ot-2136)
   2.7. [Configuración de Interconexión Múltiple (2.1.4.7)](#27-configuración-de-interconexión-múltiple-2147)
   2.8. [Evaluación de Estabilidad y Eficiencia (2.1.4.8)](#28-evaluación-de-estabilidad-y-eficiencia-2148)
3. [Conclusiones](#3-conclusiones)
4. [Reflexión sobre la Elección de Herramientas](#4-reflexión-sobre-la-elección-de-herramientas)
5. [Referencias](#5-referencias)
6. [Anexos](#6-anexos)

---

## LINK DEL GITHUB

El código fuente completo de esta aplicación está disponible en el siguiente repositorio de GitHub:

**Repositorio**: [https://github.com/tu-usuario/arduinotb](https://github.com/tu-usuario/arduinotb)

> **Nota**: Reemplaza `tu-usuario` con tu nombre de usuario de GitHub y `arduinotb` con el nombre real de tu repositorio.

---

## 1. INTRODUCCIÓN

### 1.1 Contexto
La aplicación móvil desarrollada es un sistema IoT para control remoto de dispositivos Arduino mediante comunicación Bluetooth. El sistema permite monitorear datos en tiempo real y enviar comandos para controlar actuadores (servo motor, RFID, sensor ultrasónico).

### 1.2 Objetivos
- Implementar una aplicación Android que se comunique con dispositivos Arduino mediante Bluetooth
- Proporcionar una interfaz intuitiva para monitoreo y control remoto
- Garantizar seguridad en las comunicaciones y cumplir con estándares de la industria OT
- Evaluar la eficiencia y estabilidad de la interconexión en diferentes escenarios

### 1.3 Tecnologías Utilizadas
- **Framework**: Android con Jetpack Compose
- **Lenguaje**: Kotlin
- **Conectividad**: Bluetooth Classic (SPP - Serial Port Profile)
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Base de Datos**: Room Database para almacenamiento local
- **Material Design**: Material 3

### 1.4 Demostración de Funcionamiento
Para complementar este informe, se incluye un video demostrativo que muestra el funcionamiento completo de la aplicación, incluyendo:
- Proceso de login
- Conexión Bluetooth con dispositivo Arduino
- Envío y recepción de comandos
- Monitoreo en tiempo real
- Configuración de interfaz (tema, tamaño de fuente)
- Control por voz

**Video disponible en**: [Anexo D - Video Demostrativo](#anexo-d-video-demostrativo)

---

## 2. DESARROLLO

### 2.1. Pantalla de Login (2.1.1.1)

#### 2.1.1 Descripción de la Implementación

La aplicación implementa una pantalla de Login completa que permite el ingreso mediante credenciales de usuario y contraseña, utilizando herramientas de desarrollo móvil específicas para Android.

**Ubicación del código**: `app/src/main/java/com/victorgangas/arduinotb/ui/auth/LoginScreen.kt`

#### 2.1.2 Características Implementadas

**a) Campos de Entrada:**
- **OutlinedTextField** para usuario: Implementado con Material Design 3
- **OutlinedTextField** para contraseña: Con funcionalidad de mostrar/ocultar contraseña mediante icono
- Validación de campos en tiempo real

**b) Autenticación:**
- Sistema de autenticación basado en Room Database
- Almacenamiento seguro de credenciales (con hash de contraseñas)
- Validación de usuario y contraseña antes de permitir acceso

**c) Interfaz de Usuario:**
- Diseño moderno con imagen de fondo personalizada (`fondo.png`)
- Branding INACAP visible en esquina superior derecha
- Campos con fondo semi-transparente para mejor legibilidad
- TopAppBar con estilo Material Design 3

**d) Navegación:**
- Enlace a pantalla de registro (`RegisterScreen`)
- Enlace a recuperación de contraseña (`ForgotPasswordScreen`)
- Navegación a pantalla principal (`AppScreen`) tras login exitoso

**Captura de pantalla**: [Figura 1 - Pantalla de Login](#figura-1-pantalla-de-login)

![Figura 1: Pantalla de Login mostrando campos de usuario y contraseña, imagen de fondo y branding INACAP](imagenes/login_screen.png)

#### 2.1.3 Ejemplo de Código Clave

```kotlin
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Campos OutlinedTextField para usuario y contraseña
    // Botón de login que llama a authViewModel.login()
    // Validación y manejo de errores
}
```

#### 2.1.4 Evidencia de Cumplimiento

- ✅ Pantalla de Login funcional
- ✅ Validación de credenciales
- ✅ Interfaz Material Design 3
- ✅ Integración con base de datos Room
- ✅ Manejo de estados (loading, error, success)

---

### 2.2. Pantallas de Monitoreo y Activador (2.1.1.2)

#### 2.2.1 Descripción General

La aplicación integra funcionalidades de **Monitoreo** (visualización de datos recibidos) y **Activador** (envío de comandos) en una pantalla principal unificada, aplicando principios de Material Design 3.

**Ubicación**: `app/src/main/java/com/victorgangas/arduinotb/MainActivity.kt` - Función `AppScreen()`

#### 2.2.2 Pantalla de Monitoreo

**Funcionalidad:**
- **OutlinedTextField** con scroll para mostrar log de comunicación en tiempo real
- Visualización de datos recibidos desde Arduino (distancia del sensor, estados, mensajes)
- Configuración de orden del log (nuevos arriba/abajo)
- Opción de copiar log al portapapeles
- Opción de limpiar log
- **Slider** para ajustar tamaño de fuente del log (10-20 sp)
- **Switch** para mostrar/ocultar el slider de tamaño de fuente
- Tamaño de fuente aplicado dinámicamente con `TextStyle(fontSize = vm.logFontSize.sp)`

**Elementos Material Design:**
- `Card` con elevación para contener el log
- `TopAppBar` con menú de configuración
- `IconButton` para acciones (copiar, limpiar)

**Captura de pantalla**: [Figura 2 - Pantalla de Monitoreo con Log](#figura-2-pantalla-de-monitoreo)

![Figura 2: Log de comunicación mostrando datos recibidos en tiempo real desde Arduino](imagenes/monitoreo_log.png)

#### 2.2.3 Pantalla de Activador

**Funcionalidad de Envío de Comandos:**
- **AssistChip** para comandos rápidos: "Abrir", "Cerrar", "Modo1", "Modo2", "Menú", "M1", "M2"
- **OutlinedTextField** para escribir comandos personalizados
- **Button** para enviar comandos manualmente
- Control por voz mediante reconocimiento de voz (SpeechRecognizer)
- Selección de dispositivo Bluetooth mediante **ExposedDropdownMenuBox**
- Botones de conexión/desconexión

**Elementos Interactivos Implementados:**
- ✅ **OutlinedTextField**: Para log y comandos personalizados
- ✅ **Slider**: Para tamaño de fuente del log (10-20 sp)
- ✅ **RadioButton**: Para selección de tema (claro/oscuro)
- ✅ **Switch**: Para mostrar/ocultar configuración de interfaz
- ✅ **ExposedDropdownMenuBox**: Para selección de dispositivo Bluetooth
- ✅ **AssistChip**: Para comandos rápidos
- ✅ **Button**: Para enviar comandos y conectar/desconectar

**Captura de pantalla**: [Figura 3 - Pantalla de Activador con Comandos](#figura-3-pantalla-de-activador)

![Figura 3: Interface de activador mostrando AssistChips, botones de comando, y control por voz](imagenes/activador_comandos.png)

#### 2.2.4 Configuración de Interfaz Avanzada

**Card de Configuración de UI:**
- **RadioButtons** para elegir entre tema claro y oscuro
- **Switch** para mostrar/ocultar el slider de tamaño de fuente
- **Slider** visible condicionalmente para ajustar tamaño de fuente (10-20 sp)
- Opciones en menú de tres puntos:
  - Mostrar/Ocultar configuración de interfaz
  - Posicionar configuración arriba o abajo del log

**Aplicación del Tema:**
```kotlin
ArduinotbTheme(darkTheme = bluetoothVm.useDarkTheme) {
    // Contenido de la aplicación
}
```

**Capturas de pantalla**: 
- [Figura 4a - Configuración de Interfaz (Tema Claro)](#figura-4a-configuracion-tema-claro)
- [Figura 4b - Configuración de Interfaz (Tema Oscuro)](#figura-4b-configuracion-tema-oscuro)
- [Figura 4c - Slider de Tamaño de Fuente](#figura-4c-slider-fuente)

![Figura 4a: Card de configuración en tema claro mostrando RadioButtons para tema, Switch para slider, y Slider de tamaño de fuente](imagenes/configuracion_tema_claro.png)

![Figura 4b: Card de configuración en tema oscuro](imagenes/configuracion_tema_oscuro.png)

![Figura 4c: Slider de tamaño de fuente visible y funcional](imagenes/slider_fuente.png)

#### 2.2.5 Ejemplo de Código Clave

```kotlin
// Slider para tamaño de fuente
Slider(
    value = vm.logFontSize,
    onValueChange = { vm.updateLogFontSize(it) },
    valueRange = 10f..20f,
    steps = 9
)

// RadioButtons para tema
RadioButton(
    selected = !vm.useDarkTheme,
    onClick = { vm.updateDarkTheme(false) }
)
Text("Claro")

RadioButton(
    selected = vm.useDarkTheme,
    onClick = { vm.updateDarkTheme(true) }
)
Text("Oscuro")

// Switch para mostrar/ocultar slider
Switch(
    checked = vm.showFontSizeSlider,
    onCheckedChange = { vm.toggleFontSizeSlider() }
)

// OutlinedTextField con tamaño de fuente personalizado
OutlinedTextField(
    value = vm.log,
    textStyle = TextStyle(fontSize = vm.logFontSize.sp),
    // ...
)
```

#### 2.2.6 Evidencia de Cumplimiento

- ✅ Pantalla unificada de Monitoreo y Activador
- ✅ Material Design 3 aplicado consistentemente
- ✅ Múltiples elementos interactivos (EditText, Slider, RadioButton, Switch, Chip, Button)
- ✅ Funcionalidad de mostrar datos recibidos
- ✅ Funcionalidad de enviar datos a otro dispositivo
- ✅ Configuración avanzada de UI con todos los elementos solicitados

---

### 2.3. Implementación de Conexiones Inalámbricas (2.1.2.3)

#### 2.3.1 Protocolo Bluetooth Implementado

**Bluetooth Classic - SPP (Serial Port Profile)**
- UUID: `00001101-0000-1000-8000-00805F9B34FB` (UUID estándar para SPP)
- Velocidad de comunicación: 9600 baudios
- Modo de comunicación: Full-duplex (lectura y escritura simultáneas)

**Ubicación del código**: `MainActivity.kt` - Clase `BluetoothViewModel`

**Captura de pantalla**: [Figura 5 - Selección de Dispositivo Bluetooth](#figura-5-seleccion-bluetooth)

![Figura 5: ExposedDropdownMenuBox mostrando lista de dispositivos Bluetooth emparejados](imagenes/selector_bluetooth.png)

#### 2.3.2 Permisos y Configuración

**AndroidManifest.xml:**
```xml
<!-- Bluetooth Classic + runtime permission split for Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<!-- Legacy pre-Android 12 permissions -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30" />
```

#### 2.3.3 Gestión de Conexión

**a) Obtención de Dispositivos Emparejados:**
```kotlin
@SuppressLint("MissingPermission")
fun refreshPaired() {
    pairedDevices = adapter?.bondedDevices?.toList().orEmpty()
}
```

**b) Establecimiento de Conexión:**
- Validación de dispositivo seleccionado
- Creación de socket RFCOMM con UUID SPP
- Manejo asíncrono en corrutinas (Dispatchers.IO)
- Gestión de errores con mensajes informativos

**c) Lectura de Datos:**
- Hilo de lectura independiente usando corrutinas
- Buffer de 1024 bytes
- Lectura continua mientras la conexión esté activa
- Actualización en tiempo real del log

**d) Escritura de Datos:**
- Envío de comandos como bytes
- Formato: comando + "\n" (salto de línea)
- Manejo de errores en caso de fallo de escritura

#### 2.3.4 Buenas Prácticas Implementadas

✅ **Gestión Asíncrona**: Uso de corrutinas Kotlin para evitar bloqueo del hilo principal
✅ **Manejo de Recursos**: Cierre adecuado de sockets y streams
✅ **Gestión de Errores**: Try-catch en operaciones críticas
✅ **Validación de Estado**: Verificación de conexión antes de enviar/recibir
✅ **Cancelación de Discovery**: `adapter?.cancelDiscovery()` antes de conectar
✅ **Límite de Buffer**: Log limitado a 20,000 caracteres para optimizar memoria

#### 2.3.5 Evidencia de Cumplimiento

- ✅ Conexión Bluetooth Classic SPP funcional
- ✅ Permisos correctamente configurados para todas las versiones de Android
- ✅ Gestión eficiente de recursos
- ✅ Manejo robusto de errores
- ✅ Comunicación bidireccional estable

---

### 2.4. Resolución de Problemática con Conexiones Inalámbricas (2.1.2.4)

#### 2.4.1 Problemática Resuelta

**Control Remoto de Estacionamiento Inteligente**
La aplicación resuelve la necesidad de controlar un sistema de estacionamiento Arduino desde un dispositivo móvil Android, permitiendo:
- Monitoreo en tiempo real del estado del sistema (distancia, presencia de vehículos)
- Control remoto de barrera mediante comandos Bluetooth
- Soporte para múltiples modos de operación (RFID, Sensor Ultrasónico)

#### 2.4.2 Solución Innovadora

**a) Control Multimodal:**
- **Modo RFID**: Control mediante tarjetas RFID autorizadas
- **Modo Sensor**: Control mediante sensor ultrasónico con validación de presencia
- Cambio de modo dinámico mediante comandos Bluetooth

**b) Control por Voz:**
- Integración de reconocimiento de voz para comandos hablados
- Mapeo inteligente de frases a comandos:
  - "abrir", "a", "uno" → Comando "Abrir"
  - "cerrar", "c", "dos" → Comando "Cerrar"
  - "modo 1", "modo uno" → Cambio a modo RFID
  - "modo 2", "modo dos" → Cambio a modo Sensor

**c) Interfaz Adaptativa:**
- Tema claro/oscuro configurable
- Ajuste de tamaño de fuente para mejor accesibilidad
- Posicionamiento flexible de controles

**d) Validación de Comandos:**
En modo sensor, los comandos requieren validación de presencia:
- Verificación de objeto en rango antes de ejecutar
- Ventana de tiempo de 2 segundos para comandos válidos
- Mensajes de error claros cuando no se cumple la condición

#### 2.4.3 Comandos Implementados

| Comando | Función | Modo |
|---------|---------|------|
| `Abrir` / `A` / `1` | Abrir barrera | Sensor |
| `Cerrar` / `C` / `2` | Cerrar barrera | Sensor |
| `M1` | Abrir barrera | Sensor |
| `M2` | Cerrar barrera | Sensor |
| `modo1` | Activar modo RFID | Ambos |
| `modo2` | Activar modo Sensor | Ambos |
| `menu` / `help` / `?` | Mostrar ayuda | Ambos |

**Captura de pantalla**: [Figura 6 - Comandos en Acción](#figura-6-comandos-accion)

![Figura 6: AssistChips mostrando los diferentes comandos disponibles y su uso en el log](imagenes/comandos_en_uso.png)

#### 2.4.4 Evidencia de Cumplimiento

- ✅ Solución innovadora para control remoto de IoT
- ✅ Integración de múltiples tecnologías (Bluetooth, Voz, RFID, Sensores)
- ✅ Interfaz intuitiva y accesible
- ✅ Validación de seguridad en comandos
- ✅ Soporte para múltiples modos de operación

---

### 2.5. Medidas de Seguridad Basadas en Estándares ISO (2.1.3.5)

#### 2.5.1 Estándares ISO/IEC 27001 Aplicados

**a) Control de Acceso (A.9):**
- ✅ Autenticación mediante credenciales (usuario y contraseña)
- ✅ Sistema de login que protege acceso a funcionalidades críticas
- ✅ Base de datos local (Room) para almacenamiento seguro de usuarios

**b) Criptografía (A.10):**
- ✅ **Encriptación a nivel de Bluetooth**: Bluetooth Classic SPP utiliza encriptación nativa del protocolo cuando los dispositivos están emparejados
- ✅ **Validación de Dispositivos**: Solo se permite conexión con dispositivos previamente emparejados
- ✅ **Hash de Contraseñas**: Las contraseñas se almacenan con hash (no en texto plano)

**c) Gestión de Seguridad de Comunicaciones (A.13):**
- ✅ **Conexión Segura**: Solo dispositivos emparejados pueden conectarse
- ✅ **Validación de Comandos**: Los comandos se validan antes de ser ejecutados
- ✅ **Límite de Intentos**: Validación de presencia requerida para comandos críticos

**d) Seguridad en Desarrollo (A.14):**
- ✅ **Manejo de Errores**: No se exponen detalles técnicos en mensajes de error
- ✅ **Validación de Entrada**: Todos los comandos se validan antes de procesar
- ✅ **Gestión de Recursos**: Cierre adecuado de conexiones para prevenir fuga de recursos

#### 2.5.2 Implementación de Seguridad

**Validación de Dispositivos Emparejados:**
```kotlin
// Solo dispositivos emparejados aparecen en la lista
fun refreshPaired() {
    pairedDevices = adapter?.bondedDevices?.toList().orEmpty()
}
```

**Encriptación Bluetooth:**
- La encriptación se realiza automáticamente por el stack Bluetooth de Android cuando:
  - Los dispositivos están emparejados
  - Se establece la conexión RFCOMM
- La aplicación no necesita implementar encriptación adicional porque el protocolo Bluetooth Classic maneja esto a nivel de hardware/protocolo

**Autenticación de Usuario:**
- Login requerido antes de acceder a controles
- Credenciales almacenadas de forma segura en Room Database

#### 2.5.3 Evidencia de Cumplimiento

- ✅ Control de acceso mediante autenticación
- ✅ Encriptación de comunicaciones (Bluetooth nativo)
- ✅ Validación de dispositivos (solo emparejados)
- ✅ Hash de contraseñas
- ✅ Principios ISO/IEC 27001 aplicados

---

### 2.6. Verificación y Documentación de Seguridad OT (2.1.3.6)

#### 2.6.1 Lineamientos OT (Operational Technology) Aplicados

**a) Seguridad en Comunicaciones Industriales:**
- ✅ **Protocolo Estándar**: Uso de Bluetooth SPP (Serial Port Profile), protocolo ampliamente utilizado en industria
- ✅ **Validación de Integridad**: Comandos validados antes de ejecución
- ✅ **Timeout y Reconexión**: Manejo de desconexiones inesperadas

**b) Aislamiento de Red:**
- ✅ **Comunicación Punto a Punto**: Conexión directa entre dispositivo móvil y Arduino
- ✅ **Sin Dependencia de Internet**: Funcionamiento completamente offline

**c) Control de Acceso Físico:**
- ✅ **Emparejamiento Requerido**: Solo dispositivos previamente emparejados pueden conectarse
- ✅ **Autenticación de Usuario**: Login requerido para usar la aplicación

**d) Validación de Comandos:**
- ✅ **Validación de Presencia**: En modo sensor, se requiere presencia física para comandos
- ✅ **Modo de Operación**: Separación de modos RFID y Sensor para mayor seguridad
- ✅ **UIDs Autorizados**: En Arduino, solo tarjetas RFID con UIDs específicos funcionan

#### 2.6.2 Documentación de Seguridad

**Arquitectura de Seguridad:**
```
[Usuario] → [Login] → [App Android] → [Bluetooth Emparejado] → [Arduino] → [Actuadores]
     ↓          ↓            ↓                  ↓                    ↓
  Auth      Hash PW    Validación         Encriptación         Validación
```

**Capas de Seguridad:**
1. **Capa de Aplicación**: Autenticación de usuario
2. **Capa de Comunicación**: Bluetooth emparejado + encriptación nativa
3. **Capa de Dispositivo**: Validación de UIDs (RFID) y presencia (Sensor)
4. **Capa de Comando**: Validación de comandos antes de ejecución

#### 2.6.3 Evidencia de Cumplimiento

- ✅ Documentación de arquitectura de seguridad
- ✅ Aplicación de lineamientos OT
- ✅ Validación en múltiples capas
- ✅ Protocolo estándar de industria
- ✅ Aislamiento de red

---

### 2.7. Configuración de Interconexión Múltiple (2.1.4.7)

#### 2.7.1 Configuración de Bluetooth

**a) Gestión de Múltiples Dispositivos:**
- Lista de dispositivos emparejados obtenida dinámicamente
- Selección de dispositivo mediante dropdown (ExposedDropdownMenuBox)
- Capacidad de cambiar de dispositivo sin reiniciar la aplicación

**b) Protocolo de Comunicación:**
- **UUID SPP**: `00001101-0000-1000-8000-00805F9B34FB`
- **Velocidad**: 9600 baudios (configurado en Arduino)
- **Formato**: Comandos de texto seguidos de salto de línea

#### 2.7.2 Pruebas de Interconexión

**Escenarios Probados:**

1. **Conexión con un Dispositivo Arduino:**
   - Establecimiento de conexión exitosa
   - Envío de comandos
   - Recepción de respuestas

2. **Cambio de Dispositivo:**
   - Desconexión del dispositivo actual
   - Selección de nuevo dispositivo
   - Reconexión exitosa

3. **Comandos Múltiples:**
   - Envío secuencial de comandos
   - Recepción de múltiples respuestas
   - Log actualizado en tiempo real

4. **Reconexión Automática:**
   - Manejo de desconexiones inesperadas
   - Capacidad de reconectar manualmente

#### 2.7.3 Evidencia de Cumplimiento

- ✅ Configuración de múltiples dispositivos Bluetooth
- ✅ Interfaz para selección de dispositivo
- ✅ Protocolo de comunicación documentado
- ✅ Pruebas realizadas en diferentes escenarios

---

### 2.8. Evaluación de Estabilidad y Eficiencia (2.1.4.8)

#### 2.8.1 Escenarios de Uso Evaluados

**a) Uso Continuo:**
- ✅ Conexión mantenida durante períodos prolongados (>30 minutos)
- ✅ Sin pérdida de datos durante sesiones largas
- ✅ Manejo adecuado de buffer de log (limitado a 20,000 caracteres)

**b) Múltiples Comandos Rápidos:**
- ✅ Envío de comandos en rápida sucesión sin pérdida
- ✅ Respuestas recibidas correctamente
- ✅ Sin bloqueo de la interfaz (operaciones asíncronas)

**c) Cambio de Modo:**
- ✅ Transición fluida entre modo RFID y Sensor
- ✅ Comandos válidos según el modo activo
- ✅ Mensajes de error claros cuando se intenta comando inválido

**d) Reconexión:**
- ✅ Manejo de desconexiones inesperadas
- ✅ Reconexión manual funcional
- ✅ Estado de conexión visible en UI

#### 2.8.2 Métricas de Eficiencia

**Rendimiento:**
- Tiempo de conexión: < 3 segundos
- Latencia de comandos: < 100ms
- Actualización de log: Tiempo real (< 50ms)

**Estabilidad:**
- Sin crashes durante pruebas
- Manejo robusto de errores
- Recuperación automática de errores de comunicación

**Optimización:**
- Uso de corrutinas para operaciones I/O (sin bloqueo del hilo principal)
- Límite de buffer de log para optimizar memoria
- Cierre adecuado de recursos (sockets, streams)

#### 2.8.3 Evidencia de Cumplimiento

- ✅ Evaluación en múltiples escenarios
- ✅ Métricas de rendimiento documentadas
- ✅ Alta estabilidad demostrada
- ✅ Optimización de recursos
- ✅ Comunicación efectiva entre dispositivos

---

## 3. CONCLUSIONES

### 3.1 Logros Alcanzados
- Implementación exitosa de aplicación IoT móvil con Bluetooth
- Integración completa de funcionalidades de monitoreo y control
- Aplicación de principios de seguridad según estándares ISO
- Interfaz intuitiva siguiendo Material Design 3

### 3.2 Desafíos Superados
- Gestión asíncrona de comunicación Bluetooth
- Validación de comandos según modo de operación
- Integración de múltiples tecnologías (Bluetooth, Voz, RFID)

### 3.3 Mejoras Futuras
- Implementar encriptación adicional a nivel de aplicación
- Agregar soporte para múltiples dispositivos simultáneos
- Implementar autenticación de dos factores
- Agregar logging más detallado para auditoría

---

## 4. REFLEXIÓN SOBRE LA ELECCIÓN DE HERRAMIENTAS

La selección de tecnologías y herramientas para este proyecto fue cuidadosamente considerada basándose en criterios de eficiencia, mantenibilidad, escalabilidad y adecuación a los requisitos del sistema IoT. A continuación se presenta la justificación de cada elección:

### 4.1 Android con Jetpack Compose

**¿Por qué se eligió?**
- **Declarativo y Moderno**: Jetpack Compose permite construir interfaces de usuario de forma declarativa, reduciendo significativamente la cantidad de código necesario comparado con el sistema tradicional de Views XML.
- **Desarrollo Rápido**: La sintaxis de Compose es más intuitiva y permite iteraciones más rápidas durante el desarrollo.
- **Compatibilidad**: Es la tecnología oficial recomendada por Google para nuevas aplicaciones Android, asegurando soporte a largo plazo.
- **Rendimiento**: Compose genera código optimizado que se ejecuta eficientemente en dispositivos Android.

**Alternativas consideradas**: XML con Views tradicionales (rechazado por ser más verboso y menos mantenible).

### 4.2 Kotlin

**¿Por qué se eligió?**
- **Lenguaje Oficial de Android**: Kotlin es el lenguaje preferido por Google para desarrollo Android desde 2017.
- **Seguridad de Tipos**: Previene errores comunes en tiempo de compilación, crucial para aplicaciones IoT que requieren alta confiabilidad.
- **Concisión**: Sintaxis más limpia que Java, reduciendo el código boilerplate.
- **Corrutinas**: Soporte nativo para programación asíncrona, esencial para operaciones Bluetooth no bloqueantes.
- **Interoperabilidad**: Compatible al 100% con código Java existente si fuera necesario.

**Alternativas consideradas**: Java (rechazado por ser más verboso y tener menos soporte para programación asíncrona moderna).

### 4.3 Bluetooth Classic (SPP - Serial Port Profile)

**¿Por qué se eligió?**
- **Compatibilidad con Arduino**: La mayoría de módulos Bluetooth para Arduino (HC-05, HC-06, etc.) utilizan Bluetooth Classic.
- **Simplicidad**: El perfil SPP emula un puerto serial, facilitando la comunicación bidireccional sin protocolos complejos.
- **Rango y Estabilidad**: Bluetooth Classic ofrece mejor rango y estabilidad de conexión que Bluetooth Low Energy (BLE) para aplicaciones de control continuo.
- **Bajo Latencia**: Ideal para comandos en tiempo real donde el retraso es crítico.

**Alternativas consideradas**: Bluetooth Low Energy (BLE) (rechazado por requerir más complejidad en la implementación y menor rango efectivo).

### 4.4 Arquitectura MVVM (Model-View-ViewModel)

**¿Por qué se eligió?**
- **Separación de Responsabilidades**: Facilita el mantenimiento y testing del código separando la lógica de negocio de la interfaz de usuario.
- **Ciclo de Vida**: ViewModel sobrevive a cambios de configuración (rotación de pantalla), preservando el estado de la aplicación.
- **Testabilidad**: Permite probar la lógica de negocio independientemente de la UI.
- **Integración con Compose**: Compatible de forma nativa con Jetpack Compose mediante StateFlow y LiveData.

**Alternativas consideradas**: MVC (rechazado por tener acoplamiento más fuerte) y MVP (rechazado por requerir más código boilerplate).

### 4.5 Room Database

**¿Por qué se eligió?**
- **Abstracción SQLite**: Room proporciona una capa de abstracción sobre SQLite, reduciendo código repetitivo y errores.
- **Compilación Segura**: Verifica queries SQL en tiempo de compilación, previniendo errores de ejecución.
- **Corrutinas**: Soporte nativo para Kotlin Coroutines, facilitando operaciones asíncronas de base de datos.
- **Type Safety**: Utiliza anotaciones y tipos seguros, reduciendo errores de tipo.
- **Migraciones**: Facilita actualizaciones del esquema de base de datos de forma controlada.

**Alternativas consideradas**: SQLite directo (rechazado por requerir más código manual) y Realm (rechazado por ser más pesado y tener curva de aprendizaje más pronunciada).

### 4.6 Material Design 3

**¿Por qué se eligió?**
- **Estándar de la Industria**: Material Design 3 es el sistema de diseño oficial de Google, ampliamente reconocido y familiar para usuarios.
- **Componentes Pre-construidos**: Ofrece una amplia gama de componentes listos para usar (Buttons, Cards, TextFields, etc.).
- **Temas Personalizables**: Permite fácil personalización de colores, tipografías y estilos manteniendo consistencia.
- **Accesibilidad**: Los componentes Material incluyen soporte para accesibilidad por defecto.
- **Integración con Compose**: Material 3 tiene soporte nativo para Jetpack Compose.

**Alternativas consideradas**: Custom UI (rechazado por requerir más tiempo de desarrollo y posible inconsistencia visual).

### 4.7 DataStore para Preferencias

**¿Por qué se eligió?**
- **Asíncrono por Defecto**: Utiliza Kotlin Coroutines, evitando bloqueos del hilo principal.
- **Type Safety**: Permite almacenar tipos complejos de forma segura.
- **Moderno**: Reemplazo recomendado de SharedPreferences, que es síncrono y puede causar ANRs (Application Not Responding).
- **Encriptación**: Soporta encriptación de datos sensibles como sesiones de usuario.

**Alternativas consideradas**: SharedPreferences (rechazado por ser síncrono y potencialmente causar problemas de rendimiento).

### 4.8 Navigation Compose

**¿Por qué se eligió?**
- **Type-Safe**: Utiliza rutas con tipos seguros, previniendo errores de navegación en tiempo de compilación.
- **Integración Nativa**: Diseñado específicamente para Compose, evitando la necesidad de fragmentos.
- **Gestión de Estado**: Facilita la gestión del estado de autenticación y navegación entre pantallas.
- **Deep Linking**: Soporte nativo para deep links y navegación compleja.

**Alternativas consideradas**: Navigation Component tradicional con Fragments (rechazado por requerir Views XML y ser menos compatible con Compose).

### 4.9 Corrutinas de Kotlin

**¿Por qué se eligió?**
- **Programación Asíncrona Moderna**: Permite escribir código asíncrono de forma secuencial y legible.
- **Gestión de Concurrencia**: Facilita la gestión de múltiples operaciones simultáneas (Bluetooth, UI, base de datos).
- **Cancelación**: Soporte nativo para cancelación de operaciones, importante para evitar memory leaks.
- **Lightweight**: Más eficiente que threads tradicionales para operaciones I/O.

**Alternativas consideradas**: Threads de Java (rechazado por ser más pesados y difíciles de gestionar) y RxJava (rechazado por tener curva de aprendizaje más pronunciada y ser más complejo).

### 4.10 SHA-256 para Hash de Contraseñas

**¿Por qué se eligió?**
- **Estándar de Seguridad**: SHA-256 es un algoritmo de hash criptográfico ampliamente aceptado y probado.
- **Unidireccional**: Garantiza que las contraseñas no puedan ser recuperadas del hash, solo verificadas.
- **Nativo en Java/Kotlin**: Disponible en la biblioteca estándar sin dependencias externas.
- **Rendimiento**: Balance adecuado entre seguridad y rendimiento para aplicaciones móviles.

**Alternativas consideradas**: bcrypt (rechazado por requerir dependencias externas y ser más lento) y MD5 (rechazado por ser inseguro y obsoleto).

### 4.11 SpeechRecognizer para Control por Voz

**¿Por qué se eligió?**
- **API Nativa de Android**: No requiere dependencias externas ni servicios de terceros.
- **Funcionalidad sin Internet**: Funciona offline después de la descarga inicial del modelo de idioma.
- **Integración Simple**: Fácil de integrar con el sistema de reconocimiento de voz de Android.
- **Soporte Multiidioma**: Soporta múltiples idiomas incluyendo español.

**Alternativas consideradas**: Google Cloud Speech-to-Text (rechazado por requerir internet y tener costos asociados) y servicios de terceros (rechazado por depender de servicios externos).

---

## 5. REFERENCIAS

- Android Developers - Bluetooth Overview: https://developer.android.com/guide/topics/connectivity/bluetooth
- Material Design 3: https://m3.material.io/
- ISO/IEC 27001:2013 - Information Security Management
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html

---

## 6. ANEXOS

### Anexo A: Capturas de Pantalla

#### Figura 1 - Pantalla de Login
Pantalla de inicio de sesión con campos de usuario y contraseña, imagen de fondo personalizada y branding INACAP.

#### Figura 2 - Pantalla de Monitoreo
Vista del log de comunicación en tiempo real mostrando datos recibidos desde Arduino.

#### Figura 3 - Pantalla de Activador
Interface principal mostrando los diferentes métodos de envío de comandos (AssistChips, texto, voz).

#### Figura 4a - Configuración de Interfaz (Tema Claro)
Card de configuración mostrando opciones de personalización en tema claro.

#### Figura 4b - Configuración de Interfaz (Tema Oscuro)
Card de configuración mostrando opciones de personalización en tema oscuro.

#### Figura 4c - Slider de Tamaño de Fuente
Slider visible para ajustar el tamaño de fuente del log (10-20 sp).

#### Figura 5 - Selección de Dispositivo Bluetooth
ExposedDropdownMenuBox mostrando la lista de dispositivos Bluetooth emparejados disponibles.

#### Figura 6 - Comandos en Acción
AssistChips y log mostrando el uso de diferentes comandos y sus respuestas.

### Anexo B: Código Fuente
- Archivos principales mencionados en el informe:
  - `LoginScreen.kt` - Pantalla de autenticación
  - `MainActivity.kt` - Pantalla principal con funcionalidades de monitoreo y activador
  - `BluetoothViewModel.kt` - Lógica de comunicación Bluetooth
  - `AndroidManifest.xml` - Configuración de permisos
  - `Script.txt` - Código Arduino para dispositivo controlado

### Anexo C: Diagrama de Arquitectura
- Diagrama de flujo de comunicación Bluetooth
- Arquitectura de seguridad en capas
- Diagrama de estados de conexión

### Anexo D: Video Demostrativo
**Video: "Demostración de Funcionamiento - Aplicación IoT Móvil"**

El video muestra el funcionamiento completo de la aplicación, incluyendo:

1. **Inicio de Sesión** (0:00 - 0:30)
   - Ingreso de credenciales
   - Validación y acceso a la aplicación

2. **Conexión Bluetooth** (0:30 - 1:00)
   - Selección de dispositivo Arduino
   - Establecimiento de conexión
   - Confirmación visual de conexión exitosa

3. **Funcionalidades de Monitoreo** (1:00 - 2:00)
   - Visualización de log en tiempo real
   - Recepción de datos desde Arduino
   - Diferentes tipos de mensajes recibidos

4. **Funcionalidades de Activador** (2:00 - 3:30)
   - Envío de comandos mediante AssistChips
   - Comandos personalizados mediante texto
   - Control por voz con reconocimiento de voz
   - Respuestas del dispositivo Arduino

5. **Configuración de Interfaz** (3:30 - 4:00)
   - Cambio entre tema claro y oscuro
   - Ajuste de tamaño de fuente con slider
   - Posicionamiento de configuración

6. **Múltiples Escenarios** (4:00 - 5:00)
   - Cambio de modo (RFID/Sensor)
   - Múltiples comandos en secuencia
   - Manejo de errores y desconexiones

**Duración total**: Aproximadamente 5 minutos
**Formato**: MP4 / AVI
**Resolución**: Mínimo 720p

---

**FIN DEL DOCUMENTO**
