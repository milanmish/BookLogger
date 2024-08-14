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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                                bookButtons = bookButtons + bookDetails
                                navController.navigate("home")
                            }
                        }
                        composable("bookDetail/{bookName}?pages={pages}&time={time}&rating={rating}") { backStackEntry ->
                            val bookName = backStackEntry.arguments?.getString("bookName") ?: ""
                            val numPages = backStackEntry.arguments?.getString("pages") ?: ""
                            val readingTime = backStackEntry.arguments?.getString("time") ?: ""
                            val bookRating = backStackEntry.arguments?.getString("rating") ?: ""
                            BookDetailScreen(
                                BookDetails(bookName, numPages, readingTime, bookRating),
                                navController
                            )
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
                Text(text = "+", color = Color.White, fontSize = 24.sp)
            }

            // Display logged books as buttons
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // For numerical input
        )
        TextField(
            value = readingTime.value,
            onValueChange = { readingTime.value = it },
            label = { Text("Time reading (in hours)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // For numerical input
        )
        TextField(
            value = bookRating.value,
            onValueChange = { bookRating.value = it },
            label = { Text("Rating (out of 5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // For numerical input
        )

        Button(
            onClick = {
                // Convert input strings to numbers
                val pages = numPages.value.toIntOrNull() ?: 0
                val time = readingTime.value.toFloatOrNull() ?: 0f
                val rating = bookRating.value.toFloatOrNull() ?: 0f

                val bookDetails = BookDetails(
                    name = bookName.value,
                    pages = pages.toString(),
                    time = time.toString(),
                    rating = rating.toString()
                )
                onBookLogged(bookDetails) // Call the callback with the book details
                navController.navigate("home") // Navigate back to the home screen
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Submit")
        }
    }
}

@Composable
fun BookDetailScreen(bookDetails: BookDetails, navController: NavHostController) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Book Details")
        Text(text = "Name: ${bookDetails.name}")
        Text(text = "Pages: ${bookDetails.pages}")
        Text(text = "Time: ${bookDetails.time} hrs")
        Text(text = "Rating: ${bookDetails.rating} stars")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("home") } // Navigate back to the home screen
        ) {
            Text(text = "Back to Home")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BookLoggerTheme {
        HomeScreen(navController = rememberNavController(), bookButtons = listOf(BookDetails("Sample Book", "300", "5", "4.5"))) { }
    }
}
