package com.example.booklogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booklogger.ui.theme.BookLoggerTheme

data class BookDetails(
    val name: String,
    val totalTime: Double,
    val timeToday: Double,
    val pages: Int,
    val rating: Int,
    val creationTimestamp: Long // Store timestamp when the book was created
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookLoggerTheme {
                val navController = rememberNavController()
                var bookDetailsList by remember { mutableStateOf<List<BookDetails>>(emptyList()) }
                var dailyGoal by remember { mutableStateOf(1.0) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(navController, bookDetailsList, dailyGoal) { updatedList, goal ->
                                bookDetailsList = updatedList
                                dailyGoal = goal
                            }
                        }
                        composable("bookLog") {
                            LogBookRead(navController) { newBook ->
                                bookDetailsList = bookDetailsList + newBook
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        }
                        composable("bookDetail/{bookName}") { backStackEntry ->
                            val bookName = backStackEntry.arguments?.getString("bookName") ?: ""
                            val bookDetails = bookDetailsList.find { it.name == bookName }
                            if (bookDetails != null) {
                                BookDetailScreen(bookDetails, navController) { updatedDetails ->
                                    bookDetailsList = bookDetailsList.map {
                                        if (it.name == updatedDetails.name) updatedDetails else it
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    bookDetailsList: List<BookDetails>,
    dailyGoal: Double,
    onUpdate: (List<BookDetails>, Double) -> Unit
) {
    var totalTimeLast24Hours by remember { mutableStateOf(0.0) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf(dailyGoal.toString()) }

    LaunchedEffect(bookDetailsList) {
        totalTimeLast24Hours = bookDetailsList.sumOf { it.timeToday }
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Daily Reading Goal") },
            text = {
                TextField(
                    value = input,
                    onValueChange = { input = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val goal = input.toDoubleOrNull() ?: dailyGoal
                    onUpdate(bookDetailsList, goal)
                    showGoalDialog = false
                }) {
                    Text("Set Goal")
                }
            }
        )
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome to Book Logger!",
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "Recently Read",
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .background(Color.Gray)
                    .clickable { navController.navigate("bookLog") }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = Color.White, fontSize = 24.sp)
            }

            bookDetailsList.forEach { bookDetails ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .background(Color.Gray)
                        .clickable {
                            navController.navigate("bookDetail/${bookDetails.name}")
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = bookDetails.name, color = Color.White)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.LightGray)
                .clickable { showGoalDialog = true }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = (totalTimeLast24Hours / dailyGoal).coerceIn(0.0, 1.0)
            Text(text = "Daily Goal: ${(progress * 100).toInt()}%", fontSize = 18.sp)
        }
    }
}

@Composable
fun LogBookRead(navController: NavHostController, onBookLogged: (BookDetails) -> Unit) {
    val bookName = remember { mutableStateOf("") }
    val timeToday = remember { mutableStateOf("") }
    val pagesRead = remember { mutableStateOf("") }
    val bookRating = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = bookName.value,
            onValueChange = { bookName.value = it },
            label = { Text("Enter book name") }
        )

        TextField(
            value = timeToday.value,
            onValueChange = { timeToday.value = it },
            label = { Text("Time read today (hours)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        TextField(
            value = pagesRead.value,
            onValueChange = { pagesRead.value = it },
            label = { Text("Pages read") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        TextField(
            value = bookRating.value,
            onValueChange = { bookRating.value = it },
            label = { Text("Book rating (1-5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                val currentTimestamp = System.currentTimeMillis()
                val bookDetails = BookDetails(
                    name = bookName.value,
                    totalTime = timeToday.value.toDoubleOrNull() ?: 0.0,
                    timeToday = timeToday.value.toDoubleOrNull() ?: 0.0,
                    pages = pagesRead.value.toIntOrNull() ?: 0,
                    rating = bookRating.value.toIntOrNull() ?: 1,
                    creationTimestamp = currentTimestamp
                )
                onBookLogged(bookDetails)
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun BookDetailScreen(
    bookDetails: BookDetails,
    navController: NavHostController,
    onUpdate: (BookDetails) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var updatedBookDetails by remember { mutableStateOf(bookDetails) }

    // Function to reset timeToday and update totalTime
    fun updateBookDetails(newTimeToday: Double) {
        val currentTimestamp = System.currentTimeMillis()
        val daysSinceCreation = (currentTimestamp - bookDetails.creationTimestamp) / (24 * 60 * 60 * 1000)

        val newTotalTime = if (daysSinceCreation >= 1) {
            updatedBookDetails.totalTime + newTimeToday
        } else {
            updatedBookDetails.totalTime
        }

        updatedBookDetails = updatedBookDetails.copy(
            timeToday = if (daysSinceCreation >= 1) 0.0 else newTimeToday,
            totalTime = newTotalTime
        )
        onUpdate(updatedBookDetails)
    }

    if (showEditDialog) {
        var name by remember { mutableStateOf(updatedBookDetails.name) }
        var totalTime by remember { mutableStateOf(updatedBookDetails.totalTime.toString()) }
        var timeToday by remember { mutableStateOf(updatedBookDetails.timeToday.toString()) }
        var pages by remember { mutableStateOf(updatedBookDetails.pages.toString()) }
        var rating by remember { mutableStateOf(updatedBookDetails.rating.toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Book Details") },
            text = {
                Column {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Book Name") }
                    )

                    TextField(
                        value = totalTime,
                        onValueChange = { totalTime = it },
                        label = { Text("Total Time Read (hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    TextField(
                        value = timeToday,
                        onValueChange = { timeToday = it },
                        label = { Text("Time Read Today (hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    TextField(
                        value = pages,
                        onValueChange = { pages = it },
                        label = { Text("Pages") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    TextField(
                        value = rating,
                        onValueChange = { rating = it },
                        label = { Text("Rating (1-5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newTotalTime = totalTime.toDoubleOrNull() ?: updatedBookDetails.totalTime
                    val newTimeToday = timeToday.toDoubleOrNull() ?: updatedBookDetails.timeToday
                    val newPages = pages.toIntOrNull() ?: updatedBookDetails.pages
                    val newRating = rating.toIntOrNull() ?: updatedBookDetails.rating

                    updatedBookDetails = updatedBookDetails.copy(
                        name = name,
                        totalTime = newTotalTime,
                        timeToday = newTimeToday,
                        pages = newPages,
                        rating = newRating
                    )

                    // Reset timeToday and update totalTime
                    updateBookDetails(newTimeToday)

                    showEditDialog = false
                }) {
                    Text("Save Changes")
                }
            }
        )
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row for Home and Edit Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Home Button
            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Home")
            }

            // Edit Details Button
            Button(onClick = { showEditDialog = true }) {
                Text("Edit Details")
            }
        }

        Text("Book Details", fontSize = 24.sp)

        Text("Name: ${updatedBookDetails.name}")
        Text("Total Time Read: ${updatedBookDetails.totalTime} hours")
        Text("Time Read Today: ${updatedBookDetails.timeToday} hours")
        Text("Pages Read: ${updatedBookDetails.pages}")
        Text("Rating: ${updatedBookDetails.rating}")
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BookLoggerTheme {
        HomeScreen(rememberNavController(), emptyList(), 1.0) { _, _ -> }
    }
}
