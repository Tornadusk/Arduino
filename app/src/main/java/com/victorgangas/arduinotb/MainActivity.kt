package com.victorgangas.arduinotb

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import com.victorgangas.arduinotb.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorgangas.arduinotb.ui.theme.ArduinotbTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private val bluetoothVm: BluetoothViewModel by viewModels()
    private val authVm: com.victorgangas.arduinotb.ui.auth.AuthViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestBtPermissions()
        setContent {
            ArduinotbTheme(darkTheme = bluetoothVm.useDarkTheme) {
                val navController = androidx.navigation.compose.rememberNavController()
                
                com.victorgangas.arduinotb.navigation.NavGraph(
                    navController = navController,
                    authViewModel = authVm,
                    bluetoothViewModel = bluetoothVm
                )
            }
        }
    }

    private fun requestBtPermissions() {
        val perms = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 31) {
            perms += Manifest.permission.BLUETOOTH_CONNECT
            perms += Manifest.permission.BLUETOOTH_SCAN
        } else {
            perms += Manifest.permission.ACCESS_FINE_LOCATION
        }
        perms += Manifest.permission.RECORD_AUDIO
        if (perms.isNotEmpty()) {
            permissionLauncher.launch(perms.toTypedArray())
        }
    }
}

class BluetoothViewModel : ViewModel() {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    var pairedDevices by mutableStateOf<List<BluetoothDevice>>(emptyList())
        private set
    var selectedDevice by mutableStateOf<BluetoothDevice?>(null)
        private set
    var isConnected by mutableStateOf(false)
        private set
    var log by mutableStateOf("")
        private set
    var listening by mutableStateOf(false)
        private set
    var logNewestFirst by mutableStateOf(true)
        private set
    var showLog by mutableStateOf(true)
        private set
    // Configuración de UI
    var logFontSize by mutableStateOf(14f) // Tamaño de fuente del log en sp (rango 10-20)
        private set
    var showFontSizeSlider by mutableStateOf(false) // Mostrar/ocultar slider de tamaño de fuente
        private set
    var useDarkTheme by mutableStateOf(false) // Tema oscuro o claro
        private set
    var showUIConfig by mutableStateOf(true) // Mostrar/ocultar configuración de interfaz
        private set
    var uiConfigAboveLog by mutableStateOf(true) // true = arriba del log, false = abajo del log
        private set

    private var socket: BluetoothSocket? = null
    private var readJob: Job? = null
    private var output: OutputStream? = null
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        refreshPaired()
    }

    @SuppressLint("MissingPermission")
    fun refreshPaired() {
        pairedDevices = adapter?.bondedDevices?.toList().orEmpty()
        if (selectedDevice == null) selectedDevice = pairedDevices.firstOrNull()
    }

    fun select(device: BluetoothDevice?) {
        selectedDevice = device
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        val device = selectedDevice ?: return
        if (isConnected) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                appendLog("Conectando a ${device.name ?: device.address}…\n")
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP
                val s = device.createRfcommSocketToServiceRecord(uuid)
                adapter?.cancelDiscovery()
                s.connect()
                socket = s
                output = s.outputStream
                startReader(s.inputStream)
                isConnected = true
                appendLog("Conectado.\n")
            } catch (t: Throwable) {
                appendLog("Error de conexión: ${t.message}\n")
                closeInternal()
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            closeInternal()
            appendLog("Desconectado.\n")
        }
    }

    fun sendLine(line: String) {
        val bytes = (line + "\n").toByteArray()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                output?.write(bytes)
                appendLog("> $line\n")
            } catch (t: Throwable) {
                appendLog("Error al enviar: ${t.message}\n")
            }
        }
    }

    private fun startReader(input: InputStream) {
        readJob?.cancel()
        readJob = viewModelScope.launch(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            while (isActive) {
                try {
                    val n = input.read(buffer)
                    if (n > 0) {
                        val s = String(buffer, 0, n)
                        appendLog(s)
                    } else if (n < 0) {
                        break
                    }
                } catch (t: Throwable) {
                    appendLog("Lectura finalizada: ${t.message}\n")
                    break
                }
            }
            closeInternal()
        }
    }

    private suspend fun closeInternal() {
        try {
            readJob?.cancelAndJoin()
        } catch (_: Throwable) { }
        readJob = null
        try { output?.close() } catch (_: Throwable) { }
        try { socket?.close() } catch (_: Throwable) { }
        output = null
        socket = null
        isConnected = false
    }

    private fun appendLog(s: String) {
        if (logNewestFirst) {
            log = s + log
            if (log.length > 20000) {
                log = log.take(20000)
            }
        } else {
            log += s
            if (log.length > 20000) {
                log = log.takeLast(20000)
            }
        }
    }

    fun clearLog() { log = "" }
    fun toggleLogOrder() { logNewestFirst = !logNewestFirst }
    fun toggleLogVisibility() { showLog = !showLog }
    // Funciones para configuración de UI
    fun updateLogFontSize(size: Float) { 
        logFontSize = size.coerceIn(10f, 20f) 
    }
    fun toggleFontSizeSlider() { showFontSizeSlider = !showFontSizeSlider }
    fun updateDarkTheme(enabled: Boolean) { useDarkTheme = enabled }
    fun toggleUIConfigVisibility() { showUIConfig = !showUIConfig }
    fun toggleUIConfigPosition() { uiConfigAboveLog = !uiConfigAboveLog }

    @SuppressLint("MissingPermission")
    fun toggleVoice(activity: ComponentActivity) {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            appendLog("Reconocimiento de voz no disponible\n")
            return
        }
        if (listening) {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            listening = false
            appendLog("Voz: detenido\n")
            return
        }
        if (speechRecognizer == null) speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
        val recognizer = speechRecognizer ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Di un comando: abrir, cerrar, modo uno/dos, menú, M1, M2")
        }
        recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                listening = false
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                val text = list.firstOrNull()?.lowercase().orEmpty()
                appendLog("Voz: $text\n")
                mapVoiceToCommand(text)?.let { sendLine(it) }
            }
            override fun onError(error: Int) { listening = false; appendLog("Voz error: $error\n") }
            override fun onReadyForSpeech(params: android.os.Bundle?) { }
            override fun onBeginningOfSpeech() { }
            override fun onRmsChanged(rmsdB: Float) { }
            override fun onBufferReceived(buffer: ByteArray?) { }
            override fun onEndOfSpeech() { }
            override fun onPartialResults(partialResults: android.os.Bundle?) { }
            override fun onEvent(eventType: Int, params: android.os.Bundle?) { }
        })
        listening = true
        recognizer.startListening(intent)
        appendLog("Voz: escuchando…\n")
    }

    private fun mapVoiceToCommand(text: String): String? {
        val t = text.trim()
        return when {
            t.contains("abrir") || t == "a" || t == "uno" -> "Abrir"
            t.contains("cerrar") || t == "c" || t == "dos" -> "Cerrar"
            t.contains("modo 1") || t.contains("modo uno") || t == "modo1" -> "modo1"
            t.contains("modo 2") || t.contains("modo dos") || t == "modo2" -> "modo2"
            t.contains("menú") || t.contains("menu") || t.contains("ayuda") -> "menu"
            t == "m1" || t.contains("eme uno") -> "M1"
            t == "m2" || t.contains("eme dos") -> "M2"
            else -> null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    vm: BluetoothViewModel, 
    modifier: Modifier = Modifier, 
    onLogout: () -> Unit = {}, 
    onNavigateToProfile: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val devices = vm.pairedDevices
    val selected = vm.selectedDevice
    val mainScroll = rememberScrollState()
    val logScroll = rememberScrollState()
    val clip = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
                TopAppBar(
                    title = { Text("Arduino BT Controller") },
                    colors = TopAppBarDefaults.topAppBarColors(),
                    actions = {
                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = !menuExpanded }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Configuración")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Mi Perfil") },
                                onClick = { 
                                    menuExpanded = false
                                    onNavigateToProfile()
                                }
                            )
                            androidx.compose.material3.Divider()
                            DropdownMenuItem(
                                text = { Text(if (vm.logNewestFirst) "Nuevos arriba ✓" else "Nuevos arriba") },
                                onClick = { 
                                    if (!vm.logNewestFirst) vm.toggleLogOrder()
                                    menuExpanded = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (!vm.logNewestFirst) "Nuevos abajo ✓" else "Nuevos abajo") },
                                onClick = { 
                                    if (vm.logNewestFirst) vm.toggleLogOrder()
                                    menuExpanded = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ocultar/Mostrar log") },
                                onClick = { vm.toggleLogVisibility(); menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (vm.showUIConfig) "Ocultar configuración" else "Mostrar configuración") },
                                onClick = { vm.toggleUIConfigVisibility(); menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (vm.uiConfigAboveLog) "Config arriba del log ✓" else "Config arriba del log") },
                                onClick = { 
                                    if (!vm.uiConfigAboveLog) vm.toggleUIConfigPosition()
                                    menuExpanded = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (!vm.uiConfigAboveLog) "Config abajo del log ✓" else "Config abajo del log") },
                                onClick = { 
                                    if (vm.uiConfigAboveLog) vm.toggleUIConfigPosition()
                                    menuExpanded = false 
                                }
                            )
                            androidx.compose.material3.Divider()
                            DropdownMenuItem(
                                text = { Text("Cerrar Sesión") },
                                onClick = { 
                                    menuExpanded = false
                                    onLogout()
                                }
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            // Sección principal: layout vertical con padding y scroll
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(mainScroll)
            ) {
        // Card: selección de dispositivo emparejado
        Text("Dispositivo", color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selected?.name ?: selected?.address ?: "Selecciona dispositivo",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dispositivo BT emparejado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    devices.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d.name ?: d.address) },
                            onClick = {
                                vm.select(d)
                                expanded = false
                            }
                        )
                    }
                    if (devices.isEmpty()) {
                        DropdownMenuItem(text = { Text("Sin emparejados") }, onClick = { expanded = false })
                    }
                }
            }
            OutlinedButton(onClick = { vm.refreshPaired() }) { Text("Actualizar") }
        }

        Spacer(Modifier.height(12.dp))

        // Card: controles de conexión y voz
        Text("Conexión", color = Color.Gray)
        Card(colors = CardDefaults.cardColors()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
                Button(onClick = { vm.connect() }, enabled = !vm.isConnected && selected != null) { Text("Conectar") }
                OutlinedButton(onClick = { vm.disconnect() }, enabled = vm.isConnected) { Text("Desconectar") }
                IconButton(onClick = { vm.toggleVoice(activity = context as ComponentActivity) }, enabled = vm.isConnected) {
                    Icon(Icons.Filled.Mic, contentDescription = "Voz")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Card: acciones principales
        Card(colors = CardDefaults.cardColors(), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                AssistChip(onClick = { vm.sendLine("Abrir") }, label = { Text("Abrir") }, leadingIcon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) })
                AssistChip(onClick = { vm.sendLine("Cerrar") }, label = { Text("Cerrar") }, leadingIcon = { Icon(Icons.Filled.Pause, contentDescription = null) })
                AssistChip(onClick = { vm.sendLine("M1") }, label = { Text("M1") })
                AssistChip(onClick = { vm.sendLine("M2") }, label = { Text("M2") })
            }
        }

        Spacer(Modifier.height(8.dp))

        // Card: comandos de modo
        Text("Comandos (modo2 = sensor, modo1 = RFID)", color = Color.Gray)
        Card(colors = CardDefaults.cardColors(), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                AssistChip(onClick = { vm.sendLine("modo1") }, label = { Text("modo1") })
                AssistChip(onClick = { vm.sendLine("modo2") }, label = { Text("modo2") })
                AssistChip(onClick = { vm.sendLine("menu") }, label = { Text("menu") })
            }
        }

        Spacer(Modifier.height(16.dp))
        
        // Configuración de UI (mostrar arriba del log si uiConfigAboveLog es true)
        if (vm.showUIConfig && vm.uiConfigAboveLog) {
            // Card: Configuración de UI (Tema y tamaño de fuente)
            Text("Configuración de Interfaz", color = Color.Gray)
            Card(colors = CardDefaults.cardColors(), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // RadioButtons para tema claro/oscuro
                    Text("Tema", modifier = Modifier.padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = !vm.useDarkTheme,
                                onClick = { vm.updateDarkTheme(false) },
                                colors = RadioButtonDefaults.colors()
                            )
                            Text("Claro", modifier = Modifier.padding(start = 4.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = vm.useDarkTheme,
                                onClick = { vm.updateDarkTheme(true) },
                                colors = RadioButtonDefaults.colors()
                            )
                            Text("Oscuro", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Switch para mostrar/ocultar slider de tamaño de fuente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ajustar tamaño de fuente del log")
                        Switch(
                            checked = vm.showFontSizeSlider,
                            onCheckedChange = { vm.toggleFontSizeSlider() }
                        )
                    }
                    
                    // Slider para cambiar tamaño de fuente (visible solo si el switch está activo)
                    if (vm.showFontSizeSlider) {
                        Spacer(Modifier.height(8.dp))
                        Text("Tamaño: ${vm.logFontSize.toInt()} sp", modifier = Modifier.padding(bottom = 4.dp))
                        Slider(
                            value = vm.logFontSize,
                            onValueChange = { vm.updateLogFontSize(it) },
                            valueRange = 10f..20f,
                            steps = 9, // 10 valores: 10, 11, 12, ..., 20
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        
        // Card: log de comunicación + limpiar/copiar (ocultable)
        if (vm.showLog) {
            Card(colors = CardDefaults.cardColors(), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Log")
                        Row {
                            IconButton(onClick = { clip.setText(buildAnnotatedString { append(vm.log) }) }) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar log") }
                            IconButton(onClick = { vm.clearLog() }) { Icon(Icons.Filled.Delete, contentDescription = "Limpiar log") }
                        }
                    }
                    OutlinedTextField(
                        value = vm.log,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = TextStyle(fontSize = vm.logFontSize.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(logScroll)
                    )
                }
            }
        }
        
        // Configuración de UI (mostrar abajo del log si uiConfigAboveLog es false)
        if (vm.showUIConfig && !vm.uiConfigAboveLog) {
            Spacer(Modifier.height(16.dp))
            // Card: Configuración de UI (Tema y tamaño de fuente)
            Text("Configuración de Interfaz", color = Color.Gray)
            Card(colors = CardDefaults.cardColors(), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // RadioButtons para tema claro/oscuro
                    Text("Tema", modifier = Modifier.padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = !vm.useDarkTheme,
                                onClick = { vm.updateDarkTheme(false) },
                                colors = RadioButtonDefaults.colors()
                            )
                            Text("Claro", modifier = Modifier.padding(start = 4.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = vm.useDarkTheme,
                                onClick = { vm.updateDarkTheme(true) },
                                colors = RadioButtonDefaults.colors()
                            )
                            Text("Oscuro", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Switch para mostrar/ocultar slider de tamaño de fuente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ajustar tamaño de fuente del log")
                        Switch(
                            checked = vm.showFontSizeSlider,
                            onCheckedChange = { vm.toggleFontSizeSlider() }
                        )
                    }
                    
                    // Slider para cambiar tamaño de fuente (visible solo si el switch está activo)
                    if (vm.showFontSizeSlider) {
                        Spacer(Modifier.height(8.dp))
                        Text("Tamaño: ${vm.logFontSize.toInt()} sp", modifier = Modifier.padding(bottom = 4.dp))
                        Slider(
                            value = vm.logFontSize,
                            onValueChange = { vm.updateLogFontSize(it) },
                            valueRange = 10f..20f,
                            steps = 9, // 10 valores: 10, 11, 12, ..., 20
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
            }
        }
    }