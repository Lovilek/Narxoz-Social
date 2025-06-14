# Narxoz Social (Mobile)

Этот документ описывает шаги по сборке и запуску мобильного приложения Narxoz Social, доступного в ветке `mobile`:  
https://github.com/Lovilek/Narxoz-Social/tree/mobile

---

## Описание проекта

Narxoz Social — это мобильное клиентское приложение внутренней социальной сети университета Narxoz. Приложение написано на Kotlin с использованием Jetpack Compose (UI), Hilt (внедрение зависимостей) и архитектуры MVVM. Основная цель — предоставить студентам, преподавателям, организациям и модераторам удобный интерфейс для:
- просмотра и публикации постов (лента, комментарии, лайки),
- работы с личными и групповыми чатами в реальном времени (WebSocket + MongoDB на бэкенде),
- подписки на события и получение уведомлений,
- авторизации через JWT.

Важно помнить, что:
1. **Регистрации в приложении нет**: все учётные записи (студент/преподаватель) создаются только администратором (логины формата `S########` или `F########`).  
2. Для корректной работы чатов и прочих функций сервис бэкенда должен быть запущен и доступен по указанным в конфигурации URL.

---

## Требования к окружению

1. **Операционная система**  
   - Windows 10/11, macOS (последние стабильные версии), Linux (Ubuntu 20.04+ или подобные).

2. **Java Development Kit (JDK) 11**  
   - Убедитесь, что в PATH доступен `java –version` (должно быть `openjdk 11.x` или `Oracle JDK 11.x`).

3. **Android Studio Arctic Fox (или новее)**  
   - Версия плагина Android Gradle Plugin: **8.1.x** (см. `libs.versions.toml`).  
   - SDK Platforms: API 35 (Android 13).  
   - Build Tools 35.x.

4. **Android SDK & эмуляторы**  
   - Android 13 (API 35).  
   - Рекомендуется настроить эмулятор с уровнем API 35 и типом образа x86_64 (или x86).  

5. **Gradle & Kotlin**  
   - В проекте используется Gradle 8.x и Kotlin 2.0.0 (см. `libs.versions.toml`).  
   - Система плагинов подключена через `libs.versions.toml` (Version Catalogs).

6. **Подключение к бэкенду**  
   - Для разработки и тестирования локально настроен адрес WebSocket в `BuildConfig.BASE_WS_URL`:  
     ```kotlin
     buildConfigField("String", "BASE_WS_URL", "\"ws://159.65.124.242:8000\"")
     ```  
   - Приложение по умолчанию обращается к серверу по адресу `http://159.65.124.242:8000`.

---

## Быстрый старт

1. **Клонируйте репозиторий и перейдите в ветку `mobile`**
   ```bash
   git clone https://github.com/Lovilek/Narxoz-Social.git
   cd Narxoz-Social
   git fetch origin
   git checkout mobile

Откройте проект в Android Studio

Запустите Android Studio → Open → выберите корневую папку Narxoz-Social → нажмите OK.

Подождите, пока Gradle синхронизируется (Sync).

Проверьте настройки SDK/NDK

В Android Studio: File → Settings → Appearance & Behavior → System Settings → Android SDK

Убедитесь, что установлен Android 13 (API 35).

В разделе SDK Tools проверьте, что стоят Android SDK Build-Tools 35.x и Android Emulator.

Настройте BASE_WS_URL (если требуется)
По умолчанию:

buildConfigField("String", "BASE_WS_URL", "\"ws://159.65.124.242:8000\"")

Для эмулятора и физического устройства используется этот же удалённый сервер.

Откройте app/build.gradle.kts → найдите:

defaultConfig {
    // ...
    buildConfigField("String", "BASE_WS_URL", "\"ws://159.65.124.242:8000\"")
}

и отредактируйте строку при необходимости. Сохраните файл.

Установите зависимости через Gradle

В Android Studio откройте Gradle Tool Window (справа) → app → Tasks → build → assembleDebug или просто нажмите кнопку Sync Now при появлении панели подсказки.

Если есть ошибки зависимостей, проверьте libs.versions.toml и подключаемые библиотеки (Moshi, Hilt, Compose, Retrofit, Coroutine, OkHttp и т.д.).

Запуск бэкенда локально (обязательно для работы клиента)

Откройте бекенд-проект (папку backend/narxoz_social) в IDE (например, PyCharm).

Настройте базу данных PostgreSQL и MongoDB, запустите миграции.

Выполните:

docker-compose up -d

(если настроена конфигурация Docker; иначе запустите Django сервер командой python manage.py runserver 0.0.0.0:8000).

Убедитесь, что WebSocket-сервер (Django Channels или аналог) запущен на ws://localhost:8000/ws/....

Проверьте, что при авторизации и попытке отправки сообщений сервер отвечает корректно.

Сборка и запуск на эмуляторе/физическом устройстве

Подключите эмулятор Android (API 35) или физическое устройство.

В Android Studio выберите конфигурацию app и нажмите Run (зелёная стрелка) → выберите устройство/эмулятор.

Gradle соберёт APK и установит его на устройство.

Первый запуск и авторизация

При первом запуске откроется экран логина: введите «логин» и «пароль», выданные администратором.

После успешной авторизации откроется главный экран с лентой постов, навигацией по чатам, событиям и профилю.

Структура проекта (ветка mobile)

Описание ключевых шагов сборки
Важно: все команды предполагают, что вы находитесь в корне репозитория Narxoz-Social/.

Шаг 1: Клонирование и переход на ветку

git clone https://github.com/Lovilek/Narxoz-Social.git
cd Narxoz-Social
git fetch origin
git checkout mobile

Шаг 2: Открытие в Android Studio
Запустите Android Studio → Open → выберите папку Narxoz-Social (она сама обнаружит модуль app).

Дождитесь, пока Android Studio сделает Gradle Sync.

Если возникнут ошибки синхронизации зависимостей, убедитесь, что:

JDK 11 корректно установлен и выбран в File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK.

В Project Structure → Modules → app → SDK Version указана Compile SDK Version 35.

В Project Structure → Project → Project SDK стоит JDK 11.

В Project Structure → Project → Gradle Version и Android Gradle Plugin Version соответствуют используемым в build.gradle.kts.

Шаг 3: Проверка и настройка BASE_WS_URL
Откройте app/build.gradle.kts → найдите секцию defaultConfig:

defaultConfig {
    applicationId = "com.narxoz.social"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    /** ↓ базовый URL для WebSocket будет доступен как BuildConfig.BASE_WS_URL  */
    buildConfigField("String", "BASE_WS_URL", "\"ws://159.65.124.242:8000\"")
    buildConfigField("String", "BASE_WS_URL", ""ws://159.65.124.242:8000"")

Если вы хотите подключиться к удалённому серверу (или к бэкенду на физическом ПК), замените на "ws://<IP_ВАШЕГО_СЕРВЕРА>:8000".

Шаг 4: Сборка зависимостей
В Android Studio:

Нажмите File → Sync Project with Gradle Files (если не произошёл автоматический синк).

В Gradle Tool Window (справа) раскройте :app → Tasks → build → assembleDebug и дважды кликните assembleDebug.

Убедитесь, что сборка прошла без ошибок.

Если в процессе возникают ошибки версий библиотек (Compose, Hilt, Moshi и т.д.), проверьте:

Файл libs.versions.toml — в нём хранятся версии:

[versions]
compose = "2024.05.00"
agp = "8.8.1"
kotlin = "2.0.0"
coreKtx = "1.10.1"
activityCompose = "1.8.0"
...

В разделах dependencies модуля app библиотеки подключаются через Version Catalog:

implementation(libs.androidx.core.ktx)
implementation(platform(libs.compose.bom))
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.material3)
implementation(libs.hilt.android)
kapt(libs.hilt.android.compiler)
implementation(libs.okhttp)
implementation(libs.retrofit)
implementation(libs.moshi)
kapt(libs.moshi.codegen)
...

Если после обновления Android Studio какой-то артефакт не находился, выполните в терминале ./gradlew clean build --refresh-dependencies.

Шаг 5: Запуск эмулятора или подключение устройства
Создайте эмулятор Android 13 (API 35), ABI x86_64, с минимумом 1 GB RAM.

Либо подключите физическое устройство по USB (утверждающее отладку по ADB).

Убедитесь, что в Run → Select Deployment Target выбран нужный девайс.

Шаг 6: Запуск приложения
В Android Studio выберите конфигурацию app (Debug) и нажмите Run.

Приложение автоматически установится и запустится.

На первом экране появится форма логина. Используйте логин/пароль, выданные админом.

Полезные примечания
Версия Kotlin & Compose

Kotlin 2.0.0 (обратите внимание, что некоторые плагины KAPT могут выдавать предупреждения о совместимости с Kotlin 2.x, но это ожидаемо).

Compose BOM 2024.05.00.

Внедрение зависимостей (Hilt)

В корне приложения (в App.kt) подключается Hilt:

@HiltAndroidApp
class App : Application()

Дополнительные модули Hilt (Retrofit, Moshi, OkHttp, WebSocket) находятся в di/.

WebSocket (чат)

Для чатов используется собственный WsManager.kt, который подключается к BuildConfig.BASE_WS_URL.

Все события приходят через WebSocket, а репозиторий обрабатывает MutableStateFlow и SharedFlow для UI.

Moshi-Kotlin конвертеры

В utils/MoshiModule.kt подключается Moshi.Builder().add(KotlinJsonAdapterFactory()).build().

Кодогенерация (@JsonClass(generateAdapter = true)) прописана в Gradle (kapt).

Структура Navigation (Compose Navigation)

В ui/navigation/Navigation.kt определены маршруты:

LoginScreen

MainFeedScreen (лента постов)

ChatListScreen → ChatScreen (личные и групповые чаты)

EventsScreen (список событий и календарь)

ProfileScreen, SettingsScreen, OrganizationsScreen.

Больше контекста о функциях

Лента (Feed): отображение списка PostDto, репозиторий PostsRepository использует Retrofit для взаимодействия с API /api/posts/.

Комментарии/Лайки: при нажатии на кнопку лайка вызывается PostsRepository.toggleLike(postId), UI реагирует на ответ.

События: EventsRepository получает список и создает локальный кэш (Room или DataStore, если понадобится).

Уведомления: пуш-уведомления реализованы через Firebase Cloud Messaging (FCM). Приложение отправляет FCM‑токен на бэкенд, где Celery‑задача сохраняет токен и использует его для отправки push‑сообщений.
Для работы плагина Google Services требуется файл `app/google-services.json`. В репозитории находится пример с фиктивными данными. Перед сборкой замените его на файл из консоли Firebase.

Что делать, если сборка падает

Часто помогает команды:

./gradlew clean
./gradlew assembleDebug --stacktrace

Проверьте, что в build.gradle.kts (android { ... }) указан compileSdk = 35, minSdk = 26, targetSdk = 35.

Обновите кеш Gradle: File → Invalidate Caches / Restart → Invalidate and Restart.

Настройка бэкенда для локальной разработки
Эта часть не является частью мобильного приложения, но необходима для корректной работы клиента.

Клонируйте бекенд (если ещё не сделано)

git clone https://github.com/Lovilek/Narxoz-Social.git
cd Narxoz-Social/backend
git fetch origin
git checkout mobile-backend       # или ветка, соответствующая текущему API

Установите зависимости Python (виртуальное окружение)

python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

Настройка баз данных

PostgreSQL: создайте базу данных narxoz_social, пользователя и пароль.

MongoDB: по умолчанию запускается на mongodb://localhost:27017.

Данные о подключении хранятся в settings.py:

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": "narxoz_social",
        "USER": "postgres",
        "PASSWORD": "пароль",
        "HOST": "localhost",
        "PORT": "5432",
    }
}

MONGODB_SETTINGS = {
    "db": "narxoz_chat",
    "host": "localhost",
    "port": 27017,
}

Миграции и статические файлы

python manage.py makemigrations
python manage.py migrate
python manage.py collectstatic --noinput

Запуск сервера (Django + Channels)

# Для обычного режима
python manage.py runserver 0.0.0.0:8000

# Если используется Daphne/ASGI (для Channels/WebSocket)
daphne narxoz_social.asgi:application --port 8000

Проверка WebSocket

После запуска сервера WebSocket должен быть доступен по адресу:

ws://localhost:8000/ws/chat/<chat_id>/

В мобильном приложении BuildConfig.BASE_WS_URL должен указывать на ws://<IP_Бэкенда>:8000/ws/chat/.

Советы по отладке
Ошибка зависимостей Compose

Проверьте, что в libs.versions.toml версии Compose совпадают с composeBom и что в build.gradle.kts подключен:

implementation(platform(libs.compose.bom))
implementation(libs.androidx.compose.ui)
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.compose.ui.toolingPreview)
debugImplementation(libs.androidx.compose.ui.tooling)

Убедитесь, что в settings.gradle.kts прописан плагин Compose:

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

WebSocket не подключается

Проверьте настройки CORS и origin на бэкенде (Django Channels).

Убедитесь, что BASE_WS_URL правильный и сервер слушает именно этот порт.

Ошибка JWT-кода

Если при логине возвращается ошибка “401 Unauthorized”, посмотрите логи Django (python manage.py runserver) и убедитесь, что:

Пользователь создан (логин формата S########),

Правильный пароль,

Модуль JWT добавлен в INSTALLED_APPS и настроены SIMPLE_JWT.

Полезные ссылки
ERD (схема БД):

Основная (PostgreSQL): users, posts, comments, post_likes, events, event_participants и т.д.

Чаты (MongoDB): direct_messages, group_messages.

User Flow & CJM (Figma):

https://www.figma.com/board/Zblz3wERhNTpRKf5wO0m3Y/narxoz-flow?node-id=0-1

Дизайн UI (Figma):

https://www.figma.com/design/jsli8TUWmm4IbhvEZkasBJ/narxozsocialproject?node-id=35-2

Ответственность ролей и ограничения
Регистрация:

Регистрация в мобильном приложении НЕ поддерживается. Все учётные записи создаёт только администратор через бэкенд.

Если при попытке регистрации вы получите ошибку, сообщите администратору, чтобы он создал учётную запись.

Роли на клиенте:

После логина приложение определяет роль пользователя (student, teacher, organization, moderator, admin) и отображает соответствующий UI.

Студент может просматривать ленту, комментировать, лайкать, участвовать в личных чатах.

Преподаватель/организация дополнительно могут создавать групповые чаты и мероприятия.

Модератор/админ имеют расширенные возможности (удаление постов/комментариев, просмотр любых чатов).

Заключение
После выполнения всех шагов выше вы получите работающее мобильное приложение Narxoz Social, готовое к тестированию и дальнейшему развитию. Если возникнут проблемы на любом этапе:

Проверьте логи Gradle и Android Studio (последовательность ошибок).

Пересмотрите настройки build.gradle.kts, особенно версии библиотек и конфигурацию Compose.

Убедитесь, что бэкенд запущен и доступен (HTTPS/WS-соединение).

При необходимости создайте issue в репозитории или обратитесь к документации.

Удачной разработки!