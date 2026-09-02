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
- **`OpenMeteoDto.kt`** — сырые DTO Open-Meteo, **продублированные** из
  `core/network/dto/ForecastResponseDto.kt`. Осознанный trade-off: сервер — отдельный
  (не-KMP) Gradle-модуль, подключить к нему Android-библиотеку напрямую нельзя.
  Вынести в общий KMP-модуль — вариант на будущее, если решится делать это всерьёз
  (см. похожее решение про рукописные vs сгенерированные DTO в `docs/PLAN.md` §4.2 —
  та же логика: «рукописное, под контролем, задокументированный компромисс»).

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

## Тесты

`server/src/test/kotlin/.../WeatherServiceTest.kt` — 5 тестов на `MockEngine`
(без единого реального сетевого вызова):
1. параллельные массивы Open-Meteo корректно превращаются в список объектов
   (включая `null` в `precipitation_probability` и день/ночь по часам);
2. второй запрос с теми же координатами отдаётся из кэша (проверяется, что
   `MockEngine` не получил второго запроса);
3–4. `latitude`/`longitude` вне диапазона → `InvalidCoordinatesException`.

Прогнано: `./gradlew.bat :server:test` → **BUILD SUCCESSFUL**, все 5 зелёные.
Живая проверка (не мок): реальный вызов `/api/v1/weather` против настоящего
Open-Meteo прошёл (см. пример ответа выше) — деплой при этом не потребовался,
только `./gradlew.bat :server:buildFatJar` + `java -jar server-all.jar` локально.

## Деплой

Не обязателен по ТЗ этапа — сделан только локальный запуск (`docker compose up` /
`java -jar`). Если понадобится реальный деплой — по образцу `devlog`/`expenses`
(Dockerfile уже готов, нужен только `docker-compose.yml` на сервере и DuckDNS-домен).
