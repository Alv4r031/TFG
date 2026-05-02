# 🏗️ ARQUITECTURA DE LA BASE DE DATOS - LudoMix

## Diagrama de Flujo General

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          APLICACIÓN LudoMix                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────┐        ┌────────────────────┐                   │
│  │  LoginActivity   │───────>│  UsuarioDAO        │                   │
│  │                  │        │  ────────────────  │                   │
│  │ - Registro       │        │ - registrarUsuario │                   │
│  │ - Login          │        │ - usuarioExiste    │                   │
│  │ - Validaciones   │        │ - validarLogin     │                   │
│  └──────────────────┘        │ - obtenerUsuario   │                   │
│                              └────────────────────┘                   │
│                                       ▼                                │
│  ┌──────────────────┐        ┌────────────────────┐                   │
│  │  MenuActivity    │        │  DatabaseHelper    │                   │
│  │                  │        │  ────────────────  │                   │
│  │ - Juegos         │        │ - onCreate()       │                   │
│  │ - Puntuaciones   │        │ - onUpgrade()      │                   │
│  └──────────────────┘        └────────────────────┘                   │
│         │                             ▼                                │
│         ├──────────────────>┌─────────────────────────────┐           │
│         │                   │  BASE DE DATOS SQLite       │           │
│         │                   │  ludomix.db                 │           │
│         │                   │                             │           │
│         │                   │ ┌─────────────────────────┐ │           │
│         │                   │ │ Tabla: usuarios         │ │           │
│         │                   │ ├─────────────────────────┤ │           │
│         │                   │ │ - id (PK)               │ │           │
│         │                   │ │ - username (UNIQUE)     │ │           │
│         │                   │ │ - email                 │ │           │
│         │                   │ │ - password              │ │           │
│         │                   │ │ - puntuacion            │ │           │
│         │                   │ └─────────────────────────┘ │           │
│         │                   │                             │           │
│         │                   │ ┌─────────────────────────┐ │           │
│         │                   │ │ Tabla: puntuaciones     │ │           │
│         │                   │ ├─────────────────────────┤ │           │
│         │                   │ │ - id (PK)               │ │           │
│         │                   │ │ - username (FK)         │ │           │
│         │                   │ │ - puntos                │ │           │
│         │                   │ │ - juego                 │ │           │
│         │                   │ │ - fecha                 │ │           │
│         │                   │ └─────────────────────────┘ │           │
│         │                   └─────────────────────────────┘           │
│         │                                                              │
│  ┌──────────────────────┐     ┌────────────────────┐                  │
│  │  Actividades Juegos  │────>│  PuntuacionDAO     │                  │
│  │  ──────────────────  │     │  ───────────────── │                  │
│  │ - GuessNumber        │     │ - registrar        │                  │
│  │ - Rps                │     │ - obtener          │                  │
│  │ - TicTacToe          │     │ - eliminar         │                  │
│  │ - Memory             │     │ - calcularTop      │                  │
│  └──────────────────────┘     └────────────────────┘                  │
│         ▲                                                              │
│         │                                                              │
│  ┌──────────────────┐                                                 │
│  │ ScoresActivity   │──────────────────────────────────────────────┐  │
│  │                  │                                              │  │
│  │ Muestra:         │                                              │  │
│  │ - Usuario actual │                                              │  │
│  │ - Puntuación tot │                                              │  │
│  │ - Historico      │                                              │  │
│  └──────────────────┘                                              │  │
│         ▲                                                          │  │
│         └──────────────────────────────────────────────────────────┘  │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Ciclo de Vida de Datos

### **1. Registro de Usuario**
```
Usuario introduce datos
    ↓
LoginActivity captura
    ↓
Validación de email y username
    ↓
UsuarioDAO.registrarUsuario(Usuario)
    ↓
DatabaseHelper inserta en tabla usuarios
    ↓
BD: INSERT INTO usuarios (username, email, password, puntuacion)
    ↓
Usuario creado ✓
Login automático
    ↓
MenuActivity
```

### **2. Login de Usuario**
```
Usuario introduce credenciales
    ↓
LoginActivity captura
    ↓
UsuarioDAO.usuarioExiste(username)?
    ├─ NO → Error: "Usuario no registrado"
    └─ SÍ → UsuarioDAO.validarLogin(username, password)?
        ├─ NO → Error: "Contraseña incorrecta"
        └─ SÍ → Login exitoso ✓
            ↓
            MenuActivity
```

### **3. Guardado de Puntuación**
```
Actividad de Juego
    ↓
Usuario juega y gana
    ↓
Se calcula puntos = f(intentos, tiempo, etc)
    ↓
Obtener username actual de SharedPreferences
    ↓
Crear: Puntuacion(username, puntos, nombreJuego)
    ↓
PuntuacionDAO.registrarPuntuacion(Puntuacion)
    ↓
DatabaseHelper inserta en tabla puntuaciones
    ↓
BD: INSERT INTO puntuaciones (username, puntos, juego, fecha)
    ↓
UsuarioDAO.actualizarPuntuacion(username, nuevoTotal)
    ↓
BD: UPDATE usuarios SET puntuacion = ? WHERE username = ?
    ↓
Puntuación guardada ✓
```

### **4. Visualización de Puntuaciones**
```
ScoresActivity abre
    ↓
Obtener username de SharedPreferences
    ↓
UsuarioDAO.obtenerUsuario(username)
    ├─ Datos del usuario (username, email, puntuacion)
    └─ BD: SELECT * FROM usuarios WHERE username = ?
    ↓
PuntuacionDAO.obtenerPuntuacionesUsuario(username)
    ├─ Historial completo de puntuaciones
    └─ BD: SELECT * FROM puntuaciones WHERE username = ?
    ↓
Procesar y mostrar estadísticas
    ├─ Puntuación total
    ├─ Puntos por juego
    └─ Número de partidas por juego
    ↓
Pantalla de puntuaciones renderizada ✓
```

---

## Modelo de Datos Relacional

```
┌─────────────────────────────────┐
│         TABLA: usuarios         │
├─────────────────────────────────┤
│ id (PK)          INTEGER        │
│ username (UQ)    TEXT           │
│ email            TEXT           │
│ password         TEXT           │
│ puntuacion       INTEGER (0)    │
└─────────────────────────────────┘
         ▲
         │ 1:N
         │
┌─────────────────────────────────┐
│      TABLA: puntuaciones        │
├─────────────────────────────────┤
│ id (PK)          INTEGER        │
│ username (FK)    TEXT ──────────┤
│ puntos           INTEGER        │
│ juego            TEXT           │
│ fecha            LONG           │
└─────────────────────────────────┘
```

**Relación**: Un usuario (1) puede tener muchas puntuaciones (N)

---

## Flujo de Datos en LoginActivity

```
┌─────────────────────────────────────────────────────────┐
│              LoginActivity.onCreate()                   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  1. Crear UsuarioDAO                                   │
│     DAO = new UsuarioDAO(this)                         │
│     DAO.open() ← Conexión a BD                         │
│                                                         │
│  2. Obtener referencias de UI                          │
│     edtEmail = findViewById(R.id.edtEmail)             │
│     edtUsername = findViewById(R.id.edtUsername)       │
│     edtPassword = findViewById(R.id.edtPassword)       │
│                                                         │
│  3. Click en "Iniciar Sesión"                          │
│     └─→ btnAction.setOnClickListener                   │
│         ├─ isLoginMode = true                          │
│         ├─ DAO.usuarioExiste(username)?                │
│         ├─ DAO.validarLogin(username, password)?       │
│         └─ Intent → MenuActivity                       │
│                                                         │
│  4. Click en "Registrar"                               │
│     └─→ btnShowRegister.setOnClickListener             │
│         └─ edtEmail.setVisibility(VISIBLE)             │
│                                                         │
│  5. Click en "Registrar" (después de llenar datos)     │
│     └─→ btnAction.setOnClickListener                   │
│         ├─ isLoginMode = false                         │
│         ├─ Validar email, usuario, contraseña          │
│         ├─ DAO.registrarUsuario(Usuario)               │
│         └─ Intent → MenuActivity                       │
│                                                         │
│  6. onDestroy()                                        │
│     └─ DAO.close() ← Cerrar conexión a BD              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Flujo de Datos en Actividades de Juego

```
┌──────────────────────────────────────────────┐
│  Actividad de Juego (Ej: GuessNumber)       │
├──────────────────────────────────────────────┤
│                                              │
│  onCreate()                                  │
│  ├─ Crear UsuarioDAO                        │
│  ├─ Crear PuntuacionDAO                     │
│  ├─ Abrir conexiones (open())                │
│  └─ Inicializar elementos de UI              │
│                                              │
│  checkGuess()                                │
│  ├─ Validar entrada de usuario               │
│  ├─ Comparar con número secreto              │
│  └─ Si es correcto:                          │
│      ├─ Calcular puntos (100 - intentos*5)   │
│      ├─ Obtener username de SharedPrefs      │
│      ├─ Crear Puntuacion(user, pts, juego)   │
│      ├─ PuntuacionDAO.registrarPuntuacion()  │
│      ├─ UsuarioDAO.actualizarPuntuacion()    │
│      └─ Toast: "¡+X puntos!"                 │
│                                              │
│  onDestroy()                                 │
│  ├─ usuarioDAO.close()                      │
│  └─ puntuacionDAO.close()                    │
│                                              │
└──────────────────────────────────────────────┘
```

---

## Flujo de Datos en ScoresActivity

```
┌────────────────────────────────────────────────┐
│        ScoresActivity.loadStats()              │
├────────────────────────────────────────────────┤
│                                                │
│  1. Obtener username de SharedPreferences      │
│     username = prefs.getString(KEY_LOGGED_IN)  │
│                                                │
│  2. Obtener datos del usuario                  │
│     usuario = usuarioDAO.obtenerUsuario()      │
│     ├─ Nombre de usuario                       │
│     ├─ Email                                   │
│     └─ Puntuación total                        │
│                                                │
│  3. Iterar sobre juegos                        │
│     Para cada juego:                           │
│     ├─ Obtener puntuaciones                    │
│     │  cursor = puntuacionDAO                  │
│     │          .obtenerPuntuacionesUsuario()   │
│     │                                          │
│     ├─ Filtrar por nombre de juego             │
│     ├─ Contar partidas                         │
│     └─ Sumar puntos totales                    │
│                                                │
│  4. Construir texto StringBuilder              │
│     ├─ Datos del usuario                       │
│     ├─ Tabla de puntuaciones por juego         │
│     └─ Mostrar en TextView                     │
│                                                │
└────────────────────────────────────────────────┘
```

---

## Diagrama de Clases

```
┌──────────────────┐
│   DatabaseHelper │
├──────────────────┤
│ - DATABASE_NAME  │
│ - DATABASE_VER   │
│ - TABLE_USUARIOS │
│ - TABLE_PUNTAJES │
├──────────────────┤
│ + onCreate()     │
│ + onUpgrade()    │
└──────────────────┘
         ▲
         │ usa
         │
    ┌────┴─────────────────────┐
    │                           │
┌───────────────┐       ┌──────────────────┐
│  UsuarioDAO   │       │ PuntuacionDAO    │
├───────────────┤       ├──────────────────┤
│ - db          │       │ - db             │
│ - dbHelper    │       │ - dbHelper       │
├───────────────┤       ├──────────────────┤
│ + open()      │       │ + open()         │
│ + close()     │       │ + close()        │
│ + registrar() │       │ + registrar()    │
│ + existe()    │       │ + obtener()      │
│ + validar()   │       │ + eliminar()     │
│ + obtener()   │       │ + mejores()      │
│ + actualizar()│       │ + totalPuntos()  │
│ + obtenerTodo │       └──────────────────┘
└───────────────┘
     ▲               ▲
     │ usa           │ usa
     │               │
┌────────────┐  ┌────────────────┐
│   Usuario  │  │   Puntuacion   │
├────────────┤  ├────────────────┤
│ - id       │  │ - id           │
│ - username │  │ - username     │
│ - email    │  │ - puntos       │
│ - password │  │ - juego        │
│ - puntaje  │  │ - fecha        │
├────────────┤  ├────────────────┤
│ + getters  │  │ + getters      │
│ + setters  │  │ + setters      │
└────────────┘  └────────────────┘
```

---

## Secuencia de Operaciones

### Secuencia: Registro de Usuario

```
Usuario               LoginActivity      UsuarioDAO      DatabaseHelper      BD
  │                       │                  │                 │               │
  │─ Introduce datos ─────>│                  │                 │               │
  │                        │─ Valida ────────>│                 │               │
  │                        │                  │─ open() ───────>│─────────────>│
  │                        │                  │<── BD conectada ─│<──────────────│
  │                        │<── Validado ─────│                 │               │
  │                        │                  │                 │               │
  │                        │─ registrar() ───>│                 │               │
  │                        │                  │─ insert() ────>│──────────────>│ INSERT
  │                        │                  │                 │  usuarios     │
  │                        │                  │<── id usuario ──│<──────────────│
  │                        │<── creado ───────│                 │               │
  │                        │                  │─ close() ──────>│──────────────>│ commit
  │                        │                  │<──────────────────────────────>│
  │                        │                                     │               │
  │<── Login automático ───│                                     │               │
  │                        │─ Intent ─────────────────────────────────────────>│
  │                        │                                     │               │
  └────────────────────────────────────────────────────────────────────────────┘
```

### Secuencia: Guardado de Puntuación

```
Jugador            GameActivity       PuntuacionDAO      UsuarioDAO      BD
  │                    │                  │                  │           │
  │─ Juega y gana ────>│                  │                  │           │
  │                    │─ Calcular puntos │                  │           │
  │                    │                  │                  │           │
  │                    │─ registrar() ───>│                  │           │
  │                    │                  │─ open() ────────────────────>│
  │                    │                  │<───── conectado ────────────>│
  │                    │                  │─ insert Puntuacion ────────>│
  │                    │                  │                  │           │
  │                    │<── registrado ───│                  │           │
  │                    │                  │─ actualizar() ──>│           │
  │                    │                  │                  │─ update ─>│
  │                    │                  │<──────────────────────────>│
  │                    │                  │─ close() ──────────────────>│
  │<── Toast +puntos ──│                  │                  │           │
  │                    │                  │                  │           │
  └────────────────────────────────────────────────────────────────────┘
```

---

**Versión**: 1.0  
**Tipo**: Documentación Técnica - Arquitectura  
**Estado**: ✅ Completo

