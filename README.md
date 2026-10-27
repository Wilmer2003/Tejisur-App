# 📱 Tejisur-App

¡Bienvenidos al repositorio oficial de **Tejisur-App**! Este es un proyecto de Android desarrollado en Kotlin utilizando las últimas guías de diseño y arquitectura limpia.

---

## 👥 Equipo de Desarrollo

* **Desarrolladores:** 
  * Rider Zevallos
  * Jhon Campos
  * Fernando Benites
  * Wilmer Silva

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

## 📂 Estructura Detallada del Proyecto (¡LEER ANTES DE PROGRAMAR!)

Para trabajar de manera organizada y evitar código espagueti, utilizaremos una arquitectura basada en capas (`ui`, `domain`, `data`). Todo el código de Kotlin debe organizarse estrictamente dentro del paquete principal (`com.app_moviles.tejisurapp`) siguiendo este mapa:

### 🧠 1. El Cerebro de la App (Carpeta `java` o `kotlin+java`)

*   📁 **`data/` (Capa de Datos y Base de Datos)**
  *   Aquí va todo lo relacionado con la persistencia y almacenamiento de datos.
  *   📁 `local/`: Configuración de la base de datos local (Room o SQLite), las entidades de las tablas y los DAOs.
  *   📁 `network/`: Conexiones a APIs externas de internet (clases de Retrofit, interfaces de servicios).
  *   📁 `repository/`: Las implementaciones que deciden si los datos se cargan de internet o de la base de datos local.
*   📁 **`domain/` (Capa de Dominio / Reglas de Negocio)**
  *   Es la capa más limpia; no sabe nada de bases de datos ni de interfaces visuales.
  *   📁 `model/`: Clases de datos puras (ej: `Producto.kt`, `Usuario.kt`, `Cliente.kt`).
  *   📁 `usecase/`: Las acciones específicas de la app (ej: `CalcularTotalVenta.kt`, `ValidarLogin.kt`).
*   📁 **`ui/` (Capa de Interfaz de Usuario / Presentación)**
  *   Aquí va únicamente lo que el usuario ve y controla. Nos organizaremos **por pantallas**:
  *   📁 `login/`: `LoginActivity.kt` (Código de la pantalla) y `LoginViewModel.kt` (Controla el estado y la lógica de esa pantalla).
  *   📁 `home/`: `MainActivity.kt` (Pantalla principal o menú de navegación).
  *   📁 `productos/`: Pantallas para el catálogo de tejidos, registro de mercancía, etc.

---

### 🎨 2. El Diseño y Recursos Visuales (Carpeta `res/`)

*   📁 **`layout/` (Lienzos XML)**
  *   Aquí se diseñan las pantallas. Cada Activity debe tener su archivo aquí (ej: `activity_main.xml`, `activity_login.xml`).
  *   *Regla:* Los nombres siempre van en minúsculas y separados por guiones bajos.
*   📁 **`drawable/` (Imágenes y Vectores)**
  *   Guarda aquí los logotipos de Tejisur, iconos de botones, fondos vectoriales o cualquier recurso visual `.png`, `.jpg` o `.xml`.
*   📁 **`values/` (Configuraciones de Estilo Global)**
  *   📄 `colors.xml`: Aquí definiremos la paleta de colores oficial de la app. **Prohibido** usar colores aleatorios ("hardcoded") en los layouts; se deben llamar desde este archivo.
  *   📄 `strings.xml`: Aquí van todos los textos de la aplicación (ej: `<string name="btn_login">Iniciar Sesión</string>`). **Prohibido** escribir texto directo en los XML de diseño; todo debe estar centralizado aquí para mantener el orden.

---

### ⚙️ 3. Configuraciones Generales

*   📄 **`AndroidManifest.xml`**: Cada vez que crees una pantalla nueva (Activity), asegúrate de que esté registrada aquí. También se añaden aquí los permisos (como `<uses-permission android:name="android.permission.INTERNET" />`).
*   📄 **`build.gradle.kts (Module: app)`**: Aquí es donde agregaremos las librerías del proyecto (como Room para la base de datos o Retrofit para internet). Si necesitas agregar una dependencia, avisa primero al grupo.


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
