# 📚 GUÍA COMPLETA - BASE DE DATOS SQLite LudoMix

## 🎯 Objetivo

Se ha implementado un sistema completo de base de datos SQLite para:
- ✅ Almacenar usuarios con email, nombre de usuario y contraseña
- ✅ Guardar puntuaciones de cada juego
- ✅ Controlar acceso: solo usuarios registrados pueden iniciar sesión
- ✅ Mantener histórico de puntuaciones

---

## 📋 Contenido de la Implementación

### **Archivos Nuevos Creados:**
1. `DatabaseHelper.java` - Gestor de la BD
2. `UsuarioDAO.java` - Operaciones de usuarios
3. `PuntuacionDAO.java` - Operaciones de puntuaciones

### **Archivos Modificados:**
1. `Usuario.java` - Campos actualizados
2. `Puntuacion.java` - Campos actualizados
3. `LoginActivity.java` - Integración de BD
4. `ScoresActivity.java` - Integración de BD

### **Documentación Generada:**
1. `DATABASE_README.md` - Documentación técnica
2. `RESUMEN_CAMBIOS_BD.md` - Resumen visual de cambios
3. `EXAMPLE_GUARDAR_PUNTUACION.java` - Ejemplo de integración
4. `GUIA_COMPLETA_BD.md` - Este archivo

---

## 🔐 Sistema de Autenticación

### **Requisitos de Registro:**
- Email válido (con validación de formato)
- Nombre de usuario único (no puede repetirse)
- Contraseña (se almacena en texto, ver nota de seguridad)

### **Flujo de Registro:**
```
1. Usuario abre la app
   ↓
2. Haz clic en "Registrar"
   ↓
3. El campo de email aparece
   ↓
4. Introduce: email, usuario, contraseña
   ↓
5. Haz clic en "Registrar"
   ↓
6. Se validan los datos
   ↓
7. Se crea el usuario en la BD
   ↓
8. Inicio de sesión automático
   ↓
9. Acceso a MenuActivity
```

### **Flujo de Login:**
```
1. Usuario abre la app
   ↓
2. Introduce: usuario, contraseña
   ↓
3. Haz clic en "Iniciar sesión"
   ↓
4. Se verifica si el usuario existe en la BD
   ↓
   ├─ NO EXISTE → Mensaje: "Usuario no registrado. Regístrate primero"
   │
   └─ SÍ EXISTE → Valida contraseña
      ├─ INCORRECTA → Mensaje: "Contraseña incorrecta"
      │
      └─ CORRECTA → ¡Acceso concedido!
         ↓
         Abre MenuActivity
```

---

## 📊 Estructura de Datos

### **Tabla: usuarios**
```
id          : INTEGER (PRIMARY KEY, AUTO INCREMENT)
username    : TEXT (UNIQUE, NOT NULL) - Nombre de usuario único
email       : TEXT (NOT NULL) - Correo electrónico
password    : TEXT (NOT NULL) - Contraseña
puntuacion  : INTEGER (DEFAULT 0) - Puntuación total acumulada
```

### **Tabla: puntuaciones**
```
id          : INTEGER (PRIMARY KEY, AUTO INCREMENT)
username    : TEXT (NOT NULL) - Referencia a usuarios.username
puntos      : INTEGER (NOT NULL) - Puntos obtenidos
juego       : TEXT (NOT NULL) - Nombre del juego
fecha       : LONG (NOT NULL) - Timestamp de cuándo se registró
```

---

## 🎮 Implementación en Actividades de Juego

### **IMPORTANTE: Todavía no está integrado en las actividades de juego**

Para integrar la puntuación en cualquier actividad de juego, sigue estos pasos:

#### **1. Agregar DAOs a la Activity**
```java
private UsuarioDAO usuarioDAO;
private PuntuacionDAO puntuacionDAO;
private SharedPreferences prefs;
private static final String KEY_LOGGED_IN = "logged_in_user";
```

#### **2. Inicializar en onCreate()**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);
    
    prefs = getSharedPreferences("ludomix_prefs", Context.MODE_PRIVATE);
    
    usuarioDAO = new UsuarioDAO(this);
    usuarioDAO.open();
    
    puntuacionDAO = new PuntuacionDAO(this);
    puntuacionDAO.open();
    
    // ... resto del código
}
```

#### **3. Guardar puntuación cuando el jugador gana**
```java
private void onGameWon(int puntos) {
    String username = prefs.getString(KEY_LOGGED_IN, null);
    
    if (username != null) {
        // Crear puntuación
        Puntuacion puntuacion = new Puntuacion(username, puntos, "Nombre del Juego");
        
        // Registrar en BD
        puntuacionDAO.registrarPuntuacion(puntuacion);
        
        // Actualizar puntuación total del usuario
        Usuario usuario = usuarioDAO.obtenerUsuario(username);
        int nuevaPuntuacion = usuario.getPuntuacion() + puntos;
        usuarioDAO.actualizarPuntuacion(username, nuevaPuntuacion);
        
        Toast.makeText(this, "¡Puntuación guardada!", Toast.LENGTH_SHORT).show();
    }
}
```

#### **4. Cerrar en onDestroy()**
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (usuarioDAO != null) usuarioDAO.close();
    if (puntuacionDAO != null) puntuacionDAO.close();
}
```

---

## 🎯 Ejemplo Completo: RpsActivity

Aquí está cómo integrar en Rock-Paper-Scissors:

```java
public class RpsActivity extends AppCompatActivity {
    private UsuarioDAO usuarioDAO;
    private PuntuacionDAO puntuacionDAO;
    private SharedPreferences prefs;
    private static final String KEY_LOGGED_IN = "logged_in_user";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rps);
        
        prefs = getSharedPreferences("ludomix_prefs", Context.MODE_PRIVATE);
        usuarioDAO = new UsuarioDAO(this);
        usuarioDAO.open();
        puntuacionDAO = new PuntuacionDAO(this);
        puntuacionDAO.open();
        
        // Botones de opciones
        findViewById(R.id.btnRock).setOnClickListener(v -> playGame("rock"));
        findViewById(R.id.btnPaper).setOnClickListener(v -> playGame("paper"));
        findViewById(R.id.btnScissors).setOnClickListener(v -> playGame("scissors"));
    }
    
    private void playGame(String playerChoice) {
        String computerChoice = getComputerChoice();
        String result = determineWinner(playerChoice, computerChoice);
        
        if (result.equals("win")) {
            // El jugador ganó, registrar puntuación
            int puntos = 10; // O calcular según dificultad
            String username = prefs.getString(KEY_LOGGED_IN, null);
            
            if (username != null) {
                Puntuacion puntuacion = new Puntuacion(username, puntos, "Piedra Papel Tijera");
                puntuacionDAO.registrarPuntuacion(puntuacion);
                
                Usuario usuario = usuarioDAO.obtenerUsuario(username);
                usuarioDAO.actualizarPuntuacion(username, usuario.getPuntuacion() + puntos);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioDAO != null) usuarioDAO.close();
        if (puntuacionDAO != null) puntuacionDAO.close();
    }
}
```

---

## 📱 ScoresActivity (Ya Integrada)

La pantalla de puntuaciones ahora:

```
┌─────────────────────────────────────┐
│ Usuario: john_doe                   │
│ Email: john@example.com             │
│ Puntuación Total: 450               │
├─────────────────────────────────────┤
│ === PUNTUACIONES POR JUEGO ===      │
│                                     │
│ Piedra Papel Tijera:                │
│   Partidas: 15                      │
│   Puntos Totales: 120               │
│                                     │
│ Tres en Raya:                       │
│   Partidas: 8                       │
│   Puntos Totales: 80                │
│                                     │
│ Adivina Número:                     │
│   Partidas: 12                      │
│   Puntos Totales: 150               │
│                                     │
│ Memoria:                            │
│   Partidas: 10                      │
│   Puntos Totales: 100               │
└─────────────────────────────────────┘
```

---

## ⚙️ Métodos Disponibles

### **UsuarioDAO**

| Método | Parámetros | Retorna | Descripción |
|--------|-----------|---------|-------------|
| `registrarUsuario()` | Usuario | boolean | Registra nuevo usuario |
| `usuarioExiste()` | username | boolean | Verifica si existe |
| `validarLogin()` | username, password | boolean | Valida credenciales |
| `obtenerUsuario()` | username | Usuario | Obtiene datos del usuario |
| `actualizarPuntuacion()` | username, puntos | boolean | Actualiza puntuación |
| `obtenerTodosLosUsuarios()` | - | Cursor | Obtiene todos usuarios |

### **PuntuacionDAO**

| Método | Parámetros | Retorna | Descripción |
|--------|-----------|---------|-------------|
| `registrarPuntuacion()` | Puntuacion | boolean | Registra puntuación |
| `obtenerPuntuacionesUsuario()` | username | Cursor | Obtiene todas del usuario |
| `obtenerMejoresPuntuacionesPorJuego()` | juego | Cursor | Top 10 de un juego |
| `obtenerPuntosTotal()` | username | int | Suma total de puntos |
| `eliminarPuntuacionesUsuario()` | username | boolean | Elimina todas del usuario |

---

## 🔄 Ciclo de Vida de los DAOs

```
Activity
  ↓
onCreate()
  ├─ DAO.open()  ← Abre conexión a BD
  ├─ ... uso de DAO ...
  ↓
onDestroy()
  └─ DAO.close() ← Cierra conexión a BD
```

**⚠️ IMPORTANTE**: Siempre cerrar los DAOs en `onDestroy()` para evitar fugas de memoria.

---

## 🛡️ Notas de Seguridad

⚠️ **LA CONTRASEÑA SE ALMACENA EN TEXTO PLANO**

Para producción, se recomienda:
1. Usar encriptación (como BCrypt)
2. No almacenar contraseñas en el cliente
3. Usar autenticación en servidor remoto
4. Implementar SSL/TLS

**Ejemplo con encriptación (futuro):**
```java
// Instalar: implementation 'org.mindrot:jbcrypt:0.4'
String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
usuarioDAO.registrarUsuario(new Usuario(username, email, hashPassword));

// Validar:
if (BCrypt.checkpw(password, hashStoredHash)) {
    // Login correcto
}
```

---

## 📈 Ejemplo de Integración: GuessNumberActivity

Aquí está el código que necesitas para GuessNumberActivity:

```java
// En la clase
private UsuarioDAO usuarioDAO;
private PuntuacionDAO puntuacionDAO;
private SharedPreferences prefs;
private static final String KEY_LOGGED_IN = "logged_in_user";

// En onCreate()
usuarioDAO = new UsuarioDAO(this);
usuarioDAO.open();
puntuacionDAO = new PuntuacionDAO(this);
puntuacionDAO.open();

// En checkGuess(), cuando el jugador acierta:
if (guess == secretNumber) {
    String msg = getString(R.string.correct_guess_in_turns, attempts);
    txtResult.setText(msg);
    
    // Guardar puntuación
    String username = prefs.getString(KEY_LOGGED_IN, null);
    if (username != null) {
        int puntos = Math.max(100 - attempts * 5, 10);
        Puntuacion p = new Puntuacion(username, puntos, "Adivina Número");
        puntuacionDAO.registrarPuntuacion(p);
        
        Usuario u = usuarioDAO.obtenerUsuario(username);
        usuarioDAO.actualizarPuntuacion(username, u.getPuntuacion() + puntos);
    }
}

// En onDestroy()
@Override
protected void onDestroy() {
    super.onDestroy();
    if (usuarioDAO != null) usuarioDAO.close();
    if (puntuacionDAO != null) puntuacionDAO.close();
}
```

---

## 🐛 Posibles Errores y Soluciones

### Error: "table usuarios already exists"
**Causa**: Base de datos ya existe  
**Solución**: Es normal en la segunda ejecución, ignorar

### Error: "UNIQUE constraint failed: username"
**Causa**: El username ya está registrado  
**Solución**: Usar otro nombre de usuario

### Error: "NullPointerException"
**Causa**: DAO no inicializado  
**Solución**: Asegurar que `open()` se llama en `onCreate()`

### Error: "database is locked"
**Causa**: Base de datos no se cerró correctamente  
**Solución**: Asegurar que `close()` se llama en `onDestroy()`

---

## ✅ Checklist de Implementación

Para integrar completamente en todas las actividades:

- [ ] LoginActivity ✅ (Ya integrado)
- [ ] ScoresActivity ✅ (Ya integrado)
- [ ] GuessNumberActivity (Pendiente)
- [ ] RpsActivity (Pendiente)
- [ ] TicTacToeActivity (Pendiente)
- [ ] MemoryGameActivity (Pendiente)

---

## 📞 Soporte

Para agregar puntuaciones en una nueva actividad:

1. Copia el código del archivo `EXAMPLE_GUARDAR_PUNTUACION.java`
2. Adapta el nombre del juego
3. Define cómo calcular los puntos
4. Prueba guardando una puntuación
5. Verifica en ScoresActivity que aparezca

---

**Versión**: 1.0  
**Última actualización**: 2025  
**Estado**: ✅ Implementación Completa

