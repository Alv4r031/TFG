# Resumen de Cambios - Base de Datos SQLite LudoMix

## ✅ Cambios Realizados

### 1. **Creación de Tablas en SQLite**

#### Tabla `usuarios`
```sql
CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT NOT NULL,
    password TEXT NOT NULL,
    puntuacion INTEGER DEFAULT 0
);
```

#### Tabla `puntuaciones`
```sql
CREATE TABLE puntuaciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    puntos INTEGER NOT NULL,
    juego TEXT NOT NULL,
    fecha LONG NOT NULL,
    FOREIGN KEY(username) REFERENCES usuarios(username)
);
```

---

### 2. **Archivos Nuevos Creados**

| Archivo | Descripción |
|---------|-------------|
| `DatabaseHelper.java` | Gestor de la base de datos SQLite (SQLiteOpenHelper) |
| `UsuarioDAO.java` | Data Access Object para operaciones con usuarios |
| `PuntuacionDAO.java` | Data Access Object para operaciones con puntuaciones |

---

### 3. **Archivos Modificados**

| Archivo | Cambios |
|---------|---------|
| `Usuario.java` | Añadidos campos: id, email, password, puntuacion |
| `Puntuacion.java` | Actualizado con campos: id, username, puntos, juego, fecha |
| `LoginActivity.java` | Integración de UsuarioDAO para login y registro |
| `ScoresActivity.java` | Integración de DAOs para mostrar estadísticas desde BD |

---

## 🔐 Lógica de Autenticación

### Proceso de Login:
```
1. Usuario intenta iniciar sesión
   ↓
2. Se verifica si existe en la BD
   ├→ NO EXISTE: "Usuario no registrado. Por favor regístrate primero"
   └→ SÍ EXISTE: Valida contraseña
      ├→ CONTRASEÑA INCORRECTA: "Contraseña incorrecta"
      └→ CONTRASEÑA CORRECTA: ¡Login exitoso!
```

### Proceso de Registro:
```
1. Usuario introduce: email, usuario, contraseña
   ↓
2. Validaciones:
   ├→ Email válido?
   ├→ Usuario no existe en BD?
   └→ Todos los campos llenos?
   ↓
3. Si todo es correcto:
   ├→ Crear usuario en BD
   ├→ Iniciar sesión automáticamente
   └→ Abrir MenuActivity
```

---

## 📊 Métodos Principales de UsuarioDAO

```java
// Registrar nuevo usuario
boolean registrarUsuario(Usuario usuario)

// Verificar si usuario existe
boolean usuarioExiste(String username)

// Validar login
boolean validarLogin(String username, String password)

// Obtener usuario
Usuario obtenerUsuario(String username)

// Actualizar puntuación total
boolean actualizarPuntuacion(String username, int puntos)

// Obtener todos los usuarios
Cursor obtenerTodosLosUsuarios()
```

---

## 📊 Métodos Principales de PuntuacionDAO

```java
// Registrar puntuación
boolean registrarPuntuacion(Puntuacion puntuacion)

// Obtener puntuaciones de un usuario
Cursor obtenerPuntuacionesUsuario(String username)

// Obtener top 10 de un juego
Cursor obtenerMejoresPuntuacionesPorJuego(String juego)

// Obtener total de puntos de usuario
int obtenerPuntosTotal(String username)

// Eliminar puntuaciones de un usuario
boolean eliminarPuntuacionesUsuario(String username)
```

---

## 🎮 Cómo Integrar en Actividades de Juego

### Paso 1: Añadir referencias a la clase
```java
private UsuarioDAO usuarioDAO;
private PuntuacionDAO puntuacionDAO;
```

### Paso 2: Inicializar en onCreate()
```java
usuarioDAO = new UsuarioDAO(this);
usuarioDAO.open();

puntuacionDAO = new PuntuacionDAO(this);
puntuacionDAO.open();
```

### Paso 3: Guardar puntuación cuando el jugador gana
```java
String username = prefs.getString(KEY_LOGGED_IN, null);
if (username != null) {
    Puntuacion puntuacion = new Puntuacion(username, puntos, "Nombre del Juego");
    puntuacionDAO.registrarPuntuacion(puntuacion);
    
    // Actualizar puntuación total
    Usuario usuario = usuarioDAO.obtenerUsuario(username);
    usuarioDAO.actualizarPuntuacion(username, usuario.getPuntuacion() + puntos);
}
```

### Paso 4: Cerrar en onDestroy()
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    if (usuarioDAO != null) usuarioDAO.close();
    if (puntuacionDAO != null) puntuacionDAO.close();
}
```

---

## 📱 ScoresActivity - Cambios

La pantalla de puntuaciones ahora:
- ✅ Muestra información del usuario activo (username, email)
- ✅ Muestra puntuación total acumulada
- ✅ Lista todas las puntuaciones por juego
- ✅ Cuenta el número de partidas por juego
- ✅ Suma los puntos totales por juego

---

## 🔄 Flujo de Datos

```
LoginActivity
    ↓
    ├─→ Registro
    │   ├─→ Crear Usuario en UsuarioDAO
    │   └─→ Guardar en Base de Datos
    │
    └─→ Login
        ├─→ Verificar en UsuarioDAO
        ├─→ Validar contraseña
        └─→ Abrir MenuActivity

Actividades de Juego
    ↓
    ├─→ Usuario juega
    ├─→ Usuario gana
    ├─→ PuntuacionDAO registra puntuación
    ├─→ UsuarioDAO actualiza puntuación total
    └─→ BD almacena información

ScoresActivity
    ↓
    ├─→ Obtiene username de SharedPreferences
    ├─→ UsuarioDAO obtiene datos del usuario
    ├─→ PuntuacionDAO obtiene puntuaciones
    └─→ Muestra estadísticas en pantalla
```

---

## 📁 Estructura de Carpetas

```
app/src/main/java/com/example/ludomix/
├── DatabaseHelper.java          (NUEVO)
├── UsuarioDAO.java              (NUEVO)
├── PuntuacionDAO.java           (NUEVO)
├── Usuario.java                 (MODIFICADO)
├── Puntuacion.java              (MODIFICADO)
├── LoginActivity.java           (MODIFICADO)
├── ScoresActivity.java          (MODIFICADO)
└── [Otras actividades]
```

---

## 🚀 Próximos Pasos (Opcional)

1. **Integración con AWS**: Sincronizar datos locales con RDS
2. **Ranking Global**: Comparar puntuaciones con otros usuarios
3. **Backups Automáticos**: Respaldar datos en la nube
4. **Estadísticas Avanzadas**: Gráficos y análisis de rendimiento
5. **Encriptación de Contraseñas**: Usar BCrypt en lugar de texto plano

---

## ⚠️ Notas Importantes

- La BD se crea automáticamente en la primera ejecución
- Los usernames son **ÚNICOS** (no se pueden repetir)
- Siempre cerrar los DAOs en `onDestroy()`
- La BD está almacenada **localmente** en el dispositivo
- Se recomienda hacer backups periódicamente

---

**Versión**: 1.0
**Fecha**: 2025
**Estado**: ✅ Completo

