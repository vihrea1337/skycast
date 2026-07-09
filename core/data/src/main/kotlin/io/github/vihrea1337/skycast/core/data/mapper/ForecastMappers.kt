package io.github.vihrea1337.skycast.core.data.mapper

import io.github.vihrea1337.skycast.core.database.entity.CurrentWeatherEntity
import io.github.vihrea1337.skycast.core.database.entity.DailyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.HourlyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.LocationEntity
import io.github.vihrea1337.skycast.core.model.CurrentWeather
import io.github.vihrea1337.skycast.core.model.DailyForecast
import io.github.vihrea1337.skycast.core.model.HourlyForecast
import io.github.vihrea1337.skycast.core.model.Location
import io.github.vihrea1337.skycast.core.model.UserSettings
import io.github.vihrea1337.skycast.core.model.WeatherData
import io.github.vihrea1337.skycast.core.model.WeatherType
import io.github.vihrea1337.skycast.core.network.dto.DailyDto
import io.github.vihrea1337.skycast.core.network.dto.ForecastResponseDto
import io.github.vihrea1337.skycast.core.network.dto.HourlyDto
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

internal const val HOURS_IN_FORECAST = 24

// ---------- DTO → Entity (запись при refresh) ----------

internal fun ForecastResponseDto.toCurrentWeatherEntity(
    locationId: Long,
    settings: UserSettings,
    updatedAt: Instant,
): CurrentWeatherEntity = CurrentWeatherEntity(
    locationId = locationId,
    temperature = current.temperature,
    feelsLike = current.feelsLike,
    humidityPercent = current.humidityPercent,
    pressureHpa = current.pressureHpa,
    windSpeed = current.windSpeed,
    wmoCode = current.weatherCode,
    isDay = current.isDay == 1,
    updatedAtEpochSec = updatedAt.epochSecond,
    temperatureUnit = settings.temperatureUnit.name,
    windSpeedUnit = settings.windSpeedUnit.name,
)

/**
 * «Сшивает» параллельные массивы; срез — от текущего часа [currentTime] включительно,
 * не более [HOURS_IN_FORECAST]. Рассинхрон массивов бросает исключение —
 * репозиторий превращает его в AppError.Unknown.
 */
internal fun HourlyDto.toHourlyForecastEntities(
    locationId: Long,
    currentTime: String,
): List<HourlyForecastEntity> {
    val from = LocalDateTime.parse(currentTime).truncatedTo(ChronoUnit.HOURS)
    return time.indices
        .asSequence()
        .filter { i -> LocalDateTime.parse(time[i]) >= from }
        .take(HOURS_IN_FORECAST)
        .map { i ->
            HourlyForecastEntity(
                locationId = locationId,
                time = time[i],
                temperature = temperature[i],
                wmoCode = weatherCode[i],
                precipitationProbability = precipitationProbability.getOrNull(i) ?: 0,
                // Отсутствие массива деградирует в «день» — нейтральная иконка
                isDay = isDay.getOrNull(i) != 0,
            )
        }
        .toList()
}

internal fun DailyDto.toDailyForecastEntities(locationId: Long): List<DailyForecastEntity> =
    time.indices.map { i ->
        DailyForecastEntity(
            locationId = locationId,
            date = time[i],
            tempMin = temperatureMin[i],
            tempMax = temperatureMax[i],
            wmoCode = weatherCode[i],
            sunrise = sunrise[i],
            sunset = sunset[i],
        )
    }

// ---------- Entity → домен (чтение при observe) ----------

internal fun toWeatherData(
    location: LocationEntity,
    current: CurrentWeatherEntity,
    hourly: List<HourlyForecastEntity>,
    daily: List<DailyForecastEntity>,
): WeatherData = WeatherData(
    location = location.toLocation(),
    current = CurrentWeather(
        temperature = current.temperature,
        feelsLike = current.feelsLike,
        humidityPercent = current.humidityPercent,
        pressureHpa = current.pressureHpa,
        windSpeed = current.windSpeed,
        weatherType = WeatherType.fromWmoCode(current.wmoCode),
        isDay = current.isDay,
    ),
    hourly = hourly.map { it.toHourlyForecast() },
    daily = daily.map { it.toDailyForecast() },
    updatedAt = Instant.ofEpochSecond(current.updatedAtEpochSec),
    temperatureUnit = current.temperatureUnit.toEnumOrDefault(UserSettings.Default.temperatureUnit),
    windSpeedUnit = current.windSpeedUnit.toEnumOrDefault(UserSettings.Default.windSpeedUnit),
)

/** Битое/устаревшее значение из БД тихо падает в дефолт — не повод крэшиться. */
private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default

internal fun LocationEntity.toLocation(): Location = Location(
    id = id,
    name = name,
    region = region,
    country = country,
    latitude = latitude,
    longitude = longitude,
)

internal fun HourlyForecastEntity.toHourlyForecast(): HourlyForecast = HourlyForecast(
    time = LocalDateTime.parse(time),
    temperature = temperature,
    weatherType = WeatherType.fromWmoCode(wmoCode),
    precipitationProbability = precipitationProbability,
    isDay = isDay,
)

internal fun DailyForecastEntity.toDailyForecast(): DailyForecast = DailyForecast(
    date = LocalDate.parse(date),
    tempMin = tempMin,
    tempMax = tempMax,
    weatherType = WeatherType.fromWmoCode(wmoCode),
    sunrise = LocalDateTime.parse(sunrise),
    sunset = LocalDateTime.parse(sunset),
)
