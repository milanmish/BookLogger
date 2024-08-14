package com.example.booklogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booklogger.ui.theme.BookLoggerTheme

data class BookDetails(
    val name: String,
    val pages: String,
    val time: String,
    val rating: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookLoggerTheme {
                val navController = rememberNavController()
                var bookButtons by remember { mutableStateOf<List<BookDetails>>(emptyList()) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(navController, bookButtons) { newBooks ->
                                bookButtons = newBooks
                            }
                        }
                        composable("bookLog") {
                            LogBookRead(navController) { bookDetails ->
                                // Add the bookDetails to the list and navigate back
                                bookButtons = bookButtons + bookDetails
                                navController.navigate("home")
                            }
                        }
                        composable("bookDetail/{bookName}?pages={pages}&time={time}&rating={rating}") { backStackEntry ->
                            val bookName = backStackEntry.arguments?.getString("bookName") ?: ""
                            val numPages = backStackEntry.arguments?.getString("pages") ?: ""
                            val readingTime = backStackEntry.arguments?.getString("time") ?: ""
                            val bookRating = backStackEntry.arguments?.getString("rating") ?: ""
                            BookDetailScreen(bookName, numPages, readingTime, bookRating)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, bookButtons: List<BookDetails>, onBookButtonsUpdated: (List<BookDetails>) -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome to Book Logger!",
            modifier = Modifier.padding(top = 16.dp, start = 0.dp, end = 8.dp)
        )

        Text(
            text = "Recently Read",
            modifier = Modifier.padding(top = 8.dp, start = 0.dp, end = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
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
                Text(text = "+", color = Color.White)
            }

            // Create a grey box for each logged book
            bookButtons.forEach { bookDetails ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .background(Color.Gray)
                        .clickable {
                            navController.navigate("bookDetail/${bookDetails.name}?pages=${bookDetails.pages}&time=${bookDetails.time}&rating=${bookDetails.rating}")
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = bookDetails.name, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LogBookRead(navController: NavHostController, onBookLogged: (BookDetails) -> Unit) {
    val bookName = remember { mutableStateOf("") }
    val numPages = remember { mutableStateOf("") }
    val readingTime = remember { mutableStateOf("") }
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
            value = numPages.value,
            onValueChange = { numPages.value = it },
            label = { Text("Number of pages") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = readingTime.value,
            onValueChange = { readingTime.value = it },
            label = { Text("Time reading") }
        )
        TextField(
            value = bookRating.value,
            onValueChange = { bookRating.value = it },
            label = { Text("Current book rating") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                val bookDetails = BookDetails(
                    name = bookName.value,
                    pages = numPages.value,
                    time = readingTime.value,
                    rating = bookRating.value
                )
                onBookLogged(bookDetails) // Call the callback with the book details
                navController.navigate("home") // Navigate back to the home screen
            }
        ) {
            Text(text = "Submit")
        }
    }
}

@Composable
fun BookDetailScreen(bookName: String, numPages: String, readingTime: String, bookRating: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Book Details")
        Text(text = "Name: $bookName")
        Text(text = "Pages: $numPages")
        Text(text = "Time: $readingTime hrs")
        Text(text = "Rating: $bookRating stars")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BookLoggerTheme {
        HomeScreen(navController = rememberNavController(), bookButtons = listOf(BookDetails("Sample Book", "300", "5 hours", "4.5"))) { }
    }
}
