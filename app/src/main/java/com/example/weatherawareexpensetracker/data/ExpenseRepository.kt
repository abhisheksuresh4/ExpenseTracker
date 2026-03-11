package com.example.weatherawareexpensetracker.data

import com.example.weatherawareexpensetracker.network.WeatherApiService
import com.example.weatherawareexpensetracker.network.WeatherResponse
import com.example.weatherawareexpensetracker.network.JokeApiService
import com.example.weatherawareexpensetracker.network.JokeResponse
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val weatherApiService: WeatherApiService,
    private val jokeApiService: JokeApiService
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun getWeather(query: String, apiKey: String): WeatherResponse {
        return weatherApiService.getWeather(apiKey, query)
    }

    suspend fun getRandomJoke(): JokeResponse {
        return jokeApiService.getRandomJoke()
    }
}
