# ConnectCamp 🌱

App Android que conecta **productores agrícolas** con **consumidores** de frutas, verduras y huevos.

## ✨ Funcionalidades

### Productor
- Registro y login con rol de **Productor**
- Posicionarse en el mapa (Google Maps)
- Gestionar productos por temporada (frutas, verduras, huevos, legumbres…)
- Configurar métodos de pago (online, efectivo), envío y punto de recogida
- Chat en tiempo real con consumidores

### Consumidor
- Registro y login con rol de **Consumidor**
- Lista de la compra por temporada
- Ver productores en el mapa
- Ver el perfil del productor y sus productos disponibles
- Chat en tiempo real con productores

## 🛠️ Stack técnico

| Capa | Tecnología |
|------|-----------|
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Repository |
| DI | Hilt |
| Autenticación | Firebase Auth |
| Base de datos | Cloud Firestore |
| Almacenamiento | Firebase Storage |
| Mapas | Google Maps SDK + Maps Compose |
| Notificaciones | Firebase Cloud Messaging |
| Permisos | Accompanist Permissions |
| Imágenes | Coil |

## 🚀 Configuración inicial

### 1. Crear proyecto Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto
3. Habilita **Authentication** (Email/Password)
4. Crea una base de datos **Firestore** en modo producción
5. Descarga `google-services.json` y colócalo en `app/`

### 2. Google Maps API Key

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Habilita **Maps SDK for Android**
3. Crea una API Key
4. Añade al `local.properties`:
   ```
   MAPS_API_KEY=tu_api_key_aqui
   ```
5. En `app/build.gradle.kts` dentro de `defaultConfig` añade:
   ```kotlin
   manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""
   ```

### 3. Reglas de Firestore

Aplica las reglas del fichero `firestore.rules` en la consola de Firebase.

## 📁 Estructura del proyecto

```
app/src/main/java/com/connectcamp/
├── data/
│   ├── model/          # Modelos de datos (User, Product, Chat…)
│   └── repository/     # Repositorios Firebase
├── di/                 # Módulos Hilt
├── service/            # FCM Service
├── ui/
│   ├── auth/           # Pantallas de login y registro
│   ├── chat/           # Lista de chats y pantalla de chat
│   ├── consumer/       # Pantallas del consumidor
│   ├── navigation/     # NavGraph y Screen
│   ├── producer/       # Pantallas del productor
│   └── theme/          # Tema Material 3
├── viewmodel/          # ViewModels
├── ConnectCampApp.kt   # Application class (Hilt)
└── MainActivity.kt
```

## 🗂️ Colecciones Firestore

| Colección | Descripción |
|-----------|-------------|
| `users` | Datos básicos del usuario (nombre, email, rol) |
| `producers` | Perfil extendido del productor (ubicación, pagos, envío) |
| `products` | Productos del productor (temporada, precio, disponibilidad) |
| `shoppingList` | Lista de la compra del consumidor |
| `chats` | Metadatos de cada conversación |
| `messages` | Mensajes individuales de cada chat |

## 🔮 Próximas versiones

- [ ] Subida de fotos para productos y perfil
- [ ] Notificaciones push cuando hay un nuevo mensaje
- [ ] Filtro de productores por producto en el mapa
- [ ] Valoraciones de productores
- [ ] Sistema de pedidos
- [ ] Pago in-app