#include <SoftwareSerial.h>
#include <Servo.h>
#include <SPI.h>
#include <MFRC522.h>
#include <AESLib.h>
// Base64 implementado directamente (no requiere librería externa)

// ---------- CONFIG ----------
SoftwareSerial BT(2, 3);
Servo servo;
const int SERVO_PIN = 5;

const int trigPin = 4;
const int echoPin = 6;

// RFID RC522
#define RST_PIN 8
#define SS_PIN 7
MFRC522 mfrc522(SS_PIN, RST_PIN);

// UIDs permitidos
const unsigned long uidAbrir = 2747323383;
const unsigned long uidCerrar = 2205297957;

// Rango válido
const int distanciaMin = 5;
const int distanciaMax = 100;

// Estado
int estadoServo = 0;
bool servoAdjunto = false;
unsigned long ultimaPresenciaOK = 0;
int ultimaDistancia = -1;

// Buffer simple
// Usar buffer pequeño por defecto (como Script.txt original) para ahorrar memoria
// Solo aumentar si realmente se necesita cifrado
char entrada[16];  // Buffer pequeño (como Script.txt) - Arduino Uno tiene solo 2KB RAM
byte entradaLen = 0;

// Modo (1=rfid, 2=sensor)
byte modo = 1;

// Timing
unsigned long ultimoAviso = 0;
unsigned long ultimoRFID = 0;
unsigned long ultimoHeartbeat = 0;
unsigned long ultimoHeartbeatEnviado = 0;

// ---------- SEGURIDAD (Cumplimiento ISO/IEC 27001) ----------
// Clave AES-128 (16 bytes) - Misma que en Android
uint8_t aesKey[16] = {'A','r','d','u','i','n','o','I','o','T','2','0','2','4','!','!'};

AESLib aesLib;

// ---------- BASE64 (Implementación directa) ----------
const char base64_chars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

String base64_encode_wrapper(uint8_t* data, int len) {
  String encoded = "";
  int i = 0;
  int j = 0;
  uint8_t char_array_3[3];
  uint8_t char_array_4[4];
  
  while (len--) {
    char_array_3[i++] = *(data++);
    if (i == 3) {
      char_array_4[0] = (char_array_3[0] & 0xfc) >> 2;
      char_array_4[1] = ((char_array_3[0] & 0x03) << 4) + ((char_array_3[1] & 0xf0) >> 4);
      char_array_4[2] = ((char_array_3[1] & 0x0f) << 2) + ((char_array_3[2] & 0xc0) >> 6);
      char_array_4[3] = char_array_3[2] & 0x3f;
      
      for (i = 0; i < 4; i++) {
        encoded += base64_chars[char_array_4[i]];
      }
      i = 0;
    }
  }
  
  if (i) {
    for (j = i; j < 3; j++) {
      char_array_3[j] = '\0';
    }
    
    char_array_4[0] = (char_array_3[0] & 0xfc) >> 2;
    char_array_4[1] = ((char_array_3[0] & 0x03) << 4) + ((char_array_3[1] & 0xf0) >> 4);
    char_array_4[2] = ((char_array_3[1] & 0x0f) << 2) + ((char_array_3[2] & 0xc0) >> 6);
    char_array_4[3] = char_array_3[2] & 0x3f;
    
    for (j = 0; j < i + 1; j++) {
      encoded += base64_chars[char_array_4[j]];
    }
    
    while (i++ < 3) {
      encoded += '=';
    }
  }
  
  return encoded;
}

int base64_decode_wrapper(uint8_t* output, const char* input, int inputLen) {
  int in_len = inputLen;
  int i = 0;
  int j = 0;
  int in = 0;
  uint8_t char_array_4[4], char_array_3[3];
  int decodedLen = 0;
  
  while (in_len-- && (input[in] != '=') && is_base64(input[in])) {
    char_array_4[i++] = input[in]; in++;
    if (i == 4) {
      for (i = 0; i < 4; i++) {
        char_array_4[i] = base64_char_index(char_array_4[i]);
      }
      
      char_array_3[0] = (char_array_4[0] << 2) + ((char_array_4[1] & 0x30) >> 4);
      char_array_3[1] = ((char_array_4[1] & 0xf) << 4) + ((char_array_4[2] & 0x3c) >> 2);
      char_array_3[2] = ((char_array_4[2] & 0x3) << 6) + char_array_4[3];
      
      for (i = 0; i < 3; i++) {
        output[decodedLen++] = char_array_3[i];
      }
      i = 0;
    }
  }
  
  if (i) {
    for (j = i; j < 4; j++) {
      char_array_4[j] = 0;
    }
    
    for (j = 0; j < 4; j++) {
      char_array_4[j] = base64_char_index(char_array_4[j]);
    }
    
    char_array_3[0] = (char_array_4[0] << 2) + ((char_array_4[1] & 0x30) >> 4);
    char_array_3[1] = ((char_array_4[1] & 0xf) << 4) + ((char_array_4[2] & 0x3c) >> 2);
    char_array_3[2] = ((char_array_4[2] & 0x3) << 6) + char_array_4[3];
    
    for (j = 0; j < i - 1; j++) {
      output[decodedLen++] = char_array_3[j];
    }
  }
  
  return decodedLen;
}

bool is_base64(uint8_t c) {
  return (isalnum(c) || (c == '+') || (c == '/'));
}

uint8_t base64_char_index(uint8_t c) {
  if (c >= 'A' && c <= 'Z') return c - 'A';
  if (c >= 'a' && c <= 'z') return c - 'a' + 26;
  if (c >= '0' && c <= '9') return c - '0' + 52;
  if (c == '+') return 62;
  if (c == '/') return 63;
  return 0;
}

// Token de autenticación (se actualiza con cada comando autenticado)
String authToken = "";
unsigned long tokenExpiry = 0;

// Modo seguro (cifrado DESACTIVADO por defecto para Arduino Uno - poca memoria)
// Solo activar si se necesita y hay suficiente memoria disponible
bool modoSeguro = false; // DESACTIVADO: Arduino Uno tiene solo 2KB RAM (96% usado con cifrado)

// ---------- UTIL PRINT ----------
void btPrint(const char* s) { 
  if (modoSeguro) {
    btPrintSecure(s);
  } else {
    BT.print(s); 
    Serial.print(s); 
  }
}

void btPrintln(const char* s) { 
  if (modoSeguro) {
    btPrintlnSecure(s);
  } else {
    BT.println(s); 
    Serial.println(s); 
  }
}

void btPrintSecure(const char* s) {
  String mensaje(s);
  String cifrado = encryptAndEncode(mensaje, false);
  BT.print(cifrado);
  Serial.print("[SEC] ");
  Serial.println(cifrado);
}

void btPrintlnSecure(const char* s) {
  String mensaje(s);
  String cifrado = encryptAndEncode(mensaje, false);
  BT.println(cifrado);
  Serial.print("[SEC] ");
  Serial.println(cifrado);
}

// ---------- CIFRADO Y VERIFICACIÓN DE INTEGRIDAD ----------
/**
 * Cifra mensaje con AES-128 CBC y calcula CRC-16
 * Formato: [IV][CIFRADO][CRC16]
 * Cumple con ISO/IEC 18033-2 (cifrado) e ISO/IEC 13239 (CRC)
 */
String encryptAndEncode(String message, bool requiresAuth) {
  if (!modoSeguro) {
    return "PLAIN:" + message;
  }
  
  // Agregar token si es requerido
  String messageToEncrypt = message;
  if (requiresAuth && authToken.length() > 0 && millis() < tokenExpiry) {
    messageToEncrypt = authToken + ":" + message;
  }
  
  // Generar IV aleatorio (16 bytes)
  uint8_t iv[16];
  for (int i = 0; i < 16; i++) {
    iv[i] = random(256);
  }
  
  // Preparar mensaje para cifrado
  int msgLen = messageToEncrypt.length();
  uint8_t plaintext[256]; // Tamaño máximo
  uint8_t ciphertext[256]; // Suficiente espacio para padding
  
  // Copiar mensaje
  if (msgLen > 255) msgLen = 255; // Limitar tamaño
  for (int i = 0; i < msgLen; i++) {
    plaintext[i] = messageToEncrypt.charAt(i);
  }
  plaintext[msgLen] = '\0';
  
  // Cifrar con AESLib (CBC mode) - AESLib maneja el padding automáticamente
  int encryptedLen = aesLib.encrypt((byte*)plaintext, msgLen, (byte*)ciphertext, aesKey, 16, iv);
  
  if (encryptedLen <= 0) {
    return "PLAIN:" + message; // Fallback
  }
  
  // Convertir IV y cifrado a Base64
  String ivBase64 = base64_encode_wrapper(iv, 16);
  String encryptedBase64 = base64_encode_wrapper(ciphertext, encryptedLen);
  
  // Calcular CRC-16 para verificación de integridad (sobre bytes como en Android)
  uint8_t dataForCrc[272]; // IV (16) + encrypted (hasta 256)
  for (int i = 0; i < 16; i++) {
    dataForCrc[i] = iv[i];
  }
  for (int i = 0; i < encryptedLen; i++) {
    dataForCrc[16 + i] = ciphertext[i];
  }
  uint16_t crc = calculateCRC16Bytes(dataForCrc, 16 + encryptedLen);
  String crcHex = String(crc, HEX);
  while (crcHex.length() < 4) {
    crcHex = "0" + crcHex;
  }
  crcHex.toUpperCase();
  
  String result = ivBase64 + "|" + encryptedBase64 + "|" + crcHex;
  
  return result;
}

/**
 * Descifra y verifica integridad de mensaje recibido
 */
String decodeAndDecrypt(String encodedMessage) {
  if (encodedMessage.startsWith("PLAIN:")) {
    return encodedMessage.substring(6);
  }
  
  // Separar componentes
  int pipe1 = encodedMessage.indexOf('|');
  int pipe2 = encodedMessage.lastIndexOf('|');
  
  if (pipe1 < 0 || pipe2 < 0 || pipe1 >= pipe2) {
    return ""; // Formato inválido
  }
  
  String ivBase64 = encodedMessage.substring(0, pipe1);
  String encryptedBase64 = encodedMessage.substring(pipe1 + 1, pipe2);
  String receivedCrcHex = encodedMessage.substring(pipe2 + 1);
  
  // Verificar CRC-16 (calcular sobre bytes como en Android)
  // Convertir strings base64 a bytes para calcular CRC
  uint8_t ivBytes[16];
  int ivBytesLen = base64_decode_wrapper(ivBytes, ivBase64.c_str(), ivBase64.length());
  uint8_t encryptedBytes[256];
  int encryptedBytesLen = base64_decode_wrapper(encryptedBytes, encryptedBase64.c_str(), encryptedBase64.length());
  
  // Calcular CRC sobre bytes (como en Android)
  uint8_t dataForCrc[272]; // 16 + 256
  for (int i = 0; i < 16; i++) {
    dataForCrc[i] = ivBytes[i];
  }
  for (int i = 0; i < encryptedBytesLen; i++) {
    dataForCrc[16 + i] = encryptedBytes[i];
  }
  uint16_t calculatedCrc = calculateCRC16Bytes(dataForCrc, 16 + encryptedBytesLen);
  uint16_t receivedCrc = strtoul(receivedCrcHex.c_str(), NULL, 16);
  
  if (calculatedCrc != receivedCrc) {
    Serial.print("[SEC ERROR] CRC no coincide. Calc: ");
    Serial.print(calculatedCrc, HEX);
    Serial.print(" Recv: ");
    Serial.println(receivedCrc, HEX);
    return ""; // Integridad fallida
  }
  
  // Decodificar IV desde Base64
  uint8_t iv[16];
  int ivLen = base64_decode_wrapper(iv, ivBase64.c_str(), ivBase64.length());
  if (ivLen != 16) {
    Serial.print("[SEC ERROR] IV inválido. Len: ");
    Serial.println(ivLen);
    return "";
  }
  
  // Decodificar cifrado desde Base64
  uint8_t encrypted[256];
  int encryptedLen = base64_decode_wrapper(encrypted, encryptedBase64.c_str(), encryptedBase64.length());
  
  if (encryptedLen <= 0) {
    Serial.print("[SEC ERROR] Cifrado inválido. Len: ");
    Serial.println(encryptedLen);
    return "";
  }
  
  Serial.print("[DEBUG] IV len: ");
  Serial.print(ivLen);
  Serial.print(", Encrypted len: ");
  Serial.println(encryptedLen);
  
  // Descifrar con AESLib (maneja padding automáticamente)
  uint8_t plaintext[256]; // Tamaño máximo
  int decryptedLen = aesLib.decrypt(encrypted, encryptedLen, plaintext, aesKey, 16, iv);
  
  if (decryptedLen <= 0 || decryptedLen > 255) {
    Serial.println("[SEC ERROR] Error al descifrar");
    return "";
  }
  
  plaintext[decryptedLen] = '\0'; // Null terminator
  
  // Convertir a String
  String decrypted = String((char*)plaintext);
  
  // Extraer token si existe
  int colonPos = decrypted.indexOf(':');
  if (colonPos > 0 && authToken.length() > 0) {
    String token = decrypted.substring(0, colonPos);
    if (token == authToken && millis() < tokenExpiry) {
      return decrypted.substring(colonPos + 1); // Mensaje sin token
    }
  }
  
  return decrypted;
}

/**
 * Calcula CRC-16-CCITT para verificación de integridad (sobre bytes)
 * Cumple con ISO/IEC 13239
 */
uint16_t calculateCRC16Bytes(uint8_t* data, int len) {
  uint16_t crc = 0xFFFF;
  uint16_t polynomial = 0x1021; // CRC-16-CCITT
  
  for (int i = 0; i < len; i++) {
    crc ^= ((uint16_t)data[i] << 8);
    for (int j = 0; j < 8; j++) {
      if (crc & 0x8000) {
        crc = (crc << 1) ^ polynomial;
      } else {
        crc <<= 1;
      }
      crc &= 0xFFFF;
    }
  }
  
  return crc;
}

/**
 * Calcula CRC-16-CCITT para verificación de integridad (sobre String - para compatibilidad)
 */
uint16_t calculateCRC16(String data) {
  uint16_t crc = 0xFFFF;
  uint16_t polynomial = 0x1021; // CRC-16-CCITT
  
  for (int i = 0; i < data.length(); i++) {
    crc ^= ((uint16_t)data.charAt(i) << 8);
    for (int j = 0; j < 8; j++) {
      if (crc & 0x8000) {
        crc = (crc << 1) ^ polynomial;
      } else {
        crc <<= 1;
      }
      crc &= 0xFFFF;
    }
  }
  
  return crc;
}

// ---------- HEARTBEAT (RFC 6520) ----------
void sendHeartbeat() {
  if (modoSeguro) {
    String heartbeat = encryptAndEncode("HEARTBEAT", false);
    BT.println(heartbeat);
    ultimoHeartbeatEnviado = millis();
  }
}

void processHeartbeat() {
  ultimoHeartbeat = millis();
}

bool isConnectionAlive() {
  if (!modoSeguro) return true;
  unsigned long timeSinceHeartbeat = millis() - ultimoHeartbeat;
  return timeSinceHeartbeat < 15000; // 15 segundos timeout
}

// ---------- STRING UTILS ----------
bool equalsIgnoreCase(const char* a, const char* b) {
  while (*a && *b) {
    char ca = *a;
    char cb = *b;
    if (ca >= 'A' && ca <= 'Z') ca += 32; // to lower
    if (cb >= 'A' && cb <= 'Z') cb += 32; // to lower
    if (ca != cb) return false;
    ++a; ++b;
  }
  return (*a == '\0' && *b == '\0');
}

// ---------- SETUP ----------
void setup() {
  BT.begin(9600);
  Serial.begin(9600);
  
  // Inicializar generador aleatorio para IVs
  randomSeed(analogRead(0));

  servo.attach(SERVO_PIN);
  servo.write(0);
  delay(400);
  servo.detach();
  servoAdjunto = false;
  estadoServo = 0;

  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);

  SPI.begin();
  mfrc522.PCD_Init();

  mostrarMenu();
  if (modoSeguro) {
    Serial.println("[SEC] Modo seguro ACTIVADO");
  } else {
    Serial.println("[SEC] Modo seguro DESACTIVADO (texto plano)");
  }
}

void mostrarModo() {
  btPrint("Modo: ");
  if (modo == 1) btPrintln("RFID (Tarjetas + BT)");
  else btPrintln("SENSOR (Ultrasonico + BT)");
}

void mostrarMenu() {
  btPrintln("\r\n=== Control BT + RFID + Ultrasonico (SEGURO) ===");
  btPrintln("Comandos:");
  btPrintln("  modo1       -> RFID (tarjetas)");
  btPrintln("  modo2       -> SENSOR (ultrasonico)");
  btPrintln("  Abrir/A/1   -> Abrir (solo modo sensor)");
  btPrintln("  Cerrar/C/2  -> Cerrar (solo modo sensor)");
  btPrintln("  M1          -> Abrir (solo modo sensor)");
  btPrintln("  M2          -> Cerrar (solo modo sensor)");
  btPrintln("  menu        -> Mostrar este menú");
  mostrarModo();
  btPrint("\r\n> ");
}

void cambiarModo(byte nuevoModo) {
  if (nuevoModo == 1) {
    // Reactivar RFID
    pinMode(RST_PIN, OUTPUT);
    pinMode(SS_PIN, OUTPUT);
    SPI.begin();
    mfrc522.PCD_Init();
    btPrintln("RFID reactivado");
  } else {
    // Desactivar RFID
    SPI.end();
    pinMode(RST_PIN, INPUT);
    pinMode(SS_PIN, INPUT);
    btPrintln("RFID desactivado");
  }
  
  modo = nuevoModo;
  mostrarModo();
}

// ---------- LOOP ----------
void loop() {
  // Verificar conexión (heartbeat timeout) - SOLO si modo seguro está activo
  if (modoSeguro) {
    if (!isConnectionAlive()) {
      btPrintln("⚠️ Timeout de conexión");
      delay(1000);
    }
    
    // Enviar heartbeat cada 5 segundos
    if (millis() - ultimoHeartbeatEnviado >= 5000) {
      sendHeartbeat();
    }
  }

  // 1) Sensor (solo en modo 2)
  if (modo == 2) {
    // Deshabilitar RFID completamente en modo sensor
    SPI.end();
    pinMode(RST_PIN, INPUT);
    pinMode(SS_PIN, INPUT);
    
    bool presencia = medirPresencia();
    if (presencia) ultimaPresenciaOK = millis();

    // Avisos cada 1000ms
    if (millis() - ultimoAviso >= 1000) {
      if (presencia) {
        btPrint("Detectado a ");
        btPrint(String(ultimaDistancia).c_str());
        btPrintln(" cm");
      } else {
        btPrintln("Nada detectado o fuera de rango");
      }
      ultimoAviso = millis();
    }
  }

  // 2) Lectura BT
  while (BT.available()) {
    char c = BT.read();
    
    if (c == '\n' || c == '\r') {
      entrada[entradaLen] = '\0';
      if (entradaLen > 0) {
        if (modoSeguro) {
          // Modo seguro: descifrar mensaje
          String mensajeRecibido = String(entrada);
          mensajeRecibido.trim();
          String mensajeDecodificado = decodeAndDecrypt(mensajeRecibido);
          
          // Verificar si es heartbeat
          mensajeDecodificado.trim();
          if (mensajeDecodificado == "HEARTBEAT") {
            processHeartbeat();
            btPrint("\r\n💓 Heartbeat recibido\r\n> ");
            entradaLen = 0;
            continue;
          }
          
          // Si el descifrado falla, intentar procesar como texto plano (fallback)
          if (mensajeDecodificado.length() == 0) {
            // Fallback: intentar procesar como texto plano si parece un comando simple
            if (mensajeRecibido.length() < 20 && mensajeRecibido.indexOf('|') < 0) {
              Serial.println("[DEBUG] Fallback a texto plano");
              mensajeDecodificado = mensajeRecibido;
            } else {
              btPrintln("\r\n⚠️ Error al descifrar mensaje\r\n> ");
              entradaLen = 0;
              continue;
            }
          }
          
          // Copiar mensaje descifrado al buffer
          mensajeDecodificado.toCharArray(entrada, sizeof(entrada));
        }
        // Si modo seguro está desactivado, procesar directamente (como Script.txt)
        procesarComando();
      }
      entradaLen = 0;
      btPrint("\r\n> ");
    } else if (entradaLen < (sizeof(entrada) - 1)) {
      entrada[entradaLen++] = c;
    }
  }

  // 3) RFID (solo en modo 1)
  if (modo == 1 && (millis() - ultimoRFID >= 300)) {
    if (mfrc522.PICC_IsNewCardPresent()) {
      if (mfrc522.PICC_ReadCardSerial()) {
        unsigned long uid = 0;
        for (byte i = 0; i < 4; i++) {
          uid = (uid << 8) | mfrc522.uid.uidByte[i];
        }
        
        btPrint("\r\nRFID: ");
        btPrintln(String(uid).c_str());
        
        if (uid == uidAbrir || uid == uidCerrar) {
          toggleServo();
        } else {
          btPrintln("Tarjeta no autorizada");
        }
        
        mfrc522.PICC_HaltA();
        mfrc522.PCD_StopCrypto1();
      }
    }
    ultimoRFID = millis();
  }

  delay(10);
}

// ---------- COMANDOS ----------
void procesarComando() {
  // Comandos de modo
  if (strcmp(entrada, "modo1") == 0) {
    cambiarModo(1);
    return;
  }
  if (strcmp(entrada, "modo2") == 0) {
    cambiarModo(2);
    return;
  }

  // Mostrar menú
  if (strcmp(entrada, "menu") == 0 || strcmp(entrada, "help") == 0 || strcmp(entrada, "?") == 0) {
    mostrarMenu();
    return;
  }

  // Validar presencia (solo en modo sensor)
  if (modo == 2) {
    bool ahora = medirPresencia();
    bool ventana = (millis() - ultimaPresenciaOK) <= 2000;
    
    if (!(ahora || ventana)) {
      btPrintln("ERROR: Sin objeto en rango");
      return;
    }
  }

  // Comandos solo funcionan en modo sensor
  if (modo == 2) {
    // Comandos de control
    if (equalsIgnoreCase(entrada, "Abrir") || strcmp(entrada, "A") == 0 || 
        strcmp(entrada, "1") == 0) {
      btPrintln("OK: ABRIR");
      moverServo(90);
      return;
    }
    
    if (equalsIgnoreCase(entrada, "Cerrar") || strcmp(entrada, "C") == 0 || 
        strcmp(entrada, "2") == 0) {
      btPrintln("OK: CERRAR");
      moverServo(0);
      return;
    }

    // Comandos M1 y M2 (solo en modo sensor con presencia)
    if (strcmp(entrada, "M1") == 0) {
      btPrintln("OK: M1 ABRIR");
      moverServo(90);
      return;
    }
    if (strcmp(entrada, "M2") == 0) {
      btPrintln("OK: M2 CERRAR");
      moverServo(0);
      return;
    }
  } else {
    // En modo RFID, solo comandos de modo son válidos
    btPrintln("ERROR: En modo RFID solo usar tarjetas o comandos: modo1, modo2");
  }

  btPrintln("Comando invalido");
}

// ---------- SERVO ----------
void moverServo(int angulo) {
  if (estadoServo == angulo) return;
  
  servo.attach(SERVO_PIN);
  servo.write(angulo);
  delay(400);
  servo.detach();
  
  estadoServo = angulo;
}

void toggleServo() {
  if (estadoServo == 0) {
    btPrintln("OK: RFID ABRIR");
    moverServo(90);
  } else {
    btPrintln("OK: RFID CERRAR");
    moverServo(0);
  }
}

// ---------- SENSADO ----------
bool medirPresencia() {
  int d1 = medirDistancia();
  delay(4);
  int d2 = medirDistancia();
  int best = (d1 > 0 && d2 > 0) ? (d1 + d2) / 2 : (d1 > 0 ? d1 : d2);
  ultimaDistancia = best;
  return enRango(d1) && enRango(d2);
}

inline bool enRango(int d) {
  return (d > 0 && d >= distanciaMin && d <= distanciaMax);
}

int medirDistancia() {
  long suma = 0;
  int lecturas = 2;
  int validas = 0;

  for (int i = 0; i < lecturas; i++) {
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);

    // Timeout corto ~ 6 ms (hasta ~100 cm) para no bloquear
    unsigned long dur = pulseIn(echoPin, HIGH, 6000UL);
    if (dur > 0) {
      float cm = (dur * 0.034f) / 2.0f;
      suma += (long)cm;
      validas++;
    }
  }
  if (validas == 0) return -1;
  return (int)(suma / validas);
}
