package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.WeatherApiService
import com.example.data.model.City
import com.example.data.model.CompleteWeatherData
import com.example.data.model.DefaultGlobalCities
import com.example.data.model.WeatherException
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/**
 * Loads forecasts and city search results from Open-Meteo.
 *
 * Failures are reported by throwing [WeatherException]; this class NEVER returns made-up weather.
 */
class WeatherRepository(
  private val api: WeatherApiService = createDefaultApi(),
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val clock: () -> Long = { System.currentTimeMillis() }
) {

  /** @throws WeatherException if live data could not be loaded or was incomplete. */
  suspend fun fetchWeather(city: City): CompleteWeatherData = withContext(ioDispatcher) {
    val response = try {
      api.getForecast(latitude = city.latitude, longitude = city.longitude)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      throw e.toWeatherException()
    }
    OpenMeteoParser.parse(city, response, clock())
  }

  /**
   * Searches the geocoding service. An empty list means "no matches".
   * @throws WeatherException if the service could not be reached.
   */
  suspend fun searchCities(query: String): List<City> = withContext(ioDispatcher) {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return@withContext emptyList()
    val response = try {
      api.searchLocations(trimmed)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      throw e.toWeatherException()
    }
    response.results.orEmpty().mapNotNull { item ->
      val name = item.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
      val lat = item.latitude ?: return@mapNotNull null
      val lon = item.longitude ?: return@mapNotNull null
      City(
        id = item.id?.let { "geo_$it" } ?: "geo_${lat}_$lon",
        name = name,
        region = item.admin1.orEmpty(),
        country = item.country.orEmpty(),
        latitude = lat,
        longitude = lon
      )
    }
  }

  /** Matches against the built-in city list only. Used when the search service is unreachable. */
  fun searchBuiltInCities(query: String): List<City> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return DefaultGlobalCities.filter {
      it.name.contains(q, ignoreCase = true) ||
        it.country.contains(q, ignoreCase = true) ||
        it.region.contains(q, ignoreCase = true)
    }
  }

  companion object {
    fun createDefaultApi(): WeatherApiService {
      val logging = HttpLoggingInterceptor().apply {
        // Request URLs contain coordinates; only log them in debug builds.
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
        else HttpLoggingInterceptor.Level.NONE
      }
      val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()
      val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
      return Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(WeatherApiService::class.java)
    }
  }
}

internal fun Throwable.toWeatherException(): WeatherException = when (this) {
  is WeatherException -> this
  is HttpException -> WeatherException.Server(code())
  // JsonEncodingException extends IOException, so it must be checked before IOException.
  is JsonEncodingException -> WeatherException.InvalidData("unreadable response", this)
  is JsonDataException -> WeatherException.InvalidData("unexpected response format", this)
  is IOException -> WeatherException.Network(this)
  else -> WeatherException.Unknown(this)
}
