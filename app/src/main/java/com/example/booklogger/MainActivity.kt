package com.example.booklogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booklogger.ui.theme.BookLoggerTheme
import kotlinx.coroutines.delay

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
                            BookDetailScreen(bookName, numPages, readingTime, bookRating, navController) // Pass navController here
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, bookButtons: List<BookDetails>, onBookButtonsUpdated: (List<BookDetails>) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<BookDetails?>(null) }

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
                BookItem(
                    bookDetails = bookDetails,
                    onClick = {
                        navController.navigate("bookDetail/${bookDetails.name}?pages=${bookDetails.pages}&time=${bookDetails.time}&rating=${bookDetails.rating}")
                    },
                    onLongClick = {
                        bookToDelete = bookDetails
                        showDialog = true
                    }
                )
            }
        }

        if (showDialog && bookToDelete != null) {
            DeleteConfirmationDialog(
                bookDetails = bookToDelete!!,
                onDeleteConfirmed = {
                    onBookButtonsUpdated(bookButtons - bookToDelete!!)
                    showDialog = false
                    bookToDelete = null
                },
                onDismiss = {
                    showDialog = false
                    bookToDelete = null
                }
            )
        }
    }
}

@Composable
fun BookItem(
    bookDetails: BookDetails,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(150.dp)
            .background(Color.Gray)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = bookDetails.name, color = Color.White)
    }
}


@Composable
fun DeleteConfirmationDialog(
    bookDetails: BookDetails,
    onDeleteConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete Book")
        },
        text = {
            Text(text = "Are you sure you want to delete ${bookDetails.name}?")
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirmed) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
fun BookDetailScreen(bookName: String, numPages: String, readingTime: String, bookRating: String, navController: NavHostController) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Book Details")
        Text(text = "Name: $bookName")
        Text(text = "Pages: $numPages")
        Text(text = "Time: $readingTime hrs")
        Text(text = "Rating: $bookRating stars")

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
        HomeScreen(navController = rememberNavController(), bookButtons = listOf(BookDetails("Sample Book", "300", "5 hours", "4.5"))) { }
    }
}
