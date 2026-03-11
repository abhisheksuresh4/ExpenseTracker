package com.example.weatherawareexpensetracker.network

import retrofit2.http.GET

data class JokeResponse(
    val error: Boolean,
    val category: String,
    val type: String,
    val joke: String? = null,
    val setup: String? = null,
    val delivery: String? = null,
    val id: Int,
    val safe: Boolean,
    val lang: String
)

interface JokeApiService {
    @GET("joke/Any")
    suspend fun getRandomJoke(): JokeResponse
}
