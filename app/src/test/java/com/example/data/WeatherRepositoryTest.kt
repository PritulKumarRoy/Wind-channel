package com.example.data

import com.example.data.api.OpenMeteoFields
import com.example.data.model.GeocodingResponse
import com.example.data.model.GeocodingResult
import com.example.data.model.WeatherException
import com.example.data.repository.WeatherRepository
import com.example.testing.FakeWeatherApi
import com.example.testing.TestData
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryTest {

  private val api = FakeWeatherApi()
  private val dispatcher = UnconfinedTestDispatcher()
  private val repo = WeatherRepository(api, dispatcher) { 1_800_000_000_000L }

  @Test fun `forecast request asks for hourly wind and the other fields the app reads`() = runTest(dispatcher) {
    repo.fetchWeather(TestData.city)
    assertTrue(api.lastHourly!!.split(",").contains("wind_speed_10m"))
    assertTrue(api.lastHourly!!.split(",").contains("is_day"))
    assertTrue(api.lastHourly!!.split(",").contains("visibility"))
    assertTrue(api.lastCurrent!!.split(",").contains("wind_gusts_10m"))
    assertTrue(api.lastDaily!!.split(",").contains("precipitation_probability_max"))
    assertEquals(OpenMeteoFields.FORECAST_DAYS, api.lastForecastDays)
    assertEquals(1, api.lastPastDays)
  }

  @Test fun `successful fetch records the download time`() = runTest(dispatcher) {
    val data = repo.fetchWeather(TestData.city)
    assertEquals(1_800_000_000_000L, data.fetchedAtMillis)
    assertEquals(TestData.city, data.city)
  }

  @Test fun `network problems become WeatherException Network`() = runTest(dispatcher) {
    for (error in listOf(IOException("boom"), UnknownHostException("x"), SocketTimeoutException("t"))) {
      api.forecast = { _, _ -> throw error }
      assertThrows<WeatherException.Network> { repo.fetchWeather(TestData.city) }
    }
  }

  @Test fun `http errors become WeatherException Server with the status code`() = runTest(dispatcher) {
    val body = "".toResponseBody("text/plain".toMediaType())
    api.forecast = { _, _ -> throw HttpException(Response.error<Any>(503, body)) }
    val e = assertThrows<WeatherException.Server> { repo.fetchWeather(TestData.city) }
    assertEquals(503, e.httpCode)
  }

  @Test fun `malformed json becomes InvalidData`() = runTest(dispatcher) {
    api.forecast = { _, _ -> throw JsonDataException("bad") }
    assertThrows<WeatherException.InvalidData> { repo.fetchWeather(TestData.city) }
    api.forecast = { _, _ -> throw JsonEncodingException("bad") }
    assertThrows<WeatherException.InvalidData> { repo.fetchWeather(TestData.city) }
  }

  @Test fun `incomplete response becomes InvalidData and never fake weather`() = runTest(dispatcher) {
    api.forecast = { _, _ -> TestData.response(current = null) }
    assertThrows<WeatherException.InvalidData> { repo.fetchWeather(TestData.city) }
  }

  @Test fun `cancellation is not swallowed`() = runTest(dispatcher) {
    api.forecast = { _, _ -> throw CancellationException("cancelled") }
    assertThrows<CancellationException> { repo.fetchWeather(TestData.city) }
  }

  // ---- search ----

  @Test fun `blank query returns empty without calling the service`() = runTest(dispatcher) {
    assertEquals(emptyList<Any>(), repo.searchCities("   "))
    assertEquals(0, api.searchCalls)
  }

  @Test fun `no results means an empty list`() = runTest(dispatcher) {
    api.search = { GeocodingResponse(results = null) }
    assertEquals(emptyList<Any>(), repo.searchCities("zzzzzz"))
    api.search = { GeocodingResponse(results = emptyList()) }
    assertEquals(emptyList<Any>(), repo.searchCities("zzzzzz"))
  }

  @Test fun `search maps results and drops entries without coordinates or name`() = runTest(dispatcher) {
    api.search = {
      GeocodingResponse(
        listOf(
          GeocodingResult(id = 1, name = "Springfield", latitude = 39.8, longitude = -89.6, country = "United States", admin1 = "Illinois"),
          GeocodingResult(id = 2, name = "NoCoords", latitude = null, longitude = null),
          GeocodingResult(id = 3, name = null, latitude = 1.0, longitude = 2.0),
          GeocodingResult(id = null, name = "NoId", latitude = 5.0, longitude = 6.0)
        )
      )
    }
    val cities = repo.searchCities(" Springfield ")
    assertEquals(2, cities.size)
    assertEquals("geo_1", cities[0].id)
    assertEquals("Illinois", cities[0].region)
    assertEquals("geo_5.0_6.0", cities[1].id)
    assertEquals("", cities[1].country)
  }

  @Test fun `search failure is reported not hidden`() = runTest(dispatcher) {
    api.search = { throw IOException("offline") }
    assertThrows<WeatherException.Network> { repo.searchCities("Paris") }
  }

  @Test fun `built-in search matches name country and region`() {
    assertEquals("Tokyo", repo.searchBuiltInCities("tok").single().name)
    assertEquals(emptyList<Any>(), repo.searchBuiltInCities("zzzz"))
    assertEquals(emptyList<Any>(), repo.searchBuiltInCities(" "))
  }

  private inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
    try {
      block()
    } catch (e: Throwable) {
      if (e is T) return e
      throw AssertionError("expected ${T::class.simpleName} but got $e", e)
    }
    throw AssertionError("expected ${T::class.simpleName} but nothing was thrown")
  }
}
