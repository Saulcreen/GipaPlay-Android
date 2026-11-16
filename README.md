# GipaPlay Android — gestos originales conectados al dispositivo

## Entrega
- `app/`: proyecto Kotlin para Android Studio.
- `web/index.html`: copia del HTML adjunto con los dos manejadores originales conectados al puente Android.

## Qué se modificó en la web
No se añadieron gestos nuevos ni se rediseñó el reproductor. Se conservaron los controles, zonas táctiles, cálculo, HUD y almacenamiento originales. En `ponerBrillo(n)` se añadió `AndroidControls.setBrightness(n * 100)`; en `ponerVolumen(n)` se añadió `AndroidControls.setVolume(n * 100)`.

## Publicar
Copia el contenido de `web/index.html` como `index.html` en el proyecto web original y despliega en Vercel. La APK carga la web publicada en vivo. Sin este despliegue, la web remota todavía no enviará órdenes al puente Android.

## Compilar APK
Abrir la carpeta raíz en Android Studio. Instalar Android SDK Platform 35 y JDK 17 si se solicitan. Luego Build > Build Bundle(s) / APK(s) > Build APK(s). Salida habitual: `app/build/outputs/apk/debug/GipaPlay-debug.apk`.

Brillo: brillo de la ventana de GipaPlay, no el ajuste global del sistema. Volumen: stream multimedia Android. Orientación: automática.

## APK con GitHub
Sube el contenido de esta carpeta (no la carpeta en sí) a un repositorio. En la pestaña Actions se ejecuta "Build APK" (o pulsa Run workflow). Al terminar, descarga `GipaPlay-APK` desde Artifacts: dentro está `GipaPlay-debug.apk`.
