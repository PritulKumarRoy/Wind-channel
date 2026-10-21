package com.example.data.api

import com.example.data.model.GeocodingResponse
import com.example.data.model.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

/** Variable lists requested from Open-Meteo. Kept as constants so tests can verify them. */
object OpenMeteoFields {
  const val CURRENT = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation," +
    "weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m,cloud_cover"
  const val HOURLY = "temperature_2m,precipitation_probability,weather_code,surface_pressure," +
    "wind_speed_10m,visibility,dew_point_2m,is_day"
  const val DAILY = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max," +
    "sunrise,sunset,uv_index_max"
  /** Yesterday is requested so the previous hours are available for the pressure trend. */
  const val PAST_DAYS = 1
  const val FORECAST_DAYS = 10
}

interface WeatherApiService {

  @GET("https://api.open-meteo.com/v1/forecast")
  suspend fun getForecast(
    @Query("latitude") latitude: Double,
    @Query("longitude") longitude: Double,
    @Query("current") current: String = OpenMeteoFields.CURRENT,
    @Query("hourly") hourly: String = OpenMeteoFields.HOURLY,
    @Query("daily") daily: String = OpenMeteoFields.DAILY,
    @Query("timezone") timezone: String = "auto",
    @Query("past_days") pastDays: Int = OpenMeteoFields.PAST_DAYS,
    @Query("forecast_days") forecastDays: Int = OpenMeteoFields.FORECAST_DAYS
  ): OpenMeteoResponse

  @GET("https://geocoding-api.open-meteo.com/v1/search")
  suspend fun searchLocations(
    @Query("name") query: String,
    @Query("count") count: Int = 10,
    @Query("language") language: String = "en",
    @Query("format") format: String = "json"
  ): GeocodingResponse
}
