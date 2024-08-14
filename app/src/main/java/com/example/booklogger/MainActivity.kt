package com.example.booklogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.booklogger.ui.theme.BookLoggerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookLoggerTheme {
                val navController = rememberNavController()
                var loggedBooks by remember { mutableStateOf<List<String>>(emptyList()) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                loggedBooks = loggedBooks,
                                onBookLogged = { newBook ->
                                    loggedBooks = loggedBooks + newBook
                                    navController.popBackStack() // Navigate back to the home screen
                                }
                            )
                        }
                        composable("bookLog") {
                            LogBookRead(
                                navController = navController,
                                onBookLogged = { newBook ->
                                    loggedBooks = loggedBooks + newBook
                                    navController.popBackStack() // Navigate back to the home screen
                                }
                            )
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
    loggedBooks: List<String>,
    modifier: Modifier = Modifier,
    onBookLogged: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Welcome to Book Logger!",
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "Recently Read",
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        )

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .background(Color.Gray)
                    .clickable {
                        navController.navigate("bookLog")
                    }
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            }

            // Display logged books as buttons
            loggedBooks.forEach { bookName ->
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(150.dp)
                        .background(Color.Gray)
                ) {
                    Text(
                        text = bookName,
                        color = Color.White,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun LogBookRead(
    navController: NavHostController,
    onBookLogged: (String) -> Unit
) {
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
                // Handle the saving of data here
                onBookLogged(bookName.value)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Submit")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BookLoggerTheme {
        HomeScreen(navController = rememberNavController(), loggedBooks = emptyList(), onBookLogged = {})
    }
}
