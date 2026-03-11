package com.example.weatherawareexpensetracker

import android.app.Application
import com.example.weatherawareexpensetracker.data.ExpenseDatabase
import com.example.weatherawareexpensetracker.data.ExpenseRepository
import com.example.weatherawareexpensetracker.network.WeatherApiService
import com.example.weatherawareexpensetracker.network.JokeApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherApplication : Application() {
    private val database by lazy { ExpenseDatabase.getDatabase(this) }
    
    private val weatherRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val jokeRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://v2.jokeapi.dev/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private val weatherApiService by lazy {
        weatherRetrofit.create(WeatherApiService::class.java)
    }

    private val jokeApiService by lazy {
        jokeRetrofit.create(JokeApiService::class.java)
    }

    val repository by lazy { 
        ExpenseRepository(database.expenseDao(), weatherApiService, jokeApiService)
    }
}
