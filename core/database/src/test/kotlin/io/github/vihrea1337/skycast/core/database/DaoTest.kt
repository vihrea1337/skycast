package io.github.vihrea1337.skycast.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.vihrea1337.skycast.core.database.entity.CurrentWeatherEntity
import io.github.vihrea1337.skycast.core.database.entity.DailyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.HourlyForecastEntity
import io.github.vihrea1337.skycast.core.database.entity.LocationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DaoTest {

    private lateinit var database: SkyCastDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SkyCastDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `replaceForecast replaces old rows atomically`() = runTest {
        val dao = database.weatherDao()
        database.locationDao().upsert(petrozavodsk)
        dao.replaceForecast(
            locationId = petrozavodsk.id,
            current = current(temperature = 10.0),
            hourly = listOf(hour("2026-07-08T14:00"), hour("2026-07-08T15:00")),
            daily = listOf(day("2026-07-08")),
        )

        dao.replaceForecast(
            locationId = petrozavodsk.id,
            current = current(temperature = 21.4),
            hourly = listOf(hour("2026-07-09T10:00")),
            daily = listOf(day("2026-07-09"), day("2026-07-10")),
        )

        assertEquals(21.4, dao.observeCurrent(petrozavodsk.id).first()!!.temperature, 0.0)
        assertEquals(listOf("2026-07-09T10:00"), dao.observeHourly(petrozavodsk.id).first().map { it.time })
        assertEquals(2, dao.observeDaily(petrozavodsk.id).first().size)
    }

    @Test
    fun `select keeps exactly one location selected`() = runTest {
        val dao = database.locationDao()
        dao.upsert(petrozavodsk)
        dao.upsert(moscow)
        dao.select(petrozavodsk.id)

        dao.select(moscow.id)

        val all = dao.observeAll().first()
        assertFalse(all.single { it.id == petrozavodsk.id }.isSelected)
        assertTrue(all.single { it.id == moscow.id }.isSelected)
        assertEquals(moscow.id, dao.observeSelected().first()?.id)
    }

    @Test
    fun `saveAndSelect appends to the end of the list`() = runTest {
        val dao = database.locationDao()
        dao.saveAndSelect(petrozavodsk)
        dao.saveAndSelect(moscow)

        val all = dao.observeAll().first()
        assertEquals(listOf(petrozavodsk.id, moscow.id), all.map { it.id })
        assertTrue(all.single { it.id == moscow.id }.isSelected)
    }

    @Test
    fun `deleting location cascades to its forecast`() = runTest {
        database.locationDao().upsert(petrozavodsk)
        database.weatherDao().replaceForecast(
            locationId = petrozavodsk.id,
            current = current(temperature = 21.4),
            hourly = listOf(hour("2026-07-08T14:00")),
            daily = listOf(day("2026-07-08")),
        )

        database.locationDao().delete(petrozavodsk.id)

        assertNull(database.weatherDao().observeCurrent(petrozavodsk.id).first())
        assertTrue(database.weatherDao().observeHourly(petrozavodsk.id).first().isEmpty())
        assertTrue(database.weatherDao().observeDaily(petrozavodsk.id).first().isEmpty())
    }

    private val petrozavodsk = LocationEntity(
        id = 509820,
        name = "Петрозаводск",
        region = "Карелия",
        country = "Россия",
        latitude = 61.78491,
        longitude = 34.34691,
        isSelected = false,
        sortOrder = 0,
    )

    private val moscow = petrozavodsk.copy(id = 524901, name = "Москва", region = null, sortOrder = 1)

    private fun current(temperature: Double) = CurrentWeatherEntity(
        locationId = petrozavodsk.id,
        temperature = temperature,
        feelsLike = temperature - 1.5,
        humidityPercent = 62,
        pressureHpa = 1006.9,
        windSpeed = 4.2,
        wmoCode = 3,
        isDay = true,
        updatedAtEpochSec = 1_784_000_000,
        temperatureUnit = "CELSIUS",
        windSpeedUnit = "METERS_PER_SECOND",
    )

    private fun hour(time: String) = HourlyForecastEntity(
        locationId = petrozavodsk.id,
        time = time,
        temperature = 20.0,
        wmoCode = 1,
        precipitationProbability = 10,
    )

    private fun day(date: String) = DailyForecastEntity(
        locationId = petrozavodsk.id,
        date = date,
        tempMin = 14.0,
        tempMax = 22.0,
        wmoCode = 2,
        sunrise = "${date}T02:57",
        sunset = "${date}T22:12",
    )
}
