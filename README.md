# 📱 Tejisur-App

¡Bienvenidos al repositorio oficial de **Tejisur-App**! Este es un proyecto de Android desarrollado en Kotlin utilizando las últimas guías de diseño y arquitectura limpia.

---

## 👥 Equipo de Desarrollo
* **Líder de Proyecto / Integrador:** Wilmer
* **Desarrolladores:** 
  * Rider Zevallos
  * Jhon Campos
  * Fernando Benites

---

## 📂 Estructura del Proyecto

Para mantener el orden, el proyecto está estructurado de la siguiente manera:
* **`app/`**: Contiene todo el código fuente de la aplicación móvil.
  * **`src/main/java/`**: Clases de Kotlin, actividades, vistas y lógica de negocio.
  * **`src/main/res/`**: Recursos visuales (layouts XML, imágenes, colores, strings y temas).
* **`gradle/` & `build.gradle.kts`**: Archivos de configuración de dependencias y empaquetado del proyecto.
* **`.gitignore`**: Archivo que evita que se suban archivos basura o configuraciones locales pesadas de Android Studio.

---

## 🚀 Guía de Trabajo con Git (Paso a Paso)

Para evitar conflictos y trabajar organizados, **está estrictamente prohibido subir cambios directamente a la rama `main`**. Sigan este flujo de trabajo:

### 1. Clonar el repositorio por primera vez
Si aún no tienes el proyecto en tu computadora, abre tu terminal en una carpeta limpia y ejecuta:
```bash
git clone https://github.com
```
*Luego, abre la carpeta clonada desde Android Studio mediante **File > Open**.*

### 2. Crear tu propia rama de trabajo
Antes de empezar a programar cualquier pantalla o función, asegúrate de estar en `main`, actualiza tu entorno y crea una rama con tu nombre o la tarea asignada:
```bash
git checkout main
git pull origin main
git checkout -b feature/nombre-tu-tarea
```
*Ejemplo: `git checkout -b feature/login-jhon`*

### 3. Guardar tus avances locales
Mientras vayas programando en Android Studio, ve guardando tus cambios en tu terminal local:
```bash
git add .
git commit -m "Descripción corta de lo que programaste (ej: Agregado diseño de botones)"
```

### 4. Subir tus cambios a GitHub
Cuando termines tu tarea y esté lista para revisión, sube tu rama a la nube:
```bash
git push origin feature/nombre-tu-tarea
```

### 5. Solicitar la unión del código (Pull Request)
1. Entra al enlace del repositorio en la web: https://github.com
2. Verás un cartel amarillo que dice **"Compare & pull request"**. Dale clic.
3. Escribe un resumen de lo que hiciste y envía la solicitud.
4. **Wilmer** revisará que todo compile correctamente y le dará el visto bueno (**Merge**) para unirlo a la app oficial.
