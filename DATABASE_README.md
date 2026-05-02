# Base de Datos SQLite - LudoMix

## Descripción

Se ha implementado un sistema de base de datos SQLite para almacenar y gestionar:
- **Usuarios**: nombres de usuario, correos electrónicos, contraseñas y puntuación total
- **Puntuaciones**: registros detallados de puntuaciones por juego y fecha

## Clases Principales

### 1. DatabaseHelper
- **Archivo**: `DatabaseHelper.java`
- **Propósito**: Gestor de la base de datos SQLite
- **Tablas creadas**:
  - `usuarios`: almacena información de usuarios
  - `puntuaciones`: registra puntuaciones de cada juego

### 2. UsuarioDAO
- **Archivo**: `UsuarioDAO.java`
- **Métodos principales**:
  - `registrarUsuario(Usuario)`: Registra un nuevo usuario
  - `usuarioExiste(username)`: Verifica si un usuario está registrado
  - `validarLogin(username, password)`: Valida credenciales
  - `obtenerUsuario(username)`: Obtiene datos del usuario
  - `actualizarPuntuacion(username, puntos)`: Actualiza la puntuación total

### 3. PuntuacionDAO
- **Archivo**: `PuntuacionDAO.java`
- **Métodos principales**:
  - `registrarPuntuacion(Puntuacion)`: Registra una nueva puntuación
  - `obtenerPuntuacionesUsuario(username)`: Obtiene todas las puntuaciones de un usuario
  - `obtenerMejoresPuntuacionesPorJuego(juego)`: Obtiene el top 10 de un juego
  - `obtenerPuntosTotal(username)`: Suma total de puntos de un usuario
  - `eliminarPuntuacionesUsuario(username)`: Elimina todas las puntuaciones de un usuario

## Modelos de Datos

### Usuario
```java
- id (int): ID único
- username (String): Nombre de usuario único
- email (String): Correo electrónico
- password (String): Contraseña
- puntuacion (int): Puntuación total acumulada
```

### Puntuacion
```java
- id (int): ID único
- username (String): Nombre de usuario (relación con tabla usuarios)
- puntos (int): Puntos obtenidos
- juego (String): Nombre del juego
- fecha (long): Timestamp de cuándo se registró
```

## Lógica de Autenticación

### Login
1. El usuario intenta iniciar sesión con usuario y contraseña
2. Se verifica si el usuario existe en la base de datos
3. Si no existe: Se muestra mensaje "Usuario no registrado. Por favor regístrate primero"
4. Si existe: Se valida la contraseña
5. Si la contraseña es correcta: Se inicia sesión y abre MenuActivity
6. Si es incorrecta: Se muestra "Contraseña incorrecta"

### Registro
1. El usuario debe proporcionar: correo electrónico, nombre de usuario y contraseña
2. Se valida el formato del email
3. Se valida que no exista un usuario con el mismo nombre
4. Se crea el usuario en la base de datos
5. Se inicia sesión automáticamente

## Uso en Actividades

### En LoginActivity
```java
// Inicializar
usuarioDAO = new UsuarioDAO(this);
usuarioDAO.open();

// Registrar usuario
Usuario nuevoUsuario = new Usuario(username, email, password);
if (usuarioDAO.registrarUsuario(nuevoUsuario)) {
    // Registro exitoso
}

// Validar login
if (usuarioDAO.validarLogin(username, password)) {
    // Login exitoso
}

// Cerrar en onDestroy
usuarioDAO.close();
```

### En otras Actividades (para guardar puntuaciones)
```java
// Inicializar
puntuacionDAO = new PuntuacionDAO(this);
puntuacionDAO.open();

// Registrar una puntuación
Puntuacion puntuacion = new Puntuacion(username, puntos, "Piedra Papel Tijera");
puntuacionDAO.registrarPuntuacion(puntuacion);

// Actualizar puntuación total del usuario
usuarioDAO.actualizarPuntuacion(username, puntuacionesNuevas);

// Cerrar en onDestroy
puntuacionDAO.close();
```

## Notas Importantes

- La base de datos se crea automáticamente en la primera ejecución
- Los usernames son únicos (constraint UNIQUE en la BD)
- Las puntuaciones se guardan con timestamp automático
- La BD está almacenada localmente en el dispositivo/emulador
- Se recomienda cerrar los DAOs en onDestroy() de cada Activity

## Cambios a futuro (Integración con AWS)

La información actual del endpoint de AWS (ludomixdb.c4dskxh4pkdi.us-east-1.rds.amazonaws.com) puede ser utilizada para:
1. Sincronizar las puntuaciones a la nube
2. Crear un ranking global entre usuarios
3. Hacer backups de datos
4. Consultar estadísticas globales del juego

