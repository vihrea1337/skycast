# Свой Ktor-бэкенд со Swagger UI (Этап 9, опционально)

Мини-прокси/агрегатор поверх Open-Meteo. Живёт в `server/` — отдельном JVM-модуле
Gradle-проекта SkyCast (Android-плагины его не касаются, `:app:assembleDebug` собирается
как прежде).

## Зачем

Open-Meteo отдаёт `hourly`/`daily` как параллельные массивы: `time[i]`, `temperature_2m[i]`,
... относятся к одному моменту `i` — клиенту приходится сшивать их руками (см.
`core/network/dto/ForecastResponseDto.kt`). Этот сервер делает ту же сшивку один раз на
сервере: `GET /api/v1/weather?lat=&lon=` отдаёт `hourly`/`daily` как список готовых
объектов, плюс кэширует ответ на 10 минут (по координатам, округлённым до 0.01° ≈ 1 км),
чтобы не дёргать Open-Meteo на каждый чих.

## Как устроено

- **`Application.kt`** — маршруты: `GET /health`, `GET /api/v1/weather`, `GET /swagger`
  (Swagger UI из `io.ktor:ktor-server-swagger`, читает статическую спеку
  `resources/openapi/documentation.yaml`).
- **`OpenMeteoClient.kt`** — Ktor-клиент к Open-Meteo `/v1/forecast` с теми же полями
  запроса, что и у Android-приложения (`ForecastApi.kt`), но с зафиксированными
  единицами (цельсии, км/ч) — сервер не хранит пользовательские настройки.
- **`WeatherService.kt`** — валидация координат (`lat ∈ [-90,90]`, `lon ∈ [-180,180]`,
  иначе `400` через `InvalidCoordinatesException`), поход в кэш, при промахе — в
  Open-Meteo, агрегация, запись в кэш.
- **`WeatherCache.kt`** — `ConcurrentHashMap` в памяти процесса с TTL 10 минут. Без
  Redis/БД: сервис учебный, переживать перезапуск ему не обязательно.
- **Сырой DTO Open-Meteo больше не дублируется.** `ForecastResponseDto`/`CurrentDto`/
  `HourlyDto`/`DailyDto` вынесены в новый модуль `core:openmeteo` — чистый Kotlin/JVM
  (`kotlin.jvm` + `kotlin.serialization`, без единой Android-зависимости). И Android-клиент
  (`core:network`), и этот сервер подключают его напрямую (`implementation(project(":core:openmeteo"))`).
  Раньше файл был продублирован дословно в обоих местах — до первого расхождения
  полей, которое никто бы не заметил.

## Почему `AggregatedWeatherResponse` — НЕ переиспользует `core:model`

На первый взгляд `core.model.CurrentWeather`/`HourlyForecast`/`DailyForecast`/`WeatherData`
(тоже чистый JVM-модуль!) выглядят как тот же дубль. Это не так — это два разных понятия:

| | `core:model` (Android-приложение) | этот сервер (`AggregatedWeatherResponse`) |
|---|---|---|
| Назначение | внутренняя доменная модель приложения | публичный HTTP-контракт (JSON API) |
| Погода | `weatherType: WeatherType` (enum, для выбора иконки) | `weatherCode: Int` (сырой WMO-код) |
| Время | `java.time.LocalDateTime`/`Instant` | ISO-строка (`String`) |
| Доп. поля | `temperatureUnit`, `windSpeedUnit`, `updatedAt` — забота офлайн-синка и настроек пользователя | `cached: Boolean` — забота HTTP-кэша |

Смешать их означало бы протащить решения Android-приложения (enum `WeatherType`,
`java.time` без кастомных сериализаторов, единицы измерения из настроек пользователя)
в контракт, которым может воспользоваться произвольный клиент через Swagger — то есть
искусственно связать два независимых деплоя ради формального «не повторяться». Здесь
дублирование по форме — не то же самое, что дублирование по смыслу; `core:openmeteo`
дедуплицировали именно потому, что там смысл был буквально один и тот же (контракт
внешнего API), а не потому что дедупликация — самоцель.

## Формат ответа

```json
{
  "latitude": 61.785,
  "longitude": 34.347,
  "current": { "time": "...", "temperatureC": 17.3, "feelsLikeC": 17.4, "humidityPercent": 78,
                "weatherCode": 3, "windSpeedKmh": 6.8, "pressureHpa": 993.7, "isDay": true },
  "hourly": [ { "time": "...", "temperatureC": 13.8, "weatherCode": 3,
                 "precipitationProbabilityPercent": 0, "isDay": false }, "... (168 часов)" ],
  "daily": [ { "date": "...", "weatherCode": 3, "temperatureMaxC": 22.0, "temperatureMinC": 14.0,
                "sunrise": "...", "sunset": "..." }, "... (7 дней)" ],
  "generatedAt": "2026-09-02T12:45:00Z",
  "cached": false
}
```

`cached: true` — второй запрос с теми же координатами в пределах 10 минут не ходил в
Open-Meteo повторно.

## Как запустить локально

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
.\gradlew.bat ":server:test"          # 5 юнит-тестов, HTTP замокан (MockEngine) — сети не требуют
.\gradlew.bat ":server:buildFatJar"   # -> server/build/libs/server-all.jar
```

Через Docker (порт **8090** — выбран, чтобы не пересекаться с moimatch на 8080/5432,
devlog-pg на 5433, expenses-pg на 5434 на этой же машине):

```powershell
cd server
docker compose up -d --build
```

Проверка:
```bash
curl http://localhost:8090/health                              # {"status":"ok"}
curl "http://localhost:8090/api/v1/weather?lat=61.785&lon=34.347"
curl "http://localhost:8090/api/v1/weather?lat=999&lon=34.347"  # 400, координаты вне диапазона
```

Swagger UI: открыть `http://localhost:8090/swagger` в браузере — там же можно
«Try it out» прямо из интерфейса, без curl.

## Продакшен-гигиена (что добавлено сверх базового ТЗ этапа)

- **Rate limiting, per-IP** (`io.ktor:ktor-server-rate-limit`, 20 запросов/60с на
  `/api/v1/weather`): мы сами проксируем Open-Meteo, и без лимита один клиент мог бы
  исчерпать общую бесплатную квоту сервиса для всех.
  ⚠️ **Реальный баг, найденный и исправленный при ручной проверке**: ключ клиента
  сначала был на `call.request.origin.remoteHost` — это `InetAddress.getHostName()`,
  который на каждый(!) запрос делает **обратный DNS-lookup**; в Docker-сети для
  внутреннего IP такой запрос виснет на секунды — каждый вызов `/api/v1/weather`
  занимал ~8с вместо десятков миллисекунд, включая попадания в кэш. Без явного ключа
  вообще лимит не применялся (все запросы делят один и тот же безлимитный бакет).
  Исправлено на `call.request.origin.remoteAddress` — сырой IP, без резолвинга.
  Закреплено тестом (`ApplicationTest`, см. ниже), чтобы регрессия не прошла незамеченной.
- **Таймаут HTTP-клиента к Open-Meteo** (`HttpTimeout`, 12с на запрос/6с на коннект +
  `StatusPages` на `HttpRequestTimeoutException` → `503`): без него медленный/недоступный
  Open-Meteo подвесил бы запрос клиента без всякой предсказуемой границы.
- **Graceful shutdown**: `monitor.subscribe(ApplicationStopped) { httpClient.close() }` —
  HTTP-клиент к Open-Meteo закрывается при остановке сервера, а не утекает.
- Автоматизированный **интеграционный тест через сам Docker-образ** (Testcontainers и
  т.п.) сознательно **не добавлен** — для опционального stretch-этапа это лишний вес
  (новая зависимость, более медленный/хрупкий локальный запуск и CI) относительно
  пользы. Вместо этого — воспроизводимый ручной чек-лист ниже, которым сервис реально
  проверен целиком, включая настоящий `docker compose up --build` (не только
  `java -jar`, который не проверяет сам `Dockerfile`).

## Тесты

`server/src/test/kotlin/.../WeatherServiceTest.kt` — 4 теста на `MockEngine`
(без единого реального сетевого вызова): агрегация параллельных массивов Open-Meteo
в список объектов (включая `null` в `precipitation_probability` и день/ночь по часам),
кэш-хит без повторного сетевого вызова, отказ на `latitude`/`longitude` вне диапазона.

`server/src/test/kotlin/.../ApplicationTest.kt` — 3 теста через `testApplication`
(настоящий Ktor-роутинг + плагины, включая `RateLimit`, но с фейковым `WeatherService` —
ни одного похода в интернет): `/health` отвечает, отсутствующие `lat`/`lon` → `400`
(а не `500`), и **21-й запрос подряд с одними координатами получает `429`** — тест
специально написан после найденного выше бага с `remoteHost`, чтобы такая регрессия
падала на `./gradlew.bat :server:test`, а не обнаруживалась вручную в Docker.

Прогнано: `./gradlew.bat :server:test` → **BUILD SUCCESSFUL**, 7/7 зелёных.
`./gradlew.bat :app:assembleDebug :core:network:test :core:data:test` — тоже зелёные
(перенос DTO в `core:openmeteo` не задел Android-сборку и её тесты).

## Ручная проверка через настоящий Docker-образ

```powershell
cd server
docker compose up -d --build   # собирает Dockerfile, а не просто java -jar
```
```bash
curl http://localhost:8090/health                                       # {"status":"ok"}
curl "http://localhost:8090/api/v1/weather?lat=61.785&lon=34.347"       # первый раз ~1-2с (реальный Open-Meteo)
curl "http://localhost:8090/api/v1/weather?lat=61.785&lon=34.347"       # повтор — из кэша, ~40мс, "cached":true
for i in $(seq 1 21); do curl -s -o /dev/null -w "%{http_code} " \
  "http://localhost:8090/api/v1/weather?lat=61.785&lon=34.347"; done    # 20×200, затем 429
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8090/swagger  # 200
docker compose stop   # проверка graceful shutdown — в логах нет исключений/зависаний
docker compose down
```
Всё перечисленное реально прогонялось при разработке этого этапа (включая тот самый
баг с `remoteHost`, который был найден именно так — не в юнит-тестах).

## Деплой

Не обязателен по ТЗ этапа — сделан только локальный запуск (`docker compose up` /
`java -jar`). Если понадобится реальный деплой — по образцу `devlog`/`expenses`
(Dockerfile уже готов, нужен только `docker-compose.yml` на сервере и DuckDNS-домен).
