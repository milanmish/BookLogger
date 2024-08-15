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
        // ... Other UI elements ...

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
                            detectTapGestures(
                                onLongPress = {
                                    bookToDelete = bookDetails
                                }
                            )
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = bookDetails.name, color = Color.White)
                }
            }
        }

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

            readingList.forEach { book ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .background(Color.Gray)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    readingListBookToDelete = book
                                }
                            )
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = book.title, color = Color.White)
                }
            }
        }
    }
}



@Composable
fun LogBookRead(
    navController: NavHostController,
    onBookLogged: (BookDetails) -> Unit
) {
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
            label = { Text("Enter time spent reading today (hours)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        TextField(
            value = pagesRead.value,
            onValueChange = { pagesRead.value = it },
            label = { Text("Enter pages read") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        TextField(
            value = bookRating.value,
            onValueChange = { bookRating.value = it },
            label = { Text("Enter book rating (1-5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                val timeReadToday = timeToday.value.toDoubleOrNull() ?: 0.0
                val pages = pagesRead.value.toIntOrNull() ?: 0
                val rating = bookRating.value.toIntOrNull() ?: 1

                val newBook = BookDetails(
                    name = bookName.value,
                    totalTime = timeReadToday, // Initially equal to time read today
                    timeToday = timeReadToday,
                    pages = pages,
                    rating = rating,
                    creationTimestamp = System.currentTimeMillis()
                )
                onBookLogged(newBook)
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Log Book")
        }
    }
}



@Composable
fun AddReadingListBook(
    navController: NavHostController,
    onBookAdded: (ReadingListBook) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val author = remember { mutableStateOf("") }
    val selectedGenre = remember { mutableStateOf("Fiction") }
    val genres = listOf("Fiction", "Non-fiction", "Other")
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text("Enter book title") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = author.value,
            onValueChange = { author.value = it },
            label = { Text("Enter book author") },
            modifier = Modifier.fillMaxWidth()
        )

        // Genre Selection Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .background(Color.White)
                .border(1.dp, Color.Gray)
                .padding(16.dp)
        ) {
            Text(text = "Select genre: ${selectedGenre.value}", color = Color.Black)
        }

        // Dropdown Menu for genres
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            genres.forEach { genreOption ->
                DropdownMenuItem(
                    onClick = {
                        selectedGenre.value = genreOption
                        expanded = false
                    },
                    text = { Text(text = genreOption) } // Correctly provide the text parameter
                )
            }
        }

        Button(
            onClick = {
                val newBook = ReadingListBook(
                    title = title.value,
                    author = author.value,
                    genre = selectedGenre.value
                )
                onBookAdded(newBook)
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Add Book")
        }
    }
}


@Composable
fun BookDetailScreen(
    bookDetails: BookDetails,
    navController: NavHostController,
    onDetailsUpdated: (BookDetails) -> Unit
) {
    val bookName = remember { mutableStateOf(bookDetails.name) }
    val totalTime = remember { mutableStateOf(bookDetails.totalTime.toString()) }
    val timeToday = remember { mutableStateOf(bookDetails.timeToday.toString()) }
    val pages = remember { mutableStateOf(bookDetails.pages.toString()) }
    val rating = remember { mutableStateOf(bookDetails.rating.toString()) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = bookName.value,
            onValueChange = { bookName.value = it },
            label = { Text("Book name") }
        )
        TextField(
            value = totalTime.value,
            onValueChange = { totalTime.value = it },
            label = { Text("Total time spent reading (hours)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = timeToday.value,
            onValueChange = { timeToday.value = it },
            label = { Text("Time spent reading today (hours)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = pages.value,
            onValueChange = { pages.value = it },
            label = { Text("Number of pages read") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = rating.value,
            onValueChange = { rating.value = it },
            label = { Text("Book rating (out of 5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {
                val updatedBook = bookDetails.copy(
                    name = bookName.value,
                    totalTime = totalTime.value.toDoubleOrNull() ?: 0.0,
                    timeToday = timeToday.value.toDoubleOrNull() ?: 0.0,
                    pages = pages.value.toIntOrNull() ?: 0,
                    rating = rating.value.toIntOrNull() ?: 0
                )
                onDetailsUpdated(updatedBook)
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Update Details")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BookLoggerTheme {
        val navController = rememberNavController()
        HomeScreen(navController, emptyList(), emptyList(), 1.0) { _, _, _ -> }
    }
}
