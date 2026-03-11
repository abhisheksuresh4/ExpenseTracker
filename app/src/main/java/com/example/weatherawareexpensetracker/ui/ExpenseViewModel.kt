package com.example.weatherawareexpensetracker.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherawareexpensetracker.data.Expense
import com.example.weatherawareexpensetracker.data.ExpenseRepository
import com.example.weatherawareexpensetracker.network.JokeResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _monthlyBudget = mutableStateOf(50000.0)
    val monthlyBudget: State<Double> = _monthlyBudget

    private val _dailyLimit = mutableStateOf(2000.0)
    val dailyLimit: State<Double> = _dailyLimit

    private val _sarcasticMessage = MutableSharedFlow<String>()
    val sarcasticMessage = _sarcasticMessage.asSharedFlow()

    private val _joke = mutableStateOf<String>("Loading your financial doom...")
    val joke: State<String> = _joke

    val expenseListState: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weatherInsight: StateFlow<String> = expenseListState.map { expenses ->
        generateWeatherInsight(expenses)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Analyzing your spending patterns..."
    )

    init {
        fetchJoke()
    }

    fun fetchJoke() {
        viewModelScope.launch {
            try {
                val response = repository.getRandomJoke()
                _joke.value = if (response.type == "single") {
                    response.joke ?: "No joke found, just like your savings."
                } else {
                    "${response.setup}\n\n${response.delivery}"
                }
            } catch (e: Exception) {
                _joke.value = "Error loading joke. Your bank balance is the real joke anyway."
            }
        }
    }

    fun updateBudget(amount: Double) {
        _monthlyBudget.value = amount
    }

    fun updateDailyLimit(amount: Double) {
        _dailyLimit.value = amount
    }

    fun addExpense(amount: Double, category: String, description: String, lat: Double, lon: Double, apiKey: String, payerImageUri: String? = null) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val todayTotal = expenseListState.value
                .filter { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.date)) == today }
                .sumOf { it.amount }

            if (todayTotal + amount > _dailyLimit.value) {
                _sarcasticMessage.emit("Wow, look at Mr. Moneybags! You've already hit your daily limit of ₹${_dailyLimit.value}. Stop spending!")
            }

            if (expenseListState.value.sumOf { it.amount } + amount > _monthlyBudget.value) {
                _sarcasticMessage.emit("Budget exceeded. Time to start eating grass from the backyard.")
            }

            try {
                val weather = repository.getWeather("$lat,$lon", apiKey)
                val expense = Expense(
                    amount = amount,
                    category = category,
                    description = description,
                    temperature = weather.current.tempC,
                    weatherCondition = weather.current.condition.text,
                    payerImageUri = payerImageUri
                )
                repository.insertExpense(expense)
            } catch (e: Exception) {
                repository.insertExpense(Expense(amount = amount, category = category, description = description, payerImageUri = payerImageUri))
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    private fun generateWeatherInsight(expenses: List<Expense>): String {
        if (expenses.isEmpty()) return "Start logging expenses to see weather-driven insights."
        
        val rainExpenses = expenses.filter { it.weatherCondition?.contains("Rain", ignoreCase = true) == true }
        val transportRain = rainExpenses.filter { it.category.contains("Transport", ignoreCase = true) || it.category.contains("Travel", ignoreCase = true) }
        
        return when {
            transportRain.isNotEmpty() -> "Insight: Transport expenses rose during rain. Buy an umbrella next time?"
            rainExpenses.size > 3 -> "Insight: You spend more on food when it rains. Emotional eating much?"
            else -> "Insight: Spending stable. Don't ruin it."
        }
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
