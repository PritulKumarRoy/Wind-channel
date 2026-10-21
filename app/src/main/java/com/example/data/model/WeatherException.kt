package com.example.data.model

/** Failures that can occur while talking to the weather / geocoding service. */
sealed class WeatherException(message: String, cause: Throwable? = null) : Exception(message, cause) {
  /** No connectivity, DNS failure, timeout, etc. */
  class Network(cause: Throwable? = null) :
    WeatherException("Can't reach the weather service. Check your internet connection and try again.", cause)

  /** The service answered with a non-2xx HTTP status. */
  class Server(val httpCode: Int) :
    WeatherException("The weather service returned an error (HTTP $httpCode). Please try again shortly.")

  /** The response arrived but was malformed or lacked fields that are required to show a forecast. */
  class InvalidData(detail: String, cause: Throwable? = null) :
    WeatherException("The weather service returned incomplete data ($detail).", cause)

  class Unknown(cause: Throwable? = null) :
    WeatherException("Something went wrong while loading the forecast.", cause)
}
