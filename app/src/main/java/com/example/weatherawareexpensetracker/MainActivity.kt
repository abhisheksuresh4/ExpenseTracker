package com.example.weatherawareexpensetracker

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.weatherawareexpensetracker.data.Expense
import com.example.weatherawareexpensetracker.ui.ExpenseViewModel
import com.example.weatherawareexpensetracker.ui.ExpenseViewModelFactory
import com.example.weatherawareexpensetracker.ui.theme.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as WeatherApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAwareExpenseTrackerTheme {
                MainNavigation(viewModel)
            }
        }
    }
}

@Composable
fun MainNavigation(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navBarColor by animateColorAsState(
        targetValue = when (currentRoute) {
            "split" -> SplitBackground
            "weather" -> GoogleWeatherLightBlue
            else -> White
        },
        animationSpec = tween(durationMillis = 500)
    )

    val contentColor by animateColorAsState(
        targetValue = when (currentRoute) {
            "split" -> White
            "weather" -> GoogleWeatherNight
            else -> TextDark
        },
        animationSpec = tween(durationMillis = 500)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != "loading") {
                NavigationBar(
                    containerColor = navBarColor,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .shadow(12.dp, RoundedCornerShape(32.dp))
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (currentRoute == "split") DarkerGreen else DeepNavy,
                            selectedTextColor = if (currentRoute == "split") DarkerGreen else DeepNavy,
                            unselectedIconColor = contentColor.copy(alpha = 0.6f),
                            unselectedTextColor = contentColor.copy(alpha = 0.6f),
                            indicatorColor = if (currentRoute == "split") DarkPurple.copy(alpha = 0.2f) else SoftGreen
                        ),
                        onClick = { 
                            if (currentRoute != "home") {
                                navController.navigate("home") { popUpTo("home") { inclusive = true } }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Cloud, contentDescription = "Weather") },
                        label = { Text("Weather") },
                        selected = currentRoute == "weather",
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GoogleWeatherBlue,
                            selectedTextColor = GoogleWeatherBlue,
                            unselectedIconColor = contentColor.copy(alpha = 0.6f),
                            unselectedTextColor = contentColor.copy(alpha = 0.6f),
                            indicatorColor = White
                        ),
                        onClick = { 
                            if (currentRoute != "weather") {
                                navController.navigate("weather") 
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Group, contentDescription = "Split") },
                        label = { Text("Split") },
                        selected = currentRoute == "split",
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkerGreen,
                            selectedTextColor = DarkerGreen,
                            unselectedIconColor = contentColor.copy(alpha = 0.6f),
                            unselectedTextColor = contentColor.copy(alpha = 0.6f),
                            indicatorColor = DarkPurple.copy(alpha = 0.4f)
                        ),
                        onClick = { 
                            if (currentRoute != "split") {
                                navController.navigate("split") 
                            }
                        }
                    )
                }
            }
        },
        containerColor = navBarColor // Sync scaffold background with nav bar for seamless look
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "loading",
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable("loading") {
                LoadingScreen {
                    navController.navigate("home") {
                        popUpTo("loading") { inclusive = true }
                    }
                }
            }
            composable("home") {
                ExpenseTrackerApp(viewModel, navController)
            }
            composable("weather") {
                val insight by viewModel.weatherInsight.collectAsState()
                val expenses by viewModel.expenseListState.collectAsState()
                WeatherModule(insight, expenses, navController)
            }
            composable("split") {
                SplitBillScreen(viewModel, navController)
            }
        }
    }
}

@Composable
fun LoadingScreen(onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NeonYellow)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Initializing Ledger...", color = White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp(viewModel: ExpenseViewModel, navController: NavController) {
    val expenses by viewModel.expenseListState.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget
    val dailyLimit by viewModel.dailyLimit
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scrollState = rememberScrollState()
    
    val apiKey = "f40a764995834884ba632431260603" 

    LaunchedEffect(Unit) {
        viewModel.sarcasticMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundCream,
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) { Icon(Icons.Default.Settings, contentDescription = "Settings") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DeepNavy,
                contentColor = NeonYellow,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 80.dp) // Move FAB up so it doesn't overlap with the new nav bar
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            BudgetOverviewCard(expenses, monthlyBudget, dailyLimit)
            Spacer(modifier = Modifier.height(24.dp))
            TrendsCard(expenses)
            Spacer(modifier = Modifier.height(24.dp))
            CategoryListSection(expenses, viewModel)
            Spacer(modifier = Modifier.height(24.dp))
            RecentTransactions(expenses, viewModel)
            Spacer(modifier = Modifier.height(100.dp)) // Extra padding for bottom nav
        }

        if (showAddDialog) {
            AddExpenseDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { amount, category, desc ->
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            viewModel.addExpense(amount, category, desc, location?.latitude ?: 0.0, location?.longitude ?: 0.0, apiKey)
                        }
                    } catch (e: SecurityException) {
                        viewModel.addExpense(amount, category, desc, 0.0, 0.0, apiKey)
                    }
                    showAddDialog = false
                }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentBudget = monthlyBudget,
                currentLimit = dailyLimit,
                onDismiss = { showSettingsDialog = false },
                onSave = { budget, limit ->
                    viewModel.updateBudget(budget)
                    viewModel.updateDailyLimit(limit)
                    showSettingsDialog = false
                }
            )
        }
    }
}

@Composable
fun BudgetOverviewCard(expenses: List<Expense>, budget: Double, limit: Double) {
    val totalSpent = expenses.sumOf { it.amount }
    val remaining = (budget - totalSpent).coerceAtLeast(0.0)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Remaining Budget", style = MaterialTheme.typography.labelMedium, color = TextGrey)
                    Text("₹${String.format("%,.0f", remaining)}", style = MaterialTheme.typography.headlineLarge, color = TextDark, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.size(48.dp).background(SoftGreen, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = DeepNavy)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (totalSpent / budget).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = if (totalSpent > budget) Color.Red else DeepNavy,
                trackColor = BackgroundCream
            )
        }
    }
}

@Composable
fun TrendsCard(expenses: List<Expense>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Last 7 Days", color = White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val last7Days = (0..6).map { i ->
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    cal
                }.reversed()

                val dailyTotals = last7Days.map { day ->
                    expenses.filter { exp ->
                        val expCal = Calendar.getInstance().apply { timeInMillis = exp.date }
                        expCal.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
                    }.sumOf { it.amount }
                }

                val maxAmount = (dailyTotals.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                
                dailyTotals.forEach { amount ->
                    val heightFactor = (amount / maxAmount).toFloat().coerceIn(0.1f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightFactor)
                            .background(if (heightFactor > 0.8f) Color.Red else NeonYellow, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherModule(insight: String, expenses: List<Expense>, navController: NavController) {
    val scrollState = rememberScrollState()
    val latestExpense = expenses.lastOrNull()
    val temp = latestExpense?.temperature ?: 28.0
    val condition = latestExpense?.weatherCondition ?: "Partly Cloudy"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GoogleWeatherBlue, GoogleWeatherLightBlue)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Mumbai", style = MaterialTheme.typography.titleLarge, color = White)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Big Weather Icon & Temp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (condition.contains("Rain")) Icons.Default.WaterDrop else Icons.Default.WbSunny, 
                    contentDescription = null, 
                    modifier = Modifier.size(100.dp), 
                    tint = NeonYellow
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "${temp.toInt()}°", 
                    style = MaterialTheme.typography.displayLarge, 
                    color = White, 
                    fontWeight = FontWeight.Light,
                    fontSize = 80.sp
                )
            }
            Text(condition, style = MaterialTheme.typography.headlineSmall, color = White.copy(alpha = 0.9f))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Weather Details Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                WeatherDetailItem(Icons.Default.Air, "12 km/h", "Wind")
                WeatherDetailItem(Icons.Default.WaterDrop, "64%", "Humidity")
                WeatherDetailItem(Icons.Default.Thermostat, "${(temp + 2).toInt()}°", "Feels like")
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Spending Insight Card (Glassmorphism effect)
            Card(
                colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, White.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = NeonYellow)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Financial Weather Alert", style = MaterialTheme.typography.titleMedium, color = White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyLarge,
                        color = White,
                        lineHeight = 26.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Forecast Mockup
            Text("Daily Forecast", style = MaterialTheme.typography.titleMedium, color = White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ForecastItem("Mon", Icons.Default.Cloud, "28°")
                ForecastItem("Tue", Icons.Default.WbSunny, "31°")
                ForecastItem("Wed", Icons.Default.WaterDrop, "26°")
                ForecastItem("Thu", Icons.Default.Cloud, "29°")
                ForecastItem("Fri", Icons.Default.WbSunny, "32°")
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun WeatherDetailItem(icon: ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = White, fontWeight = FontWeight.Bold)
        Text(label, color = White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ForecastItem(day: String, icon: ImageVector, temp: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, color = White.copy(alpha = 0.8f))
        Icon(icon, contentDescription = null, tint = NeonYellow, modifier = Modifier.padding(vertical = 4.dp))
        Text(temp, color = White, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillScreen(viewModel: ExpenseViewModel, navController: NavController) {
    var amount by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("1") }
    var payerImageUri by remember { mutableStateOf<Uri?>(null) }
    var showLogDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val apiKey = "f40a764995834884ba632431260603"
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> payerImageUri = uri }
    )

    val total = amount.toDoubleOrNull() ?: 0.0
    val count = people.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val perPerson = total / count

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplitBackground)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Split the Bill", 
                style = MaterialTheme.typography.headlineLarge, 
                color = DarkerGreen, 
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(40.dp))
            
            // Visual feedback for splitting with Payer Image
            Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable { 
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawCircle(
                        color = DarkPurple.copy(alpha = 0.2f),
                        radius = size.minDimension / 2
                    )
                }
                if (payerImageUri != null) {
                    AsyncImage(
                        model = payerImageUri,
                        contentDescription = "Payer",
                        modifier = Modifier.size(160.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = DarkerGreen, modifier = Modifier.size(48.dp))
                        Text("Add Payer Pic", color = White.copy(alpha = 0.6f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkPurple.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, DarkPurple)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Per Person Pays", color = White.copy(alpha = 0.7f))
                    Text(
                        "₹${String.format("%,.2f", perPerson)}", 
                        style = MaterialTheme.typography.displayMedium, 
                        color = DarkerGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Total Amount", color = DarkerGreen) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = DarkerGreen,
                    unfocusedBorderColor = DarkPurple
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("₹", color = DarkerGreen, fontWeight = FontWeight.Bold) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = people,
                onValueChange = { people = it },
                label = { Text("Number of People", color = DarkerGreen) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = DarkerGreen,
                    unfocusedBorderColor = DarkPurple
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DarkerGreen) }
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { if (total > 0) showLogDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkerGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Confirm & Log Split", color = White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showLogDialog) {
            AlertDialog(
                onDismissRequest = { showLogDialog = false },
                title = { Text("Confirm Split Log") },
                text = {
                    Column {
                        Text("Total Amount: ₹$amount")
                        Text("People: $count")
                        Text("Per Person: ₹${String.format("%.2f", perPerson)}")
                        if (payerImageUri != null) {
                            Text("Payer Pic Attached", color = DarkerGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                viewModel.addExpense(
                                    amount = total,
                                    category = "Split Bill",
                                    description = "Split between $count people",
                                    lat = location?.latitude ?: 0.0,
                                    lon = location?.longitude ?: 0.0,
                                    apiKey = apiKey,
                                    payerImageUri = payerImageUri?.toString()
                                )
                            }
                        } catch (e: SecurityException) {
                            viewModel.addExpense(total, "Split Bill", "Split between $count people", 0.0, 0.0, apiKey, payerImageUri?.toString())
                        }
                        showLogDialog = false
                        navController.navigate("home") { popUpTo("home") { inclusive = true } }
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun CategoryListSection(expenses: List<Expense>, viewModel: ExpenseViewModel) {
    Text(text = "Categories", style = MaterialTheme.typography.headlineSmall, color = TextDark, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(12.dp))
    
    val categoryData = expenses.groupBy { it.category }
        .map { (name, list) -> 
            val total = list.sumOf { it.amount }
            val icon = when(name.lowercase()) {
                "travel", "transport" -> "✈️"
                "utilities", "bills" -> "🔌"
                "food" -> "🍕"
                "shopping" -> "🛍️"
                "investment" -> "💰"
                else -> "📦"
            }
            Triple(name, total, icon)
        }
        .sortedByDescending { it.second }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categoryData.forEach { (name, amount, icon) ->
            var showEditDialog by remember { mutableStateOf(false) }
            
            CategoryItem(name, amount, icon, onClick = { showEditDialog = true })
            
            if (showEditDialog) {
                EditCategoryDialog(
                    oldName = name,
                    onDismiss = { showEditDialog = false },
                    onDelete = {
                        expenses.filter { it.category == name }.forEach { viewModel.deleteExpense(it) }
                        showEditDialog = false
                    },
                    onRename = { newName ->
                        showEditDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, amount: Double, icon: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, style = MaterialTheme.typography.titleMedium, color = TextDark)
            Spacer(modifier = Modifier.weight(1f))
            Text("₹${String.format("%.0f", amount)}", fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGrey)
        }
    }
}

@Composable
fun EditCategoryDialog(oldName: String, onDismiss: () -> Unit, onDelete: () -> Unit, onRename: (String) -> Unit) {
    var newName by remember { mutableStateOf(oldName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Rename or delete category '$oldName'")
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Category Name") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onRename(newName) }) { Text("Rename") }
        },
        dismissButton = {
            TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("Delete All") }
        }
    )
}

@Composable
fun RecentTransactions(expenses: List<Expense>, viewModel: ExpenseViewModel) {
    if (expenses.isEmpty()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Recent Transactions", style = MaterialTheme.typography.headlineSmall, color = TextDark, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        expenses.take(10).forEach { expense ->
            TransactionItem(expense, onDelete = { viewModel.deleteExpense(expense) })
        }
    }
}

@Composable
fun TransactionItem(expense: Expense, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = White
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (expense.payerImageUri != null) {
                AsyncImage(
                    model = expense.payerImageUri,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(expense.description.ifEmpty { expense.category }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(expense.category, style = MaterialTheme.typography.labelSmall, color = TextGrey)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("₹${String.format("%.0f", expense.amount)}", fontWeight = FontWeight.Bold)
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); showMenu = false })
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (Double, String, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Investment", "Other")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                        }
                    }
                }
                
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, category, description) }) { Text("Add") }
        }
    )
}

@Composable
fun SettingsDialog(currentBudget: Double, currentLimit: Double, onDismiss: () -> Unit, onSave: (Double, Double) -> Unit) {
    var budget by remember { mutableStateOf(currentBudget.toString()) }
    var limit by remember { mutableStateOf(currentLimit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Limits") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Monthly Budget") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Daily Limit") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = { onSave(budget.toDoubleOrNull() ?: 0.0, limit.toDoubleOrNull() ?: 0.0) }) { Text("Save") }
        }
    )
}
