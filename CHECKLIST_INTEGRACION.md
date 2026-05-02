# 🎯 CHECKLIST DE INTEGRACIÓN - BASE DE DATOS

## ✅ Estado Actual de Integración

### Actividades Completadas:
- ✅ **LoginActivity** - Registro y login con BD
- ✅ **ScoresActivity** - Muestra estadísticas desde BD
- ✅ **Usuario.java** - Modelo actualizado
- ✅ **Puntuacion.java** - Modelo actualizado
- ✅ **DatabaseHelper.java** - Gestor de BD creado
- ✅ **UsuarioDAO.java** - DAO de usuarios creado
- ✅ **PuntuacionDAO.java** - DAO de puntuaciones creado

### Actividades Pendientes de Integración:
- ⏳ **GuessNumberActivity** - Pendiente
- ⏳ **RpsActivity** - Pendiente
- ⏳ **TicTacToeActivity** - Pendiente
- ⏳ **MemoryGameActivity** - Pendiente

---

## 📋 Plantilla para Integrar en Actividades de Juego

Copia este código en cada actividad de juego:

### **PASO 1: Agregar Imports**
```java
import android.content.Context;
import android.content.SharedPreferences;
```

### **PASO 2: Agregar Variables Miembro**
```java
private UsuarioDAO usuarioDAO;
private PuntuacionDAO puntuacionDAO;
private SharedPreferences prefs;
private static final String PREFS = "ludomix_prefs";
private static final String KEY_LOGGED_IN = "logged_in_user";
```

### **PASO 3: Inicializar en onCreate()**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_[nombre]);
    
    // Inicializar SharedPreferences
    prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    
    // Inicializar DAO de usuarios
    usuarioDAO = new UsuarioDAO(this);
    usuarioDAO.open();
    
    // Inicializar DAO de puntuaciones
    puntuacionDAO = new PuntuacionDAO(this);
    puntuacionDAO.open();
    
    // ... resto del código ...
}
```

### **PASO 4: Función para Guardar Puntuación**
```java
private void guardarPuntuacion(String nombreJuego, int puntos) {
    String username = prefs.getString(KEY_LOGGED_IN, null);
    
    if (username != null) {
        // Crear y registrar puntuación
        Puntuacion puntuacion = new Puntuacion(username, puntos, nombreJuego);
        puntuacionDAO.registrarPuntuacion(puntuacion);
        
        // Actualizar puntuación total del usuario
        Usuario usuario = usuarioDAO.obtenerUsuario(username);
        int nuevaPuntuacion = usuario.getPuntuacion() + puntos;
        usuarioDAO.actualizarPuntuacion(username, nuevaPuntuacion);
        
        Toast.makeText(this, "¡Puntuación guardada! +" + puntos + " pts", 
                       Toast.LENGTH_SHORT).show();
    }
}
```

### **PASO 5: Llamar cuando el Jugador Gana**
```java
// En el método donde termina el juego y el jugador gana:
int puntosObtenidos = calcularPuntos(); // Tu lógica
guardarPuntuacion("Nombre del Juego", puntosObtenidos);
```

### **PASO 6: Cerrar en onDestroy()**
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    
    if (usuarioDAO != null) {
        usuarioDAO.close();
    }
    if (puntuacionDAO != null) {
        puntuacionDAO.close();
    }
}
```

---

## 🎮 GuessNumberActivity

### Cambios Necesarios:

**Ubicación del cambio**: En el método `checkGuess()`, cuando `guess == secretNumber`

**Código actual:**
```java
if (guess == secretNumber) {
    String msg = getString(R.string.correct_guess_in_turns, attempts);
    txtResult.setText(msg);
    txtGuess.setEnabled(false);
    btnCheck.setEnabled(false);

    int plays = prefs.getInt("plays_guess", 0);
    int wins = prefs.getInt("wins_guess", 0);
    prefs.edit().putInt("plays_guess", plays + 1).putInt("wins_guess", wins + 1).apply();
}
```

**Código actualizado:**
```java
if (guess == secretNumber) {
    String msg = getString(R.string.correct_guess_in_turns, attempts);
    txtResult.setText(msg);
    txtGuess.setEnabled(false);
    btnCheck.setEnabled(false);

    // AGREGAR ESTO:
    int puntos = Math.max(100 - attempts * 5, 10); // Menos intentos = más puntos
    guardarPuntuacion("Adivina Número", puntos);

    // Mantener compatibilidad (opcional)
    int plays = prefs.getInt("plays_guess", 0);
    int wins = prefs.getInt("wins_guess", 0);
    prefs.edit().putInt("plays_guess", plays + 1).putInt("wins_guess", wins + 1).apply();
}
```

**Nombres de juego a usar**: `"Adivina Número"`

---

## 🪨 RpsActivity (Rock-Paper-Scissors)

### Cambios Necesarios:

**Ubicación del cambio**: Después de determinar el ganador

**Código a agregar:**
```java
private void playGame(String playerChoice) {
    String computerChoice = getComputerChoice();
    String result = determineWinner(playerChoice, computerChoice);
    
    // Mostrar resultado
    displayResult(result, playerChoice, computerChoice);
    
    // AGREGAR ESTO:
    if (result.equals("win")) {
        int puntos = 25; // O ajusta según dificultad
        guardarPuntuacion("Piedra Papel Tijera", puntos);
    }
}
```

**Nombres de juego a usar**: `"Piedra Papel Tijera"`

---

## ❌ TicTacToeActivity

### Cambios Necesarios:

**Ubicación del cambio**: En el método donde se detecta ganador

**Código a agregar:**
```java
private void checkGameEnd() {
    if (isGameOver()) {
        if (hasCurrentPlayerWon()) {
            int puntos = 50; // Victoria en TicTacToe
            guardarPuntuacion("Tres en Raya", puntos);
        }
    }
}
```

**Nombres de juego a usar**: `"Tres en Raya"`

---

## 🧠 MemoryGameActivity

### Cambios Necesarios:

**Ubicación del cambio**: En el método donde el jugador completa el juego

**Código a agregar:**
```java
private void onGameWon() {
    // Calcular puntos basado en tiempo o movimientos
    int puntos = calcularPuntosMemoria();
    guardarPuntuacion("Memoria", puntos);
}

private int calcularPuntosMemoria() {
    // Ejemplo: basado en tiempo transcurrido
    long timeElapsed = System.currentTimeMillis() - gameStartTime;
    int segundos = (int) (timeElapsed / 1000);
    
    // Menos tiempo = más puntos (máximo 100)
    return Math.max(100 - segundos, 10);
}
```

**Nombres de juego a usar**: `"Memoria"`

---

## 📊 Tabla de Cálculo de Puntos (Recomendada)

| Juego | Puntos | Fórmula |
|-------|--------|---------|
| Adivina Número | 10-100 | `100 - (intentos * 5)` |
| Piedra Papel Tijera | 25 | Fijo por victoria |
| Tres en Raya | 50 | Fijo por victoria |
| Memoria | 10-100 | `100 - (segundos)` |

---

## 🧪 Pruebas Recomendadas

Después de integrar cada actividad:

1. **Registrar un usuario**
   - [ ] Email válido
   - [ ] Username único
   - [ ] Contraseña guardada

2. **Jugar y ganar**
   - [ ] Jugar partida en la actividad
   - [ ] Ganar la partida
   - [ ] Ver mensaje "Puntuación guardada"

3. **Verificar en ScoresActivity**
   - [ ] Aparece la nueva puntuación
   - [ ] El juego aparece listado
   - [ ] Los puntos se suman correctamente

4. **Jugar múltiples veces**
   - [ ] Jugar 3 partidas
   - [ ] Verificar que todas aparecen en ScoresActivity
   - [ ] Verificar suma total

---

## 🔧 Pasos para Integración (Rápido)

Para **CADA** actividad:

1. Copia estas líneas al inicio de la clase:
```java
private UsuarioDAO usuarioDAO;
private PuntuacionDAO puntuacionDAO;
private SharedPreferences prefs;
private static final String PREFS = "ludomix_prefs";
private static final String KEY_LOGGED_IN = "logged_in_user";
```

2. En `onCreate()`, agrega (después de `setContentView`):
```java
prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
usuarioDAO = new UsuarioDAO(this);
usuarioDAO.open();
puntuacionDAO = new PuntuacionDAO(this);
puntuacionDAO.open();
```

3. Agrega este método:
```java
private void guardarPuntuacion(String nombreJuego, int puntos) {
    String username = prefs.getString(KEY_LOGGED_IN, null);
    if (username != null) {
        Puntuacion p = new Puntuacion(username, puntos, nombreJuego);
        puntuacionDAO.registrarPuntuacion(p);
        Usuario u = usuarioDAO.obtenerUsuario(username);
        usuarioDAO.actualizarPuntuacion(username, u.getPuntuacion() + puntos);
        Toast.makeText(this, "¡+" + puntos + " pts!", Toast.LENGTH_SHORT).show();
    }
}
```

4. Cuando el jugador gana, llama:
```java
guardarPuntuacion("Nombre del Juego", puntos);
```

5. Agrega `onDestroy()`:
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (usuarioDAO != null) usuarioDAO.close();
    if (puntuacionDAO != null) puntuacionDAO.close();
}
```

---

## ✅ Integración Completada

Cuando hayas integrado todas las actividades, verifica:

- [ ] GuessNumberActivity - ✅ Integrado
- [ ] RpsActivity - ✅ Integrado  
- [ ] TicTacToeActivity - ✅ Integrado
- [ ] MemoryGameActivity - ✅ Integrado
- [ ] Todas las puntuaciones aparecen en ScoresActivity
- [ ] Los usuarios pueden ver su progreso

---

## 🚀 Siguiente Paso

Una vez integrado en todas las actividades, considera:

1. **Estadísticas avanzadas** en ScoresActivity
2. **Ranking global** entre usuarios
3. **Sincronización con AWS** para backups
4. **Notificaciones** de nuevos records

---

**Última actualización**: 2025  
**Responsable**: Sistema de Base de Datos SQLite

