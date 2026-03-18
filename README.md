# LudoMix

Proyecto Android "LudoMix" — colección de minijuegos (Adivina el número, Piedra-Papel-Tijera, Tres en Raya, Juego de Memoria).

Este README explica cómo inicializar el repositorio localmente y subirlo a GitHub.

## Pasos rápidos para subir a GitHub (local)
1. Inicializa git (si aún no lo has hecho):

```bash
cd "C:\Users\jeage\AndroidStudioProjects\LudoMix"
git init
git add .
git commit -m "Initial project import"
```

2. Crea un repositorio en GitHub (puedes usar la web): por ejemplo `github.com/<tu_usuario>/LudoMix`.

3. Añade el remoto y sube la rama `main` (reemplaza `<tu_usuario>` por tu usuario y `main` por `master` si prefieres):

```bash
git remote add origin git@github.com:<tu_usuario>/LudoMix.git
# o con HTTPS:
# git remote add origin https://github.com/<tu_usuario>/LudoMix.git

git branch -M main
git push -u origin main
```

4. (Opcional) Si tu proyecto contiene credenciales o archivos sensibles, muévelos fuera del repo y añádelos a `.gitignore`.

## Recomendaciones
- Antes de subir, revisa `local.properties` y otras rutas creadas por Android Studio (no subir contraseñas ni keystores).
- Considera crear un `.gitattributes` si necesitas normalizar finales de línea.

Si quieres, puedo también:
- Crear el repositorio en GitHub usando la API (necesitaré un token tuyo),
- Añadir un `.gitattributes` y etiquetas de licencia (MIT/Apache),
- Hacer el primer commit yo mismo (pero prefiero que confirmes el remote),
- O darte un script PowerShell listo para ejecutar que haga todo localmente.

¿Qué prefieres que haga a continuación?
