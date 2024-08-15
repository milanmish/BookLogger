package com.example.booklogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

data class ReadingListBook(
    val title: String,
    val author: String,
    val genre: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookLoggerTheme {
                val navController = rememberNavController()
                var bookDetailsList by remember { mutableStateOf<List<BookDetails>>(emptyList()) }
                var readingList by remember { mutableStateOf<List<ReadingListBook>>(emptyList()) }
                var dailyGoal by remember { mutableStateOf(1.0) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(navController, bookDetailsList, readingList, dailyGoal) { updatedList, updatedReadingList, goal ->
                                bookDetailsList = updatedList
                                readingList = updatedReadingList
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
                        composable("addReadingListBook") {
                            AddReadingListBook(navController) { newBook ->
                                readingList = readingList + newBook
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
    readingList: List<ReadingListBook>,
    dailyGoal: Double,
    onUpdate: (List<BookDetails>, List<ReadingListBook>, Double) -> Unit
) {
    var totalTimeLast24Hours by remember { mutableStateOf(0.0) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf(dailyGoal.toString()) }
    var bookToDelete by remember { mutableStateOf<BookDetails?>(null) }
    var readingListBookToDelete by remember { mutableStateOf<ReadingListBook?>(null) }

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
                    onUpdate(bookDetailsList, readingList, goal)
                    showGoalDialog = false
                }) {
                    Text("Set Goal")
                }
            }
        )
    }

    bookToDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete '${book.name}'?") },
            confirmButton = {
                Button(onClick = {
                    // Remove the book from the list
                    onUpdate(
                        bookDetailsList.filter { it != book },
                        readingList,
                        dailyGoal
                    )
                    bookToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { bookToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    readingListBookToDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { readingListBookToDelete = null },
            title = { Text("Delete Book") },
            text = { Text("Are you sure you want to delete '${book.title}'?") },
            confirmButton = {
                Button(onClick = {
                    // Remove the book from the list
                    onUpdate(
                        bookDetailsList,
                        readingList.filter { it != book },
                        dailyGoal
                    )
                    readingListBookToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { readingListBookToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress Bar with time metrics
        var showProgressDialog by remember { mutableStateOf(false) }

        val progressPercentage = (totalTimeLast24Hours / dailyGoal).coerceAtMost(1.0) * 100
        val formattedTime = "%.1f hours".format(totalTimeLast24Hours)
        val formattedGoal = "%.1f hours".format(dailyGoal)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable { showProgressDialog = true }
        ) {
            // Progress Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .background(Color.Gray)
            ) {
                LinearProgressIndicator(
                    progress = progressPercentage / 100f,
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterStart),
                    color = Color.Green,
                    backgroundColor = Color.LightGray
                )
            }

            // Time Metrics
            Text(
                text = "$formattedTime / $formattedGoal",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically),
                color = Color.Black
            )
        }

        if (showProgressDialog) {
            AlertDialog(
                onDismissRequest = { showProgressDialog = false },
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
                        onUpdate(bookDetailsList, readingList, goal)
                        showProgressDialog = false
                    }) {
                        Text("Set Goal")
                    }
                },
                dismissButton = {
                    Button(onClick = { showProgressDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Book Lists
        Text(
            text = "Recently Read",
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .background(Color.Gray)
                    .clickable { navController.navigate("bookLog") }
                    .padding(0.dp),
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
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = {
                                bookToDelete = bookDetails
                            })
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                bookToDelete = bookDetails
                            }
                    )
                    Text(text = bookDetails.name, color = Color.White)
                }
            }
        }

        // My List
        Text(
            text = "My List",
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .background(Color.Gray)
                    .clickable { navController.navigate("addReadingListBook") }
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", color = Color.White, fontSize = 24.sp)
            }

            readingList.forEach { readingListBook ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .background(Color.Gray)
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = {
                                readingListBookToDelete = readingListBook
                            })
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                readingListBookToDelete = readingListBook
                            }
                    )
                    Text(text = readingListBook.title, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LogBookRead(navController: NavHostController, onBookAdded: (BookDetails) -> Unit) {
    var name by remember { mutableStateOf("") }
    var totalTime by remember { mutableStateOf("") }
    var timeToday by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            label = { Text("Pages Read") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Rating (1-5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(onClick = {
            if (name.isNotBlank()) {
                onBookAdded(
                    BookDetails(
                        name = name,
                        totalTime = totalTime.toDoubleOrNull() ?: 0.0,
                        timeToday = timeToday.toDoubleOrNull() ?: 0.0,
                        pages = pages.toIntOrNull() ?: 0,
                        rating = rating.toIntOrNull() ?: 0,
                        creationTimestamp = System.currentTimeMillis()
                    )
                )
            }
            navController.popBackStack()
        }) {
            Text("Add Book")
        }
    }
}

@Composable
fun AddReadingListBook(navController: NavHostController, onBookAdded: (ReadingListBook) -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Fiction") }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Book Title") }
        )
        TextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author") }
        )
        DropdownMenu(
            expanded = true,
            onDismissRequest = { /* Do nothing */ }
        ) {
            listOf("Fiction", "Non-fiction", "Other").forEach { option ->
                DropdownMenuItem(onClick = { genre = option }) {
                    Text(text = option)
                }
            }
        }

        Button(onClick = {
            if (title.isNotBlank() && author.isNotBlank()) {
                onBookAdded(
                    ReadingListBook(
                        title = title,
                        author = author,
                        genre = genre
                    )
                )
            }
            navController.popBackStack()
        }) {
            Text("Add Book to Reading List")
        }
    }
}

@Composable
fun BookDetailScreen(bookDetails: BookDetails, navController: NavHostController, onDetailsUpdated: (BookDetails) -> Unit) {
    var timeToday by remember { mutableStateOf(bookDetails.timeToday.toString()) }
    var pages by remember { mutableStateOf(bookDetails.pages.toString()) }
    var rating by remember { mutableStateOf(bookDetails.rating.toString()) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Book Details: ${bookDetails.name}")

        TextField(
            value = timeToday,
            onValueChange = { timeToday = it },
            label = { Text("Time Read Today (hours)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = pages,
            onValueChange = { pages = it },
            label = { Text("Pages Read") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Rating (1-5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(onClick = {
            onDetailsUpdated(
                bookDetails.copy(
                    timeToday = timeToday.toDoubleOrNull() ?: bookDetails.timeToday,
                    pages = pages.toIntOrNull() ?: bookDetails.pages,
                    rating = rating.toIntOrNull() ?: bookDetails.rating
                )
            )
            navController.popBackStack()
        }) {
            Text("Update Details")
        }

        Button(onClick = { navController.navigate("home") }) {
            Text("Back to Home")
        }
    }
}
