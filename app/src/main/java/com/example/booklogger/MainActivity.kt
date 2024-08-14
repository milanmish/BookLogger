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
    val rating: Int
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
                            bookDetails?.let { BookDetailScreen(it) }
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
    var input by remember { mutableStateOf(dailyGoal.toString()) } // Define `input` here

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
                    val goal = input.toDoubleOrNull() ?: dailyGoal // Handle invalid input
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

            // Display logged books as buttons
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
                val bookDetails = BookDetails(
                    name = bookName.value,
                    totalTime = 0.0, // Initial value, time will be updated in BookDetailScreen
                    timeToday = timeToday.value.toDouble(),
                    pages = pagesRead.value.toInt(),
                    rating = bookRating.value.toInt()
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
fun BookDetailScreen(bookDetails: BookDetails) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Book Details")
        Text(text = "Name: ${bookDetails.name}")
        Text(text = "Time read today: ${bookDetails.timeToday} hours")
        Text(text = "Total time read: ${bookDetails.totalTime + bookDetails.timeToday} hours")
        Text(text = "Pages read: ${bookDetails.pages}")
        Text(text = "Book rating: ${bookDetails.rating}")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BookLoggerTheme {
        HomeScreen(navController = rememberNavController(), bookDetailsList = emptyList(), dailyGoal = 1.0, onUpdate = { _, _ -> })
    }
}
