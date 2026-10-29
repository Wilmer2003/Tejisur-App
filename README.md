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

## 💻 Requisitos para abrir el proyecto

Antes de comenzar, asegúrate de contar con el siguiente entorno de desarrollo configurado:

1. **Android Studio**: Se requiere **Android Studio Ladybug (2024.2.1)** o **Meerkat (2024.3.1)** o superior, compatible con Android Gradle Plugin (AGP) 8.9.1.
2. **JDK requerido**: **JDK 17 o superior** para la ejecución de Gradle 8.13 y AGP 8.9.1 (el JBR que incluye Android Studio es totalmente compatible). La compatibilidad del código fuente y bytecode de la aplicación está configurada para **Java 11 (JVM Target 11)**.
3. **Emulador o dispositivo físico**: Dispositivo o emulador con **Android API 26 (Android 8.0 Oreo)** como mínimo. Se recomienda **API 35 (Android 15)** para coincidir con el `targetSdk`.
4. **Archivo `google-services.json`**: Es necesario para la integración de los servicios de Firebase (Authentication, Realtime Database, Analytics). **Este archivo SÍ está incluido en el repositorio dentro de la carpeta `app/` (`app/google-services.json`)**, por lo que el proyecto está listo para compilar inmediatamente tras clonarlo.

---

## 🛠️ Versiones utilizadas

Las siguientes versiones han sido verificadas y leídas directamente de los archivos de configuración del proyecto (`gradle/libs.versions.toml`, `app/build.gradle.kts`, `build.gradle.kts` y `gradle/wrapper/gradle-wrapper.properties`):

| Componente / Dependencia | Versión configurada |
|---|---|
| **Android Gradle Plugin (AGP)** | `8.9.1` |
| **Kotlin** | `2.4.0` |
| **Gradle** | `8.13` |
| **compileSdk** | `35` (Android 15) |
| **targetSdk** | `35` (Android 15) |
| **minSdk** | `26` (Android 8.0) |
| **JDK requerido** | JDK 17+ (Entorno Gradle / IDE) & Java 11 (`jvmTarget = JVM_11`) |
| **Firebase BoM** | `35.1.0` |
| **core-ktx** | `1.16.0` |
| **appcompat** | `1.7.0` |
| **material** | `1.12.0` |
| **lifecycle (livedata / viewmodel / runtime)** | `2.9.1` |
| **fragment (fragment-ktx)** | `1.9.1` |
| **activity** | `1.10.1` |
| **constraintlayout** | `2.2.1` |
| **recyclerview** | `1.4.0` |

---

## ▶️ Pasos para correr la app

Sigue estos sencillos pasos para compilar y ejecutar la aplicación en tu entorno local:

1. **Clonar el repositorio**:
   Abre una terminal y clona el proyecto con el siguiente comando:
   ```bash
   git clone https://github.com/Wilmer2003/Tejisur-App.git
   cd Tejisur-App
   ```

2. **Abrir en Android Studio**:
   Inicia Android Studio, ve a **File > Open** y selecciona la carpeta raíz del proyecto (`Tejisur-App`).

3. **Esperar el Gradle Sync**:
   El entorno descargará automáticamente las dependencias y sincronizará el proyecto. Aguarda hasta ver el mensaje de confirmación **"Gradle sync finished"**.

4. **Seleccionar el emulador o dispositivo**:
   En el selector de dispositivos de la barra superior, escoge un emulador o dispositivo físico conectado con API 26 o superior (recomendado API 35). Si no tienes uno creado, puedes crearlo desde **Tools > Device Manager > Create Device**.

5. **Compilar y ejecutar la app**:
   Haz clic en el botón verde **Run 'app'** (o presiona `Shift + F10`). Android Studio compilará el APK e instalará la aplicación en el dispositivo seleccionado.

6. **Iniciar sesión**:
   Una vez lanzada la pantalla de bienvenida / login, introduce las credenciales de prueba indicadas en la siguiente sección.

---

## 🔑 Acceso a la app (Login)

Para ingresar y explorar las funcionalidades de la aplicación, utiliza las siguientes credenciales de prueba registradas en **Firebase Authentication**:

* **Usuario (correo):** `operario01@gmail.com`
* **Contraseña:** `123456`

> **Nota importante:** Este usuario corresponde a una cuenta de prueba habilitada exclusivamente en Firebase Authentication para fines de desarrollo, pruebas y demostración.

---

## 📂 Estructura del Proyecto

Para mantener el orden, el proyecto está estructurado de la siguiente manera:
* **`app/`**: Contiene todo el código fuente de la aplicación móvil.
  * **`src/main/java/`**: Clases de Kotlin, actividades, vistas y lógica de negocio.
  * **`src/main/res/`**: Recursos visuales (layouts XML, imágenes, colores, strings y temas).
* **`gradle/` & `build.gradle.kts`**: Archivos de configuración de dependencias y empaquetado del proyecto.
* **`.gitignore`**: Archivo que evita que se suban archivos basura o configuraciones locales pesadas de Android Studio.

---

## 📂 Estructura Detallada del Proyecto (¡LEER ANTES DE PROGRAMAR!)

Para trabajar de manera organizada y evitar código espagueti, utilizaremos una arquitectura basada en capas (`ui`, `domain`, `data`). Todo el código de Kotlin debe organizarse estrictamente dentro del paquete principal (`com.app_moviles.tejisurapp`) siguiendo este mapa:

### 🧠 1. El Cerebro de la App (Carpeta `java` o `kotlin+java`)

* 📁 **`data/` (Capa de Datos y Base de Datos)**
  * Aquí va todo lo relacionado con la persistencia y almacenamiento de datos.
  * 📁 `local/`: Configuración de la base de datos local (Room o SQLite), las entidades de las tablas y los DAOs.
  * 📁 `network/`: Conexiones a APIs externas de internet (clases de Retrofit, interfaces de servicios).
  * 📁 `repository/`: Las implementaciones que deciden si los datos se cargan de internet o de la base de datos local.
* 📁 **`domain/` (Capa de Dominio / Reglas de Negocio)**
  * Es la capa más limpia; no sabe nada de bases de datos ni de interfaces visuales.
  * 📁 `model/`: Clases de datos puras (ej: `Producto.kt`, `Usuario.kt`, `Cliente.kt`).
  * 📁 `usecase/`: Las acciones específicas de la app (ej: `CalcularTotalVenta.kt`, `ValidarLogin.kt`).
* 📁 **`ui/` (Capa de Interfaz de Usuario / Presentación)**
  * Aquí va únicamente lo que el usuario ve y controla. Nos organizaremos **por pantallas**:
  * 📁 `login/`: `LoginActivity.kt` (Código de la pantalla) y `LoginViewModel.kt` (Controla el estado y la lógica de esa pantalla).
  * 📁 `home/`: `MainActivity.kt` (Pantalla principal o menú de navegación).
  * 📁 `productos/`: Pantallas para el catálogo de tejidos, registro de mercancía, etc.

---

### 🎨 2. El Diseño y Recursos Visuales (Carpeta `res/`)

* 📁 **`layout/` (Lienzos XML)**
  * Aquí se diseñan las pantallas. Cada Activity debe tener su archivo aquí (ej: `activity_main.xml`, `activity_login.xml`).
  * *Regla:* Los nombres siempre van en minúsculas y separados por guiones bajos.
* 📁 **`drawable/` (Imágenes y Vectores)**
  * Guarda aquí los logotipos de Tejisur, iconos de botones, fondos vectoriales o cualquier recurso visual `.png`, `.jpg` o `.xml`.
* 📁 **`values/` (Configuraciones de Estilo Global)**
  * 📄 `colors.xml`: Aquí definiremos la paleta de colores oficial de la app. **Prohibido** usar colores aleatorios ("hardcoded") en los layouts; se deben llamar desde este archivo.
  * 📄 `strings.xml`: Aquí van todos los textos de la aplicación (ej: `<string name="btn_login">Iniciar Sesión</string>`). **Prohibido** escribir texto directo en los XML de diseño; todo debe estar centralizado aquí para mantener el orden.

---

### ⚙️ 3. Configuraciones Generales

* 📄 **`AndroidManifest.xml`**: Cada vez que crees una pantalla nueva (Activity), asegúrate de que esté registrada aquí. También se añaden aquí los permisos (como `<uses-permission android:name="android.permission.INTERNET" />`).
* 📄 **`build.gradle.kts (Module: app)`**: Aquí es donde agregaremos las librerías del proyecto (como Room para la base de datos o Retrofit para internet). Si necesitas agregar una dependencia, avisa primero al grupo.

---

## 🚀 Guía de Trabajo con Git (Paso a Paso)

Para evitar conflictos y trabajar organizados, **está estrictamente prohibido subir cambios directamente a la rama `main`**. Sigan este flujo de trabajo:

### 1. Preparar tu entorno local
Si es la primera vez que trabajas en el repositorio, sigue los pasos de clonación explicados arriba. Si ya lo tienes clonado, asegúrate de situarte en `main` y traer la versión más reciente:
```bash
git checkout main
git pull origin main
```

### 2. Crear tu propia rama de trabajo
Antes de empezar a programar cualquier pantalla o función, crea una rama con tu nombre o la tarea asignada:
```bash
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
1. Entra al enlace del repositorio en la web: https://github.com/Wilmer2003/Tejisur-App
2. Verás un cartel amarillo que dice **"Compare & pull request"**. Dale clic.
3. Escribe un resumen de lo que hiciste y envía la solicitud.
4. **Wilmer** revisará que todo compile correctamente y le dará el visto bueno (**Merge**) para unirlo a la app oficial.
